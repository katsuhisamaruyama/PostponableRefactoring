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

import org.jtool.postponablerefactoring.eclipse.internal.ExtractMethodRefactoringP;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;

@SuppressWarnings("restriction")
public class ExtractMethodWizardP extends RefactoringWizard {
    
    public static final String DIALOG_SETTING_SECTION = "ExtractMethodWizard";
    
    public ExtractMethodWizardP(ExtractMethodRefactoringP ref) {
        super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
        setDefaultPageTitle(RefactoringMessages.ExtractMethodWizard_extract_method);
        setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
    }
    
    @Override
    protected void addUserInputPages() {
        addPage(new ExtractMethodInputPageP());
    }
}
