/*******************************************************************************
 * Copyright (c) 2012, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core.remote;

import static org.eclipse.mylyn.gerrit.tests.core.client.rest.IsEmpty.empty;
import static org.eclipse.mylyn.internal.gerrit.core.client.rest.ApprovalUtil.CRVW;
import static org.eclipse.mylyn.internal.gerrit.core.client.rest.ApprovalUtil.VRIF;
import static org.eclipse.mylyn.internal.gerrit.core.client.rest.ApprovalUtil.toNameWithDash;
import static org.eclipse.mylyn.internal.gerrit.core.remote.TestRemoteObserverConsumer.retrieveForRemoteKey;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.mylyn.gerrit.tests.core.client.rest.ChangeInfoTest;
import org.eclipse.mylyn.gerrit.tests.support.GerritProject.CommitResult;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritChange;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritException;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritVersion;
import org.eclipse.mylyn.internal.gerrit.core.client.compat.ChangeDetailX;
import org.eclipse.mylyn.internal.gerrit.core.client.compat.PatchSetPublishDetailX;
import org.eclipse.mylyn.internal.gerrit.core.client.compat.PermissionLabel;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.ChangeInfo;
import org.eclipse.mylyn.reviews.core.model.IApprovalType;
import org.eclipse.mylyn.reviews.core.model.IChange;
import org.eclipse.mylyn.reviews.core.model.IComment;
import org.eclipse.mylyn.reviews.core.model.IEmfModelLocation;
import org.eclipse.mylyn.reviews.core.model.ILocation;
import org.eclipse.mylyn.reviews.core.model.IRepository;
import org.eclipse.mylyn.reviews.core.model.IRequirementEntry;
import org.eclipse.mylyn.reviews.core.model.IReview;
import org.eclipse.mylyn.reviews.core.model.IReviewItemSet;
import org.eclipse.mylyn.reviews.core.model.IReviewerEntry;
import org.eclipse.mylyn.reviews.core.model.IUser;
import org.eclipse.mylyn.reviews.core.model.RequirementStatus;
import org.eclipse.mylyn.reviews.core.model.ReviewStatus;
import org.junit.Test;

import com.google.common.base.CharMatcher;
import com.google.gerrit.common.data.ApprovalDetail;
import com.google.gerrit.common.data.ChangeDetail;
import com.google.gerrit.common.data.ReviewerResult;
import com.google.gerrit.reviewdb.ApprovalCategory;
import com.google.gerrit.reviewdb.ApprovalCategoryValue;
import com.google.gerrit.reviewdb.Change;
import com.google.gerrit.reviewdb.Change.Status;
import com.google.gerrit.reviewdb.ChangeMessage;
import com.google.gerrit.reviewdb.PatchSet;
import com.google.gerrit.reviewdb.PatchSetApproval;

/**
 * @author Miles Parker
 */
public class GerritReviewRemoteFactoryTest extends GerritRemoteTest {

	public void testGlobalComments() throws Exception {
		String message1 = "new comment, time: " + System.currentTimeMillis(); //$NON-NLS-1$
		reviewHarness.client.publishComments(reviewHarness.shortId, 1, message1,
				Collections.<ApprovalCategoryValue.Id> emptySet(), null);
		String message2 = "new comment, time: " + System.currentTimeMillis(); //$NON-NLS-1$
		reviewHarness.client.publishComments(reviewHarness.shortId, 1, message2,
				Collections.<ApprovalCategoryValue.Id> emptySet(), null);
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();
		List<IComment> comments = getReview().getComments();
		int offset = getCommentOffset();
		assertThat(comments.size(), is(offset + 2));
		if (isVersion28OrLater()) {
			IComment comment1 = comments.get(0);
			assertThat(comment1.getAuthor().getDisplayName(), is("tests"));
			assertThat(comment1.getDescription(), is("Uploaded patch set 1."));

		}
		IComment comment1 = comments.get(offset + 0);
		assertThat(comment1.getAuthor().getDisplayName(), is("tests"));
		assertThat(comment1.getDescription(), is("Patch Set 1:\n\n" + message1));
		IComment comment2 = comments.get(offset + 1);
		assertThat(comment2.getAuthor().getDisplayName(), is("tests"));
		assertThat(comment2.getDescription(), is("Patch Set 1:\n\n" + message2));
	}

