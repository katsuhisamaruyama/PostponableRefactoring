/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.jtool.postponablerefactoring.ui.PostponableAction;

public interface PostponableRefactoring {
    
    public PostponableRefactoring newPostponableRefactoring(CompilationUnit astRoot, int selectionStart, int selectionLength);
    
    public String getName();
    
    public ICompilationUnit getCompilationUnit();
    
    public void loadDialogSettings(PostponableDialogSettings settings);
    
    public PostponableDialogSettings getDialogSettings();
    
    public boolean isRunWithDialogSettings();
    
    public void setPostponableAction(PostponableAction action);
    
    public PostponableAction getPostponableAction();
    
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException;
    
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException;
    
    public Change createChange(IProgressMonitor pm) throws CoreException;
    
    public PostponableRefactoringStatus getPostponableRefactoringStatus();
    
    public static PostponableRefactoring getPostponableRefactoring(Refactoring refactoring) {
        if (refactoring instanceof PostponableRefactoring) {
            return (PostponableRefactoring)refactoring;
        }
        return null;
    }
}
