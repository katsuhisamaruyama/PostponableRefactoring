/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

public class Message {
    
    private final static boolean DEBUG = true;
    
    public static void informationDialog(IEditorPart editor, String title, String msg) {
        MessageDialog.openInformation(getShell(editor), title, msg);
    }
    
    public static void errorDialog(IEditorPart editor, String title, String msg) {
        MessageDialog.openError(getShell(editor), Activator.PLUGIN_ID, msg);
    }
    
    public static boolean yesnoDialog(IEditorPart editor, String title, String msg) {
        return MessageDialog.openQuestion(getShell(editor), Activator.PLUGIN_ID, msg);
    }
    
    public static void print(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
    
    public static void error(String msg) {
        System.err.println(msg);
    }
    
    private static Shell getShell(IEditorPart editor) {
        if (editor != null) {
            return editor.getSite().getShell();
        } else {
            return Activator.getWorkbenchWindow().getShell();
        }
    }
}
