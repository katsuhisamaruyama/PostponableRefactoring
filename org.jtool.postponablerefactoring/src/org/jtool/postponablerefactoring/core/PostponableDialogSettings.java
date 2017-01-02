/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.core;

import java.util.Map;
import java.util.HashMap;

public class PostponableDialogSettings {
    
    private Map<String, String> settings = new HashMap<String, String>();
    
    public PostponableDialogSettings() {
    }
    
    public void put(String key, String value) {
        settings.put(key, value);
    }
    
    public void put(String key, int value) {
        settings.put(key, String.valueOf(value));
    }
    
    public void put(String key, long value) {
        settings.put(key, String.valueOf(value));
    }
    
    public void put(String key, float value) {
        settings.put(key, String.valueOf(value));
    }
    
    public void put(String key, double value) {
        settings.put(key, String.valueOf(value));
    }
    
    public void put(String key, boolean value) {
        settings.put(key, String.valueOf(value));
    }
    
    public String get(String key) throws PostponableDialogSettingsException {
        String setting = settings.get(key);
        if (setting == null) {
            throw new PostponableDialogSettingsException("There is no setting associated with the key \"" + key + "\"");
        }
        
        return setting;
    }
    
    public int getInt(String key) throws PostponableDialogSettingsException {
        String setting = settings.get(key);
        if (setting == null) {
            throw new PostponableDialogSettingsException("There is no setting associated with the key \"" + key + "\"");
        }
        
        try {
            return Integer.valueOf(setting).intValue();
        } catch (NumberFormatException e) {
            throw new PostponableDialogSettingsException(e.getMessage());
        }
    }
    
    public long getLong(String key) throws PostponableDialogSettingsException {
        String setting = settings.get(key);
        if (setting == null) {
            throw new PostponableDialogSettingsException("There is no setting associated with the key \"" + key + "\"");
        }
        
        try {
            return new Long(setting).longValue();
        } catch (NumberFormatException e) {
            throw new PostponableDialogSettingsException(e.getMessage());
        }
    }
    
    public float getFloat(String key) throws PostponableDialogSettingsException {
        String setting = settings.get(key);
        if (setting == null) {
            throw new PostponableDialogSettingsException("There is no setting associated with the key \"" + key + "\"");
        }
        try {
            return new Float(setting).floatValue();
        } catch (NumberFormatException e) {
            throw new PostponableDialogSettingsException(e.getMessage());
        }
    }
    
    public double getDouble(String key) throws PostponableDialogSettingsException {
        String setting = settings.get(key);
        if (setting == null) {
            throw new PostponableDialogSettingsException("There is no setting associated with the key \"" + key + "\"");
        }
        
        try {
            return new Double(setting).doubleValue();
        } catch (NumberFormatException e) {
            throw new PostponableDialogSettingsException(e.getMessage());
        }
    }
    
    public boolean getBoolean(String key) throws PostponableDialogSettingsException {
        String setting = settings.get(key);
        if (setting == null) {
            throw new PostponableDialogSettingsException("There is no setting associated with the key \"" + key + "\"");
        }
        
        return Boolean.valueOf(setting).booleanValue();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String key : settings.keySet()) {
            buf.append(key + "=" + settings.get(key) + "\n");
        }
        return buf.toString();
    }
}
