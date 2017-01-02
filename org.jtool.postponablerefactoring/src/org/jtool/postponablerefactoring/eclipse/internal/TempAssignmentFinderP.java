/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jtool.postponablerefactoring.eclipse.internal;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;

public class TempAssignmentFinderP extends ASTVisitor {
    private ASTNode fFirstAssignment;
    private IVariableBinding fTempBinding;
    
    TempAssignmentFinderP(VariableDeclaration tempDeclaration) {
        fTempBinding= tempDeclaration.resolveBinding();
    }
    
    private boolean isNameReferenceToTemp(Name name) {
        return fTempBinding == name.resolveBinding();
    }
    
    private boolean isAssignmentToTemp(Assignment assignment) {
        if (fTempBinding == null) {
            return false;
        }
        
        if (!(assignment.getLeftHandSide() instanceof Name)) {
            return false;
        }
        Name ref = (Name)assignment.getLeftHandSide();
        return isNameReferenceToTemp(ref);
    }
    
    boolean hasAssignments() {
        return fFirstAssignment != null;
    }
    
    ASTNode getFirstAssignment() {
        return fFirstAssignment;
    }
    
    @Override
    public boolean visit(Assignment assignment) {
        if (!isAssignmentToTemp(assignment)) {
            return true;
        }
        
        fFirstAssignment = assignment;
        return false;
    }
    
    @Override
    public boolean visit(PostfixExpression postfixExpression) {
        if (postfixExpression.getOperand() == null) {
            return true;
        }
        if (! (postfixExpression.getOperand() instanceof SimpleName)) {
            return true;
        }
        SimpleName simpleName= (SimpleName)postfixExpression.getOperand();
        if (!isNameReferenceToTemp(simpleName)) {
            return true;
        }
        
        fFirstAssignment = postfixExpression;
        return false;
    }
    
    @Override
    public boolean visit(PrefixExpression prefixExpression) {
        if (prefixExpression.getOperand() == null) {
            return true;
        }
        if (!(prefixExpression.getOperand() instanceof SimpleName)) {
            return true;
        }
        if (!prefixExpression.getOperator().equals(Operator.DECREMENT) && !prefixExpression.getOperator().equals(Operator.INCREMENT)) {
            return true;
        }
        SimpleName simpleName = (SimpleName)prefixExpression.getOperand();
        if (!isNameReferenceToTemp(simpleName)) {
            return true;
        }
        
        fFirstAssignment = prefixExpression;
        return false;
    }
}
