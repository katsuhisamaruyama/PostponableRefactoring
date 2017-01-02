/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.ui;

import org.jtool.postponablerefactoring.eclipse.ui.InlineActionP;
// import org.jtool.postponablerefactoring.eclipse.ui.ReplaceWithQueryAction;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jdt.ui.actions.RenameAction;
import org.eclipse.jdt.ui.actions.MoveAction;
import org.eclipse.jdt.ui.actions.ModifyParametersAction;
import org.eclipse.jdt.ui.actions.ConvertAnonymousToNestedAction;
import org.eclipse.jdt.ui.actions.ConvertNestedToTopAction;
import org.eclipse.jdt.ui.actions.PullUpAction;
import org.eclipse.jdt.ui.actions.PushDownAction;
import org.eclipse.jdt.ui.actions.ExtractInterfaceAction;
import org.eclipse.jdt.ui.actions.ExtractClassAction;
import org.eclipse.jdt.ui.actions.ChangeTypeAction;
import org.eclipse.jdt.ui.actions.InferTypeArgumentsAction;
import org.eclipse.jdt.ui.actions.ExtractTempAction;
import org.eclipse.jdt.ui.actions.ExtractConstantAction;
import org.eclipse.jdt.ui.actions.IntroduceParameterAction;
import org.eclipse.jdt.ui.actions.IntroduceFactoryAction;
import org.eclipse.jdt.ui.actions.ConvertLocalToFieldAction;
import org.eclipse.jdt.ui.actions.SelfEncapsulateFieldAction;
import org.eclipse.jdt.ui.actions.IntroduceIndirectionAction;
import org.eclipse.jdt.ui.actions.UseSupertypeAction;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.RefactorActionGroup;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.ExtractSuperClassAction;
import org.eclipse.jdt.internal.ui.actions.IntroduceParameterObjectAction;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Menu;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("restriction")
public class RefactorActionGroupJ extends ActionGroup {
    
    static final String MENU_ID = "org.jtool.eclipse.refactoring.menu";
    
    private JavaEditor javaEditor;
    private final ISelectionProvider selectionProvider;
    
    private final List<SelectionDispatchAction> actions = new ArrayList<>();
    
    private SelectionDispatchAction moveAction;
    private SelectionDispatchAction renameAction;
    private SelectionDispatchAction modifyParametersAction;
    private SelectionDispatchAction convertAnonymousToNestedAction;
    private SelectionDispatchAction convertNestedToTopAction;
    
    private SelectionDispatchAction pullUpAction;
    private SelectionDispatchAction pushDownAction;
    private SelectionDispatchAction extractInterfaceAction;
    private SelectionDispatchAction extractSupertypeAction;
    private SelectionDispatchAction changeTypeAction;
    private SelectionDispatchAction useSupertypeAction;
    private SelectionDispatchAction inferTypeArgumentsAction;
    
    private SelectionDispatchAction inlineAction;
    private SelectionDispatchAction introduceIndirectionAction;
    private SelectionDispatchAction extractMethodAction;
    // private SelectionDispatchAction replaceWithQueryAction;
    private SelectionDispatchAction extractTempAction;
    private SelectionDispatchAction extractConstantAction;
    private SelectionDispatchAction extractClassAction;
    private SelectionDispatchAction introduceParameterAction;
    private SelectionDispatchAction introduceParameterObjectAction;
    private SelectionDispatchAction introduceFactoryAction;
    private SelectionDispatchAction convertLocalToFieldAction;
    private SelectionDispatchAction selfEncapsulateField;
    
