/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core.client;

/**
 * Indicates an authentication error during login.
 * 
 * @author Steffen Pingel
 */
public class GerritLoginException extends GerritException {

	private static final long serialVersionUID = -6128773690643367414L;

	private boolean ntlmAuthRequested;

	public GerritLoginException() {
	}

	public GerritLoginException(String message) {
		super(message);
	}

	public boolean isNtlmAuthRequested() {
		return ntlmAuthRequested;
	}

	void setNtlmAuthRequested(boolean ntlmAuthRequested) {
		this.ntlmAuthRequested = ntlmAuthRequested;
	}

}
