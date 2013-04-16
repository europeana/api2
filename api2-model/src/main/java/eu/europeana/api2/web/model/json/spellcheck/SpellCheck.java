package eu.europeana.api2.web.model.json.spellcheck;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.api2.web.model.json.common.LabelFrequency;

public class SpellCheck {

	/**
	 * Whether or not the query term(s) exist in the index
	 */
	public boolean correctlySpelled;

	/**
	 * List of suggestions
	 */
	public List<LabelFrequency> suggestions = new ArrayList<LabelFrequency>();
}
