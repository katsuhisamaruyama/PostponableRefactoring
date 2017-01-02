/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jtool.postponablerefactoring.eclipse.ui;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.text.ITextSelection;
import org.jtool.postponablerefactoring.eclipse.internal.InlineTempRefactoringP;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;

@SuppressWarnings("restriction")
public class InlineTempActionP extends SelectionDispatchAction {
    
    private JavaEditor fEditor;
    
    public InlineTempActionP(JavaEditor editor) {
        this(editor.getEditorSite());
        fEditor = editor;
        setEnabled(SelectionConverter.canOperateOn(fEditor));
    }
    
    InlineTempActionP(IWorkbenchSite site) {
        super(site);
        setText(RefactoringMessages.InlineTempAction_label);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.INLINE_ACTION);
    }
    
    @Override
    public void selectionChanged(ITextSelection selection) {
        setEnabled(true);
    }
    
    @Override
    public void selectionChanged(JavaTextSelection selection) {
        try {
            setEnabled(RefactoringAvailabilityTester.isInlineTempAvailable(selection));
        } catch (JavaModelException e) {
            setEnabled(false);
        }
    }
    
    @Override
    public void run(ITextSelection selection) {
        ICompilationUnit input = SelectionConverter.getInputAsCompilationUnit(fEditor);
        if (!ActionUtil.isEditable(fEditor)) {
            return;
        }
        
        final InlineTempRefactoring refactoring = new InlineTempRefactoringP(input, null, selection.getOffset(), selection.getLength());
        if (!refactoring.checkIfTempSelected().hasFatalError()) {
            new RefactoringStarter().activate(new InlineTempWizardP(refactoring), getShell(), RefactoringMessages.InlineTempAction_inline_temp, RefactoringSaveHelper.SAVE_NOTHING);
        }
    }
    
    @Override
    public void run(IStructuredSelection selection) {
    }
    
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(false);
    }
    
    boolean tryInlineTemp(ICompilationUnit unit, CompilationUnit node, ITextSelection selection, Shell shell) {
        final InlineTempRefactoring refactoring = new InlineTempRefactoringP(unit, node, selection.getOffset(), selection.getLength());
        if (!refactoring.checkIfTempSelected().hasFatalError()) {
            new RefactoringStarter().activate(new InlineTempWizardP(refactoring), shell, RefactoringMessages.InlineTempAction_inline_temp, RefactoringSaveHelper.SAVE_NOTHING);
            return true;
        }
        return false;
    }
}
