/*******************************************************************************
 * Copyright (c) 2013, Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastien Dubois (Ericsson) - Created for Mylyn Reviews
 *******************************************************************************/

package org.eclipse.mylyn.internal.reviews.ui.providers;

import org.eclipse.compare.ICompareInputLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.mylyn.internal.reviews.ui.compare.FileItemNode;
import org.eclipse.mylyn.internal.reviews.ui.compare.FileRevisionTypedElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

/**
 * Custom Label Provider for Review Compare Editor input
 * 
 * @author Sebastien Dubois
 */
public class FileItemNodeLabelProvider implements ICompareInputLabelProvider {

	public Image getImage(Object element) {
		return null; //Not used
	}

	public String getText(Object element) {
		return null; //Not used
	}

	public void addListener(ILabelProviderListener listener) {
		//Not supported for now
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false; //Not supported for now
	}

	public void removeListener(ILabelProviderListener listener) {
		//Not supported for now
	}

	public String getAncestorLabel(Object input) {
		return null; //Not supported
	}

	public Image getAncestorImage(Object input) {
		return null; //Not supported
	}

	public String getLeftLabel(Object input) {
		if (((FileItemNode) input).getLeft() instanceof FileRevisionTypedElement) {
			return NLS.bind("{0}: {1}", ((FileItemNode) input).getFileItem().getBase().getDescription(), //$NON-NLS-1$
					((FileItemNode) input).getFileItem().getName());
		}
		return NLS.bind("[{0}: {1}]", ((FileItemNode) input).getFileItem().getBase().getDescription(), //$NON-NLS-1$
				((FileItemNode) input).getFileItem().getName());
	}

	public Image getLeftImage(Object input) {
		if (((FileItemNode) input).getLeft() instanceof FileRevisionTypedElement) {
			return ((FileItemNode) input).getImage();
		} else {
			return null;
		}
	}

	public String getRightLabel(Object input) {
		if (((FileItemNode) input).getRight() instanceof FileRevisionTypedElement) {
			return NLS.bind("{0}: {1}", ((FileItemNode) input).getFileItem().getTarget().getDescription(), //$NON-NLS-1$
					((FileItemNode) input).getFileItem().getName());
		}
		return NLS.bind("[{0}: {1}]", ((FileItemNode) input).getFileItem().getTarget().getDescription(), //$NON-NLS-1$
				((FileItemNode) input).getFileItem().getName());
	}

	public Image getRightImage(Object input) {
		if (((FileItemNode) input).getRight() instanceof FileRevisionTypedElement) {
			return ((FileItemNode) input).getImage();
		} else {
			return null;
		}
	}
}
