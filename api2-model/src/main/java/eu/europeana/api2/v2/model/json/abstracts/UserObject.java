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

package eu.europeana.api2.v2.model.json.abstracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.web.service.impl.EuropeanaUrlServiceImpl;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public abstract class UserObject {

    public Long id;

    public String europeanaId;

    public String guid;

    public String link;

    public String title;

    public String edmPreview;

    public DocType type = DocType.IMAGE;

    public Date dateSaved;

    public Long getId() {
        return id;
    }

    public String getEuropeanaId() {
        return europeanaId;
    }

    public String getGuid() {
        return guid;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getEdmPreview() {
        if (StringUtils.isNotBlank(edmPreview)) {
            return EuropeanaUrlServiceImpl.getBeanInstance().getThumbnailUrl(edmPreview, type).toString();
        }
        return null;
    }

    public DocType getType() {
        return type;
    }

    public Date getDateSaved() {
        return dateSaved;
    }
}
