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

import java.util.List;
import java.util.Map;

import com.google.gerrit.common.data.ChangeDetail;
import com.google.gerrit.common.data.PatchSetDetail;
import com.google.gerrit.common.data.PatchSetPublishDetail;
import com.google.gerrit.reviewdb.PatchSet;

/**
 * @author Steffen Pingel
 */
public class GerritChange {

	private ChangeDetail changeDetail;

	private List<PatchSetDetail> patchSetDetails;

	private Map<PatchSet.Id, PatchSetPublishDetail> publishDetailByPatchSetId;

	public ChangeDetail getChangeDetail() {
		return changeDetail;
	}

	public List<PatchSetDetail> getPatchSetDetails() {
		return patchSetDetails;
	}

	public Map<PatchSet.Id, PatchSetPublishDetail> getPublishDetailByPatchSetId() {
		return publishDetailByPatchSetId;
	}

	void setChangeDetail(ChangeDetail changeDetail) {
		this.changeDetail = changeDetail;
	}

	void setPatchSets(List<PatchSetDetail> patchSets) {
		this.patchSetDetails = patchSets;
	}

	void setPatchSetPublishDetailByPatchSetId(Map<PatchSet.Id, PatchSetPublishDetail> patchSetPublishDetailByPatchSetId) {
		this.publishDetailByPatchSetId = patchSetPublishDetailByPatchSetId;
	}

}