    public RefactorActionGroupJ(JavaEditor editor) {
        javaEditor = editor;
        selectionProvider = editor.getEditorSite().getSelectionProvider();
        ISelection selection = selectionProvider.getSelection();
        
        renameAction = new RenameAction(editor);
        initAction(renameAction, selection, IJavaEditorActionDefinitionIds.RENAME_ELEMENT);
        editor.setAction("RenameElement", renameAction);
        
        moveAction = new MoveAction(editor);
        initAction(moveAction, selection, IJavaEditorActionDefinitionIds.MOVE_ELEMENT);
        editor.setAction("MoveElement", moveAction);
        
        modifyParametersAction = new ModifyParametersAction(editor);
        initAction(modifyParametersAction, selection, IJavaEditorActionDefinitionIds.MODIFY_METHOD_PARAMETERS);
        editor.setAction("ModifyParameters", modifyParametersAction);
        
        convertAnonymousToNestedAction = new ConvertAnonymousToNestedAction(editor);
        initUpdatingAction(convertAnonymousToNestedAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.CONVERT_ANONYMOUS_TO_NESTED);
        editor.setAction("ConvertAnonymousToNested", convertAnonymousToNestedAction);
        
        convertNestedToTopAction = new ConvertNestedToTopAction(editor);
        initAction(convertNestedToTopAction, selection, IJavaEditorActionDefinitionIds.MOVE_INNER_TO_TOP);
        editor.setAction("MoveInnerToTop", convertNestedToTopAction);
        
        pullUpAction = new PullUpAction(editor);
        initAction(pullUpAction, selection, IJavaEditorActionDefinitionIds.PULL_UP);
        editor.setAction("PullUp", pullUpAction);
        
        pushDownAction = new PushDownAction(editor);
        initAction(pushDownAction, selection, IJavaEditorActionDefinitionIds.PUSH_DOWN);
        editor.setAction("PushDown", pushDownAction);
        
        extractSupertypeAction = new ExtractSuperClassAction(editor);
        initAction(extractSupertypeAction, selection, ExtractSuperClassAction.EXTRACT_SUPERTYPE);
        editor.setAction("ExtractSupertype", extractSupertypeAction);
        
        extractInterfaceAction = new ExtractInterfaceAction(editor);
        initAction(extractInterfaceAction, selection, IJavaEditorActionDefinitionIds.EXTRACT_INTERFACE);
        editor.setAction("ExtractInterface", extractInterfaceAction);
        
        extractClassAction = new ExtractClassAction(editor);
        initAction(extractClassAction, selection, IJavaEditorActionDefinitionIds.EXTRACT_CLASS);
        editor.setAction("ExtractClass", extractClassAction);
        
        changeTypeAction = new ChangeTypeAction(editor);
        initUpdatingAction(changeTypeAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.CHANGE_TYPE);
        editor.setAction("ChangeType", changeTypeAction);
        
        inferTypeArgumentsAction = new InferTypeArgumentsAction(editor);
        initAction(inferTypeArgumentsAction, selection, IJavaEditorActionDefinitionIds.INFER_TYPE_ARGUMENTS_ACTION);
        editor.setAction("InferTypeArguments", inferTypeArgumentsAction);
        
        extractMethodAction = new PostponableExtractMethodAction(editor);
        initUpdatingAction(extractMethodAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.EXTRACT_METHOD);
        editor.setAction("ExtractMethod", extractMethodAction);
        
        extractTempAction = new ExtractTempAction(editor);
        initUpdatingAction(extractTempAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.EXTRACT_LOCAL_VARIABLE);
        editor.setAction("ExtractLocalVariable", extractTempAction);
        
        extractConstantAction = new ExtractConstantAction(editor);
        initUpdatingAction(extractConstantAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.EXTRACT_CONSTANT);
        editor.setAction("ExtractConstant", extractConstantAction);
        
        introduceParameterAction = new IntroduceParameterAction(editor);
        initUpdatingAction(introduceParameterAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.INTRODUCE_PARAMETER);
        editor.setAction("IntroduceParameter", introduceParameterAction);
        
        introduceFactoryAction = new IntroduceFactoryAction(editor);
        initUpdatingAction(introduceFactoryAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.INTRODUCE_FACTORY);
        editor.setAction("IntroduceFactory", introduceFactoryAction);
        
        convertLocalToFieldAction = new ConvertLocalToFieldAction(editor);
        initUpdatingAction(convertLocalToFieldAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.PROMOTE_LOCAL_VARIABLE);
        editor.setAction("PromoteTemp", convertLocalToFieldAction);
        
        selfEncapsulateField = new SelfEncapsulateFieldAction(editor);
        initAction(selfEncapsulateField, selection, IJavaEditorActionDefinitionIds.SELF_ENCAPSULATE_FIELD);
        editor.setAction("SelfEncapsulateField", selfEncapsulateField);
        
        introduceParameterObjectAction = new IntroduceParameterObjectAction(editor);
        initAction(introduceParameterObjectAction, selection, IJavaEditorActionDefinitionIds.INTRODUCE_PARAMETER_OBJECT);
        editor.setAction("IntroduceParameterObjectAction", introduceParameterObjectAction);
        
        introduceIndirectionAction = new IntroduceIndirectionAction(editor);
        initUpdatingAction(introduceIndirectionAction, selectionProvider, null, selection, IJavaEditorActionDefinitionIds.INTRODUCE_INDIRECTION);
        editor.setAction("IntroduceIndirection", introduceIndirectionAction);
        
        useSupertypeAction = new UseSupertypeAction(editor);
        initAction(useSupertypeAction, selection, IJavaEditorActionDefinitionIds.USE_SUPERTYPE);
        editor.setAction("UseSupertype", useSupertypeAction);
        
        inlineAction = new InlineActionP(editor);
        initAction(inlineAction, selection, IJavaEditorActionDefinitionIds.INLINE);
        editor.setAction("Inline", inlineAction);
        
        // replaceWithQueryAction = new ReplaceWithQueryAction(editor);
        // initUpdatingAction(replaceWithQueryAction, selectionProvider, null, selection, null);
        // editor.setAction("ReplaceWithQuery", replaceWithQueryAction);
    }
    
