/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import eu.europeana.corelib.definitions.edm.beans.ApiBean;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.beans.RichBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;
import org.springframework.context.MessageSource;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class ModelUtils {

	private ModelUtils() {
		// Constructor must be private
	}

	public static Class<? extends IdBean> selectBean(String profile) {
		Class<? extends IdBean> clazz;
		if (StringUtils.containsIgnoreCase(profile, "minimal")) {
			clazz = BriefBean.class;
		} else if (StringUtils.containsIgnoreCase(profile, "rich")) {
			clazz = RichBean.class;
		} else {
			clazz = ApiBean.class;
		}
		return clazz;
	}

	public static List<Facet> conventFacetList(List<FacetField> facetFields) {
		if ((facetFields != null) && !facetFields.isEmpty()) {
			List<Facet> facets = new ArrayList<>();
			for (FacetField facetField : facetFields) {
				if (facetField.getValues() != null) {
					Facet facet = new Facet();
					facet.name = facetField.getName();
					for (FacetField.Count count : facetField.getValues()) {
						if (StringUtils.isNotEmpty(count.getName())
								&& (count.getCount() > 0)) {
							LabelFrequency value = new LabelFrequency();
							value.label = count.getName();
							value.count = count.getCount();
							facet.fields.add(value);
						}
					}
					if (!facet.fields.isEmpty()) {
						facets.add(facet);
					}
				}
			}
			return facets;
		}
		return null;
	}

	public static SpellCheck convertSpellCheck(SpellCheckResponse response) {
		if (response != null) {
			SpellCheck spellCheck = new SpellCheck();
			spellCheck.correctlySpelled = response.isCorrectlySpelled();
			for (Suggestion suggestion : response.getSuggestions()) {
				for (int i = 0; i < suggestion.getNumFound(); i++) {
					LabelFrequency value = new LabelFrequency();
					value.label = suggestion.getAlternatives().get(i);
					value.count = suggestion.getAlternativeFrequencies().get(i)
							.longValue();
					spellCheck.suggestions.add(value);
				}
			}
			return spellCheck;
		}
		return null;
	}


	/**
	 * retrieves the numerical part of the substring between the ':' and '*'
	 * characters.
	 * <p>e.g. "europeana_collectionName:91697*" will result in "91697"
	 *
	 * @param queryTerms provided String
	 * @return String containing the Europeana collection ID only
	 */
	public static String getIdFromQueryTerms(String queryTerms) {
		int from = !queryTerms.contains(":") ? 0 : queryTerms.indexOf(":");
		int to = !queryTerms.contains("*") ? queryTerms.length() - 1 : queryTerms.indexOf("*");
		return queryTerms.substring(from, to).replaceAll("\\D+", "");
	}


	/**
	 * Gives a translation of the 'EdmIsShownAt' label in the appropriate
	 * language.
	 * <p>The 'appropriate language' is arrived at as follows: first it tries
	 * to retrieve the language code from the bean and look up the translation
	 * in this language.
	 * <p>If this doesn't yield a string (either because the bean contains
	 * no language settings or there is no translation provided for that
	 * language), it tries to retrieve the translation based on the language
	 * code provided in the 'language' parameter - which has the value of the
	 * 'language' GET parameter if provided, or else the channel language code.
	 * <p>If that fails as well, it looks up the English translation of the
	 * label. And if that fails too, it returns a hardcoded error message.
	 *
	 * @param bean     containing language code
	 * @param language String containing the channel's language code
	 * @return String containing the label translation
	 */
	public static String getTranslatedEdmIsShownAtLabel(MessageSource messageSource, BriefBean bean, String language){
		String translatedEdmIsShownAtLabel;
		// first try with the bean language
		try {
			translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(messageSource, getLocale(bean.getLanguage()));
		} catch (MissingResourceException e) {
			translatedEdmIsShownAtLabel = "";
		}
		// check if retrieving translation for bean language failed
		if (StringUtils.isBlank(translatedEdmIsShownAtLabel)) {
			// if so, and bean language != channel language, try channel language
			if (!isLanguageEqual(bean.getLanguage(), language)) {
				try {
					translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(messageSource, getLocale(language));
				} catch (MissingResourceException e) {
					translatedEdmIsShownAtLabel = "";
				}
			}
			// check if translation is still empty
			if (StringUtils.isBlank(translatedEdmIsShownAtLabel)) {
				// if so, try English translation
				try {
					translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(messageSource, getLocale("en"));
				} catch (MissingResourceException e) {
					translatedEdmIsShownAtLabel = "";
				}
				// check if retrieving English translation failed
				if (StringUtils.isBlank(translatedEdmIsShownAtLabel)) {
					// if so, return hardcoded message
					return "error: translations for 'edmIsShownAtLabel' unavailable";
				}
			}
		}
		return translatedEdmIsShownAtLabel;
	}

	/**
	 * Gives the translation of the 'EdmIsShownAt' label in the language of
	 * the provided Locale
	 *
	 * @param locale Locale instance initiated with the desired language
	 * @return String containing the label translation
	 */
	public static String getEdmIsShownAtLabelTranslation(MessageSource messageSource, Locale locale) throws java.util.MissingResourceException {
		return messageSource.getMessage("edm_isShownAtLabel_t", null, locale);
	}

	/**
	 * Initiates and returns a Locale instance for the language specified by
	 * the language code found in the input.
	 * <p>Checks for NULL values, and whether or not the found code is two
	 * characters long; if not, it returns a locale initiated to English
	 *
	 * @param languageArray String Array containing language code
	 * @return Locale instance
	 */
	public static Locale getLocale(String[] languageArray) {
		if (!ArrayUtils.isEmpty(languageArray)
				&& !StringUtils.isBlank(languageArray[0])
				&& languageArray[0].length() == 2) {
			return new Locale(languageArray[0]);
		} else {
			return new Locale("en");
		}
	}

	/**
	 * Initiates and returns a Locale instance for the language specified by
	 * the language code found in the input.
	 * <p>Checks for NULL values, and whether or not the found code is two
	 * characters long; if not, it returns a locale initiated to English
	 *
	 * @param language String containing language code
	 * @return Locale instance
	 */
	public static Locale getLocale(String language) {
		if (!StringUtils.isBlank(language)
				&& language.length() == 2) {
			return new Locale(language);
		} else {
			return new Locale("en");
		}
	}


	/**
	 * simple utility method to compare the language code contained in a String array
	 * with another contained in a String. Also checks for well-formedness, i.e. if they're two characters long
	 *
	 * @param languageArray String[]
	 * @param language String
	 * @return boolean TRUE if equal, else FALSE
	 */
	public static boolean isLanguageEqual(String[] languageArray, String language){
		return (!ArrayUtils.isEmpty(languageArray)
				&& !StringUtils.isBlank(languageArray[0])
				&& languageArray[0].length() == 2
				&& !StringUtils.isBlank(language)
				&& language.length() == 2
				&& language.equalsIgnoreCase(languageArray[0]));
	}


	public static String getDcLanguage(BriefBean bean) {
		if (bean.getDcLanguage() != null && bean.getDcLanguage().length > 0
				&& StringUtils.isNotBlank(bean.getDcLanguage()[0])) {
			return bean.getDcLanguage()[0];
		} else {
			return "";
		}
	}

	/**
	 * Retrieves the title from the bean if not null; otherwise, returns
	 * a concatenation of the Data Provier and ID fields.
	 * <p>! FIX ME ! Note that this method will yield unwanted results when
	 * there is more than one Title field!
	 *
	 * @param bean mapped pojo bean
	 * @return String containing the concatenated fields
	 */
	public static String getTitle(BriefBean bean) {
		if (!ArrayUtils.isEmpty(bean.getTitle())) {
			for (String title : bean.getTitle()) {
				if (!StringUtils.isBlank(title)) {
					return title;
				}
			}
		}
		return bean.getDataProvider()[0] + " " + bean.getId();
	}

	/**
	 * retrieves a concatenation of the bean's DC Creator, Year and Provider
	 * fields (if available)
	 *
	 * @param bean mapped pojo bean
	 * @return String containing the fields, separated by semicolons
	 */
	public static String getDescription(BriefBean bean) {
		StringBuilder sb = new StringBuilder();
		if (bean.getDcCreator() != null && bean.getDcCreator().length > 0
				&& StringUtils.isNotBlank(bean.getDcCreator()[0])) {
			sb.append(bean.getDcCreator()[0]);
		}
		if (bean.getYear() != null && bean.getYear().length > 0) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(StringUtils.join(bean.getYear(), ", "));
		}
		if (!ArrayUtils.isEmpty(bean.getProvider())) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(StringUtils.join(bean.getProvider(), ", "));
		}
		return sb.toString();
	}

	public static boolean isFacetsRequested(String profile) {
		return StringUtils.containsIgnoreCase(profile, "portal") || StringUtils.containsIgnoreCase(profile, "facets");
	}

	public static boolean isDefaultFacetsRequested(String profile, String[] facets) {
		return StringUtils.containsIgnoreCase(profile, "portal") ||
				(StringUtils.containsIgnoreCase(profile, "facets")
						&& (ArrayUtils.isEmpty(facets)
						|| ArrayUtils.contains(facets, "DEFAULT")
				));
	}

	public static boolean isDefaultOrReusabilityFacetRequested(String profile, String[] facets) {
		return StringUtils.containsIgnoreCase(profile, "portal")
				|| (
				StringUtils.containsIgnoreCase(profile, "facets")
						&& (
						ArrayUtils.isEmpty(facets)
								|| ArrayUtils.contains(facets, "DEFAULT")
								|| ArrayUtils.contains(facets, "REUSABILITY")
				));
	}


}
