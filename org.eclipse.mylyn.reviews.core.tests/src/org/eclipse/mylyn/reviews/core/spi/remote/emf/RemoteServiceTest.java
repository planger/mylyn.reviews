/*******************************************************************************
 * Copyright (c) 2011 GitHub Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GitHub Inc. - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/
package org.eclipse.mylyn.reviews.core.spi.remote.emf;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.reviews.core.spi.remote.AbstractRemoteConsumer;
import org.eclipse.mylyn.reviews.core.spi.remote.JobRemoteService;
import org.junit.Test;

/**
 * @author Miles Parker
 */
public class RemoteServiceTest {

	class Consumer extends AbstractRemoteConsumer {

		boolean pull;

		boolean applyModel;

		boolean push;

		boolean applyRemote;

		boolean notify;

		boolean async = true;

		IStatus status;

		@Override
		public void pull(boolean force, IProgressMonitor monitor) throws CoreException {
			pull = true;
		}

		@Override
		public void applyModel(boolean force) {
			applyModel = true;
		}

		@Override
		public void push(boolean force, IProgressMonitor monitor) throws CoreException {
			push = true;
		}

		@Override
		public void applyRemote(boolean force) {
			applyRemote = true;
		}

		@Override
		public void notifyDone(IStatus status) {
			this.status = status;
		}

		@Override
		public boolean isAsynchronous() {
			return async;
		}

		@Override
		public String getDescription() {
			return "";
		}

		protected void waitForDone() {
			long delay;
			delay = 0;
			while (delay < 100) {
				if (status == null) {
					try {
						Thread.sleep(10);
						delay += 10;
					} catch (InterruptedException e) {
					}
				} else {
					break;
				}
			}
			assertThat(status, notNullValue());
		}

		@Override
		public boolean isUserJob() {
			// ignore
			return false;
		}

		@Override
		public boolean isSystemJob() {
			// ignore
			return false;
		}
	}

	@Test
	public void testExecute() throws CoreException {
		JobRemoteService remoteService = new JobRemoteService();
		Consumer consumer = new Consumer();
		remoteService.retrieve(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.status.getSeverity(), is(IStatus.OK));
		assertThat(consumer.pull, is(true));
		assertThat(consumer.applyModel, is(true));
	}

	@Test
	public void testExecuteSync() throws CoreException {
		JobRemoteService remoteService = new JobRemoteService();
		Consumer consumer = new Consumer();
		consumer.async = false;
		remoteService.retrieve(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.status.getSeverity(), is(IStatus.OK));
		assertThat(consumer.pull, is(true));
		assertThat(consumer.applyModel, is(true));
	}

	class BrokenConsumer extends Consumer {
		@Override
		public void pull(boolean force, IProgressMonitor monitor) throws CoreException {
			throw new CoreException(new Status(IStatus.ERROR, "blah", "Whoops!"));
		}
	}

	@Test
	public void testExecuteCoreException() throws CoreException {
		JobRemoteService remoteService = new JobRemoteService();
		Consumer consumer = new BrokenConsumer();
		remoteService.retrieve(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.status.getSeverity(), is(IStatus.WARNING));
		assertThat(consumer.pull, is(false));
		assertThat(consumer.applyModel, is(false));
	}

	@Test
	public void testExecuteCoreExceptionSync() throws CoreException {
		JobRemoteService remoteService = new JobRemoteService();
		Consumer consumer = new BrokenConsumer();
		consumer.async = false;
		remoteService.retrieve(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.status.getSeverity(), is(IStatus.ERROR));
		assertThat(consumer.pull, is(false));
		assertThat(consumer.applyModel, is(false));
	}

	Thread testThread;

	class ThreadedService extends JobRemoteService {

		@Override
		public void modelExec(Runnable runnable, boolean block) {
			if (block) {
				runnable.run();
			} else {
				testThread = new Thread(runnable, "Model Thread");
				testThread.start();
			}
		}
	}

	class ModelThreadConsumer extends Consumer {

		Thread applyModelThread;

		Thread pullThread;

		Thread applyRemoteThread;

		Thread pushThread;

		@Override
		public void pull(boolean force, IProgressMonitor monitor) throws CoreException {
			pullThread = Thread.currentThread();
			super.pull(force, monitor);
		}

		@Override
		public void applyModel(boolean force) {
			applyModelThread = Thread.currentThread();
			super.applyModel(force);
		}

		@Override
		public void push(boolean force, IProgressMonitor monitor) throws CoreException {
			pushThread = Thread.currentThread();
			super.push(force, monitor);
		}

		@Override
		public void applyRemote(boolean force) {
			applyRemoteThread = Thread.currentThread();
			super.applyRemote(force);
		}
	}

	@Test
	public void testExecuteModelThreadRetrieve() throws CoreException {
		JobRemoteService remoteService = new ThreadedService();
		ModelThreadConsumer consumer = new ModelThreadConsumer();
		remoteService.retrieve(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.applyModelThread.getName(), is("Model Thread"));
		assertThat(consumer.pullThread.getName(), not("Model Thread"));
		assertThat(consumer.status.getSeverity(), is(IStatus.OK));
		assertThat(consumer.pull, is(true));
		assertThat(consumer.applyModel, is(true));

		assertThat(consumer.pullThread, not(Thread.currentThread()));
	}

	@Test
	public void testExecuteModelThreadRetrieveSync() throws CoreException {
		JobRemoteService remoteService = new ThreadedService();
		ModelThreadConsumer consumer = new ModelThreadConsumer();
		consumer.async = false;
		remoteService.retrieve(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.applyModelThread.getName(), is("main"));
		assertThat(consumer.pullThread.getName(), not("Model Thread"));
		assertThat(consumer.status.getSeverity(), is(IStatus.OK));
		assertThat(consumer.pull, is(true));
		assertThat(consumer.applyModel, is(true));

		assertThat(consumer.pullThread, is(Thread.currentThread()));
	}

	@Test
	public void testExecuteModelThreadSend() throws CoreException {
		JobRemoteService remoteService = new ThreadedService();
		ModelThreadConsumer consumer = new ModelThreadConsumer();
		remoteService.send(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.applyRemoteThread.getName(), is("Model Thread"));
		assertThat(consumer.pushThread.getName(), not("Model Thread"));
		assertThat(consumer.status.getSeverity(), is(IStatus.OK));
		assertThat(consumer.push, is(true));
		assertThat(consumer.applyRemote, is(true));

		assertThat(consumer.pullThread, not(Thread.currentThread()));
	}

	@Test
	public void testExecuteModelThreadSendSync() throws CoreException {
		JobRemoteService remoteService = new ThreadedService();
		ModelThreadConsumer consumer = new ModelThreadConsumer();
		consumer.async = false;
		remoteService.send(consumer, false);
		consumer.waitForDone();
		assertThat(consumer.applyRemoteThread.getName(), is("main"));
		assertThat(consumer.pushThread.getName(), not("Model Thread"));
		assertThat(consumer.status.getSeverity(), is(IStatus.OK));
		assertThat(consumer.push, is(true));
		assertThat(consumer.applyRemote, is(true));

		assertThat(consumer.pushThread, is(Thread.currentThread()));
	}
}
