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

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;

@SuppressWarnings("restriction")
public class InlineTempWizardP extends RefactoringWizard {

    public InlineTempWizardP(InlineTempRefactoring ref) {
        super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
        setDefaultPageTitle(RefactoringMessages.InlineTempWizard_defaultPageTitle);
    }
    
    @Override
    protected void addUserInputPages() {
        addPage(new InlineTempInputPageP());
    }
    
    @Override
    public int getMessageLineWidthInChars() {
        return 0;
    }
}
