/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.ui;

import org.jtool.postponablerefactoring.core.PostponableRefactoring;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;

@SuppressWarnings("restriction")
abstract public class PostponableAction extends SelectionDispatchAction {
    
    protected JavaEditor editor;
    
    public PostponableAction(JavaEditor editor) {
        super(editor.getEditorSite());
        this.editor = editor;
    }
    
    @Override
    public void selectionChanged(ITextSelection selection) {
        if (selection.getLength() == 0) {
            setEnabled(false);
        } else {
            setEnabled(editor != null && SelectionConverter.getInputAsCompilationUnit(editor) != null);
        }
    }
    
    @Override
    public void selectionChanged(JavaTextSelection selection) {
        setEnabled(RefactoringAvailabilityTester.isExtractMethodAvailable(selection));
    }
    
    @Override
    public void run(ITextSelection selection) {
        if (!ActionUtil.isEditable(editor)) {
            return;
        }
        perform(selection);
    }
    
    public boolean restart(PostponableRefactoring refactoring) {
        if (ActionUtil.isEditable(editor)) {
            return perform(refactoring);
        }
        return false;
    }
    
    abstract public boolean perform(ITextSelection selection);
    
    abstract public boolean perform(PostponableRefactoring refactoring);
}
