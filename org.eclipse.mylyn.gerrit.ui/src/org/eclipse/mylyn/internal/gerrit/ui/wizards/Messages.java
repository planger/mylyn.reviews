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

package org.eclipse.mylyn.internal.gerrit.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.gerrit.ui.wizards.messages"; //$NON-NLS-1$

	public static String GerritCustomQueryPage_All_open_changes;

	public static String GerritCustomQueryPage_Custom_query;

	public static String GerritCustomQueryPage_Enter_title_and_select_query_type;

	public static String GerritCustomQueryPage_My_changes;

	public static String GerritCustomQueryPage_My_watched_changes;

	public static String GerritCustomQueryPage_Open_changes_by_project;

	public static String GerritCustomQueryPage_Query_type;

	public static String ProjectNameContentProposalProvider_Repository_configuration_needs_to_be_refreshed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
