/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jtool.postponablerefactoring.eclipse.internal;

import org.jtool.postponablerefactoring.eclipse.internal.SideEffectsNodeCollector.ErrorASTNode;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import java.util.Set;

@SuppressWarnings("restriction")
public class InlineTempRefactoringP extends InlineTempRefactoring {
    
    protected ICompilationUnit fCUnit;
    protected CompilationUnit fRoot;
    protected int fSelectionStart;
    protected int fSelectionLength;
    
    public InlineTempRefactoringP(ICompilationUnit unit, CompilationUnit node, int selectionStart, int selectionLength) {
        super(unit, node, selectionStart, selectionLength);
        
        fCUnit = unit;
        fRoot = node;
        fSelectionStart = selectionStart;
        fSelectionLength = selectionLength;
        
    }
    
    public InlineTempRefactoringP(ICompilationUnit unit, int selectionStart, int selectionLength) {
        this(unit, RefactoringASTParser.parseWithASTProvider(unit, true, null), selectionStart, selectionLength);
    }
    
    public InlineTempRefactoringP(VariableDeclaration decl) {
        super(decl);
        
        fRoot = (CompilationUnit)decl.getRoot();
        fCUnit = (ICompilationUnit)fRoot.getJavaElement();
        fSelectionStart= decl.getStartPosition();
        fSelectionLength= decl.getLength();
    }
    
    public InlineTempRefactoringP(JavaRefactoringArguments arguments, RefactoringStatus status) {
        super(arguments, status);
    }
    
    public ICompilationUnit getCompilationUnit() {
        return fCUnit;
    }
    
    public CompilationUnit getASTRoot() {
        return fRoot;
    }
    
    public int getSelectionStart() {
        return fSelectionStart;
    }
    
    public int getSelectionLength() {
        return fSelectionLength;
    }
    
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        return super.checkInitialConditions(pm);
    }
    
    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
        return super.checkFinalConditions(pm);
    }
    
    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException {
        return super.createChange(pm);
    }
    
    public String checkSideEffectOccurrences() {
        VariableDeclaration declaration = getVariableDeclaration();
        SideEffectsNodeCollector sideEffectsNodeCollector = new SideEffectsNodeCollector();
        Set<ErrorASTNode> problematicNodes = sideEffectsNodeCollector.getProblematicNodes(declaration.getInitializer());
        if (problematicNodes.size() > 0) {
            StringBuilder message = new StringBuilder();
            for (ErrorASTNode errorASTnode : problematicNodes) {
                message.append(errorASTnode.message + "\n");
            }
            return message.toString();
        }
        return "";
    }
}
