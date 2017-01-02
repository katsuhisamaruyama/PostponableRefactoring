/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import java.util.ArrayList;
import java.util.List;

public class CodeSelectionChecker {
    
    private CodeSelection selection;
    
    private List<CodeSelection> watchedSelections = new ArrayList<CodeSelection>();
    
    CodeSelectionChecker() {
    }
    
    void dispose() {
        watchedSelections.clear();
    }
    
    void setCodeSelection(CodeSelection selection) {
        this.selection = selection;
    }
    
    CodeSelection getCodeSelection() {
        return selection;
    }
    
    void setCodeSelections(List<CodeSelection> selections) {
        for (CodeSelection sel : selections) {
            watchedSelections.add(sel);
        }
    }
    
    void updateCodeSelection(CodeChange codeChange) {
        String path = codeChange.getPath();
        if (path.equals(selection.getPath())) {
            selection.replaceText(codeChange.getOffset(), codeChange.getInsertedText(), codeChange.getDeletedText());
        }
        
        for (CodeSelection sel : watchedSelections) {
            if (path.equals(sel.getPath())) {
                sel.replaceText(codeChange.getOffset(), codeChange.getInsertedText(), codeChange.getDeletedText());
            }
        }
    }
    
    boolean isCodeSelectionChanged() {
        if (selection.isChanged()) {
            return true;
        }
        
        for (CodeSelection sel : watchedSelections) {
            if (sel.isChanged()) {
                return true;
            }
        }
        return false;
    }
}
