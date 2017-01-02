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
import org.jtool.postponablerefactoring.core.PostponableRefactoringStatus;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.ExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("restriction")
public class RefactoringWizardOpenOperationP {
    
    private RefactoringWizard fWizard;
    private RefactoringStatus fInitialConditions;
    
    public static final int INITIAL_CONDITION_CHECKING_FAILED = IDialogConstants.CLIENT_ID + 1;
    
    public RefactoringWizardOpenOperationP(RefactoringWizard wizard) {
        fWizard = wizard;
    }
    
    public RefactoringStatus getInitialConditionCheckingStatus() {
        return fInitialConditions;
    }
    
    public int run(final Shell parent, final String dialogTitle) throws InterruptedException {
        return run(parent, dialogTitle, null);
    }
    
    public int run(final Shell parent, final String dialogTitle, final IRunnableContext context) throws InterruptedException {
        final Refactoring refactoring = fWizard.getRefactoring();
        final IJobManager manager = Job.getJobManager();
        final int[] result = new int[1];
        final InterruptedException[] canceled = new InterruptedException[1];
        Runnable r = new Runnable() {
           
            @Override
            public void run() {
                try {
                    manager.beginRule(ResourcesPlugin.getWorkspace().getRoot(), null);
                    
                    refactoring.setValidationContext(parent);
                    fInitialConditions = checkInitialConditions(refactoring, parent, dialogTitle, context);
                    if (fInitialConditions.hasFatalError()) {
                        PostponableRefactoring postponableRefactoring = PostponableRefactoring.getPostponableRefactoring(refactoring);
                        if (postponableRefactoring == null) {
                            result[0] = INITIAL_CONDITION_CHECKING_FAILED;
                            return;
                        }
                        
                        PostponableRefactoringStatus status = postponableRefactoring.getPostponableRefactoringStatus();
                        if (status.isInitialFatal()) {
                            String message = fInitialConditions.getMessageMatchingSeverity(RefactoringStatus.FATAL);
                            MessageDialog.openError(parent, dialogTitle, message);
                            
                        } else {
                            String message = status.getRecoverableMessage();
                            if (confirmPostponement(parent, dialogTitle, message)) {
                                PostponableRefactoringManager.getInstance().postpone(postponableRefactoring);
                            }
                        }
                        result[0] = INITIAL_CONDITION_CHECKING_FAILED;
                        
                    } else {
                        fWizard.setInitialConditionCheckingStatus(fInitialConditions);
                        Dialog dialog = RefactoringUIP.createRefactoringWizardDialog(fWizard, parent);
                        dialog.create();
                        IWizardContainer wizardContainer = (IWizardContainer)dialog;
                        if (wizardContainer.getCurrentPage() == null) {
                            result[0] = Window.CANCEL;
                        } else {
                            result[0] = dialog.open();
                        }
                    }
                    
                } catch (InterruptedException e) {
                    canceled[0] = e;
                    
                } catch (OperationCanceledException e) {
                    canceled[0] = new InterruptedException(e.getMessage());
                    
                } finally {
                    manager.endRule(ResourcesPlugin.getWorkspace().getRoot());
                    refactoring.setValidationContext(null);
                    RefactoringContext refactoringContext = fWizard.getRefactoringContext();
                    if (refactoringContext != null)
                        refactoringContext.dispose();
                }
            }
        };
        
        BusyIndicator.showWhile(parent != null ? parent.getDisplay() : null, r);
        if (canceled[0] != null) {
            throw canceled[0];
        }
        return result[0];
    }
    
    private RefactoringStatus checkInitialConditions(Refactoring refactoring, Shell parent, String title, IRunnableContext context) throws InterruptedException {
        try {
            CheckConditionsOperation cco = new CheckConditionsOperation(refactoring, CheckConditionsOperation.INITIAL_CONDITONS);
            WorkbenchRunnableAdapter workbenchRunnableAdapter = new WorkbenchRunnableAdapter(cco, ResourcesPlugin.getWorkspace().getRoot());
            if (context == null) {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(workbenchRunnableAdapter);
            } else if (context instanceof IProgressService) {
                ((IProgressService)context).busyCursorWhile(workbenchRunnableAdapter);
            } else {
                context.run(true, true, workbenchRunnableAdapter);
            }
            return cco.getStatus();
        } catch (InvocationTargetException e) {
            ExceptionHandler.handle(e, parent, title, RefactoringUIMessages.RefactoringUI_open_unexpected_exception);
            return RefactoringStatus.createFatalErrorStatus(RefactoringUIMessages.RefactoringUI_open_unexpected_exception);
        }
    }
    
    private boolean confirmPostponement(Shell parent, String dialogTitle, String message) {
        // message = message + "\n\n" + "Do you cancel this refactoring or postpone it?";
        String[] buttonLabels = new String[] { IDialogConstants.CANCEL_LABEL, PostponableRefactoringManager.POSTPONE_LABEL };
        MessageDialog dialog = new MessageDialog(parent, dialogTitle, null, message, MessageDialog.ERROR, buttonLabels, 1);
        int res = dialog.open();
        if (res == 0) {
            return false;
        }
        return true;
    }
}
