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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaggedDescription {

	private static final String TAG_MATCHING_REGEX = "\\s*:\\s*(.*)"; //$NON-NLS-1$

	private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	private final List<String> potentialTags;

	private String description;

	private TagMap tagMap;

	public TaggedDescription(String description, List<String> potentialTags) {
		this.description = description;
		this.potentialTags = potentialTags;
		initializeTags();
	}

	private void initializeTags() {
		tagMap = new TagMap();
		for (String potentialTag : potentialTags) {
			initializeTag(potentialTag);
		}
	}

	private void initializeTag(String potentialTag) {
		for (String tagValue : matchTagValues(potentialTag)) {
			tagMap.add(potentialTag, tagValue);
		}
	}

	private List<String> matchTagValues(String tagName) {
		final List<String> tagValues = new ArrayList<String>();
		final String tagMatchRegEx = tagName + TAG_MATCHING_REGEX;
		final Pattern pattern = Pattern.compile(tagMatchRegEx);
		final Matcher matcher = pattern.matcher(description);
		while (matcher.find()) {
			final String match = matcher.group(1);
			final String trimmedValue = match.trim();
			if (!trimmedValue.isEmpty()) {
				tagValues.add(trimmedValue);
			}
		}
		return tagValues;
	}

	private class TagMap {
		private final Map<String, List<String>> map;

		public TagMap() {
			map = new HashMap<String, List<String>>();
		}

		public void add(String tag, String value) {
			if (containsTag(tag)) {
				map.get(tag).add(value);
			} else {
				map.put(tag, new ArrayList<String>());
				add(tag, value);
			}
		}

		public boolean containsTag(String tag) {
			return map.containsKey(tag) && map.get(tag) != null;
		}

		public List<String> getValues(String tag) {
			if (containsTag(tag)) {
				return Collections.unmodifiableList(map.get(tag));
			} else {
				return Collections.emptyList();
			}
		}

		public void remove(String tag, String value) {
			if (containsTag(tag)) {
				map.get(tag).remove(value);
			}
		}

		public void remove(String tag) {
			map.remove(tag);
		}

		public Set<String> getTags() {
			return Collections.unmodifiableSet(map.keySet());
		}
	}

	public void addTagValue(String tag, String value) {
		tagMap.add(tag, value);
		description = description + NL + NL + tag + ": " + value; //$NON-NLS-1$
	}

	public void removeTagValue(String tag, String value) {
		try {
			String currentLine;
			StringWriter stringWriter = new StringWriter();
			final BufferedReader reader = createDescriptionReader();
			while ((currentLine = reader.readLine()) != null) {
				if (!isTagValueInLine(tag, value, currentLine)) {
					stringWriter.append(currentLine).append(NL);
				}
			}
			description = stringWriter.getBuffer().toString();
			tagMap.remove(tag, value);
		} catch (IOException e) {
			// cannot happen
		}
	}

	public void removeTag(String tag) {
		try {
			String currentLine;
			StringWriter stringWriter = new StringWriter();
			final BufferedReader reader = createDescriptionReader();
			while ((currentLine = reader.readLine()) != null) {
				if (!isTagInLine(tag, currentLine)) {
					stringWriter.append(currentLine).append(NL);
				}
			}
			description = stringWriter.getBuffer().toString();
			tagMap.remove(tag);
		} catch (IOException e) {
			// cannot happen
		}
	}

	private boolean isTagInLine(String tag, String line) {
		return line.startsWith(tag);
	}

	private boolean isTagValueInLine(String tag, String value, String line) {
		return isTagInLine(tag, line) && line.substring(line.indexOf(tag) + tag.length()).contains(value);
	}

	private BufferedReader createDescriptionReader() {
		return new BufferedReader(new StringReader(description));
	}

	public Set<String> getTags() {
		return tagMap.getTags();
	}

	public List<String> getTagValues(String tag) {
		return tagMap.getValues(tag);
	}

	public String getTaggedDescription() {
		return description;
	}

}
