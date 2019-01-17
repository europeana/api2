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

package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.utils.technicalfacets.*;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import eu.europeana.corelib.definitions.model.Orientation;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class TagUtils {

    private TagUtils(){}

    // 1) the fixlist() method cleans up duplicates and replaces NULL Lists with a list containing only "null". This is
    // to facilitate the default "0" values of the filter tags.
    // 2) The nested forEach creates a cartesian combination of the input lists
    // 3) The filtertag integers are assembled right away inside the loops, as it adds nothing to delegate that to
    // multiple downstream methods. But it compacts the code by a factor 4 at least.
    public static List<Integer> imageFilterTags(final List<String> imageMimeTypeFacets, final List<String> imageSizeFacets,
                                                final List<String> imageColourSpaceFacets, final List<String> imageAspectRatioFacets,
                                                final List<String> imageColourPaletteFacets) {
        List<Integer> filterTags = new ArrayList<>();
        fixList(imageMimeTypeFacets).forEach(mimeTypeFacet ->
        fixList(imageSizeFacets).forEach(sizeFacet ->
        fixList(imageColourSpaceFacets).forEach(colourSpaceFacet ->
        fixList(imageAspectRatioFacets).forEach(aspectRatioFacet ->
        fixList(imageColourPaletteFacets).forEach(colourPaletteFacet -> {
            filterTags.add(MediaTypeEncoding.IMAGE.getEncodedValue() |
                           CommonTagExtractor.getMimeTypeCode(mimeTypeFacet.equals("null") ? null : mimeTypeFacet) << TagEncoding.MIME_TYPE.getBitPos() |
                           ImageTagExtractor.getSizeCode(sizeFacet.equals("null") ? null : sizeFacet) << TagEncoding.IMAGE_SIZE.getBitPos() |
                           ImageTagExtractor.getColorSpaceCode(colourSpaceFacet.equals("null") ? null : colourSpaceFacet) << TagEncoding.IMAGE_COLOURSPACE.getBitPos() |
                           ImageTagExtractor.getAspectRatioCode(aspectRatioFacet.equals("null") ? null : getImageOrientation(aspectRatioFacet)) << TagEncoding.IMAGE_ASPECTRATIO.getBitPos() |
                           ImageTagExtractor.getColorCode(colourPaletteFacet.equals("null") ? null : colourPaletteFacet) << TagEncoding.IMAGE_COLOUR.getBitPos());})))));
        return filterTags;
    }

    public static List<Integer> soundFilterTags(List<String> soundMimeTypeFacets, List<String> soundHQFacets, List<String> soundDurationFacets) {
        final List<Integer> filterTags = new ArrayList<>();
        fixList(soundMimeTypeFacets).forEach((mimeTypeFacet) ->
        fixList(soundHQFacets).forEach((hqFacet) ->
        fixList(soundDurationFacets).forEach((durationFacet) -> {
            filterTags.add(MediaTypeEncoding.AUDIO.getEncodedValue() |
                           CommonTagExtractor.getMimeTypeCode(mimeTypeFacet.equals("null") ? null : mimeTypeFacet) << TagEncoding.MIME_TYPE.getBitPos() |
                           SoundTagExtractor.getQualityCode(hqFacet.equals("null") ? null : hqFacet) << TagEncoding.SOUND_QUALITY.getBitPos() |
                           SoundTagExtractor.getDurationCode(durationFacet.equals("null") ? null : durationFacet) << TagEncoding.SOUND_DURATION.getBitPos());})));
        return filterTags;
    }

    public static List<Integer> videoFilterTags(List<String> videoMimeTypeFacets, List<String> videoHDFacets, List<String> videoDurationFacets) {
        final List<Integer> filterTags = new ArrayList<>();
        fixList(videoMimeTypeFacets).forEach((mimeTypeFacet) ->
        fixList(videoHDFacets).forEach((hdFacet) ->
        fixList(videoDurationFacets).forEach((durationFacet) ->{
            filterTags.add(MediaTypeEncoding.VIDEO.getEncodedValue() |
                           CommonTagExtractor.getMimeTypeCode(mimeTypeFacet.equals("null") ? null : mimeTypeFacet) << TagEncoding.MIME_TYPE.getBitPos() |
                           VideoTagExtractor.getQualityCode(hdFacet.equals("null") ? null : hdFacet) << TagEncoding.VIDEO_QUALITY.getBitPos() |
                           VideoTagExtractor.getDurationCode(durationFacet.equals("null") ? null : durationFacet) << TagEncoding.VIDEO_DURATION.getBitPos());})));
        return filterTags;
    }

    public static List<Integer> otherFilterTags(List<String> otherMimeTypeFacets) {
        final List<Integer> filterTags = new ArrayList<>();
        fixList(otherMimeTypeFacets).forEach((mimeTypeFacet) -> {
            filterTags.add(MediaTypeEncoding.TEXT.getEncodedValue() |
                           CommonTagExtractor.getMimeTypeCode(mimeTypeFacet) << TagEncoding.MIME_TYPE.getBitPos());});
        return filterTags;
    }

    // specifically for producing the colour palette filter tags associated with the colourPalette parameter
    // i.e. as opposed to the colourPaletteFacets as occurring in the qf:refinements / facets
    public static List<Integer> colourPaletteFilterTags(List<String> colourPalette) {
        final List<Integer> filterTags = new ArrayList<>();
        fixList(colourPalette).forEach((colour) -> {
            filterTags.add(MediaTypeEncoding.IMAGE.getEncodedValue() |
                           ImageTagExtractor.getColorCode(colour) << TagEncoding.IMAGE_COLOUR.getBitPos());});
        return filterTags;
    }

    // 1) converts Lists of any type to List of String
    // 2) replaces NULL Lists with List of String containing just "null" in order to create the default 'zero'
    // positions for every possible qf parameter (if they were NULL they would be skipped in the foreach loops)
    // 3) removes any duplicate values
    private static List<String> fixList(List<?> fixMe){
        ArrayList<String> retval = new ArrayList<>();
        if (fixMe != null && !fixMe.isEmpty()) fixMe.forEach(value -> { retval.add(value.toString());});
        else retval.add("null");
        return new ArrayList<>(new LinkedHashSet<>(retval));
    }

    // why only this property should have its own class representing exactly ONE bit of information?
    // Add that to the mysteries of life ...
    private static Orientation getImageOrientation(String imageAspectRatio){
        if (StringUtils.isBlank(imageAspectRatio)) return null;
        else if (imageAspectRatio.contains("portrait")) return Orientation.PORTRAIT;
        else if (imageAspectRatio.contains("landscape")) return Orientation.LANDSCAPE;
        else return null;
    }
}