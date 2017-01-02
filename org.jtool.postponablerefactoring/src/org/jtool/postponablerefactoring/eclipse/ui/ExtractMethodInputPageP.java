/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [extract method] remember selected access modifier - https://bugs.eclipse.org/bugs/show_bug.cgi?id=101233
 *     Samrat Dhillon <samrat.dhillon@gmail.com> -  [extract method] Extracted method should be declared static if extracted expression is also used in another static method https://bugs.eclipse.org/bugs/show_bug.cgi?id=393098
 *******************************************************************************/

package org.jtool.postponablerefactoring.eclipse.ui;

import org.jtool.postponablerefactoring.core.PostponableRefactoring;
import org.jtool.postponablerefactoring.core.PostponableDialogSettings;
import org.jtool.postponablerefactoring.core.PostponableDialogSettingsException;
import org.jtool.postponablerefactoring.eclipse.internal.ExtractMethodRefactoringP;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.refactoring.ChangeParametersControl;
import org.eclipse.jdt.internal.ui.refactoring.IParameterListChangeListener;
import org.eclipse.jdt.internal.ui.refactoring.InputPageUtil;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.util.RowLayouter;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("restriction")
class ExtractMethodInputPageP extends UserInputWizardPage {
    
    public static final String PAGE_NAME = "ExtractMethodInputPage";
    
    private ExtractMethodRefactoringP fRefactoring;
    private boolean runWithDialogSettings;
    private Text fTextField;
    private boolean fFirstTime;
    private JavaSourceViewer fSignaturePreview;
    private IDialogSettings fSettings;
    private Composite accessModifiersGroup;
    private String methodName = "";
    private int destinationIndex = 0;
    
    private static final String DESCRIPTION = RefactoringMessages.ExtractMethodInputPage_description;
    private static final String THROW_RUNTIME_EXCEPTIONS = "ThrowRuntimeExceptions";
    private static final String GENERATE_JAVADOC = "GenerateJavadoc";
    private static final String ACCESS_MODIFIER = "AccessModifier";
    
    public ExtractMethodInputPageP() {
        super(PAGE_NAME);
        setImageDescriptor(JavaPluginImages.DESC_WIZBAN_REFACTOR_CU);
        setDescription(DESCRIPTION);
        fFirstTime = true;
    }
    
