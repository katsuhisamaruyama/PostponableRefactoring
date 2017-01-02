/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jtool.postponablerefactoring.eclipse.ui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

@SuppressWarnings("restriction")
public class RefactoringWizardDialogP extends WizardDialog {
    
    private static final String DIALOG_SETTINGS = "RefactoringWizard";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    
    private IDialogSettings fSettings;
    
    private boolean fMakeNextButtonDefault;
    
    public RefactoringWizardDialogP(Shell parent, RefactoringWizard wizard) {
        super(parent, wizard);
        IDialogSettings settings = wizard.getDialogSettings();
        if (settings == null) {
            settings= RefactoringUIPlugin.getDefault().getDialogSettings();
            wizard.setDialogSettings(settings);
        }
        
        int width = 600;
        int height = 400;
        
        String settingsSectionId = DIALOG_SETTINGS + '.'+ wizard.getRefactoring().getName();
        fSettings = settings.getSection(settingsSectionId);
        if (fSettings == null) {
            fSettings = new DialogSettings(settingsSectionId);
            settings.addSection(fSettings);
            fSettings.put(WIDTH, width);
            fSettings.put(HEIGHT, height);
        } else {
            try {
                width = fSettings.getInt(WIDTH);
                height = fSettings.getInt(HEIGHT);
            } catch (NumberFormatException e) {
            }
        }
        setMinimumPageSize(width, height);
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        getRefactoringWizard().getRefactoring().setValidationContext(newShell);
    }
    
    @Override
    protected void cancelPressed() {
        storeCurrentSize();
        super.cancelPressed();
    }
    
    @Override
    protected void finishPressed() {
        storeCurrentSize();
        super.finishPressed();
    }
    
    private void storeCurrentSize() {
        IWizardPage page = getCurrentPage();
        Control control = page.getControl().getParent();
        Point size = control.getSize();
        fSettings.put(WIDTH, size.x);
        fSettings.put(HEIGHT, size.y);
    }
    
    @Override
    public void updateButtons() {
        super.updateButtons();
        if (!fMakeNextButtonDefault) {
            return;
        }
        if (getShell() == null) {
            return;
        }
        Button next = getButton(IDialogConstants.NEXT_ID);
        if (next.isEnabled()) {
            getShell().setDefaultButton(next);
        }
    }
    
    public void makeNextButtonDefault() {
        fMakeNextButtonDefault = true;
    }
    
    @Override
    protected Button getButton(int id) {
        return super.getButton(id);
    }
    
    private RefactoringWizard getRefactoringWizard() {
        return (RefactoringWizard)getWizard();
    }
}
