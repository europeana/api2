package eu.europeana.api2.web.model.json.spellcheck;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.api2.web.model.json.common.LabelFrequency;

public class SpellCheck {

	public boolean correctlySpelled;

	public List<LabelFrequency> suggestions = new ArrayList<LabelFrequency>();
}
