/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import org.jtool.postponablerefactoring.ui.EditorUtilities;
import org.jtool.postponablerefactoring.ui.PostponableAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("restriction")
public class PostponableRefactoringElement {
    
    private PostponableRefactoring refactoring;
    
    private String path;
    
    private ZonedDateTime time;
    
    private CodeSelectionChecker codeSelectionChecker = new CodeSelectionChecker();
    
    private boolean needUpdate;
    
    private int prevStatusValue;
    
    PostponableRefactoringElement(PostponableRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    private PostponableRefactoringStatus getStatus() {
        return refactoring.getPostponableRefactoringStatus();
    }
    
    PostponableRefactoring getRefactoring() {
        return refactoring;
    }
    
    private void setCurrentTime() {
        time = ZonedDateTime.now();
    }
    
    public ZonedDateTime getTime() {
        return time;
    }
    
    public String getTimeRepresentation() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        return time.format(formatter);
    }
    
    public String getPath() {
        return path;
    }
    
    void setPath(String path) {
        this.path = path;
    }
    
    public String getComment() {
        if (getStatus().isOk()) {
            return "RESOLVED:\n" + getStatus().getRecoverableMessage();
        }
        if (getStatus().isYet()) {
            return getStatus().getRecoverableMessage();
        }
        return getStatus().getRefactoringResultMessage();
    }
    
    public String getName() {
        return refactoring.getName();
    }
    
    void updateCodeSelection(CodeChange codeChange) {
        codeSelectionChecker.updateCodeSelection(codeChange);
    }
    
    boolean updateRestartStatus() {
        needUpdate = false;
        if (codeSelectionChecker.isCodeSelectionChanged()) {
            CodeSelection selection = codeSelectionChecker.getCodeSelection();
            int start = selection.getStart();
            int length = selection.getLength();
            CompilationUnit astRoot = getASTRoot();
            
            prevStatusValue = getStatus().getStatusValue();
            if (getStatus().isInitial()) {
                PostponableRefactoring restartingRefactoring = refactoring.newPostponableRefactoring(astRoot, start, length);
                checkInitialConditions(restartingRefactoring);
                RefactoringStatus result = restartingRefactoring.getPostponableRefactoringStatus().getRefactoringResult();
                getStatus().setRefactoringResult(result);
                setCurrentTime();
                
            } else if (getStatus().isFinal()) {
                PostponableRefactoring restartingRefactoring = refactoring.newPostponableRefactoring(astRoot, start, length);
                restartingRefactoring.loadDialogSettings(refactoring.getDialogSettings());
                checkFinalConditions(restartingRefactoring);
                RefactoringStatus result = restartingRefactoring.getPostponableRefactoringStatus().getRefactoringResult();
                getStatus().setRefactoringResult(result);
                setCurrentTime();
            }
        }
        return needUpdate;
    }
    
    private CompilationUnit getASTRoot() {
        try {
            ICompilationUnit unit = refactoring.getCompilationUnit();
            unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
            
            RefactoringASTParser parser = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL);
            CompilationUnit astRoot = parser.parse(unit, null, true, ASTProvider.SHARED_AST_STATEMENT_RECOVERY, ASTProvider.SHARED_BINDING_RECOVERY, new NullProgressMonitor());
            return astRoot;
        } catch (CoreException e) {
        }
        return null;
    }
    
    private boolean checkInitialConditions(PostponableRefactoring restartingRefactoring) {
        try {
            restartingRefactoring.checkInitialConditions(new NullProgressMonitor());
            PostponableRefactoringStatus initialConditionsStatus = restartingRefactoring.getPostponableRefactoringStatus();
            
            if (initialConditionsStatus.isInitialOk()) {
                if (!getStatus().isOk()) {
                    needUpdate = true;
                }
                getStatus().setInitialOk();
                return true;
                
            } else if (initialConditionsStatus.isInitialYet()) {
                if (!getStatus().isYet()) {
                    needUpdate = true;
                }
                getStatus().setInitialYet();
                return false;
                
            } else if (initialConditionsStatus.isInitialFatal()) {
                if (getStatus().isOk()) {
                    needUpdate = true;
                }
                getStatus().setInitialFatal();
                return false;
            }
        } catch (CoreException e) {
            needUpdate = true;
            getStatus().setInitialFatal();
            return false;
        }
        return true;
    }
    
    private boolean checkFinalConditions(PostponableRefactoring restartingRefactoring) {
        try {
            restartingRefactoring.checkInitialConditions(new NullProgressMonitor());
            restartingRefactoring.checkFinalConditions(new NullProgressMonitor());
            PostponableRefactoringStatus finalConditionsStatus = restartingRefactoring.getPostponableRefactoringStatus();
            
            if (finalConditionsStatus.isFinalOk()) {
                if (!getStatus().isOk()) {
                    needUpdate = true;
                }
                getStatus().setFinalOk();
                return true;
                
            } else if (finalConditionsStatus.isFinalYet()) {
                if (!getStatus().isYet()) {
                    needUpdate = true;
                }
                getStatus().setFinalYet();
                return false;
                
            } else if (finalConditionsStatus.isFinalFatal()) {
                if (getStatus().isOk()) {
                    needUpdate = true;
                }
                getStatus().setFinalFatal();
                return false;
            }
        } catch (CoreException e) {
            needUpdate = true;
            getStatus().setFinalFatal();
            return false;
        }
        return true;
    }
    
    public boolean isOk() {
        return getStatus().isOk();
    }
    
    public boolean isYet() {
        return getStatus().isYet();
    }
    
    public boolean isFatal() {
        return getStatus().isFatal();
    }
    
    public boolean postpone() {
        path = refactoring.getCompilationUnit().getPath().toPortableString();
        setCurrentTime();
        
        codeSelectionChecker.setCodeSelection(getStatus().getCodeSelection());
        codeSelectionChecker.setCodeSelections(getStatus().getCodeSelections());
        needUpdate = false;
        return true;
    }
    
    public boolean restart() {
        PostponableAction action = refactoring.getPostponableAction();
        ICompilationUnit cu = refactoring.getCompilationUnit();
        CodeSelection selection = codeSelectionChecker.getCodeSelection();
        PostponableExtractMethodRefactoring restartingRefactoring = new PostponableExtractMethodRefactoring(cu, selection.getStart(), selection.getLength());
        if (PostponableRefactoringStatus.isFinal(prevStatusValue)) {
            restartingRefactoring.loadDialogSettings(refactoring.getDialogSettings());
        }
        return action.restart(restartingRefactoring);
    }
    
    public void codeSelection() {
        IEditorPart part = EditorUtilities.getActiveEditor();
        if (part instanceof ITextEditor) {
            ITextEditor editor = (ITextEditor)part;
            
            CodeSelection selection = codeSelectionChecker.getCodeSelection();
            editor.getSelectionProvider().setSelection(new TextSelection(selection.getStart(), selection.getLength()));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getTimeRepresentation() + ": ");
        buf.append(getPath() + " ");
        buf.append(getName() + " ");
        buf.append(getComment());
        return buf.toString();
    }
}
