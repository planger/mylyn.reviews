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
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer;
import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.tree.TreeContentMergeViewer;
import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.annotation.MergeItemAnnotation;
import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.impl.TreeMergeViewer;
import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem;
import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem.Container;
import org.eclipse.emf.compare.rcp.ui.mergeviewer.IMergeViewer;
import org.eclipse.emf.compare.rcp.ui.mergeviewer.IMergeViewer.MergeViewerSide;
import org.eclipse.emf.compare.rcp.ui.mergeviewer.item.IMergeViewerItem;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.reviews.ui.ReviewsUiPlugin;
import org.eclipse.mylyn.internal.reviews.ui.annotations.ReviewAnnotationModel;
import org.eclipse.mylyn.reviews.core.model.IComment;
import org.eclipse.mylyn.reviews.core.model.IFileItem;
import org.eclipse.mylyn.reviews.internal.core.ReviewsCoreConstants;
import org.eclipse.mylyn.reviews.ui.ReviewBehavior;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Class adapted from {@link ReviewAnnotationSupport}
 *
 * @author Alexandra Buzila
 */
@SuppressWarnings("restriction")
public class ReviewEMFCompareAnnotationSupport {
	public static enum Side {
		LEFT_SIDE, RIGHT_SIDE
	}

	private static String KEY_ANNOTAION_SUPPORT = ReviewItemSetCompareEditorInput.class.getName();

	public static ReviewEMFCompareAnnotationSupport getAnnotationSupport(Viewer contentViewer) {
		ReviewEMFCompareAnnotationSupport support = (ReviewEMFCompareAnnotationSupport) contentViewer.getData(KEY_ANNOTAION_SUPPORT);
		if (support == null) {
			support = new ReviewEMFCompareAnnotationSupport(contentViewer);
			contentViewer.setData(KEY_ANNOTAION_SUPPORT, support);
		}
		return support;
	}

	public class MonitorObject {
	};

	MonitorObject myMonitorObject = new MonitorObject();

	private ReviewBehavior behavior;

	private final ReviewAnnotationModel leftAnnotationModel;

	private ReviewEMFCompareInputListener leftViewerListener;

	private final ReviewAnnotationModel rightAnnotationModel;

	private ReviewEMFCompareInputListener rightViewerListener;

	private IMergeViewer leftSourceViewer;

	private IMergeViewer rightSourceViewer;

	private TreeContentMergeViewer treeContentViewer;

	private IFileItem item;