	@Test
	public void testReviewStatus() throws Exception {
		assertThat(GerritReviewRemoteFactory.getReviewStatus(Status.ABANDONED), is(ReviewStatus.ABANDONED));
		assertThat(GerritReviewRemoteFactory.getReviewStatus(Status.MERGED), is(ReviewStatus.MERGED));
		assertThat(GerritReviewRemoteFactory.getReviewStatus(Status.NEW), is(ReviewStatus.NEW));
		assertThat(GerritReviewRemoteFactory.getReviewStatus(Status.SUBMITTED), is(ReviewStatus.SUBMITTED));
		//Test for drafts hack
		assertThat(GerritReviewRemoteFactory.getReviewStatus(null), is(ReviewStatus.DRAFT));
	}

	@Test
	public void testNewChange() throws Exception {
		CommitCommand command2 = reviewHarness.createCommitCommand();
		reviewHarness.addFile("testFile2.txt");
		reviewHarness.commitAndPush(command2);
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();
		List<IReviewItemSet> items = getReview().getSets();
		assertThat(items.size(), is(2));
		IReviewItemSet patchSet2 = items.get(1);
		assertThat(patchSet2.getReference(), endsWith("/2"));
		reviewHarness.assertIsRecent(patchSet2.getCreationDate());
	}

	@Test
	public void testAccount() throws Exception {
		assertThat(reviewHarness.getRepository().getAccount(), notNullValue());
		assertThat(reviewHarness.getRepository().getAccount().getDisplayName(), is("tests"));
		assertThat(reviewHarness.getRepository().getAccount().getEmail(), is("tests@mylyn.eclipse.org"));
		assertThat(reviewHarness.getRepository().getUsers().get(0), is(reviewHarness.getRepository().getAccount()));
	}

	@Test
	public void testUsers() throws Exception {
		assertThat(reviewHarness.getRepository().getUsers().size(), is(1));
		assertThat(reviewHarness.getRepository().getUsers().get(0).getDisplayName(), is("tests"));
		assertThat(reviewHarness.getRepository().getUsers().get(0).getEmail(), is("tests@mylyn.eclipse.org"));
	}

