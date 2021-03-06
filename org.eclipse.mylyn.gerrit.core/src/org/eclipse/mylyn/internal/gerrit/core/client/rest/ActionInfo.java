/*******************************************************************************
 * Copyright (c) 2014 Ericsson AB and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Guy Perron (Ericsson) - initial API and implementation
 ******************************************************************************/
package org.eclipse.mylyn.internal.gerrit.core.client.rest;

/**
 * Data model object for <a
 * href="https://gerrit-review.googlesource.com/Documentation/rest-api-changes.html#action-info">ActionInfo</a>.
 */
public class ActionInfo {
	// e.g. "gerritcodereview#change"
	private String method;

	private String label;

	private String title;

	private boolean enabled;

	public boolean getEnabled() {
		return enabled;
	}

}
