package eu.europeana.api2.v2.web.controller;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for Thumbnail controller
 * @author Patrick Ehlert on 4-12-17.
 */
public class ThumbnailControllerTest {

    private static final String ORIG_IIIF_URL_HTTP = "http://iiif.europeana.eu/records/GGDNOQYY5N35KNXL7PZBCNRWDJN6RCWLCKN6XXPRD5632RSEEQIA/representations/presentation_images/versions/c7aaa970-fd11-11e5-bc8a-fa163e60dd72/files/node-3/image/NLE/Edasi/1922/03/15/1/19220315_1-0001/full/full/0/default.jpg";
    private static final String REVISED_IIIF_URL_HTTP = "http://iiif.europeana.eu/records/GGDNOQYY5N35KNXL7PZBCNRWDJN6RCWLCKN6XXPRD5632RSEEQIA/representations/presentation_images/versions/c7aaa970-fd11-11e5-bc8a-fa163e60dd72/files/node-3/image/NLE/Edasi/1922/03/15/1/19220315_1-0001/full/400,/0/default.jpg";

    // note that iiif currently doesn't support https, but we test it in case they add it
    private static final String ORIG_IIIF_URL_HTTPS = "https://IIIF.EUROPEANA.EU/records/NWGBII4G57XVAYLJYOFUJUFEIUS2G2BXLHVQT6QKRAWQVDA7ZRXA/representations/presentation_images/versions/9cb967b2-fcfd-11e5-bc8a-fa163e60dd72/files/node-3/image/NLE/Edasi/1920/10/10/1/19201010_1-0001/full/full/0/default.jpg";
    private static final String REVISED_IIIF_URL_HTTPS = "https://IIIF.EUROPEANA.EU/records/NWGBII4G57XVAYLJYOFUJUFEIUS2G2BXLHVQT6QKRAWQVDA7ZRXA/representations/presentation_images/versions/9cb967b2-fcfd-11e5-bc8a-fa163e60dd72/files/node-3/image/NLE/Edasi/1920/10/10/1/19201010_1-0001/full/200,/0/default.jpg";

    private static final String REGULAR_URL = "http://www.bildarchivaustria.at/Preview/15620341.jpg";

    /**
     * Tests if we detect IIIF image urls correctly
     */
    @Test
    public void DetectIiifUrlTest() {
        assertTrue(ThumbnailController.isIiifRecordUrl(ORIG_IIIF_URL_HTTP));
        assertTrue(ThumbnailController.isIiifRecordUrl(ORIG_IIIF_URL_HTTPS));
        assertFalse(ThumbnailController.isIiifRecordUrl(REGULAR_URL));
    }

    /**
     * Test if we generate IIIF image thumbnaisl urls correctly
     */
    @Test
    public void GetIiifThumbnailTest() throws URISyntaxException {
        assertEquals(REVISED_IIIF_URL_HTTP, ThumbnailController.getIiifThumbnailUrl(ORIG_IIIF_URL_HTTP, "400").toString());
        assertEquals(REVISED_IIIF_URL_HTTPS, ThumbnailController.getIiifThumbnailUrl(ORIG_IIIF_URL_HTTPS, "200").toString());
        assertNull(ThumbnailController.getIiifThumbnailUrl(REGULAR_URL, "400"));
        assertNull(ThumbnailController.getIiifThumbnailUrl(null, "300"));
    }
}
