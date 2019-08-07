package eu.europeana.api2.v2.utils.technicalfacets;

import eu.europeana.indexing.solr.facet.EncodedFacet;
import eu.europeana.indexing.solr.facet.value.MediaTypeEncoding;
import eu.europeana.indexing.solr.facet.value.MimeTypeEncoding;

import eu.europeana.metis.utils.MediaType;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Generates the common pure tags (mimetype and mediatype)
 */
public final class CommonTagExtractor {

    private static final List<String> mimeTypesKey= new ArrayList<>();
    private static Set<String> uniqueTypeOfMimeType;

    private CommonTagExtractor() {
        // empty constructor to prevent initialization
    }

    static {

        /**
         * Stores the mimetypes key values in mimeTypesKey and then removes the duplicates
         * Mime_type :  image/x-ms-bmp ; image is a type and x-ms-bmp  is a subtype
         * unique type of mimetypes are stored in uniqueTypeOfMimeType
         * @param uniqueTypeOfMimeType will be used for validating the mimetypes
         */
        for ( MimeTypeEncoding mimeType : MimeTypeEncoding.values()){
            mimeTypesKey.add(StringUtils.substringBefore(mimeType.getValue(), "/").toLowerCase(Locale.GERMAN)) ;
        }
        uniqueTypeOfMimeType = new HashSet<>(mimeTypesKey);
    }

    public static MimeTypeEncoding decodeMimeType(final String type) {
        return MimeTypeEncoding.categorizeMimeType(type);
    }

    public static String getMimeType(Integer tag) {
        return Optional.ofNullable(EncodedFacet.MIME_TYPE.decodeValue(tag)).map(MimeTypeEncoding::getValue).orElse(null);
    }

    public static boolean isImageMimeType(String type) {
        return MediaType.getMediaType(type) == MediaType.IMAGE;
    }

    public static boolean isSoundMimeType(String type) {
        return MediaType.getMediaType(type) == MediaType.AUDIO;
    }

    public static boolean isVideoMimeType(String type) {
        return MediaType.getMediaType(type) == MediaType.VIDEO;
    }

    public static MediaTypeEncoding getType(Integer tag) {
        return Optional.ofNullable(EncodedFacet.MEDIA_TYPE.decodeValue(tag)).orElse(null);
    }

    public static boolean isValidMimeType(String type){
        Iterator<String> i = uniqueTypeOfMimeType.iterator();
        while (i.hasNext())
        {   if(StringUtils.startsWithIgnoreCase(type,i.next())){
            return true;
            }
        }
        return false;
    }
}
