/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.ui;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

@SuppressWarnings("restriction")
public class MenuContribution extends ExtensionContributionFactory {
    
    private static String RefactorMenu_label = "Postponable Refactor";
    
    private RefactorActionGroupJ refactorActionGroup;
    
    public MenuContribution() {
    }
    
    @Override
    public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        MenuManager manager = new MenuManager(RefactorMenu_label, RefactorActionGroupJ.MENU_ID);
        manager.setVisible(true);
        additions.addContributionItem(manager, null);
        
        IEditorPart part = EditorUtilities.getActiveEditor();
        if (part instanceof JavaEditor) {
            JavaEditor javaEditor = (JavaEditor)part;
            refactorActionGroup = new RefactorActionGroupJ(javaEditor);
            
            ActionContext context = new ActionContext(javaEditor.getSelectionProvider().getSelection());
            refactorActionGroup.setContext(context);
            refactorActionGroup.fillContextMenu(manager);
            refactorActionGroup.setContext(null);
        }
    }
}
