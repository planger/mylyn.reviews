/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Miles Parker, Tasktop Technologies - initial API and implementation
 *     Sebastien Dubois (Ericsson) - Improvements for bug 400266
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.ui.providers;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.gerrit.ui.GerritReviewBehavior;
import org.eclipse.mylyn.internal.reviews.ui.compare.FileItemCompareEditorInput;
import org.eclipse.mylyn.internal.reviews.ui.views.ReviewExplorer;
import org.eclipse.mylyn.reviews.core.model.IComment;
import org.eclipse.mylyn.reviews.core.model.IFileItem;
import org.eclipse.mylyn.reviews.core.model.IFileVersion;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * @author Miles Parker
 */
public class OpenCompareEditorProvider extends CommonActionProvider {

	private Action openAction;

	@Override
	public void init(ICommonActionExtensionSite site) {
		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			final ICommonViewerWorkbenchSite viewSite = (ICommonViewerWorkbenchSite) site.getViewSite();
			final ISelectionProvider selectionProvider = viewSite.getSelectionProvider();
			CommonViewer viewer = (CommonViewer) selectionProvider;
			final ReviewExplorer explorer = (ReviewExplorer) viewer.getCommonNavigator();
			openAction = new Action() {

				@Override
				public void run() {
					IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
					if (selection.size() == 1) {
						IFileItem fileItem = getFileFor(selection.getFirstElement());
						if (fileItem != null) {
							IWorkbenchPart currentPart = explorer.getCurrentPart();
							if (currentPart instanceof TaskEditor) {

								//Here we try to resolve the Git repository in the workspace for this Patch Set.  
								//If so, we will use the appropriate file revision to provide navigability in the Compare Editor.
								//TODO:  Here we need to find a way to get a IUiContext and IReviewItemSet from the fileItem
								//TODO:  Once this is done, we can get use the  OpenFileUiFactory#execute command here
								//IUiContext context...;
								//IReviewItemSet set...context;
								//OpenFileUiFactory openFileFactory = new OpenFileUiFactory(context, set, fileItem);
								//openFileFactory.execute();

								TaskEditor part = (TaskEditor) currentPart;
								TaskEditorInput input = (TaskEditorInput) part.getEditorInput();
								GerritReviewBehavior behavior = new GerritReviewBehavior(input.getTask());
								CompareConfiguration configuration = new CompareConfiguration();
								CompareUI.openCompareEditor(new FileItemCompareEditorInput(configuration, fileItem,
										behavior));
							}
						}
					}
				}

				@Override
				public boolean isEnabled() {
					return true;
				}
			};
		}
	}

	private static IFileItem getFileFor(Object element) {
		//TODO Move to adapter?
		if (element instanceof IComment) {
			return getFileFor(((IComment) element).getItem());
		}
		if (element instanceof IFileVersion) {
			return ((IFileVersion) element).getFile();
		}
		if (element instanceof IFileItem) {
			return (IFileItem) element;
		}
		return null;
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
		}
	}
}
