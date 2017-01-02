/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jtool.postponablerefactoring.core.PostponableRefactoringManager;
import org.osgi.framework.BundleContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IStartup;
import org.eclipse.jface.resource.ImageDescriptor;

public class Activator extends AbstractUIPlugin implements IStartup {
    
    public static final String PLUGIN_ID = "org.jtool.postponablerefactoring";
    
    private static Activator plugin;
    
    public Activator() {
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        PostponableRefactoringManager.getInstance().start();
        
        System.out.println("Refactoring plug-in is activated.");
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        PostponableRefactoringManager.getInstance().stop();
        
        plugin = null;
        super.stop(context);
        
        System.out.println("Refactoring plug-in is deactivated.");
    }
    
    public static Activator getDefault() {
        return plugin;
    }
    
    public static IWorkbenchWindow getWorkbenchWindow() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
    
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    @Override
    public void earlyStartup() {
    }
}