	public ReviewEMFCompareAnnotationSupport(Viewer contentViewer) {
		this.leftAnnotationModel = new ReviewAnnotationModel();
		this.rightAnnotationModel = new ReviewAnnotationModel();
		install(contentViewer);
		contentViewer.setData(KEY_ANNOTAION_SUPPORT, this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ReviewEMFCompareAnnotationSupport other = (ReviewEMFCompareAnnotationSupport) obj;
		if (leftAnnotationModel == null) {
			if (other.leftAnnotationModel != null) {
				return false;
			}
		} else if (!leftAnnotationModel.equals(other.leftAnnotationModel)) {
			return false;
		}
		if (rightAnnotationModel == null) {
			if (other.rightAnnotationModel != null) {
				return false;
			}
		} else if (!rightAnnotationModel.equals(other.rightAnnotationModel)) {
			return false;
		}
		return true;
	}

	public ReviewBehavior getBehavior() {
		return behavior;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftAnnotationModel == null) ? 0 : leftAnnotationModel.hashCode());
		result = prime * result + ((rightAnnotationModel == null) ? 0 : rightAnnotationModel.hashCode());
		return result;
	}

	@SuppressWarnings("restriction")
	public void install(Viewer contentViewer) {
		if (contentViewer instanceof TreeContentMergeViewer) {
			treeContentViewer = (TreeContentMergeViewer) contentViewer;
			treeContentViewer.setContentUpdateListener(new Listener() {

				@Override
				public void handleEvent(Event event) {
					updateSourceViewer();
				}
			});
			try {
				Class<EMFCompareContentMergeViewer> clazz = EMFCompareContentMergeViewer.class;
				Field declaredField = clazz.getDeclaredField("fLeft"); //$NON-NLS-1$
				declaredField.setAccessible(true);
				leftSourceViewer = (IMergeViewer) declaredField.get(treeContentViewer);

				declaredField = clazz.getDeclaredField("fRight"); //$NON-NLS-1$
				declaredField.setAccessible(true);
				rightSourceViewer = (IMergeViewer) declaredField.get(treeContentViewer);

				leftViewerListener = registerInputListener(leftSourceViewer, leftAnnotationModel);
				rightViewerListener = registerInputListener(rightSourceViewer, rightAnnotationModel);
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.WARNING, ReviewsUiPlugin.PLUGIN_ID,
						"Could not initialize annotation model for " + Viewer.class.getName(), t)); //$NON-NLS-1$
			}
		}
	}

	@SuppressWarnings("restriction")
	public void setReviewItem(IFileItem item, ReviewBehavior behavior) {
		this.item = item;
		leftAnnotationModel.setItem(item.getBase(), behavior);
		rightAnnotationModel.setItem(item.getTarget(), behavior);
	}

	private void updateSourceViewer() {
		if (item == null) {
			return;
		}
		if (rightSourceViewer instanceof TreeMergeViewer) {
			TreeMergeViewer treeMergeViewer = (TreeMergeViewer) rightSourceViewer;
			TreeItem[] roots = treeMergeViewer.getStructuredViewer().getTree().getItems();
			updateSourceViewer(treeContentViewer.getExpandedTreeItems(roots), item.getAllComments());
		}
	}

	private void updateSourceViewer(List<TreeItem> treeItems, List<IComment> comments) {
		for (TreeItem treeItem : treeItems) {
			IComment comment = getComment(treeItem, comments);
			if (comment != null) {
				addCommentToTreeItem(treeItem, comment);
			}

		}

	}

	@SuppressWarnings("restriction")
	private IComment getComment(TreeItem treeItem, List<IComment> comments) {
		IComment result = null;
		for (IComment comment : comments) {
			MergeViewerItem.Container container = (Container) treeItem.getData();
			EObject value = (EObject) container.getSideValue(MergeViewerSide.RIGHT);
			String uriFragment = value.eResource().getURIFragment(value);
			String parentElement = getCommentParentElement(comment.getDescription());
			if (uriFragment.equals(parentElement)) {
				result = comment;
				break;
			}
		}
		return result;
	}

	@SuppressWarnings("restriction")
	private String getCommentParentElement(String description) {
		int lastIndex = description.lastIndexOf(ReviewsCoreConstants.MODEL_ELEMENT_TAG);
		final String tagText = ReviewsCoreConstants.MODEL_ELEMENT_TAG + ": ";
		return description.substring(lastIndex + tagText.length());
	}

	private void addCommentToTreeItem(TreeItem treeItem, IComment comment) {
		IMergeViewerItem rightData = (IMergeViewerItem) treeItem.getData();
		Image image = EMFCompareIDEUIPlugin.getImage("icons/full/eobj16/person.gif"); //$NON-NLS-1$

		MergeItemAnnotation mergeItemAnnotation = new MergeItemAnnotation(image, getAnnotationHeader(comment),
				comment.getDescription());
		rightData.setRightAnnotation(mergeItemAnnotation);
		treeItem.getParent().getParent().layout();

	}

	private String getAnnotationHeader(IComment comment) {
		String header = comment.getAuthor().getDisplayName() + "  " + comment.getCreationDate() + "\n\n"; //$NON-NLS-1$ //$NON-NLS-2$
		return header;
	}

	@SuppressWarnings("restriction")
	private ReviewEMFCompareInputListener registerInputListener(final IMergeViewer sourceViewer,
			final ReviewAnnotationModel annotationModel) {

		if (!(sourceViewer instanceof TreeMergeViewer)) {
			return null;
		}
		TreeMergeViewer mergeViewer = TreeMergeViewer.class.cast(sourceViewer);
		ReviewEMFCompareInputListener listener = new ReviewEMFCompareInputListener(mergeViewer, annotationModel);

		TreeViewer viewer = TreeMergeViewer.class.cast(sourceViewer).getStructuredViewer();
		if (viewer != null) {
			viewer.addSelectionChangedListener(listener);
		}
		listener.registerContextMenu();

		return listener;
	}
}
