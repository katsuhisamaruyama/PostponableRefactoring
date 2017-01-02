/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

public class CodeChange {
    
    private String path;
    private int offset;
    private String insertedText;
    private String deletedText;
    
    public CodeChange(String path, int offset, String insertedText, String deletedText) {
        this.path = path;
        this.offset = offset;
        this.insertedText = insertedText;
        this.deletedText = deletedText;
    }
    
    String getPath() {
        return path;
    }
    
    int getOffset() {
        return offset;
    }
    
    String getInsertedText() {
        return insertedText;
    }
    
    String getDeletedText() {
        return deletedText;
    }
}
