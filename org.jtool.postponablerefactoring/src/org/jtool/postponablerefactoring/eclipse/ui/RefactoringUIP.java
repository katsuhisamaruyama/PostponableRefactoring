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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class RefactoringUIP {
    
    private RefactoringUIP() {
    }
    
    public static Dialog createRefactoringWizardDialog(RefactoringWizard wizard, Shell parent) {
        Dialog result;
        if (needsWizardBasedUserInterface(wizard)) {
            result = new RefactoringWizardDialogP(parent, wizard);
        } else {
            result = new RefactoringWizardDialog2P(parent, wizard);
        }
        return result;
    }
    
    private static boolean needsWizardBasedUserInterface(RefactoringWizard wizard) {
        return (wizard.getWizardFlags() & RefactoringWizard.WIZARD_BASED_USER_INTERFACE) != 0;
    }
}
