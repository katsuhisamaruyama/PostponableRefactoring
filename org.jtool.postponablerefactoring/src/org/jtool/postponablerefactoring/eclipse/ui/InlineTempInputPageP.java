package org.jtool.postponablerefactoring.eclipse.ui;

import org.jtool.postponablerefactoring.eclipse.internal.InlineTempRefactoringP;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.refactoring.MessageWizardPage;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
class InlineTempInputPageP extends MessageWizardPage {
    
    public static final String PAGE_NAME = "InlineTempInputPage";
    
    private String sideEffectMessages;
    
    public InlineTempInputPageP() {
        super(PAGE_NAME, true, MessageWizardPage.STYLE_QUESTION);
    }
    
    @Override
    public void createControl(Composite parent) {
        InlineTempRefactoringP refactoring = (InlineTempRefactoringP)getRefactoring();
        sideEffectMessages = refactoring.checkSideEffectOccurrences();
        
        super.createControl(parent);
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaHelpContextIds.INLINE_TEMP_WIZARD_PAGE);
    }
    
    @Override
    protected Image getMessageImage() {
        if (sideEffectMessages.length() != 0) {
            return Display.getCurrent().getSystemImage(SWT.ICON_ERROR);
        }
        return Display.getCurrent().getSystemImage(SWT.ICON_QUESTION);
    }
    
    @Override
    protected String getMessageString() {
        InlineTempRefactoring refactoring= (InlineTempRefactoring) getRefactoring();
        int occurrences= refactoring.getReferences().length;
        final String identifier= BasicElementLabels.getJavaElementName(refactoring.getVariableDeclaration().getName().getIdentifier());
        switch (occurrences) {
            case 0:
                return Messages.format(RefactoringMessages.InlineTempInputPage_message_zero, identifier);
                
            case 1:
                return getSideEfefctMessages() + Messages.format(RefactoringMessages.InlineTempInputPage_message_one, identifier);
                
            default:
                
                return getSideEfefctMessages() + Messages.format(RefactoringMessages.InlineTempInputPage_message_multi,
                        new Object[] { new Integer(occurrences), identifier });
        }
    }
    
    private String getSideEfefctMessages() {
        if (sideEffectMessages.length() != 0) {
            return sideEffectMessages + "\n";
        }
        return "";
    }
}
