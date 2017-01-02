/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.eclipse.internal;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("restriction")
public class SideEffectsNodeCollector extends ASTVisitor {
    
    private Map<String, MethodDeclaration> methodDeclarations = new HashMap<String, MethodDeclaration>();
    
    class ErrorASTNode {
        ASTNode node;
        String message;
        
        ErrorASTNode(ASTNode node, String message) {
            this.node = node;
            this.message = message;
        }
    }
    
    public Set<ErrorASTNode> getProblematicNodes(Expression expression) {
        collectMethodCalls(expression);
        
        Set<ErrorASTNode> problematicNodes = new HashSet<ErrorASTNode>();
        ProblematicNodeFinder problematicNodeFinder = new ProblematicNodeFinder();
        problematicNodes.addAll(problematicNodeFinder.perform(expression));
        
        for (MethodDeclaration methodDeclaration : methodDeclarations.values()) {
            problematicNodes.addAll(problematicNodeFinder.perform(methodDeclaration));
        }
        return problematicNodes;
    }
    
    private void collectMethodCalls(Expression node) {
        MethodCallCollector collector = new MethodCallCollector();
        for (MethodDeclaration methodDeclaration : collector.perform(node)) {
            if (methodDeclarations.get(getKey(methodDeclaration)) == null) {
                methodDeclarations.put(getKey(methodDeclaration), methodDeclaration);
                collectMethodCalls(methodDeclaration);
            }
        }
    }
    
    private void collectMethodCalls(MethodDeclaration node) {
        MethodCallCollector collector = new MethodCallCollector();
        for (MethodDeclaration methodDeclaration : collector.perform(node)) {
            if (methodDeclarations.get(getKey(methodDeclaration)) == null) {
                methodDeclarations.put(getKey(methodDeclaration), methodDeclaration);
                collectMethodCalls(methodDeclaration);
            }
        }
    }
    
    private String getKey(MethodDeclaration methodDeclaration) {
        return methodDeclaration.resolveBinding().getKey();
    }
    
    private class MethodCallCollector extends ASTVisitor {
        
        private Set<MethodDeclaration> methodDeclarations = new HashSet<MethodDeclaration>();
        
        private MethodCallCollector() {
        }
        
        private Set<MethodDeclaration> perform(Expression node) {
            node.accept(this);
            return methodDeclarations;
        }
        
        private Set<MethodDeclaration> perform(MethodDeclaration node) {
            node.accept(this);
            return methodDeclarations;
        }
        
        @Override
        public boolean visit(MethodInvocation node) {
            MethodDeclaration methodDeclaration = getMethodDeclaration(node.resolveMethodBinding());
            if (methodDeclaration != null) {
                methodDeclarations.add(methodDeclaration);
            }
            return true;
        }
        
        @Override
        public boolean visit(SuperMethodInvocation node) {
            MethodDeclaration methodDeclaration = getMethodDeclaration(node.resolveMethodBinding());
            if (methodDeclaration != null) {
                methodDeclarations.add(methodDeclaration);
            }
            return true;
        }
        
        private MethodDeclaration getMethodDeclaration(IMethodBinding binding) {
            IJavaElement elem = binding.getMethodDeclaration().getJavaElement();
            if (elem instanceof IMethod) {
                IMethod method = (IMethod)elem;
                ICompilationUnit cu = method.getCompilationUnit();
                CompilationUnit astRoot = RefactoringASTParser.parseWithASTProvider(cu, true, null);
                
                try {
                    int offset = method.getSourceRange().getOffset();
                    int length = method.getSourceRange().getLength();
                    MethodDeclarationFinder methodDeclarationFinder = new MethodDeclarationFinder();
                    MethodDeclaration methodDeclaration = methodDeclarationFinder.perform(astRoot, offset, length);
                    return methodDeclaration;
                    
                } catch (JavaModelException e) { }
            }
            return null;
        }
    }
    
    private class MethodDeclarationFinder extends ASTVisitor {
        
        private int selectionOffset;
        private int selectionLength;
        private MethodDeclaration methodDeclation = null;
        
        private MethodDeclarationFinder() {
        }
        
        private MethodDeclaration perform(CompilationUnit cu, int selectionOffset, int selectionLength) {
            this.selectionOffset = selectionOffset;
            this.selectionLength = selectionLength;
            cu.accept(this);
            return methodDeclation;
        }
        
        @Override
        public boolean visit(MethodDeclaration node) {
            if (node.getStartPosition() == selectionOffset && node.getLength() == selectionLength) {
                methodDeclation = node;
                return false;
            }
            return true;
        }
    }
    
    private class ProblematicNodeFinder extends ASTVisitor {
        
        private Map<String, ErrorASTNode> problematicNodes = new HashMap<String, ErrorASTNode>();
        
        private ProblematicNodeFinder() {
        }
        
        private Set<ErrorASTNode> perform(Expression node) {
            problematicNodes.clear();
            node.accept(this);
            Set<ErrorASTNode> nodes = new HashSet<ErrorASTNode>();
            nodes.addAll(problematicNodes.values());
            return nodes;
        }
        
        private Set<ErrorASTNode> perform(MethodDeclaration node) {
            node.accept(this);
            Set<ErrorASTNode> nodes = new HashSet<ErrorASTNode>();
            nodes.addAll(problematicNodes.values());
            return nodes;
        }
        
        @Override
        public boolean visit(SimpleName node) { 
            addFieldVariableAccess(node, node.resolveBinding());
            return false;
        }
        
        @Override
        public boolean visit(QualifiedName node) {
            addFieldVariableAccess(node, node.resolveBinding());
            return false;
        }
        
        private void addFieldVariableAccess(ASTNode node, IBinding binding) {
            if (binding != null && binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding vbinding = (IVariableBinding)binding;
                
                if (vbinding.isField() || vbinding.isEnumConstant()) {
                    problematicNodes.put(vbinding.getVariableDeclaration().getKey(), new ErrorASTNode(node, "Contains a field or enumconstant access to " + vbinding.getName() + " of " + vbinding.getDeclaringClass().getQualifiedName()));
                }
            }
        }
        
        @Override
        public boolean visit(ClassInstanceCreation node) {
            IMethodBinding mbinding = node.resolveConstructorBinding();
            
            problematicNodes.put(mbinding.getKey(), new ErrorASTNode(node, "Contains an instance creation of " + mbinding.getDeclaringClass().getQualifiedName()));
            return false;
        }
    }
}
