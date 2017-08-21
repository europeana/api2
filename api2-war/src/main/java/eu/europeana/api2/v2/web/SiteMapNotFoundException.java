package eu.europeana.api2.v2.web;


/**
 * Error thrown when a requested sitemap file cannot be found/retrieved
 * Created by jeroen on 23-12-16.
 */
public class SiteMapNotFoundException extends Exception {

    /**
     * Error thrown when a requested sitemap file cannot be found/retrieved
     * @param s
     */
    public SiteMapNotFoundException(String s) {
        super(s);
    }
}
