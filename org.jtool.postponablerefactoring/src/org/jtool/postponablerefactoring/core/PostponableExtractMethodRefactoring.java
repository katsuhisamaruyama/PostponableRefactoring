/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import org.jtool.postponablerefactoring.eclipse.internal.ExtractMethodRefactoringP;
import org.jtool.postponablerefactoring.ui.PostponableAction;
import com.ibm.icu.text.MessageFormat;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import java.util.List;

@SuppressWarnings("restriction")
public class PostponableExtractMethodRefactoring extends ExtractMethodRefactoringP implements PostponableRefactoring {
    
    private PostponableRefactoringStatus postponableRefactoringStatus;
    
    private PostponableAction postponableAction;
    
    private boolean runWithDialogSettings = false;
    
    public PostponableExtractMethodRefactoring(ICompilationUnit unit, int selectionStart, int selectionLength) {
        super(unit, selectionStart, selectionLength);
    }
    
    public PostponableExtractMethodRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
        super(arguments, status);
    }
    
    public PostponableExtractMethodRefactoring(CompilationUnit astRoot, int selectionStart, int selectionLength) {
        super(astRoot, selectionStart, selectionLength);
    }
    
    public PostponableRefactoring newPostponableRefactoring(CompilationUnit astRoot, int selectionStart, int selectionLength) {
        return new PostponableExtractMethodRefactoring(astRoot, selectionStart, selectionLength);
    }
    
    public void loadDialogSettings(PostponableDialogSettings settings) {
        try {
            fMethodName = settings.get(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
            fDestinationIndex = settings.getInt(ATTRIBUTE_DESTINATION);
            fVisibility = settings.getInt(ATTRIBUTE_VISIBILITY);
            fGenerateJavadoc = settings.getBoolean(ATTRIBUTE_COMMENTS);
            fReplaceDuplicates = settings.getBoolean(ATTRIBUTE_REPLACE);
            fThrowRuntimeExceptions = settings.getBoolean(ATTRIBUTE_EXCEPTIONS);
        } catch (PostponableDialogSettingsException e) {
            runWithDialogSettings = false;
            return;
        }
        runWithDialogSettings = true;
    }
    
    public PostponableDialogSettings getDialogSettings() {
        PostponableDialogSettings settings = new PostponableDialogSettings();
        settings.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, fMethodName);
        settings.put(ATTRIBUTE_DESTINATION, fDestinationIndex);
        settings.put(ATTRIBUTE_VISIBILITY, fVisibility);
        settings.put(ATTRIBUTE_COMMENTS, fGenerateJavadoc);
        settings.put(ATTRIBUTE_REPLACE, fReplaceDuplicates);
        settings.put(ATTRIBUTE_EXCEPTIONS, fThrowRuntimeExceptions);
        return settings;
    }
    
    public boolean isRunWithDialogSettings() {
        return runWithDialogSettings;
    }
    
    @Override
    public String getName() {
        return RefactoringCoreMessages.ExtractMethodRefactoring_name;
    }
    
    @Override
    public ICompilationUnit getCompilationUnit() {
        return super.getCompilationUnit();
    }
    
    @Override
    public void setPostponableAction(PostponableAction action) {
        postponableAction = action;
    }
    
    @Override
    public PostponableAction getPostponableAction() {
        return postponableAction;
    }
    
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        RefactoringStatus result = super.checkInitialConditions(pm);
        postponableRefactoringStatus = new PostponableRefactoringStatus();
        if (!result.hasFatalError()) {
            postponableRefactoringStatus.setInitialOk();
            postponableRefactoringStatus.setRefactoringResult(result);
            return result;
        }
        
        RefactoringStatusEntry[] errors = PostponableRefactoringStatus.getFatalErrorEntries(result.getEntries());
        if (errors.length > 1) {
            postponableRefactoringStatus.setInitialFatal();
            postponableRefactoringStatus.setRefactoringResult(result);
            return result;
        }
        
        pm.beginTask("", 1);
        pm.subTask(EMPTY);
        // FatalError: RefactoringCoreMessages.ExtractMethodAnalyzer_assignments_to_local
        boolean checkResult = checkLocalVariablesForReturn();
        pm.worked(1);
        pm.done();
        
        if (checkResult && postponableRefactoringStatus.hasSameRecoverableMessages(result)) {
            postponableRefactoringStatus.setInitialYet();
        } else {
            postponableRefactoringStatus.setInitialFatal();
        }
        
        postponableRefactoringStatus.setRefactoringResult(result);
        return result;
    }
    
    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
        RefactoringStatus result = super.checkFinalConditions(pm);
        postponableRefactoringStatus = new PostponableRefactoringStatus();
        
        if (result.hasFatalError()) {
            postponableRefactoringStatus.setFinalFatal();
            postponableRefactoringStatus.setRefactoringResult(result);
            return result;
        }
        
        pm.beginTask("", 1);
        pm.subTask(EMPTY);
        // Error: RefactoringCoreMessages.Checks_methodName_exists
        boolean checkResult = checkMethodNameDuplication();
        pm.worked(1);
        pm.done();
        
        if (checkResult) {
            postponableRefactoringStatus.setFinalYet();
        } else {
            postponableRefactoringStatus.setFinalOk();
        }
        
        postponableRefactoringStatus.setRefactoringResult(result);
        return result;
    }
    
    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException {
        return super.createChange(pm);
    }
    
    private boolean checkLocalVariablesForReturn() {
        List<IVariableBinding> localVariablesForReturn = fAnalyzer.getLocalReads();
        if (localVariablesForReturn != null && localVariablesForReturn.size() > 1) {
            StringBuilder affectedLocals = new StringBuilder();
            for (int idx = 0; idx < localVariablesForReturn.size(); idx++) {
                IVariableBinding variable = localVariablesForReturn.get(idx);
                String bindingName = BindingLabelProvider.getBindingLabel(variable, BindingLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.F_PRE_TYPE_SIGNATURE);
                affectedLocals.append(bindingName);
                if (idx != localVariablesForReturn.size() - 1) {
                    affectedLocals.append("\n");
                }
            }
            
            String message = MessageFormat.format(RefactoringCoreMessages.ExtractMethodAnalyzer_assignments_to_local,
                    new Object[] { affectedLocals.toString() });
            postponableRefactoringStatus.addRecoverableMessage(message);
            
            String path = getCompilationUnit().getPath().toPortableString();
            postponableRefactoringStatus.setCodeSelection(CodeSelection.newCodeSelection(path, getSelectionStart(), getSelectionLength()));
            IMethodBinding method = localVariablesForReturn.get(0).getDeclaringMethod();
            postponableRefactoringStatus.addCodeSelection(CodeSelection.getCodeSelectionForWholeRange(method.getJavaElement()));
            
            return true;
        }
        
        return false;
    }
    
    private boolean checkMethodNameDuplication() {
        ITypeBinding[] arguments = fAnalyzer.getArgumentTypes();
        ITypeBinding type = ASTNodes.getEnclosingType(fDestination);
        
        IMethodBinding method = Bindings.findMethodInType(type, fMethodName, arguments);
        if (method != null && !method.isConstructor()) {
            String message = Messages.format(RefactoringCoreMessages.Checks_methodName_exists,
                    new Object[] { BasicElementLabels.getJavaElementName(fMethodName), BasicElementLabels.getJavaElementName(type.getName()) });
            postponableRefactoringStatus.addRecoverableMessage(message);
            
            String path = getCompilationUnit().getPath().toPortableString();
            postponableRefactoringStatus.setCodeSelection(CodeSelection.newCodeSelection(path, getSelectionStart(), getSelectionLength()));
            postponableRefactoringStatus.addCodeSelection(CodeSelection.getCodeSelectionForNameRange(method.getJavaElement()));
            
            return true;
        }
        return false;
    }
    
    @Override
    public PostponableRefactoringStatus getPostponableRefactoringStatus() {
        if (postponableRefactoringStatus == null) {
            return new PostponableRefactoringStatus();
        }
        return postponableRefactoringStatus;
    }
}
