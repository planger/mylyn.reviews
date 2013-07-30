/*******************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core.remote;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.reviews.core.spi.remote.emf.AbstractRemoteEmfFactory;
import org.eclipse.mylyn.reviews.core.spi.remote.emf.RemoteEmfObserver;

final class TestRemoteObserver<P extends EObject, T, L, C> extends RemoteEmfObserver<P, T, L, C> {

	static final int TEST_TIMEOUT = 15000;

	int updated;

	int responded;

	IStatus failure;

	AbstractRemoteEmfFactory<P, T, L, ?, ?, C> factory;

	public TestRemoteObserver(AbstractRemoteEmfFactory<P, T, L, ?, ?, C> factory) {
		this.factory = factory;
	}

	@Override
	public void updating() {
	}

	@Override
	public void updated(boolean modified) {
		responded++;
		if (modified) {
			updated++;
		}
	}

	protected void waitForResponse(int response, int update) {
		long delay;
		delay = 0;
		while (delay < TEST_TIMEOUT) {
			if (responded < response || updated < update) {
				try {
					Thread.sleep(10);
					delay += 10;
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}
		try {
			//wait extra to ensure there aren't remaining jobs
			Thread.sleep(25);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		assertThat("Wrong # responses: " + responded + ", updated: " + updated, responded, is(response));
		assertThat("Wrong # updates" + updated, updated, is(update));
		if (factory != null) {
			assertThat(factory.getService().isActive(), is(false));
		}
	}
}