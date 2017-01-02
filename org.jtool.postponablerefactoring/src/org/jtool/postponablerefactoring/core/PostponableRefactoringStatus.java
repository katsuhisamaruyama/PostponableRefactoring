/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import java.util.List;
import java.util.ArrayList;

public class PostponableRefactoringStatus {
    
    private static final int NONE  = 1;
    private static final int INITIAL_OK  = 1;
    private static final int FINAL_OK    = 2;
    private static final int INITIAL_YET = 3;
    private static final int FINAL_YET   = 4;
    private static final int INITIAL_FATAL = 5;
    private static final int FINAL_FATAL   = 6;
    
    private int statusValue;
    
    private List<String> recoverableMessages = new ArrayList<String>();
    
    private RefactoringStatus refactoringResult;
    
    private CodeSelection codeSelection;
    
    private List<CodeSelection> watchedCodeSelections = new ArrayList<CodeSelection>();
    
    PostponableRefactoringStatus() {
        this.statusValue = PostponableRefactoringStatus.NONE;
    }
    
    void setStatus(int status) {
        this.statusValue = status;
    }
    
    void addRecoverableMessage(String msg) {
        recoverableMessages.add(msg);
    }
    
    public boolean isInitialOk() {
        return statusValue == PostponableRefactoringStatus.INITIAL_OK;
    }
    
    void setInitialOk() {
        statusValue = PostponableRefactoringStatus.INITIAL_OK;
    }
    
    public boolean isFinalOk() {
        return statusValue == PostponableRefactoringStatus.FINAL_OK;
    }
    
    void setFinalOk() {
        statusValue = PostponableRefactoringStatus.FINAL_OK;
    }
    
    public boolean isInitialYet() {
        return statusValue == PostponableRefactoringStatus.INITIAL_YET;
    }
    
    void setInitialYet() {
        statusValue = PostponableRefactoringStatus.INITIAL_YET;
    }
    
    public boolean isFinalYet() {
        return statusValue == PostponableRefactoringStatus.FINAL_YET;
    }
    
    void setFinalYet() {
        statusValue = PostponableRefactoringStatus.FINAL_YET;
    }
    
    public boolean isInitialFatal() {
        return statusValue == PostponableRefactoringStatus.INITIAL_FATAL;
    }
    
    void setInitialFatal() {
        statusValue = PostponableRefactoringStatus.INITIAL_FATAL;
    }
    
    public boolean isFinalFatal() {
        return statusValue == PostponableRefactoringStatus.FINAL_FATAL;
    }
    
    void setFinalFatal() {
        statusValue = PostponableRefactoringStatus.FINAL_FATAL;
    }
    
    boolean isInitial() {
        return isInitialOk() || isInitialYet() || isInitialFatal();
    }
    
    boolean isFinal() {
        return isFinalOk() || isFinalYet() || isFinalFatal();
    }
    
    int getStatusValue() {
        return statusValue;
    }
    
    boolean isOk() {
        return isInitialOk() || isFinalOk();
    }
    
    boolean isYet() {
        return isInitialYet() || isFinalYet();
    }
    
    boolean isFatal() {
        return isInitialFatal() || isFinalFatal();
    }
    
    static boolean isInitial(int value) {
        return value == PostponableRefactoringStatus.INITIAL_OK ||
               value == PostponableRefactoringStatus.INITIAL_YET ||
               value == PostponableRefactoringStatus.INITIAL_FATAL;
    }
    
    static boolean isFinal(int value) {
        return value == PostponableRefactoringStatus.FINAL_OK ||
               value == PostponableRefactoringStatus.FINAL_YET ||
               value == PostponableRefactoringStatus.FINAL_FATAL;
    }
    
    void setRefactoringResult(RefactoringStatus result) {
        refactoringResult = result;
    }
    
    RefactoringStatus getRefactoringResult() {
        return refactoringResult;
    }
    
    public String getRefactoringResultMessage() {
        if (refactoringResult == null) {
            return "";
        }
        
        StringBuilder message = new StringBuilder();
        for (RefactoringStatusEntry entry : refactoringResult.getEntries()) {
            message.append("\n" + entry.getMessage());
        }
        if (message.length() > 0) {
            return message.toString().substring(1);
        }
        return "";
    }
    
    public String getRecoverableMessage() {
        StringBuilder message = new StringBuilder();
        for (String msg : recoverableMessages) {
            message.append("\n" + msg);
        }
        if (message.length() > 0) {
            return message.toString().substring(1);
        }
        return "";
    }
    
    boolean hasSameRecoverableMessages(RefactoringStatus status) {
        if (recoverableMessages.size() != status.getEntries().length) {
            return false;
        }
        
        if (recoverableMessages.size() == 0) {
            return true;
        }
        
        if (recoverableMessages.size() == 1) {
            return recoverableMessages.get(0).equals(status.getEntries()[0].getMessage());
        }
        
        boolean[] checks = new boolean[recoverableMessages.size()];
        for (int idx = 0; idx < checks.length; idx++) {
            checks[idx] = false;
        }
        
        for (int idx = 0; idx < checks.length; idx++) {
            String message = recoverableMessages.get(idx);
            for (RefactoringStatusEntry entry : status.getEntries()) {
                if (message.equals(entry.getMessage())) {
                    checks[idx] = true;
                }
            }
        }
        
        boolean result = true;
        for (int idx = 0; idx < checks.length; idx++) {
             result = result && checks[idx];
        }
        return result;
    }
    
    static RefactoringStatusEntry[] getFatalErrorEntries(RefactoringStatusEntry[] entries) {
        return getEntries(entries, RefactoringStatus.FATAL);
    }
    
    static RefactoringStatusEntry[] getErrorEntries(RefactoringStatusEntry[] entries) {
        return getEntries(entries, RefactoringStatus.ERROR);
    }
    
    static RefactoringStatusEntry[] getEntries(RefactoringStatusEntry[] entries, int severity) {
        List<RefactoringStatusEntry> matches = new ArrayList<>(entries.length);
        for (RefactoringStatusEntry entry : entries) {
            if (entry.getSeverity() == severity) {
                matches.add(entry);
            }
        }
        return matches.toArray(new RefactoringStatusEntry[matches.size()]);
    }
    
    void setCodeSelection(CodeSelection selection) {
        codeSelection = selection;
    }
    
    CodeSelection getCodeSelection() {
        return codeSelection;
    }
    
    void addCodeSelection(CodeSelection selection) {
        watchedCodeSelections.add(selection);
    }
    
    List<CodeSelection> getCodeSelections() {
        return watchedCodeSelections;
    }
}
