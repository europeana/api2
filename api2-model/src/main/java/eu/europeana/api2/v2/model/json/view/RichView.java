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

package eu.europeana.api2.v2.model.json.view;

import eu.europeana.corelib.definitions.edm.beans.RichBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RichView extends ApiView implements RichBean {

    private String[] isShownBy;
    private String[] dcDescription;
    private String[] edmLandingPage;
    private Map<String, List<String>> dcDescriptionLangAware;
    private Map<String, List<String>> dcSubjectLangAware;
    private Map<String, List<String>> dcTypeLangAware;


    public RichView(RichBean bean, String profile, String wskey, boolean optOut) {
        super(bean, profile, wskey, optOut);
        dcDescription = bean.getDcDescription();
        isShownBy = bean.getEdmIsShownBy();
        edmLandingPage = bean.getEdmLandingPage();
        dcTypeLangAware = bean.getDcTypeLangAware();
        dcSubjectLangAware = bean.getDcSubjectLangAware();
        dcDescriptionLangAware = bean.getDcDescriptionLangAware();
    }

    @Override
    public String[] getEdmIsShownBy() {
        if (ArrayUtils.isEmpty(isShownBy)) {
            return isShownBy;
        }
        // String provider = getProvider()[0];
        List<String> isShownByLinks = new ArrayList<>();
        for (String item : isShownBy) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            isShownByLinks.add(item);
        }
        return isShownByLinks.toArray(new String[isShownByLinks.size()]);
    }

    @Override
    public String[] getDcDescription() {
        return dcDescription;
    }

    @Override
    public String[] getEdmLandingPage() {
        return edmLandingPage;
    }

    @Override
    public Map<String, List<String>> getDcDescriptionLangAware() {
        return dcDescriptionLangAware;
    }

    @Override
    public Map<String, List<String>> getDcTypeLangAware() {
        return dcTypeLangAware;
    }

    @Override
    public Map<String, List<String>> getDcSubjectLangAware() {

        return dcSubjectLangAware;
    }
}