    private void initAction(SelectionDispatchAction action, ISelection selection, String actionDefinitionId) {
        initUpdatingAction(action, null, null, selection, actionDefinitionId);
    }
    
    private void initUpdatingAction(SelectionDispatchAction action, ISelectionProvider provider, ISelectionProvider specialProvider, ISelection selection, String actionDefinitionId) {
        action.setActionDefinitionId(actionDefinitionId);
        action.update(selection);
        
        if (provider != null) {
            provider.addSelectionChangedListener(action);
        }
        if (specialProvider != null) {
            action.setSpecialSelectionProvider(specialProvider);
        }
        
        actions.add(action);
    }
    
    @Override
    public void dispose() {
        disposeAction(selfEncapsulateField, selectionProvider);
        disposeAction(moveAction, selectionProvider);
        disposeAction(renameAction, selectionProvider);
        disposeAction(modifyParametersAction, selectionProvider);
        disposeAction(pullUpAction, selectionProvider);
        disposeAction(pushDownAction, selectionProvider);
        disposeAction(extractTempAction, selectionProvider);
        disposeAction(extractConstantAction, selectionProvider);
        disposeAction(introduceParameterAction, selectionProvider);
        disposeAction(introduceParameterObjectAction, selectionProvider);
        disposeAction(introduceFactoryAction, selectionProvider);
        disposeAction(extractMethodAction, selectionProvider);
        disposeAction(extractInterfaceAction, selectionProvider);
        disposeAction(extractClassAction, selectionProvider);
        disposeAction(extractSupertypeAction, selectionProvider);
        disposeAction(changeTypeAction, selectionProvider);
        disposeAction(convertNestedToTopAction, selectionProvider);
        disposeAction(inferTypeArgumentsAction, selectionProvider);
        disposeAction(convertLocalToFieldAction, selectionProvider);
        disposeAction(convertAnonymousToNestedAction, selectionProvider);
        disposeAction(introduceIndirectionAction, selectionProvider);
        disposeAction(inlineAction, selectionProvider);
        // disposeAction(replaceWithQueryAction, selectionProvider);
        disposeAction(useSupertypeAction, selectionProvider);
        
        super.dispose();
    }
    
