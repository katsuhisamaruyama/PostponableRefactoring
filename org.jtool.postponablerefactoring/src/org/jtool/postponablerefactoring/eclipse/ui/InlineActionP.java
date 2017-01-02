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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.InlineConstantAction;
import org.eclipse.jdt.internal.ui.refactoring.actions.InlineMethodAction;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;

@SuppressWarnings("restriction")
public class InlineActionP extends SelectionDispatchAction {

    private JavaEditor fEditor;
    private final InlineTempActionP fInlineTemp;
    private final InlineMethodAction fInlineMethod;
    private final InlineConstantAction fInlineConstant;
    
    public InlineActionP(IWorkbenchSite site) {
        super(site);
        setText(RefactoringMessages.InlineAction_Inline);
        fInlineTemp = new InlineTempActionP(site);
        fInlineConstant = new InlineConstantAction(site);
        fInlineMethod = new InlineMethodAction(site);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.INLINE_ACTION);
    }
    
    public InlineActionP(JavaEditor editor) {
        super(editor.getEditorSite());
        setText(RefactoringMessages.InlineAction_Inline);
        fEditor = editor;
        fInlineTemp = new InlineTempActionP(editor);
        fInlineConstant = new InlineConstantAction(editor);
        fInlineMethod = new InlineMethodAction(editor);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.INLINE_ACTION);
        setEnabled(SelectionConverter.getInputAsCompilationUnit(fEditor) != null);
    }
    
    @Override
    public void selectionChanged(ISelection selection) {
        fInlineConstant.update(selection);
        fInlineMethod.update(selection);
        fInlineTemp.update(selection);
        setEnabled((fInlineTemp.isEnabled() || fInlineConstant.isEnabled() || fInlineMethod.isEnabled()));
    }
    
    @Override
    public void run(ITextSelection selection) {
        if (!ActionUtil.isEditable(fEditor)) {
            return;
        }
        
        ITypeRoot typeRoot = SelectionConverter.getInput(fEditor);
        if (typeRoot == null) {
            return;
        }
        
        CompilationUnit node = RefactoringASTParser.parseWithASTProvider(typeRoot, true, null);
        
        if (typeRoot instanceof ICompilationUnit) {
            ICompilationUnit cu = (ICompilationUnit)typeRoot;
            if (fInlineTemp.isEnabled() && fInlineTemp.tryInlineTemp(cu, node, selection, getShell())) {
                return;
            }
            if (fInlineConstant.isEnabled() && fInlineConstant.tryInlineConstant(cu, node, selection, getShell())) {
                return;
            }
        }
        
        if (fInlineMethod.isEnabled() && fInlineMethod.tryInlineMethod(typeRoot, node, selection, getShell())) {
            return;
        }
        
        MessageDialog.openInformation(getShell(), RefactoringMessages.InlineAction_dialog_title, RefactoringMessages.InlineAction_select);
    }
    
    @Override
    public void run(IStructuredSelection selection) {
        if (fInlineConstant.isEnabled()) {
            fInlineConstant.run(selection);
        } else if (fInlineMethod.isEnabled()) {
            fInlineMethod.run(selection);
        }
    }
}
