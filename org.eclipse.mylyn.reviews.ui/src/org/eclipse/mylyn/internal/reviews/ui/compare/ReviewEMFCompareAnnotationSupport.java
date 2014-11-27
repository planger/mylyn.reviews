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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer;
import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.impl.TreeMergeViewer;
import org.eclipse.emf.compare.rcp.ui.mergeviewer.IMergeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.reviews.ui.ReviewsUiPlugin;
import org.eclipse.mylyn.internal.reviews.ui.annotations.ReviewAnnotationModel;
import org.eclipse.mylyn.reviews.core.model.IFileItem;
import org.eclipse.mylyn.reviews.ui.ReviewBehavior;

/**
 * Class adapted from {@link ReviewAnnotationSupport}
 *
 * @author Alexandra Buzila
 */
public class ReviewEMFCompareAnnotationSupport {
	public static enum Side {
		LEFT_SIDE, RIGHT_SIDE
	}

	private static String KEY_ANNOTAION_SUPPORT = ReviewItemSetCompareEditorInput.class.getName();

//	private final CommentPopupDialog commentPopupDialog = null;

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
		if (contentViewer instanceof EMFCompareContentMergeViewer) {
			EMFCompareContentMergeViewer treeContentViewer = (EMFCompareContentMergeViewer) contentViewer;
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

	public void setReviewItem(IFileItem item, ReviewBehavior behavior) {
		leftAnnotationModel.setItem(item.getBase(), behavior);
		rightAnnotationModel.setItem(item.getTarget(), behavior);
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
