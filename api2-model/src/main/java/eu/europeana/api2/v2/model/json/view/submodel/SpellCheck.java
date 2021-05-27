package eu.europeana.api2.v2.model.json.view.submodel;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.api2.v2.model.json.common.LabelFrequency;

/**
 * @deprecated Not used by anyone (and not configured properly in our Solr)
 */
@Deprecated(since = "May 2021") // not used
public class SpellCheck {

	/**
	 * Whether or not the query term(s) exist in the index
	 */
	public boolean correctlySpelled;

	/**
	 * List of suggestions
	 */
	public List<LabelFrequency> suggestions = new ArrayList<>();
}
