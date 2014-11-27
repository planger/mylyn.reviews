/*******************************************************************************
 * Copyright (c) 2014 EclipseSource Muenchen GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop - initial API and implementation
 *     Alexandra Buzila - model review support
 *******************************************************************************/

package org.eclipse.mylyn.internal.reviews.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.impl.TreeMergeViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.reviews.ui.ReviewUiUtil;
import org.eclipse.mylyn.internal.reviews.ui.annotations.IModelReviewCompareSourceViewer;
import org.eclipse.mylyn.internal.reviews.ui.dialogs.AddModelCommentDialog;
import org.eclipse.mylyn.reviews.core.model.IEmfModelLocation;
import org.eclipse.mylyn.reviews.core.model.ILocation;
import org.eclipse.mylyn.reviews.core.model.IReviewItem;
import org.eclipse.mylyn.reviews.internal.core.model.ReviewsFactory;
import org.eclipse.mylyn.reviews.ui.ReviewBehavior;
import org.eclipse.ui.IEditorInput;

/**
 * Class adapted from {@link org.eclipse.mylyn.internal.reviews.ui.actions.AddLineCommentToFileAction}
 *
 * @author Alexandra Buzila
 */
public class AddModelCommentToFileAction extends AbstractReviewAction {

	private final IModelReviewCompareSourceViewer compareSourceViewer;

	private IEditorInput editorInput;

	private List<String> selectedElements;

	public AddModelCommentToFileAction(IModelReviewCompareSourceViewer compareSourceViewer) {
		super(Messages.AddLineCommentToFileAction_Add_Comment);
		this.compareSourceViewer = compareSourceViewer;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		editorInput = getEditorInputFromSelection(selection);
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (compareSourceViewer != null) {
			selectedElements = compareSourceViewer.getSelection();
			if (selectedElements != null) {
				return !ReviewUiUtil.isAnonymous(getItem());
			}
		}
		return false;
	}

	@Override
	public String getToolTipText() {
		return Messages.AddLineCommentToFileAction_Add_Comment_tooltip;
	}

	protected List<String> getSelectedElements() {
		List<String> selectedElements = new ArrayList<String>();
		if (compareSourceViewer != null) {
			selectedElements = compareSourceViewer.getSelection();
		}
		return selectedElements;
	}

	protected IEditorInput getEditorInput() {
		return editorInput;
	}

	@Override
	protected IEditorInput getEditorInputFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
			if (structuredSelection.getFirstElement() instanceof IEditorInput) {
				return (IEditorInput) structuredSelection.getFirstElement();
			}
		}
		return null;

	}

	private ILocation getLocation() {
		List<String> selectedRange = getSelectedElements();
		IEmfModelLocation location = ReviewsFactory.eINSTANCE.createEmfModelLocation();
		location.getUriFragments().addAll(selectedRange);
		return location;
	}

	@SuppressWarnings("restriction")
	public void run(IAction action) {
		IReviewItem item = getItem();
		ReviewBehavior reviewBehavior = compareSourceViewer.getAnnotationModel().getBehavior();
		TreeMergeViewer sourceViewer = compareSourceViewer.getSourceViewer();
		AddModelCommentDialog dialog = new AddModelCommentDialog(WorkbenchUtil.getShell(), reviewBehavior, item,
				getLocation(), compareSourceViewer.getSelectedElements());
		int result = dialog.open();
		if (result == Window.OK) {
			//hack - trigger refresh of center control
			ISelection selection = compareSourceViewer.getSourceViewer().getSelection();
			compareSourceViewer.getSourceViewer().setSelection(selection);
		}
	}

	public IReviewItem getItem() {
		if (compareSourceViewer != null && compareSourceViewer.getAnnotationModel() != null) {
			return compareSourceViewer.getAnnotationModel().getItem();
		}
		return null;
	}

}
