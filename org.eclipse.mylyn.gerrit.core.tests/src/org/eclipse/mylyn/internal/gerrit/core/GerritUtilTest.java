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

package org.eclipse.mylyn.internal.gerrit.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GerritUtilTest {

	@Test
	public void toChangeId() {
		assertEquals("123", GerritUtil.toChangeId("123"));
		assertEquals("I95aa5d1d28009ecc6a59b1bf33a2866d186e5c62",
				GerritUtil.toChangeId("I95aa5d1d28009ecc6a59b1bf33a2866d186e5c62"));
		assertEquals("I95aa5d1d28009ecc6a59b1bf33a2866d186e5c62",
				GerritUtil.toChangeId("123~234~I95aa5d1d28009ecc6a59b1bf33a2866d186e5c62"));
		assertEquals("abc~I95aa5d1d28009ecc6a59b1bf33a2866d186e5c62",
				GerritUtil.toChangeId("abc~I95aa5d1d28009ecc6a59b1bf33a2866d186e5c62"));
	}

}
