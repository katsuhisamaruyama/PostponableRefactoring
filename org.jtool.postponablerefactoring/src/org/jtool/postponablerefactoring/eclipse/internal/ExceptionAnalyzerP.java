/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jtool.postponablerefactoring.eclipse.internal;

import org.eclipse.jdt.internal.corext.refactoring.util.AbstractExceptionAnalyzer;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import java.util.List;

@SuppressWarnings("restriction")
class ExceptionAnalyzerP extends AbstractExceptionAnalyzer {
    
    public static ITypeBinding[] perform(ASTNode[] statements) {
        ExceptionAnalyzerP analyzer = new ExceptionAnalyzerP();
        for (int i= 0; i < statements.length; i++) {
            statements[i].accept(analyzer);
        }
        List<ITypeBinding> exceptions = analyzer.getCurrentExceptions();
        return exceptions.toArray(new ITypeBinding[exceptions.size()]);
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        ITypeBinding exception = node.getExpression().resolveTypeBinding();
        if (exception == null) {
            return true;
        }
        
        addException(exception, node.getAST());
        return true;
    }
    
    @Override
    public boolean visit(MethodInvocation node) {
        return handleExceptions((IMethodBinding)node.getName().resolveBinding(), node);
    }
    
    @Override
    public boolean visit(SuperMethodInvocation node) {
        return handleExceptions((IMethodBinding)node.getName().resolveBinding(), node);
    }
    
    @Override
    public boolean visit(ClassInstanceCreation node) {
        return handleExceptions(node.resolveConstructorBinding(), node);
    }
    
    private boolean handleExceptions(IMethodBinding binding, ASTNode node) {
        if (binding == null) {
            return true;
        }
        
        addExceptions(binding.getExceptionTypes(), node.getAST());
        return true;
    }
}
