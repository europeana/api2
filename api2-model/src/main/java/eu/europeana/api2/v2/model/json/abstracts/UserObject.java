package eu.europeana.api2.v2.model.json.abstracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.corelib.definitions.solr.DocType;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Deprecated
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
            return Api2UrlService.getBeanInstance().getThumbnailUrl(edmPreview, type);
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
