/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class EditorUtilities {
    
    public static IFile getInputFile(IEditorPart editor) {
        if (editor == null) {
            return null;
        }
        
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            return file;
        }
        return null;
    }
    
    public static String getInputFilePath(IEditorPart editor) {
        IFile file = getInputFile(editor);
        return getInputFilePath(file);
    }
    
    public static String getInputFilePath(IFile file) {
        if (file == null) {
            return null;
        }
        return file.getFullPath().toString();
    }
    
    public static ITypeRoot getEditorInput(IEditorPart editor) {
        if (editor == null) {
            return null;
        }
        return JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
    }
    
    public static IDocument getDocument(IEditorPart editor) {
        if (editor == null) {
            return null;
        }
        return JavaUI.getDocumentProvider().getDocument(editor.getEditorInput());
    }
    
    public static String getSourceCode(IEditorPart editor) {
        IDocument doc = getDocument(editor);
        if (doc == null) {
            return null;
        }
        return doc.get();
    }
    
    public static String getCharset(IEditorPart editor) {
        IFile file = EditorUtilities.getInputFile(editor);
        try {
            return file.getCharset();
        } catch (CoreException e) {
        }
        return null;
    }
    
    public static IEditorPart getActiveEditor() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        return window.getActivePage().getActiveEditor();
    }
}
