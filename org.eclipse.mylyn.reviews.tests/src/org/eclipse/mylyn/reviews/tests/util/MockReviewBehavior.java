/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.reviews.tests.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.reviews.core.model.IComment;
import org.eclipse.mylyn.reviews.core.model.IFileVersion;
import org.eclipse.mylyn.reviews.core.model.IReviewItem;
import org.eclipse.mylyn.reviews.ui.ReviewBehavior;
import org.eclipse.team.core.history.IFileRevision;

public class MockReviewBehavior extends ReviewBehavior {

	public MockReviewBehavior() {
		super(null);
	}

	@Override
	public IStatus addComment(IReviewItem fileItem, IComment comment, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public IFileRevision getFileRevision(IFileVersion reviewFileVersion) {
		return null;
	}

	@Override
	public IStatus discardComment(IReviewItem fileItem, IComment comment, IProgressMonitor monitor) {
		return null;
	}

}