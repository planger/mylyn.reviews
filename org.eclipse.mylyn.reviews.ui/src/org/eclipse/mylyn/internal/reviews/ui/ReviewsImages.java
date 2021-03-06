/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Sascha Scholz (SAP) - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.reviews.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Steffen Pingel
 * @author Sascha Scholz
 * @author Miles Parker
 * @author Lei Zhu
 */
public class ReviewsImages {

	private static final URL baseURL = ReviewsUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_ADDED = create("ovr/added.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_REMOVED = create("ovr/removed.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_RENAMED = create("ovr/renamed.gif"); //$NON-NLS-1$

	public static final ImageDescriptor REFRESH = create("elcl16/refresh.gif"); //$NON-NLS-1$

	public static final ImageDescriptor FLAT_LAYOUT = create("elcl16/flatLayout.gif"); //$NON-NLS-1$

	public static final ImageDescriptor HIERARCHICAL_LAYOUT = create("elcl16/hierarchicalLayout.gif"); //$NON-NLS-1$

	public static final ImageDescriptor REVIEW = create("obj16/review.png"); //$NON-NLS-1$

	public static final ImageDescriptor CHANGE_LOG = create("obj16/changelog_obj.gif"); //$NON-NLS-1$

	public static final ImageDescriptor REVIEW_QUOTE = create("obj16/quote.png"); //$NON-NLS-1$

	public static final ImageDescriptor APPROVED = create("obj12/approved.gif"); //$NON-NLS-1$

	public static final ImageDescriptor REJECTED = create("obj12/rejected.gif"); //$NON-NLS-1$

	public static final ImageDescriptor UNKNOWN = create("obj12/unknown.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BLANK = create("obj12/blank.gif"); //$NON-NLS-1$

	public static final ImageDescriptor NEXT_COMMENT = create("elcl16/nxtanmly_menu.png"); //$NON-NLS-1$

	public static final ImageDescriptor PREVIOUS_COMMENT = create("elcl16/prevanmly_menu.png"); //$NON-NLS-1$

	private static ImageDescriptor create(String path) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(path));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String path) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}
		return new URL(baseURL, path);
	}
}
