package eu.europeana.api2.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europeana.api2.utils.SolrEscapeDeserializer;
import io.swagger.annotations.ApiModelProperty;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Model class for search requests
 *
 * Sanitized fields correspond to request parameters annotated with @SolrEscape in
 * {@link eu.europeana.api2.v2.web.controller.SearchController#searchJsonGet}
 *
 * @author Srishti Singh
 * Created on 23 Sep 2020
 */
@JsonInclude(NON_EMPTY)
public class SearchRequest {

    @ApiModelProperty(required = true)
    @JsonDeserialize(using = SolrEscapeDeserializer.class)
    private String query;

    private String[] qf;
    private String[] nqf;
    private String[] reusability;
    private String[] profile = {"standard"};
    private int start = 1;
    private int rows = 12;

    // TODO find out way to achieve Sanitization with @SolrEscape for String []
    private String[] facet;

    private String theme;
    private String[] sort;
    private String[] colourPalette;
    private Boolean thumbnail;
    private Boolean media;
    private Boolean textFulltext;
    private Boolean landingPage;
    private String cursor;
    private String callback;
    private Hit hit = new Hit();
    private String boost;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getQf() {
        return qf;
    }

    public void setQf(String[] qf) {
        this.qf = qf;
    }

    public String[] getReusability() {
        return reusability;
    }

    public void setReusability(String[] reusability) {
        this.reusability = reusability;
    }

    public String[] getProfile() {
        return profile;
    }

    public void setProfile(String[] profile) {
        this.profile = profile;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String[] getFacet() {
        return facet;
    }

    public void setFacet(String[] facet) {
        this.facet = facet;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String[] getSort() {
        return sort;
    }

    public void setSort(String[] sort) {
        this.sort = sort;
    }

    public String[] getColourPalette() {
        return colourPalette;
    }

    public void setColourPalette(String[] colourPalette) {
        this.colourPalette = colourPalette;
    }

    public Boolean isThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Boolean thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Boolean isMedia() {
        return media;
    }

    public void setMedia(Boolean media) {
        this.media = media;
    }

    public Boolean isTextFulltext() {
        return textFulltext;
    }

    public void setTextFulltext(Boolean textFulltext) {
        this.textFulltext = textFulltext;
    }

    public Boolean isLandingPage() {
        return landingPage;
    }

    public void setLandingPage(Boolean landingPage) {
        this.landingPage = landingPage;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Hit getHit() {
        return hit;
    }

    public void setHit(Hit hit) {
        this.hit = hit;
    }

    public String getBoost() {
        return boost;
    }

    public void setBoost(String boost) { this.boost = boost;}

    public String[] getNqf() { return nqf; }

    public void setNqf(String[] nqf) { this.nqf = nqf; }
}