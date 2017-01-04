package eu.europeana.api2.v2.web.controller;

import java.io.IOException;

/**
 * Created by jeroen on 23-12-16.
 */
public class SiteMapNotFoundException extends Exception {
    public SiteMapNotFoundException(String s, IOException e) {
        super(s,e);
    }

    public SiteMapNotFoundException(String s) {
        super(s);
    }
}