    @Override
    public void createControl(Composite parent) {
        fRefactoring = (ExtractMethodRefactoringP)getRefactoring();
        
        if (fRefactoring instanceof PostponableRefactoring) {
            PostponableRefactoring postponableRefactoring = (PostponableRefactoring)fRefactoring;
            runWithDialogSettings = postponableRefactoring.isRunWithDialogSettings();
        } else {
            runWithDialogSettings = false;
        }
        loadSettings();
        
        Composite result = new Composite(parent, SWT.NONE);
        setControl(result);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        result.setLayout(layout);
        RowLayouter layouter = new RowLayouter(2);
        GridData gd = null;
        
        initializeDialogUnits(result);
        
        Label label = new Label(result, SWT.NONE);
        label.setText(getLabelText());
        
        fTextField = createTextInputField(result, SWT.BORDER);
        if (methodName.length() != 0) {
            fTextField.setText(methodName);
            textModified(methodName);
            fFirstTime = false;
        }
        fTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        layouter.perform(label, fTextField, 1);
        
        ASTNode[] destinations = fRefactoring.getDestinations();
        if (destinations.length > 1) {
            label = new Label(result, SWT.NONE);
            label.setText(RefactoringMessages.ExtractMethodInputPage_destination_type);
            final Combo combo = new Combo(result, SWT.READ_ONLY | SWT.DROP_DOWN);
            SWTUtil.setDefaultVisibleItemCount(combo);
            for (int i = 0; i < destinations.length; i++) {
                ASTNode declaration = destinations[i];
                combo.add(getLabel(declaration));
            }
            
            combo.select(destinationIndex);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new SelectionAdapter() {
                
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fRefactoring.setDestination(combo.getSelectionIndex());
                    updateAccessModifiers();
                    updatePreview(getText());
                }
            });
        }
        
        label = new Label(result, SWT.NONE);
        label.setText(RefactoringMessages.ExtractMethodInputPage_access_Modifiers);
        
        accessModifiersGroup = new Composite(result, SWT.NONE);
        accessModifiersGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout();
        layout.numColumns = 4;
        layout.marginWidth = 0;
        accessModifiersGroup.setLayout(layout);
        
        String[] labels = new String[] {
            RefactoringMessages.ExtractMethodInputPage_public,
            RefactoringMessages.ExtractMethodInputPage_protected,
            RefactoringMessages.ExtractMethodInputPage_default,
            RefactoringMessages.ExtractMethodInputPage_private
        };
        Integer[] data = new Integer[] {new Integer(Modifier.PUBLIC), new Integer(Modifier.PROTECTED), new Integer(Modifier.NONE), new Integer(Modifier.PRIVATE)};
        Integer visibility = new Integer(fRefactoring.getVisibility());
        
        for (int i = 0; i < labels.length; i++) {
            Button radio = new Button(accessModifiersGroup, SWT.RADIO);
            radio.setText(labels[i]);
            radio.setData(data[i]);
            if (data[i].equals(visibility)) {
                radio.setSelection(true);
            }
            radio.addSelectionListener(new SelectionAdapter() {
                
                @Override
                public void widgetSelected(SelectionEvent event) {
                    final Integer selectedModifier = (Integer)event.widget.getData();
                    fSettings.put(ACCESS_MODIFIER, selectedModifier.intValue());
                    setVisibility(selectedModifier);
                }
            });
        }
        updateAccessModifiers();
        layouter.perform(label, accessModifiersGroup, 1);
        
        if (!fRefactoring.getParameterInfos().isEmpty()) {
            ChangeParametersControl cp = new ChangeParametersControl(result, SWT.NONE,
                    RefactoringMessages.ExtractMethodInputPage_parameters, new IParameterListChangeListener() {
                
                @Override
                public void parameterChanged(ParameterInfo parameter) {
                    parameterModified();
                }
                
                @Override
                public void parameterListChanged() {
                    parameterModified();
                }
                
                @Override
                public void parameterAdded(ParameterInfo parameter) {
                    updatePreview(getText());
                }
            }, ChangeParametersControl.Mode.EXTRACT_METHOD);
            gd = new GridData(GridData.FILL_BOTH);
            gd.horizontalSpan= 2;
            cp.setLayoutData(gd);
            cp.setInput(fRefactoring.getParameterInfos());
        }
        
        Button checkBox = new Button(result, SWT.CHECK);
        checkBox.setText(RefactoringMessages.ExtractMethodInputPage_throwRuntimeExceptions);
        checkBox.setSelection(fSettings.getBoolean(THROW_RUNTIME_EXCEPTIONS));
        checkBox.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                setRethrowRuntimeException(((Button)e.widget).getSelection());
            }
        });
        layouter.perform(checkBox);
        
        checkBox = new Button(result, SWT.CHECK);
        checkBox.setText(RefactoringMessages.ExtractMethodInputPage_generateJavadocComment);
        boolean generate = computeGenerateJavadoc();
        setGenerateJavadoc(generate);
        checkBox.setSelection(generate);
        checkBox.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                setGenerateJavadoc(((Button)e.widget).getSelection());
            }
        });
        layouter.perform(checkBox);
        
        int duplicates = fRefactoring.getNumberOfDuplicates();
        checkBox = new Button(result, SWT.CHECK);
        if (duplicates == 0) {
            checkBox.setText(RefactoringMessages.ExtractMethodInputPage_duplicates_none);
        } else if (duplicates == 1) {
            checkBox.setText(RefactoringMessages.ExtractMethodInputPage_duplicates_single);
        } else {
            checkBox.setText(Messages.format(RefactoringMessages.ExtractMethodInputPage_duplicates_multi, new Integer(duplicates)));
        }
        checkBox.setSelection(fRefactoring.getReplaceDuplicates());
        checkBox.setEnabled(duplicates > 0);
        checkBox.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                fRefactoring.setReplaceDuplicates(((Button)e.widget).getSelection());
                updatePreview(getText());
            }
        });
        layouter.perform(checkBox);
        
        label = new Label(result, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layouter.perform(label);
        
        createSignaturePreview(result, layouter);
        
        Dialog.applyDialogFont(result);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaHelpContextIds.EXTRACT_METHOD_WIZARD_PAGE);
    }
    
    private void updateAccessModifiers() {
        final Control[] radioButtons = accessModifiersGroup.getChildren();
        if (fRefactoring.isDestinationInterface()) {
            Integer visibility = new Integer(Modifier.PUBLIC);
            fRefactoring.setVisibility(visibility.intValue());
            for (int i = 0; i < radioButtons.length; i++) {
                radioButtons[i].setEnabled(false);
                if (radioButtons[i].getData().equals(visibility)) {
                    ((Button) radioButtons[i]).setSelection(true);
                } else {
                    ((Button) radioButtons[i]).setSelection(false);
                }
            }
        } else {
            final String accessModifier = fSettings.get(ACCESS_MODIFIER);
            Integer visibility= accessModifier != null ? new Integer(accessModifier) : new Integer(fRefactoring.getVisibility());
            fRefactoring.setVisibility(visibility.intValue());
            for (int i= 0; i < radioButtons.length; i++) {
                radioButtons[i].setEnabled(true);
                if (radioButtons[i].getData().equals(visibility)) {
                    ((Button) radioButtons[i]).setSelection(true);
                } else {
                    ((Button) radioButtons[i]).setSelection(false);
                }
            }
        }
    }
    
    private String getLabel(ASTNode node) {
        if (node instanceof AbstractTypeDeclaration) {
            return ((AbstractTypeDeclaration)node).getName().getIdentifier();
        } else if (node instanceof AnonymousClassDeclaration) {
            if (node.getLocationInParent() == ClassInstanceCreation.ANONYMOUS_CLASS_DECLARATION_PROPERTY) {
                ClassInstanceCreation creation= (ClassInstanceCreation)node.getParent();
                return Messages.format(RefactoringMessages.ExtractMethodInputPage_anonymous_type_label,
                                       BasicElementLabels.getJavaElementName(ASTNodes.asString(creation.getType())));
            } else if (node.getLocationInParent() == EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY) {
                EnumConstantDeclaration decl = (EnumConstantDeclaration)node.getParent();
                return decl.getName().getIdentifier();
            }
        }
        return "UNKNOWN";
    }
    
    private Text createTextInputField(Composite parent, int style) {
        Text result = new Text(parent, style);
        result.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                textModified(getText());
            }
        });
        TextFieldNavigationHandler.install(result);
        return result;
    }
    
    private String getText() {
        if (fTextField == null) {
            return null;
        }
        return fTextField.getText();
    }
    
    private String getLabelText() {
        return RefactoringMessages.ExtractMethodInputPage_label_text;
    }
    
    private void setVisibility(Integer visibility) {
        fRefactoring.setVisibility(visibility.intValue());
        updatePreview(getText());
    }
    
    private void setRethrowRuntimeException(boolean value) {
        fSettings.put(THROW_RUNTIME_EXCEPTIONS, value);
        fRefactoring.setThrowRuntimeExceptions(value);
        updatePreview(getText());
    }
    
    private boolean computeGenerateJavadoc() {
        boolean result = fRefactoring.getGenerateJavadoc();
        if (result) {
            return result;
        }
        return fSettings.getBoolean(GENERATE_JAVADOC);
    }
    
    private void setGenerateJavadoc(boolean value) {
        fSettings.put(GENERATE_JAVADOC, value);
        fRefactoring.setGenerateJavadoc(value);
    }
    
    private void createSignaturePreview(Composite composite, RowLayouter layouter) {
        Label previewLabel = new Label(composite, SWT.NONE);
        previewLabel.setText(RefactoringMessages.ExtractMethodInputPage_signature_preview);
        layouter.perform(previewLabel);
        
        fSignaturePreview = InputPageUtil.createSignaturePreview(composite);
        layouter.perform(fSignaturePreview.getControl());
    }
    
    private void updatePreview(String text) {
        if (fSignaturePreview == null) {
            return;
        }
        
        if (text.length() == 0) {
            text = "someMethodName";
        }
        
        int top = fSignaturePreview.getTextWidget().getTopPixel();
        String signature;
        try {
            signature = fRefactoring.getSignature(text);
        } catch (IllegalArgumentException e) {
            signature = "";
        }
        fSignaturePreview.getDocument().set(signature);
        fSignaturePreview.getTextWidget().setTopPixel(top);
    }
    
    private void loadSettings() {
        fSettings = getDialogSettings().getSection(ExtractMethodWizardP.DIALOG_SETTING_SECTION);
        if (fSettings == null) {
            fSettings = getDialogSettings().addNewSection(ExtractMethodWizardP.DIALOG_SETTING_SECTION);
            fSettings.put(THROW_RUNTIME_EXCEPTIONS, false);
            fSettings.put(GENERATE_JAVADOC, JavaPreferencesSettings.getCodeGenerationSettings(fRefactoring.getCompilationUnit().getJavaProject()).createComments);
            fSettings.put(ACCESS_MODIFIER, Modifier.PRIVATE);
        }
        
        if (runWithDialogSettings) {
            PostponableDialogSettings settings = ((PostponableRefactoring)fRefactoring).getDialogSettings();
            try {
                methodName = settings.get(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
                destinationIndex = settings.getInt(ExtractMethodRefactoringP.ATTRIBUTE_DESTINATION);
                fSettings.put(GENERATE_JAVADOC, settings.getBoolean(ExtractMethodRefactoringP.ATTRIBUTE_COMMENTS));
                fSettings.put(THROW_RUNTIME_EXCEPTIONS, settings.getBoolean(ExtractMethodRefactoringP.ATTRIBUTE_EXCEPTIONS));
                fSettings.put(ACCESS_MODIFIER, settings.getInt(ExtractMethodRefactoringP.ATTRIBUTE_VISIBILITY));
            } catch (PostponableDialogSettingsException e) {
            }
        }
        
        fRefactoring.setThrowRuntimeExceptions(fSettings.getBoolean(THROW_RUNTIME_EXCEPTIONS));
        final String accessModifier = fSettings.get(ACCESS_MODIFIER);
        if (accessModifier != null) {
            fRefactoring.setVisibility(Integer.parseInt(accessModifier));
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (fFirstTime) {
                fFirstTime = false;
                setPageComplete(false);
                updatePreview(getText());
                fTextField.setFocus();
            } else {
                setPageComplete(validatePage(true));
                if (runWithDialogSettings) {
                    updatePreview(getText());
                    fTextField.setFocus();
                }
            }
        }
        super.setVisible(visible);
    }
    
    private void textModified(String text) {
        fRefactoring.setMethodName(text);
        RefactoringStatus status = validatePage(true);
        if (!status.hasFatalError()) {
            updatePreview(text);
        } else {
            fSignaturePreview.getDocument().set("");
        }
        setPageComplete(status);
    }
    
    private void parameterModified() {
        updatePreview(getText());
        setPageComplete(validatePage(false));
    }
    
    private RefactoringStatus validatePage(boolean text) {
        RefactoringStatus result = new RefactoringStatus();
        if (text) {
            result.merge(validateMethodName());
            result.merge(validateParameters());
        } else {
            result.merge(validateParameters());
            result.merge(validateMethodName());
        }
        return result;
    }
    
    private RefactoringStatus validateMethodName() {
        RefactoringStatus result = new RefactoringStatus();
        String text = getText();
        if ("".equals(text)) {
            result.addFatalError(RefactoringMessages.ExtractMethodInputPage_validation_emptyMethodName);
            return result;
        }
        result.merge(fRefactoring.checkMethodName());
        return result;
    }
    
    private RefactoringStatus validateParameters() {
        RefactoringStatus result = new RefactoringStatus();
        List<ParameterInfo> parameters = fRefactoring.getParameterInfos();
        for (Iterator<ParameterInfo> iter = parameters.iterator(); iter.hasNext();) {
            ParameterInfo info= iter.next();
            if ("".equals(info.getNewName())) {
                result.addFatalError(RefactoringMessages.ExtractMethodInputPage_validation_emptyParameterName);
                return result;
            }
        }
        result.merge(fRefactoring.checkParameterNames());
        result.merge(fRefactoring.checkVarargOrder());
        return result;
    }
}