	@Test
	public void testApprovals() throws Exception {
		int approvals = isVersion26OrLater() ? 1 : 2;
		assertThat(reviewHarness.getRepository().getApprovalTypes().size(), is(approvals));
		IApprovalType codeReviewApproval = reviewHarness.getRepository().getApprovalTypes().get(approvals - 1);
		assertThat(codeReviewApproval.getKey(), is(CRVW.getCategory().getId().get()));
		assertThat(codeReviewApproval.getName(), is(CRVW.getCategory().getName()));

		String approvalMessage = "approval, time: " + System.currentTimeMillis(); //$NON-NLS-1$
		reviewHarness.client.publishComments(reviewHarness.shortId, 1, approvalMessage,
				new HashSet<ApprovalCategoryValue.Id>(Collections.singleton(CRVW.getValue((short) 1).getId())),
				new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();
		assertThat(getReview().getReviewerApprovals().size(), is(1));
		Entry<IUser, IReviewerEntry> reviewerEntry = getReview().getReviewerApprovals().entrySet().iterator().next();
		Map<IApprovalType, Integer> reviewerApprovals = reviewerEntry.getValue().getApprovals();
		assertThat(reviewerApprovals.size(), is(1));
		Entry<IApprovalType, Integer> next = reviewerApprovals.entrySet().iterator().next();
		assertThat(next.getKey(), sameInstance(codeReviewApproval));
		assertThat(next.getValue(), is(1));

		Set<Entry<IApprovalType, IRequirementEntry>> reviewApprovals = getReview().getRequirements().entrySet();
		assertThat(reviewApprovals.size(), is(approvals));
		IRequirementEntry codeReviewEntry = getReview().getRequirements().get(codeReviewApproval);
		assertThat(codeReviewEntry, notNullValue());
		assertThat(codeReviewEntry.getBy(), nullValue());
		assertThat(codeReviewEntry.getStatus(), is(RequirementStatus.NOT_SATISFIED));
		if (!isVersion26OrLater()) {
			IApprovalType verifyApproval = reviewHarness.getRepository().getApprovalTypes().get(0);
			assertThat(verifyApproval.getKey(), is(VRIF.getCategory().getId().get()));
			assertThat(verifyApproval.getName(), is(VRIF.getCategory().getName()));

			IRequirementEntry verifyEntry = getReview().getRequirements().get(verifyApproval);
			assertThat(verifyEntry, notNullValue());
			assertThat(verifyEntry.getBy(), nullValue());
			assertThat(verifyEntry.getStatus(), is(RequirementStatus.NOT_SATISFIED));
		}
		assertThat(getReview().getState(), is(ReviewStatus.NEW));
	}

	@Test
	public void testDependencies() throws Exception {
		boolean isversion29OrLater = reviewHarness.client.isVersion29OrLater(new NullProgressMonitor());
		String changeIdDep1 = "I" + StringUtils.rightPad(System.currentTimeMillis() + "", 40, "a");
		CommitCommand commandDep1 = reviewHarness.createCommitCommand(changeIdDep1);
		reviewHarness.addFile("testFile1.txt", "test 2");
		CommitResult resultDep1 = reviewHarness.commitAndPush(commandDep1);
		String resultIdDep1 = StringUtils.trimToEmpty(StringUtils.substringAfterLast(resultDep1.push.getMessages(), "/"));
		assertThat("Bad Push: " + resultDep1.push.getMessages(), resultIdDep1.length(), greaterThan(0));

		TestRemoteObserverConsumer<IRepository, IReview, String, GerritChange, String, Date> consumerDep1 //
		= retrieveForRemoteKey(reviewHarness.provider.getReviewFactory(), reviewHarness.getRepository(), resultIdDep1,
				true);
		IReview reviewDep1 = consumerDep1.getModelObject();

		assertThat(reviewDep1.getParents().size(), is(1));
		IChange parentChange = reviewDep1.getParents().get(0);
		//Not expected to be same instance
		assertThat(parentChange.getId(), is(getReview().getId()));
		assertThat(parentChange.getSubject(), is(getReview().getSubject()));
		if (isversion29OrLater) {
			//There s an offset ~ 1 sec, so no test for now
		} else {
			assertThat(parentChange.getModificationDate().getTime(), is(getReview().getModificationDate().getTime()));
		}

		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();
		assertThat(getReview().getChildren().size(), is(1));
		IChange childChange = getReview().getChildren().get(0);
		//Not expected to be same instance
		assertThat(childChange.getId(), is(reviewDep1.getId()));
		assertThat(childChange.getSubject(), is(reviewDep1.getSubject()));
		if (isversion29OrLater) {
			//There s an offset ~ 1 sec, so no test for now
		} else {
			assertThat(childChange.getModificationDate().getTime(), is(reviewDep1.getModificationDate().getTime()));
		}
	}

	@Test
	public void testAbandonChange() throws Exception {
		String message1 = "abandon, time: " + System.currentTimeMillis(); //$NON-NLS-1$

		ChangeDetail changeDetail = reviewHarness.client.abandon(reviewHarness.shortId, 1, message1,
				new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertThat(changeDetail, notNullValue());
		assertThat(changeDetail.getChange().getStatus(), is(Status.ABANDONED));
		List<ChangeMessage> messages = changeDetail.getMessages();
		int offset = getCommentOffset();
		assertThat(messages.size(), is(offset + 1));
		ChangeMessage lastMessage = messages.get(offset + 0);
		assertThat(lastMessage.getAuthor().get(), is(1000001));
		assertThat(lastMessage.getMessage(), endsWith("Abandoned\n\n" + message1));

		assertThat(getReview().getState(), is(ReviewStatus.ABANDONED));
		List<IComment> comments = getReview().getComments();
		assertThat(comments.size(), is(offset + 1));
		IComment lastComment = comments.get(offset + 0);
		assertThat(lastComment.getAuthor().getDisplayName(), is("tests"));
		assertThat(lastComment.getAuthor().getId(), is("1000001"));
		assertThat(lastComment.getDescription(), endsWith("Abandoned\n\n" + message1));
	}

	@Test
	public void testRestoreChange() throws Exception {
		String message1 = "abandon, time: " + System.currentTimeMillis();
		reviewHarness.client.abandon(reviewHarness.shortId, 1, message1, new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();
		String message2 = "restore, time: " + System.currentTimeMillis();

		reviewHarness.client.restore(reviewHarness.shortId, 1, message2, new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertThat(getReview().getState(), is(ReviewStatus.NEW));
		List<IComment> comments = getReview().getComments();
		int offset = getCommentOffset();
		assertThat(comments.size(), is(offset + 2)); // abandon + restore
		IComment lastComment = comments.get(offset + 1);
		assertThat(lastComment.getAuthor().getDisplayName(), is("tests"));
		assertThat(lastComment.getDescription(), endsWith("Restored\n\n" + message2));
	}

	@Test
	public void testRestoreNewChange() throws Exception {
		assertThat(getReview().getState(), is(ReviewStatus.NEW));
		String message1 = "restore, time: " + System.currentTimeMillis();
		try {
			reviewHarness.client.restore(reviewHarness.shortId, 1, message1, new NullProgressMonitor());
			fail("Expected to fail when restoring a new change");
		} catch (GerritException e) {
			if (isVersion24x()) {
				assertThat(e.getMessage(), is("Change is not abandoned or patchset is not latest"));
			} else {
				assertThat(e.getMessage(), is("Not Found"));
			}
		}
	}

	public void testCannotSubmitChange() throws Exception {
		try {
			reviewHarness.client.submit(reviewHarness.shortId, 1, new NullProgressMonitor());
			fail("Expected to fail when submitting a change without approvals");
		} catch (GerritException e) {
			assertThat(e.getMessage(), startsWith("Cannot submit change"));
		}
	}

	@Test
	public void testAddNullReviewers() throws Exception {
		try {
			reviewHarness.client.addReviewers(reviewHarness.shortId, null, new NullProgressMonitor());
			fail("Expected to fail when trying to add null reviewers");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("reviewers cannot be null"));
		}
	}

	@Test
	public void testAddEmptyReviewers() throws Exception {
		ReviewerResult reviewerResult = reviewHarness.client.addReviewers(reviewHarness.shortId,
				Collections.<String> emptyList(), new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertThat(reviewerResult, notNullValue());
		assertThat(reviewerResult.getErrors().isEmpty(), is(true));
		assertThat(reviewerResult.getChange().getApprovals().isEmpty(), is(true));
		assertThat(getReview().getReviewerApprovals().isEmpty(), is(true));
	}

	@Test
	public void testAddInvalidReviewers() throws Exception {
		List<String> reviewers = Arrays.asList(new String[] { "foo" });

		ReviewerResult reviewerResult = reviewHarness.client.addReviewers(reviewHarness.shortId, reviewers,
				new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertThat(reviewerResult, notNullValue());
		assertThat(reviewerResult.getErrors().size(), is(1));
		assertThat(reviewerResult.getErrors().get(0).getName(), is("foo"));
		assertThat(reviewerResult.getErrors().get(0).getType(), nullValue());
		assertThat(reviewerResult.getChange().getApprovals().isEmpty(), is(true));
		assertThat(getReview().getReviewerApprovals().isEmpty(), is(true));
	}

	@Test
	public void testAddSomeInvalidReviewers() throws Exception {
		List<String> reviewers = Arrays.asList(new String[] { "tests", "foo" });
		int userid = 1000001; //user id for tests
		if (reviewHarness.client.isVersion29OrLater(new NullProgressMonitor())) {
			//use "admin " since this is a valid user in 2.9
			reviewers = Arrays.asList(new String[] { "admin", "foo" });
			userid = 1000000; //user id for admin
		}

		ReviewerResult reviewerResult = reviewHarness.client.addReviewers(reviewHarness.shortId, reviewers,
				new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertReviewerResult(reviewerResult, "foo", userid);
	}

	@Test
	public void testAddReviewers() throws Exception {
		assertThat(getReview().getReviewerApprovals().isEmpty(), is(true));
		List<String> reviewers = Arrays.asList(new String[] { "tests" });
		int userid = 1000001; //user id for tests
		if (reviewHarness.client.isVersion29OrLater(new NullProgressMonitor())) {
			//Need a user and not the review owner
			reviewers = Arrays.asList(new String[] { "admin" });
			userid = 1000000; //user id for admin
		}

		ReviewerResult reviewerResult = reviewHarness.client.addReviewers(reviewHarness.shortId, reviewers,
				new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertReviewerResult(reviewerResult, null, userid);
	}

	@Test
	public void testAddReviewersByEmail() throws Exception {
		List<String> reviewers = Arrays.asList(new String[] { "tests@mylyn.eclipse.org" });
		int userid = 1000001; //user id for tests
		if (reviewHarness.client.isVersion29OrLater(new NullProgressMonitor())) {
			//Need a user and not the review owner
			reviewers = Arrays.asList(new String[] { "admin@mylyn.eclipse.org" });
			userid = 1000000; //user id for admin
		}

		ReviewerResult reviewerResult = reviewHarness.client.addReviewers(reviewHarness.shortId, reviewers,
				new NullProgressMonitor());
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertReviewerResult(reviewerResult, null, userid);
	}

	private void assertReviewerResult(ReviewerResult reviewerResult, String nameInErrors, int userId) {
		assertThat(reviewerResult, notNullValue());

		assertThat(reviewerResult.getErrors().isEmpty(), is(nameInErrors == null));
		if (nameInErrors != null) {
			assertThat(reviewerResult.getErrors().size(), is(1));
			assertThat(reviewerResult.getErrors().get(0).getName(), is(nameInErrors));
			assertThat(reviewerResult.getErrors().get(0).getType(), nullValue());
		}

		List<ApprovalDetail> approvals = reviewerResult.getChange().getApprovals();
		assertThat(approvals.isEmpty(), is(false));
		assertThat(approvals.size(), is(1));
		assertThat(approvals.get(0).getAccount().get(), is(userId));

		Map<ApprovalCategory.Id, PatchSetApproval> approvalMap = approvals.get(0).getApprovalMap();
		assertThat(approvalMap, notNullValue());
		assertThat(approvalMap.isEmpty(), is(false));
		assertThat(approvalMap.size(), is(1));

		PatchSetApproval crvw = approvalMap.get(CRVW.getCategory().getId());
		assertThat(crvw, notNullValue());
		assertThat(crvw.getAccountId().get(), is(userId));
		assertThat(crvw.getValue(), is((short) 0));
		assertThat(crvw.getGranted(), notNullValue());
		assertThat(crvw.getPatchSetId(), notNullValue());
		assertThat(crvw.getPatchSetId().get(), is(1));
		assertThat(crvw.getPatchSetId().getParentKey().get(), is(Integer.parseInt(getReview().getId())));

		assertThat(getReview().getReviewerApprovals().isEmpty(), is(false));
		assertThat(getReview().getReviewerApprovals().size(), is(1));
		assertThat(getReview().getReviewerApprovals().get(0), nullValue());
	}

	@Test
	public void testCannotRebaseChangeAlreadyUpToDate() throws Exception {
		try {
			reviewHarness.client.rebase(reviewHarness.shortId, 1, new NullProgressMonitor());
			fail("Expected to fail when rebasing a change that is already up to date");
		} catch (GerritException e) {
			String message = CharMatcher.JAVA_ISO_CONTROL.removeFrom(e.getMessage());
			assertThat(message, is("Change is already up to date."));
		}
	}

	@Test
	public void testGetChangeDetailWithNoApprovals() throws Exception {
		int reviewId = Integer.parseInt(reviewHarness.shortId);

		ChangeDetailX changeDetail = reviewHarness.client.getChangeDetail(reviewId, new NullProgressMonitor());

		assertThat(changeDetail, notNullValue());
		assertThat(changeDetail.getApprovals(), empty());
	}

	@Test
	public void testGetChangeInfo() throws Exception {
		if (!isVersion26OrLater()) {
			return; // testing Gerrit REST API, available in 2.6 and later
		}
		int reviewId = Integer.parseInt(reviewHarness.shortId);

		ChangeInfo changeInfo = reviewHarness.client.getChangeInfo(reviewId, new NullProgressMonitor());
		if (reviewHarness.client.isVersion29OrLater(new NullProgressMonitor())) {
			ChangeInfoTest.assertHasCodeReviewLabels(changeInfo, true);
		} else {
			ChangeInfoTest.assertHasCodeReviewLabels(changeInfo, false);
		}
	}

	@Test
	public void testUnpermittedApproval() throws Exception {
		String approvalMessage = "approval, time: " + System.currentTimeMillis();
		try {
			reviewHarness.client.publishComments(reviewHarness.shortId, 1, approvalMessage,
					new HashSet<ApprovalCategoryValue.Id>(Collections.singleton(CRVW.getValue((short) 2).getId())),
					new NullProgressMonitor());
			fail("Expected to fail when trying to vote +2 when it's not permitted");
		} catch (GerritException e) {
			if (isVersion26OrLater()) {
				assertEquals("Applying label \"Code-Review\": 2 is restricted", e.getMessage());
			} else {
				assertEquals("Code-Review=2 not permitted", e.getMessage());
			}

		}
	}

	@Test
	public void testGetPatchSetPublishDetail() throws Exception {
		int reviewId = Integer.parseInt(reviewHarness.shortId);
		PatchSet.Id id = new PatchSet.Id(new Change.Id(reviewId), 1);

		PatchSetPublishDetailX patchSetDetail = reviewHarness.client.getPatchSetPublishDetail(id,
				new NullProgressMonitor());

		assertThat(patchSetDetail, notNullValue());
		List<PermissionLabel> allowed = patchSetDetail.getLabels();
		assertThat(allowed, notNullValue());
		assertThat(allowed, not(empty()));
		assertThat(allowed.size(), is(1));
		PermissionLabel crvwAllowed = allowed.get(0);
		assertThat(crvwAllowed.matches(CRVW.getCategory()), is(true));
		assertThat(crvwAllowed.getName(), is(PermissionLabel.toLabelName(toNameWithDash(CRVW.getCategory().getName()))));
		assertThat(crvwAllowed.getMin(), is(-1));
		assertThat(crvwAllowed.getMax(), is(1));
	}

	@Test
	public void testSetStarred() throws Exception {

		int reviewId = Integer.parseInt(reviewHarness.shortId);
		//Set the Starred to a review
		reviewHarness.client.setStarred(reviewHarness.shortId, true, new NullProgressMonitor());

		ChangeDetailX changeDetail = reviewHarness.client.getChangeDetail(reviewId, new NullProgressMonitor());
		assertEquals(true, changeDetail.isStarred());

		//Test if already the Starred was set, should react the same way
		reviewHarness.client.setStarred(reviewHarness.shortId, true, new NullProgressMonitor());
		changeDetail = reviewHarness.client.getChangeDetail(reviewId, new NullProgressMonitor());
		assertEquals(true, changeDetail.isStarred());

		reviewHarness.client.setStarred(reviewHarness.shortId, false, new NullProgressMonitor());
		changeDetail = reviewHarness.client.getChangeDetail(reviewId, new NullProgressMonitor());
		assertEquals(false, changeDetail.isStarred());

	}

	private static final String NL = "\n"; //$NON-NLS-1$

	private static final String MODEL_ELEMENT_TAG_NAME = "Model-Element"; //$NON-NLS-1$

	@Test
	public void testCommentsWithEmfModelLocation() throws Exception {
		String description = "Some comment" + NL + NL + //$NON-NLS-1$
				"Some description" + NL;//$NON-NLS-1$
		String uriFragment = "myUriFragment"; //$NON-NLS-1$
		String rawMessage1 = description + MODEL_ELEMENT_TAG_NAME + ": " + uriFragment; //$NON-NLS-1$
		reviewHarness.client.publishComments(reviewHarness.shortId, 1, rawMessage1,
				Collections.<ApprovalCategoryValue.Id> emptySet(), null);
		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		List<IComment> comments = getReview().getComments();
		int offset = getCommentOffset();

		IComment comment1 = comments.get(offset + 0);
		// assert that the description is only the message without
		// the tag for the model element uri fragment
		assertThat(comment1.getDescription(), is(description));
		// assert that there is one IEmfModelLocation
		assertThat(comment1.getLocations().size(), is(1));
		ILocation location1 = comment1.getLocations().get(0);
		assertThat(location1, instanceOf(IEmfModelLocation.class));
		// assert that there is one uri fragment in this location
		// and that this location is the original uri fragment
		IEmfModelLocation emfLocation1 = (IEmfModelLocation) location1;
		assertThat(emfLocation1.getUriFragments().size(), is(1));
		assertThat(emfLocation1.getUriFragments().get(0), is(uriFragment));
	}

	private int getCommentOffset() throws GerritException {
		// Version 2.8 adds a comment for each uploaded patch set
		return isVersion28OrLater() ? 1 : 0;
	}

	private boolean isVersion28OrLater() throws GerritException {
		return GerritVersion.isVersion28OrLater(reviewHarness.client.getVersion(new NullProgressMonitor()));
	}

	private boolean isVersion26OrLater() throws GerritException {
		return GerritVersion.isVersion26OrLater(reviewHarness.client.getVersion(new NullProgressMonitor()));
	}

	private boolean isVersion24x() throws GerritException {
		return GerritVersion.isVersion24x(reviewHarness.client.getVersion(new NullProgressMonitor()));
	}
}
