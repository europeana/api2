package eu.europeana.api2.v2.utils;

public enum FacetNames {
    MIME_TYPE("MIME_TYPE"),

    IS_FULLTEXT("TEXT_FULLTEXT"),
    HAS_MEDIA("MEDIA"),
    HAS_THUMBNAILS("THUMBNAILS"),

    IMAGE_SIZE("IMAGE_SIZE"),
    IMAGE_ASPECTRATIO("IMAGE_ASPECTRATIO"),
    IMAGE_COLOUR("IMAGE_COLOUR"),
    IMAGE_GREYSCALE("IMAGE_GREYSCALE"),

    COLOURPALETTE("COLOURPALETTE"),

    VIDEO_DURATION("VIDEO_DURATION"),
    VIDEO_HD("VIDEO_HD"),

    SOUND_HQ("SOUND_HQ"),
    SOUND_DURATION("SOUND_DURATION");

    private final String realName_;

    private FacetNames(String realName) {
        realName_ = realName;
    }

    public String getRealName() {return realName_;}

}
