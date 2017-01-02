/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.postponablerefactoring.ui;

import org.jtool.postponablerefactoring.Activator;
import org.jtool.postponablerefactoring.core.PostponableRefactoringElement;
import org.jtool.postponablerefactoring.core.PostponableRefactoringManager;
import org.jtool.postponablerefactoring.ui.PostponableRefactoringView;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseEvent;
import java.util.List;

public class PostponableRefactoringView extends ViewPart {
    
    public static final String ID = "postponableRefactoring.waitingRefactoringView";
    
    private Composite panel;
    
    private static PostponableRefactoringView view = null;
    
    private static Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    
    private static Font font = new Font(shell.getDisplay(), "", 11, SWT.NORMAL);
    
    private static Color greenColor = new Color(null, 0x99, 0xe6, 0x99);
    private static Color redColor = new Color(null, 0xff, 0xcc, 0xcc);
    private static Color brownColor = new Color(null, 0xb0, 0x9f, 0x0a);
    
    private static ImageDescriptor menuIcon = Activator.getImageDescriptor("icons/view_menu.gif");
    
    public PostponableRefactoringView() {
    }
    
    public static PostponableRefactoringView getPostponableRefactoringView() {
        if (view == null) {
            IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            view = (PostponableRefactoringView)workbenchPage.findView(PostponableRefactoringView.ID);
        }
        return view;
    }
    
    public static void show() {
        IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            workbenchPage.showView(PostponableRefactoringView.ID);
        } catch (PartInitException e) {
        }
    }
    
    @Override
    public void createPartControl(Composite parent) {
        final int VIEW_HEIGHT = 5000;
        
        parent.setLayout(new FillLayout());
        
        ScrolledComposite sc = new ScrolledComposite(parent, SWT.NONE | SWT.V_SCROLL);
        sc.setLayout(new FillLayout());
        sc.setMinHeight(VIEW_HEIGHT);
        sc.setExpandVertical(true);
        sc.setExpandHorizontal(true);
        
        panel = new Composite(sc, SWT.NONE);
        panel.setBackground(new Color(shell.getDisplay(), new RGB(255, 255, 255)));
        panel.setLayout(new GridLayout(1, true));
        sc.setContent(panel);
        
        IEditorPart editor = EditorUtilities.getActiveEditor();
        String filePath = EditorUtilities.getInputFilePath(editor);
        showPostponedRefactorings(filePath);
    }
    
    protected Composite getPanel() {
        return panel;
    }
    
    @Override
    public void setFocus() {
        if (panel != null) {
            panel.setFocus();
        }
    }
    
    @Override
    public void dispose() {
        panel.dispose();
        panel = null;
        super.dispose();
    }
    
    public void showPostponedRefactorings(String path) {
        if (path == null || panel == null || panel.isDisposed()) {
            return;
        }
        UIJob job = new UIJob("Start") {
            
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                update(path);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    private void update(String path) {
        clearItems();
        
        List<PostponableRefactoringElement> elems = PostponableRefactoringManager.getInstance().getRefactoringList(path);
        if (path == null || elems.size() == 0) {
            CLabel label = new CLabel(panel, SWT.NONE);
            label.setText("NONE");
            label.setAlignment(SWT.LEFT);
            panel.pack();
            return;
        }
        
        for (PostponableRefactoringElement elem : elems) {
            createItem(elem);
        }
    }
    
    private void createItem(PostponableRefactoringElement elem) {
        final int COMMENT_HEIGHT = 100;
        
        ViewForm viewForm = new ViewForm(panel, SWT.BORDER);
        GridData vfdata = new GridData(GridData.FILL_HORIZONTAL);
        vfdata.heightHint = COMMENT_HEIGHT;
        viewForm.setLayoutData(vfdata);
        
        CLabel label = new CLabel(viewForm, SWT.NONE);
        label.setFont(font);
        if (elem.isOk()) {
            label.setBackground(greenColor);
        } else if (elem.isYet()) {
            label.setBackground(redColor);
        } else {
            label.setBackground(brownColor);
        }
        label.setText(elem.getTimeRepresentation() + " - " + elem.getName());
        label.setAlignment(SWT.LEFT);
        viewForm.setTopLeft(label);
        
        label.addMouseListener(new MouseListener() {
            
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                elem.codeSelection();
            }
            
            @Override
            public void mouseDown(MouseEvent e) {
            }
            
            @Override
            public void mouseUp(MouseEvent e) {
            }
        });
        
        Text text = new Text(viewForm, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        text.setFont(font);
        text.setText(elem.getComment());
        text.setEditable(false);
        viewForm.setContent(text);
        
        ToolBar toolBarMenu = new ToolBar(viewForm, SWT.FLAT);
        ToolItem toolItem = new ToolItem(toolBarMenu, SWT.PUSH);
        toolItem.setImage(menuIcon.createImage());
        viewForm.setTopRight(toolBarMenu);
        
        final Menu menu = new Menu(toolBarMenu);
        toolItem.addSelectionListener(new SelectionListener() {
            
            public void widgetSelected(SelectionEvent e) {
                menu.setVisible(true);
            }
            
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        MenuItem restartMenuItem = new MenuItem(menu, SWT.NONE);
        restartMenuItem.setText("Restart");
        if (elem.isOk()) {
            restartMenuItem.setEnabled(true);
        } else {
            restartMenuItem.setEnabled(false);
        }
        restartMenuItem.addSelectionListener(new SelectionListener() {
            
            public void widgetSelected(SelectionEvent e) {
                boolean restart = elem.restart();
                if (restart) {
                    viewForm.dispose();
                    PostponableRefactoringManager.getInstance().remove(elem);
                    showPostponedRefactorings(elem.getPath());
                }
            }
            
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        MenuItem cancelmenuItem = new MenuItem(menu, SWT.NONE);
        cancelmenuItem.setText("Cancel");
        cancelmenuItem.setEnabled(true);
        cancelmenuItem.addSelectionListener(new SelectionListener() {
            
            public void widgetSelected(SelectionEvent e) {
                viewForm.dispose();
                
                PostponableRefactoringManager.getInstance().remove(elem);
                showPostponedRefactorings(elem.getPath());
            }
            
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        panel.layout();
    }
    
    private void clearItems() {
        for (Control control : panel.getChildren()) {
            control.dispose();
        }
    }
}
