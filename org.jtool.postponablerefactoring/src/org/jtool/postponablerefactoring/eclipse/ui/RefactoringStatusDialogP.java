/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jtool.postponablerefactoring.eclipse.ui;

import org.jtool.postponablerefactoring.core.PostponableRefactoring;
import org.jtool.postponablerefactoring.core.PostponableRefactoringManager;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringStatusViewer;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.ErrorWizardPage;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;

@SuppressWarnings("restriction")
public class RefactoringStatusDialogP extends Dialog {
    
    private RefactoringStatus fStatus;
    private String fWindowTitle;
    private boolean fBackButton;
    private boolean fLightWeight;
    
    private PostponableRefactoring postponableRefactoring;
    
    public RefactoringStatusDialogP(RefactoringStatus status, Shell parent, String windowTitle, boolean backButton) {
        super(parent);
        fStatus = status;
        fWindowTitle = windowTitle;
        fBackButton = backButton;
    }
    
    public RefactoringStatusDialogP(RefactoringStatus status, Shell parent, String windowTitle, boolean backButton, boolean light) {
        this(status, parent, windowTitle, backButton);
        fLightWeight = light;
    }
    
    public RefactoringStatusDialogP(Shell parent, ErrorWizardPage page, boolean backButton) {
        this(page.getStatus(), parent, parent.getText(), backButton);
    }
    
    protected void setPostponableRefactoring(Refactoring refactoring) {
        postponableRefactoring = PostponableRefactoring.getPostponableRefactoring(refactoring);
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(fWindowTitle);
    }
    
    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTSIZE;
    }
    
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = RefactoringUIPlugin.getDefault().getDialogSettings();
        return DialogSettings.getOrCreateSection(settings, "RefactoringStatusDialog");
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite result = (Composite)super.createDialogArea(parent);
        GridData gd = (GridData) result.getLayoutData();
        gd.widthHint = 800;
        gd.heightHint = 400;
        
        if (!fLightWeight) {
            Color foreground = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
            Color background = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            ViewForm messagePane = new ViewForm(result, SWT.BORDER | SWT.FLAT);
            messagePane.marginWidth = 3;
            messagePane.marginHeight = 3;
            gd = new GridData(GridData.FILL_HORIZONTAL);
            
            Rectangle rect = messagePane.computeTrim(0, 0, 0, convertHeightInCharsToPixels(2) + messagePane.marginHeight * 2);
            gd.heightHint = rect.height;
            messagePane.setLayoutData(gd);
            messagePane.setForeground(foreground);
            messagePane.setBackground(background);
            
            Label label = new Label(messagePane, SWT.LEFT | SWT.WRAP);
            if (fStatus.hasFatalError()) {
                label.setText(RefactoringUIMessages.RefactoringStatusDialog_Cannot_proceed);
            } else {
                label.setText(RefactoringUIMessages.RefactoringStatusDialog_Please_look);
            }
            label.setForeground(foreground);
            label.setBackground(background);
            messagePane.setContent(label);
        }
        
        RefactoringStatusViewer viewer = new RefactoringStatusViewer(result, SWT.NONE);
        viewer.setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setStatus(fStatus);
        applyDialogFont(result);
        return result;
    }
    
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.BACK_ID) {
            setReturnCode(IDialogConstants.BACK_ID);
            close();
        } else if (buttonId == PostponableRefactoringManager.POSTPONE_ID) {
            setReturnCode(PostponableRefactoringManager.POSTPONE_ID);
            close();
        } else {
            super.buttonPressed(buttonId);
        }
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (!fStatus.hasFatalError()) {
            if (fBackButton) {
                createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, false);
            }
            createButton(parent, IDialogConstants.OK_ID, fLightWeight ? IDialogConstants.OK_LABEL : RefactoringUIMessages.RefactoringStatusDialog_Continue, true);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
            if (postponableRefactoring != null && postponableRefactoring.getPostponableRefactoringStatus().isFinalYet()) {
                createButton(parent, PostponableRefactoringManager.POSTPONE_ID, PostponableRefactoringManager.POSTPONE_LABEL, true);
            }
            
        } else {
            if (fBackButton) {
                createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, true);
            }
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
            if (postponableRefactoring != null && postponableRefactoring.getPostponableRefactoringStatus().isFinalYet()) {
                createButton(parent, PostponableRefactoringManager.POSTPONE_ID, PostponableRefactoringManager.POSTPONE_LABEL, true);
            }
        }
    }
}
