/*
 * Copyright 2007-2019 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.api2.v2.service;

import eu.europeana.api2.v2.model.json.view.submodel.HighlightHit;
import eu.europeana.api2.v2.model.json.view.submodel.HitSelector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luthien on 03/01/2019.
 */
public class HitMaker {
    private static final Logger LOG = LogManager.getLogger(HitMaker.class);

    private static final Pattern FULLTEXT_PATTERN   = Pattern.compile("fulltext\\.\\w{2,3}", Pattern.CASE_INSENSITIVE);
    private static final String  RDF_VALUE          = "rdf:value";
    private static final String  UNKNOWN            = "unknown value";
    private static final String  EM_START           = "<em>";
    private static final String  EM_END             = "</em>";
    private static final String  THE_END            = "@end@";

    public List<HighlightHit> createHitList(Map<String, Map<String,List<String>>> highlighting, int nrSelectors){

        final List<HighlightHit> hitList = new ArrayList<>();

        // loop through the Map of Highlighting Maps returned by Solr
        for (Map.Entry<String, Map<String,List<String>>> hitParade : highlighting.entrySet()){
            HighlightHit hit = new HighlightHit(hitParade.getKey());

            Map<String,List<String>> hitContent = hitParade.getValue();
            List<HitSelector> selectors = new ArrayList<>();

            // loop through the Map of Selector Maps returned by Solr
            for (Map.Entry<String,List<String>> topOrFlop : hitContent.entrySet()){
                String keyString = getFieldString(topOrFlop.getKey());
                if (CollectionUtils.isNotEmpty(topOrFlop.getValue())){
                    int i = 0;
                    for (String lyrics : topOrFlop.getValue()){
                        if (StringUtils.isNotBlank(lyrics)){
                            HitSelector selector;
                            do {
                                selector = createSelector(lyrics);
                                if (selector != null){
                                    selector.setField(keyString);
                                    selectors.add(selector);
                                    lyrics = selector.getRemainder();
                                } else {
                                    LOG.error("Error: no highlighting could be retrieved for this element");
                                    break;
                                }
                                i++;
                            } while (!StringUtils.equalsIgnoreCase(lyrics, THE_END) && i <= nrSelectors);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(selectors)){
                hit.setSelectors(selectors);
                hitList.add(hit);
            }
        }
        return hitList;
    }

    private HitSelector createSelector (String lyrics){
        String remainder;
        String suffix;
        String prefix   = StringUtils.substringBefore(lyrics, EM_START);
        lyrics          = StringUtils.removeStart(StringUtils.removeStart(lyrics, prefix), EM_START);
        String exact    = StringUtils.substringBefore(lyrics, EM_END);
        lyrics          = StringUtils.removeStart(StringUtils.removeStart(lyrics, exact), EM_END);
        if (StringUtils.containsIgnoreCase(lyrics, EM_START)){
            suffix      = StringUtils.substringBefore(lyrics, EM_START);
            remainder   = lyrics;
        } else {
            suffix      = lyrics;
            remainder   = THE_END;
        }
        if (prefix != null && exact != null && suffix != null && remainder != null){
            return new HitSelector(prefix, exact, suffix, remainder);
        } else {
            return null;
        }
    }

    private static String getFieldString(String keyValue){
        // TODO turn this into a Switch-Case when the need for more possible values arises
        Matcher matcher = FULLTEXT_PATTERN.matcher(keyValue);
        if (matcher.find()) {
            return RDF_VALUE;
        } else {
            return UNKNOWN;
        }
    }

}
