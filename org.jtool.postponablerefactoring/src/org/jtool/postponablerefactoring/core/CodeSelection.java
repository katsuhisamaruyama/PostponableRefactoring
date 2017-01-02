/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class CodeSelection {
    
    private String path;
    private int start;
    private int end;
    
    private boolean isInsertionChanged;
    private boolean isDeletionChanged;
    
    private CodeSelection(String path, int start, int length) {
        this.path = path;
        this.start = start;
        this.end = start + length - 1;
    }
    
    private CodeSelection(CodeSelection selection) {
        this(selection.path, selection.start, selection.end);
    }
    
    String getPath() {
        return path;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public int getLength() {
        return end - start + 1;
    }
    
    boolean isChanged() {
        return isInsertionChanged || isDeletionChanged;
    }
    
    void insertText(int offset, String text) {
        isInsertionChanged = false;
        
        if (text.length() == 0) {
            return;
        }
        
        int insertOffset = offset;
        if (insertOffset <= start) {
            start = start + text.length();
            end = end + text.length();
        } else if (start < insertOffset && insertOffset <= end) {
            end = end + text.length();
            isInsertionChanged = true;
        } else if (end < insertOffset) {
            // do nothing
        }
    }
    
    void deleteText(int offset, String text) {
        isDeletionChanged = false;
        
        if (text.length() == 0) {
            return;
        }
        
        for (int deleteOffset = offset; deleteOffset < offset + text.length(); deleteOffset++) {
            if (deleteOffset < start) {
                start--;
                end--;
            } else if (start <= deleteOffset && deleteOffset <= end) {
                end--;
                isDeletionChanged = true;
            } else if (end < deleteOffset) {
                // do nothing
            }
        }
    }
    
    void replaceText(int offset, String insertedText, String deletedText) {
        deleteText(offset, deletedText);
        insertText(offset, insertedText);
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("(" + String.valueOf(start) + "-" + String.valueOf(end) + ")");
        return buf.toString();
    }
    
    static CodeSelection newCodeSelection(String path, int start, int length) {
        return new CodeSelection(path, start, length);
    }
    
    static CodeSelection getCodeSelectionForWholeRange(IJavaElement elem) {
        try {
            String path = elem.getPath().toPortableString();
            
            if (elem instanceof IType) {
                IType type = (IType)elem;
                ISourceRange range = type.getSourceRange();
                return new CodeSelection(path, range.getOffset(), range.getLength());
                
            } else if (elem instanceof IMethod) {
                IMethod method = (IMethod)elem;
                ISourceRange range = method.getSourceRange();
                return new CodeSelection(path, range.getOffset(), range.getLength());
                
            } else if (elem instanceof ILocalVariable) {
                ILocalVariable local = (ILocalVariable)elem;
                ISourceRange range = local.getSourceRange();
                return new CodeSelection(path, range.getOffset(), range.getLength());
            }
        } catch (JavaModelException e) {
            return null;
        }
        return null;
    }
    
    static CodeSelection getCodeSelectionForNameRange(IJavaElement elem) {
        try {
            String path = elem.getPath().toPortableString();
            
            if (elem instanceof IType) {
                IType type = (IType)elem;
                ISourceRange range = type.getNameRange();
                return new CodeSelection(path, range.getOffset(), range.getLength());
                
            } else if (elem instanceof IMethod) {
                IMethod method = (IMethod)elem;
                ISourceRange range = method.getNameRange();
                return new CodeSelection(path, range.getOffset(), range.getLength());
                
            } else if (elem instanceof ILocalVariable) {
                ILocalVariable local = (ILocalVariable)elem;
                ISourceRange range = local.getNameRange();
                return new CodeSelection(path, range.getOffset(), range.getLength());
            }
        } catch (JavaModelException e) {
            return null;
        }
        return null;
    }
}
