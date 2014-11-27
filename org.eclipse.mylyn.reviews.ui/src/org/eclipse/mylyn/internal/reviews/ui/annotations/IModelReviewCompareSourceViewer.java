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

package org.eclipse.mylyn.internal.reviews.ui.annotations;

import java.util.List;

import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.impl.TreeMergeViewer;
import org.eclipse.mylyn.reviews.core.model.ILocation;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Alexandra Buzila
 */
@SuppressWarnings("restriction")
public interface IModelReviewCompareSourceViewer {
	List<String> getSelection();

	void focusOnLines(ILocation ranges);

	void registerContextMenu();

	ReviewAnnotationModel getAnnotationModel();

	TreeMergeViewer getSourceViewer();

	TreeItem[] getSelectedElements();

}
