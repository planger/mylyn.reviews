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

package org.eclipse.mylyn.internal.reviews.ui.compare;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.impl.TreeMergeViewer;
import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem;
import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem.Container;
import org.eclipse.emf.compare.rcp.ui.mergeviewer.IMergeViewer.MergeViewerSide;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.reviews.ui.ReviewsUiPlugin;
import org.eclipse.mylyn.internal.reviews.ui.actions.AddModelCommentToFileAction;
import org.eclipse.mylyn.internal.reviews.ui.annotations.IModelReviewCompareSourceViewer;
import org.eclipse.mylyn.internal.reviews.ui.annotations.ReviewAnnotationModel;
import org.eclipse.mylyn.reviews.core.model.ILocation;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class ReviewEMFCompareInputListener implements ISelectionChangedListener, IModelReviewCompareSourceViewer {

	private AddModelCommentToFileAction addLineCommentAction;

	private final ReviewAnnotationModel annotationModel;

	private final TreeMergeViewer mergeSourceViewer;

	private TreeViewer sourceViewer = null;

	ReviewEMFCompareInputListener(TreeMergeViewer sourceViewer, ReviewAnnotationModel annotationModel) {
		try {
			Class<TreeMergeViewer> clazz = TreeMergeViewer.class;
			Field declaredField = clazz.getDeclaredField("fTreeViewer"); //$NON-NLS-1$
			declaredField.setAccessible(true);
			this.sourceViewer = (TreeViewer) declaredField.get(sourceViewer);
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.WARNING, ReviewsUiPlugin.PLUGIN_ID,
					"Could not initialize annotation model for " + Viewer.class.getName(), t)); //$NON-NLS-1$
		}
		this.mergeSourceViewer = sourceViewer;
		this.annotationModel = annotationModel;
	}

	public ReviewAnnotationModel getAnnotationModel() {
		return annotationModel;
	}

	public List<String> getSelection() {
		if (sourceViewer != null) {
			TreeSelection selection = (TreeSelection) sourceViewer.getSelection();
			ArrayList<String> selectedElements = new ArrayList<String>();
			MergeViewerItem.Container container = (Container) selection.getFirstElement();
			if (container == null) {
				return null;
			}
			EObject value = (EObject) container.getSideValue(MergeViewerSide.RIGHT);
			value.eResource().getURIFragment(value);
			selectedElements.add(value.eResource().getURIFragment(value));
			return selectedElements;
		}
		return null;
	}

	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		// ignore
	}

	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		//TODO
	}

	public boolean isListenerFor(TreeMergeViewer viewer, ReviewAnnotationModel annotationModel) {
		return this.mergeSourceViewer == viewer && this.annotationModel == annotationModel;
	}

	public void registerContextMenu() {
		addLineCommentAction = new AddModelCommentToFileAction(this);

		if (sourceViewer != null) {
			sourceViewer.addSelectionChangedListener(addLineCommentAction);

			MenuManager menuMgr = new MenuManager();
			Menu menu = menuMgr.createContextMenu(sourceViewer.getControl());
			sourceViewer.getControl().setMenu(menu);
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchPartSite site = wb.getActiveWorkbenchWindow().getPartService().getActivePart().getSite();
			site.registerContextMenu(menuMgr, sourceViewer);
			menuMgr.add(addLineCommentAction);
		}
	}

	@Override
	public void focusOnLines(ILocation ranges) {
		// ignore

	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
	}

	@Override
	public TreeMergeViewer getSourceViewer() {
		return mergeSourceViewer;
	}

	@Override
	public TreeItem[] getSelectedElements() {
		if (sourceViewer != null) {
			return sourceViewer.getTree().getSelection();
		}
		return null;
	}

}