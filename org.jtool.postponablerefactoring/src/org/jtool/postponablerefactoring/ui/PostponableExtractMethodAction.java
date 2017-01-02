/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.ui;

import org.jtool.postponablerefactoring.core.PostponableExtractMethodRefactoring;
import org.jtool.postponablerefactoring.core.PostponableRefactoring;
import org.jtool.postponablerefactoring.eclipse.ui.RefactoringStarterP;
import org.jtool.postponablerefactoring.eclipse.ui.ExtractMethodWizardP;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;

@SuppressWarnings("restriction")
public class PostponableExtractMethodAction extends PostponableAction {
    
    public PostponableExtractMethodAction(JavaEditor editor) {
        super(editor);
        
        setText(RefactoringMessages.ExtractMethodAction_label);
        setEnabled(SelectionConverter.getInputAsCompilationUnit(editor) != null);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.EXTRACT_METHOD_ACTION);
    }
    
    @Override
    public boolean perform(ITextSelection selection) {
        ICompilationUnit cu = SelectionConverter.getInputAsCompilationUnit(editor);
        PostponableExtractMethodRefactoring refactoring = new PostponableExtractMethodRefactoring(cu, selection.getOffset(), selection.getLength());
        return perform(refactoring);
    }
    
    @Override
    public boolean perform(PostponableRefactoring refactoring) {
        if (refactoring instanceof PostponableExtractMethodRefactoring) {
            return perform((PostponableExtractMethodRefactoring)refactoring);
        }
        return false;
    }
    
    private boolean perform(PostponableExtractMethodRefactoring refactoring) {
        refactoring.setPostponableAction(this);
        RefactoringStarterP starter = new RefactoringStarterP();
        return starter.activate(new ExtractMethodWizardP(refactoring), getShell(), RefactoringMessages.ExtractMethodAction_dialog_title, RefactoringSaveHelper.SAVE_NOTHING);
    }
}
