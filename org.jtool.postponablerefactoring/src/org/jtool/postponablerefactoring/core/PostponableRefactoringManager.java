/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import org.jtool.macrorecorder.recorder.MacroRecorder;
import org.jtool.postponablerefactoring.ui.PostponableRefactoringView;
import org.jtool.macrorecorder.recorder.IMacroRecorder;
import org.jtool.macrorecorder.recorder.IMacroListener;
import org.jtool.macrorecorder.recorder.MacroEvent;
import org.jtool.macrorecorder.recorder.IMacroCompressor;
import org.jtool.macrorecorder.macro.Macro;
import org.jtool.macrorecorder.macro.FileMacro;
import org.jtool.macrorecorder.macro.DocumentMacro;
import org.jtool.macrorecorder.macro.CompoundMacro;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.time.ZonedDateTime;

public class PostponableRefactoringManager implements IMacroListener {
    
    private static PostponableRefactoringManager manager = new PostponableRefactoringManager();
    
    private Map<PostponableRefactoring, PostponableRefactoringElement> refactoringElements = new HashMap<PostponableRefactoring, PostponableRefactoringElement>();
    
    public final static int POSTPONE_ID = 30;
    public final static String POSTPONE_LABEL = "Postpone";
    
    private String prevPath = "";
    
    public static PostponableRefactoringManager getInstance() {
        return manager;
    }
    
    public void start() {
        startListeners();
    }
    
    public void stop() {
        stopListeners();
    }
    
    public void startListeners() {
        IMacroRecorder recorder = MacroRecorder.getInstance();
        IMacroCompressor compressor = recorder.getMacroCompressor();
        compressor.setDelimiter(new char[] { });
        recorder.addMacroListener(this);
        recorder.start();
    }
    
    public void stopListeners() {
        IMacroRecorder recorder = MacroRecorder.getInstance();
        recorder.removeMacroListener(this);
        recorder.stop();
    }
    
    @Override
    public void macroAdded(MacroEvent evt) {
        Macro macro = evt.getMacro();
        
        PostponableRefactoringView view = PostponableRefactoringView.getPostponableRefactoringView();
        if (view == null) {
            return;
        }
        
        if (macro instanceof CompoundMacro) {
            CompoundMacro cmacro = (CompoundMacro)macro;
            boolean needUpdate = false;
            for (Macro m : cmacro.getMacros()) {
                boolean update = perform(m, view);
                needUpdate = needUpdate || update;
            }
            if (needUpdate) {
                view.showPostponedRefactorings(macro.getPath());
            }
            
        } else {
            boolean needUpdate = perform(macro, view);
            if (needUpdate) {
                view.showPostponedRefactorings(macro.getPath());
            }
        }
    }
    
    @Override
    public void rawMacroAdded(MacroEvent evt) {
    }
    
    private boolean perform(Macro macro, PostponableRefactoringView view) {
        // System.out.println("MACRO " + macro.toString());
        
        if (macro instanceof FileMacro) {
            FileMacro fmacro = (FileMacro)macro;
            if (fmacro.isOpen() || fmacro.isSave() || fmacro.isRefactor()) {
                updateRestartStatus(fmacro.getPath());
                return true;
                
            } else if (fmacro.isActivate()) {
                if (!prevPath.equals(fmacro.getPath())) {
                    updateRestartStatus(fmacro.getPath());
                    return true;
                }
                return false;
                
            } else if (fmacro.isDelete()) {
                remove(fmacro.getPath());
                return true;
                
            } else if (fmacro.isMoveFrom() || fmacro.isRenameFrom()) {
                replace(macro.getPath(), fmacro.getSrcDstPath());
                return true;
            }
            prevPath = fmacro.getPath();
            
        } else if (macro instanceof DocumentMacro) {
            DocumentMacro dmacro = (DocumentMacro)macro;
            CodeChange codeChange = new CodeChange(dmacro.getPath(), dmacro.getStart(), dmacro.getInsertedText(), dmacro.getDeletedText());
            
            for (PostponableRefactoringElement elem : refactoringElements.values()) {
                elem.updateCodeSelection(codeChange);
            }
            prevPath = dmacro.getPath();
            return updateRestartStatus(dmacro.getPath());
        }
        return false;
    }
    
    public int size() {
        return refactoringElements.size();
    }
    
    public void postpone(PostponableRefactoring refactoring) {
        PostponableRefactoringElement elem = new PostponableRefactoringElement(refactoring);
        elem.postpone();
        add(elem);
        
        PostponableRefactoringView.show();
    }
    
    private void add(PostponableRefactoringElement elem) {
        refactoringElements.put(elem.getRefactoring(), elem);
        PostponableRefactoringView view = PostponableRefactoringView.getPostponableRefactoringView();
        if (view != null) {
            view.showPostponedRefactorings(elem.getPath());
            view.setFocus();
        }
    }
    
    public void remove(PostponableRefactoringElement elem) {
        refactoringElements.remove(elem.getRefactoring());
    }
    
    private void remove(String path) {
        for (PostponableRefactoringElement elem : getRefactorings(path)) {
            refactoringElements.remove(elem.getRefactoring());
        }
    }
    
    private void replace(String path, String fromPath) {
        for (PostponableRefactoringElement elem : getRefactorings(fromPath)) {
            elem.setPath(path);
        }
    }
    
    private boolean updateRestartStatus(String path) {
        boolean needUpdate = false;
        for (PostponableRefactoringElement elem : getRefactorings(path)) {
            if (elem.updateRestartStatus()) {
                needUpdate = true;
            }
        }
        return needUpdate;
    }
    
    private List<PostponableRefactoringElement> getRefactorings(String path) {
        List<PostponableRefactoringElement> elems = new ArrayList<PostponableRefactoringElement>();
        if (path == null) {
            return elems;
        }
        
        for (PostponableRefactoringElement elem : refactoringElements.values()) {
            if (path.equals(elem.getPath())) {
                elems.add(elem);
            }
        }
        
        return elems;
    }
    
    public List<PostponableRefactoringElement> getRefactoringList(String path) {
        List<PostponableRefactoringElement> elems = getRefactorings(path);
        sort(elems);
        return elems;
    }
    
    private static void sort(List<PostponableRefactoringElement> elems) {
        Collections.sort(elems, new Comparator<PostponableRefactoringElement>() {
            
            public int compare(PostponableRefactoringElement elem1, PostponableRefactoringElement elem2) {
                ZonedDateTime time1 = elem1.getTime();
                ZonedDateTime time2 = elem2.getTime();
                
                if (time1.isAfter(time2)) {
                    return -1;
                } else if (time1.isBefore(time2)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }
}