    private void disposeAction(ISelectionChangedListener action, ISelectionProvider provider) {
        if (action != null) {
            provider.removeSelectionChangedListener(action);
        }
    }
    
    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        addRefactorSubmenu(menu);
    }
    
    private void addRefactorSubmenu(IMenuManager refactorMenu) {
        if (javaEditor != null) {
            ITypeRoot element = EditorUtilities.getEditorInput(javaEditor);
            if (element != null && ActionUtil.isOnBuildPath(element)) {
                refactorMenu.addMenuListener(new IMenuListener() {
                    
                    @Override
                    public void menuAboutToShow(IMenuManager manager) {
                        refactorMenuShown(manager);
                    }
                });
                
                refactorMenu.add(fNoActionAvailable);
                fillRefactorMenu(refactorMenu);
            }
        }
    }
    
    private int fillRefactorMenu(IMenuManager refactorMenu) {
        int added = 0;
        refactorMenu.add(new Separator(RefactorActionGroup.GROUP_REORG));
        added += addAction(refactorMenu, renameAction);
        added += addAction(refactorMenu, moveAction);
        
        refactorMenu.add(new Separator(RefactorActionGroup.GROUP_CODING));
        added += addAction(refactorMenu, modifyParametersAction);
        added += addAction(refactorMenu, extractMethodAction);
        added += addAction(refactorMenu, extractTempAction);
        added += addAction(refactorMenu, extractConstantAction);
        added += addAction(refactorMenu, inlineAction);
        // added += addAction(refactorMenu, replaceWithQueryAction);
        
        refactorMenu.add(new Separator(RefactorActionGroup.GROUP_REORG2));
        added += addAction(refactorMenu, convertAnonymousToNestedAction);
        added += addAction(refactorMenu, convertNestedToTopAction);
        added += addAction(refactorMenu, convertLocalToFieldAction);
        
        refactorMenu.add(new Separator(RefactorActionGroup.GROUP_TYPE));
        added += addAction(refactorMenu, extractInterfaceAction);
        added += addAction(refactorMenu, extractSupertypeAction);
        added += addAction(refactorMenu, useSupertypeAction);
        added += addAction(refactorMenu, pullUpAction);
        added += addAction(refactorMenu, pushDownAction);
        
        refactorMenu.add(new Separator(RefactorActionGroup.GROUP_TYPE2));
        added += addAction(refactorMenu, extractClassAction);
        added += addAction(refactorMenu, introduceParameterObjectAction);
        
        refactorMenu.add(new Separator(RefactorActionGroup.GROUP_CODING2));
        added += addAction(refactorMenu, introduceIndirectionAction);
        added += addAction(refactorMenu, introduceFactoryAction);
        added += addAction(refactorMenu, introduceParameterAction);
        added += addAction(refactorMenu, selfEncapsulateField);
        
        refactorMenu.add(new Separator(RefactorActionGroup.GROUP_TYPE3));
        added += addAction(refactorMenu, changeTypeAction);
        added += addAction(refactorMenu, inferTypeArgumentsAction);
        
        return added;
    }
    
    private int addAction(IMenuManager menu, IAction action) {
        if (action != null && action.isEnabled()) {
            menu.add(action);
            return 1;
        }
        return 0;
    }
    
    private void refactorMenuShown(IMenuManager refactorMenu) {
        Menu menu = ((MenuManager)refactorMenu).getMenu();
        menu.addMenuListener(new MenuAdapter() {
            
            @Override
            public void menuHidden(MenuEvent e) {
                refactorMenuHidden();
            }
        });
        
        ITextSelection textSelection = (ITextSelection)javaEditor.getSelectionProvider().getSelection();
        ITypeRoot element = EditorUtilities.getEditorInput(javaEditor);
        IDocument document = EditorUtilities.getDocument(javaEditor);
        JavaTextSelection javaSelection = new JavaTextSelection(element, document, textSelection.getOffset(), textSelection.getLength());
        
        for (Iterator<SelectionDispatchAction> iter = actions.iterator(); iter.hasNext(); ) {
            SelectionDispatchAction action = iter.next();
            action.update(javaSelection);
        }
        
        refactorMenu.removeAll();
        if (fillRefactorMenu(refactorMenu) == 0) {
            refactorMenu.add(fNoActionAvailable);
        }
    }
    
    private void refactorMenuHidden() {
        ITextSelection textSelection = (ITextSelection)javaEditor.getSelectionProvider().getSelection();
        for (Iterator<SelectionDispatchAction> iter = actions.iterator(); iter.hasNext(); ) {
            SelectionDispatchAction action = iter.next();
            action.update(textSelection);
        }
    }
    
    private Action fNoActionAvailable = new NoActionAvailable();
    
    private static class NoActionAvailable extends Action {
        
        public NoActionAvailable() {
            setEnabled(true);
            setText(RefactoringMessages.RefactorActionGroup_no_refactoring_available);
        }
    }
}
