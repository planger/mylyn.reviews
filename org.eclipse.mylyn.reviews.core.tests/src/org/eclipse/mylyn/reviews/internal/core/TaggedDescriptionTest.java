/*******************************************************************************
 * Copyright (c) 2014 EclipseSource Munich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Philip Langer - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.reviews.internal.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TaggedDescriptionTest {

	private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	@Test
	public void testReadingTagsWhereAllPotentialAreExisting() {
		String description = "Tag1: value1" + NL //
				+ "Some other text" + NL //
				+ "Tag2: value1";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag2" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);

		assertTrue(taggedDescription.getTags().contains("Tag1"));
		assertTrue(taggedDescription.getTags().contains("Tag2"));
	}

	@Test
	public void testReadingTagsWhereLessThanAllPotentialAreExisting() {
		String description = "Tag1: value1" + NL //
				+ "Some other text" + NL //
				+ "Tag2: value1";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag2", "Tag3" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);

		assertTrue(taggedDescription.getTags().contains("Tag1"));
		assertTrue(taggedDescription.getTags().contains("Tag2"));
		assertFalse(taggedDescription.getTags().contains("Tag3"));
	}

	@Test
	public void testReadingTagsWhereMoreThanThePotentialAreExisting() {
		String description = "Tag1: value1" + NL //
				+ "Some other text" + NL //
				+ "Tag2: value1";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag3" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);

		assertTrue(taggedDescription.getTags().contains("Tag1"));
		assertFalse(taggedDescription.getTags().contains("Tag2"));
		assertFalse(taggedDescription.getTags().contains("Tag3"));
	}

	@Test
	public void testReadingTagValuesWhereOnlyOneValueExistsPerTag() {
		String description = "Tag1: value1" + NL //
				+ "Some other text" + NL //
				+ "Tag2: value1";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag2", "Tag3" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);

		assertThat(taggedDescription.getTagValues("Tag1").size(), is(1));
		assertThat(taggedDescription.getTagValues("Tag1").get(0), is("value1"));

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(1));
		assertThat(taggedDescription.getTagValues("Tag2").get(0), is("value1"));

		assertThat(taggedDescription.getTagValues("Tag3").size(), is(0));
	}

	@Test
	public void testReadingTagValuesWhereMultipleValuesExistPerTag() {
		String description = "Tag1: tag1value1" + NL //
				+ "Tag2: tag2value1" + NL //
				+ "Some other text" + NL //
				+ "Tag1: tag1value2" + NL //
				+ "Tag2: tag2value2";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag2", "Tag3" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);

		assertThat(taggedDescription.getTagValues("Tag1").size(), is(2));
		assertThat(taggedDescription.getTagValues("Tag1").get(0), is("tag1value1"));
		assertThat(taggedDescription.getTagValues("Tag1").get(1), is("tag1value2"));

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(2));
		assertThat(taggedDescription.getTagValues("Tag2").get(0), is("tag2value1"));
		assertThat(taggedDescription.getTagValues("Tag2").get(1), is("tag2value2"));

		assertThat(taggedDescription.getTagValues("Tag3").size(), is(0));
	}

	@Test
	public void testRemovingTags() {
		String description = "Tag1: tag1value1" + NL //
				+ "Tag2: tag2value1" + NL //
				+ "Some other text" + NL //
				+ "Tag1: tag1value2" + NL //
				+ "Tag2: tag2value2";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag2", "Tag3" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);
		taggedDescription.removeTag("Tag2");

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(0));

		taggedDescription = new TaggedDescription(taggedDescription.getTaggedDescription(), potentialTags);

		assertThat(taggedDescription.getTagValues("Tag1").size(), is(2));
		assertThat(taggedDescription.getTagValues("Tag1").get(0), is("tag1value1"));
		assertThat(taggedDescription.getTagValues("Tag1").get(1), is("tag1value2"));

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(0));
		assertThat(taggedDescription.getTagValues("Tag3").size(), is(0));
	}

	@Test
	public void testRemovingTagValues() {
		String description = "Tag1: tag1value1" + NL //
				+ "Tag2: tag2value1" + NL //
				+ "Some other text" + NL //
				+ "Tag1: tag1value2" + NL //
				+ "Tag2: tag2value2";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag2", "Tag3" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);
		taggedDescription.removeTagValue("Tag2", "tag2value1");

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(1));

		taggedDescription = new TaggedDescription(taggedDescription.getTaggedDescription(), potentialTags);

		assertThat(taggedDescription.getTagValues("Tag1").size(), is(2));
		assertThat(taggedDescription.getTagValues("Tag1").get(0), is("tag1value1"));
		assertThat(taggedDescription.getTagValues("Tag1").get(1), is("tag1value2"));

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(1));
		assertThat(taggedDescription.getTagValues("Tag2").get(0), is("tag2value2"));

		assertThat(taggedDescription.getTagValues("Tag3").size(), is(0));
	}

	@Test
	public void testAddingTagValues() {
		String description = "Tag1: tag1value1" + NL //
				+ "Tag2: tag2value1" + NL //
				+ "Some other text" + NL //
				+ "Tag1: tag1value2" + NL //
				+ "Tag2: tag2value2";
		List<String> potentialTags = Arrays.asList(new String[] { "Tag1", "Tag2", "Tag3" });
		TaggedDescription taggedDescription = new TaggedDescription(description, potentialTags);
		taggedDescription.addTagValue("Tag2", "tag2value3");
		taggedDescription.addTagValue("Tag3", "tag3value1");

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(3));
		assertThat(taggedDescription.getTagValues("Tag3").size(), is(1));

		taggedDescription = new TaggedDescription(taggedDescription.getTaggedDescription(), potentialTags);

		assertThat(taggedDescription.getTagValues("Tag1").size(), is(2));
		assertThat(taggedDescription.getTagValues("Tag1").get(0), is("tag1value1"));
		assertThat(taggedDescription.getTagValues("Tag1").get(1), is("tag1value2"));

		assertThat(taggedDescription.getTagValues("Tag2").size(), is(3));
		assertTrue(taggedDescription.getTagValues("Tag2").contains("tag2value1"));
		assertTrue(taggedDescription.getTagValues("Tag2").contains("tag2value2"));
		assertTrue(taggedDescription.getTagValues("Tag2").contains("tag2value3"));

		assertThat(taggedDescription.getTagValues("Tag3").size(), is(1));
		assertTrue(taggedDescription.getTagValues("Tag3").contains("tag3value1"));
	}

}
