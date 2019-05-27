package eu.europeana.api2.v2.utils.technicalfacets;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.europeana.corelib.definitions.model.Orientation;

/**
 * Extracts the pure tags from an image resource and generates the fake tags.
 */
public class ImageTagExtractor {

    private static final Logger LOG = LogManager.getLogger(ImageTagExtractor.class.getName());
    public static final BiMap<String, Integer> hexColors = HashBiMap.create(139);

    static {
        hexColors.put("",0);
        hexColors.put("#F0F8FF",1);
        hexColors.put("#FAEBD7",2);
        hexColors.put("#00FFFF",3);
        hexColors.put("#7FFFD4",4);
        hexColors.put("#F0FFFF",5);
        hexColors.put("#F5F5DC",6);
        hexColors.put("#FFE4C4",7);
        hexColors.put("#000000",8);
        hexColors.put("#FFEBCD",9);
        hexColors.put("#0000FF",10);
        hexColors.put("#8A2BE2",11);
        hexColors.put("#A52A2A",12);
        hexColors.put("#DEB887",13);
        hexColors.put("#5F9EA0",14);
        hexColors.put("#7FFF00",15);
        hexColors.put("#D2691E",16);
        hexColors.put("#FF7F50",17);
        hexColors.put("#6495ED",18);
        hexColors.put("#FFF8DC",19);
        hexColors.put("#DC143C",20);
        hexColors.put("#00008B",21);
        hexColors.put("#008B8B",22);
        hexColors.put("#B8860B",23);
        hexColors.put("#A9A9A9",24);
        hexColors.put("#006400",25);
        hexColors.put("#BDB76B",26);
        hexColors.put("#8B008B",27);
        hexColors.put("#556B2F",28);
        hexColors.put("#FF8C00",29);
        hexColors.put("#9932CC",30);
        hexColors.put("#8B0000",31);
        hexColors.put("#E9967A",32);
        hexColors.put("#8FBC8F",33);
        hexColors.put("#483D8B",34);
        hexColors.put("#2F4F4F",35);
        hexColors.put("#00CED1",36);
        hexColors.put("#9400D3",37);
        hexColors.put("#FF1493",38);
        hexColors.put("#00BFFF",39);
        hexColors.put("#696969",40);
        hexColors.put("#1E90FF",41);
        hexColors.put("#B22222",42);
        hexColors.put("#FFFAF0",43);
        hexColors.put("#228B22",44);
        hexColors.put("#FF00FF",45);
        hexColors.put("#DCDCDC",46);
        hexColors.put("#F8F8FF",47);
        hexColors.put("#FFD700",48);
        hexColors.put("#DAA520",49);
        hexColors.put("#808080",50);
        hexColors.put("#008000",51);
        hexColors.put("#ADFF2F",52);
        hexColors.put("#F0FFF0",53);
        hexColors.put("#FF69B4",54);
        hexColors.put("#CD5C5C",55);
        hexColors.put("#4B0082",56);
        hexColors.put("#FFFFF0",57);
        hexColors.put("#F0E68C",58);
        hexColors.put("#E6E6FA",59);
        hexColors.put("#FFF0F5",60);
        hexColors.put("#7CFC00",61);
        hexColors.put("#FFFACD",62);
        hexColors.put("#ADD8E6",63);
        hexColors.put("#F08080",64);
        hexColors.put("#E0FFFF",65);
        hexColors.put("#FAFAD2",66);
        hexColors.put("#D3D3D3",67);
        hexColors.put("#90EE90",68);
        hexColors.put("#FFB6C1",69);
        hexColors.put("#FFA07A",70);
        hexColors.put("#20B2AA",71);
        hexColors.put("#87CEFA",72);
        hexColors.put("#778899",73);
        hexColors.put("#B0C4DE",74);
        hexColors.put("#FFFFE0",75);
        hexColors.put("#00FF00",76);
        hexColors.put("#32CD32",77);
        hexColors.put("#FAF0E6",78);
        hexColors.put("#800000",79);
        hexColors.put("#66CDAA",80);
        hexColors.put("#0000CD",81);
        hexColors.put("#BA55D3",82);
        hexColors.put("#9370DB",83);
        hexColors.put("#3CB371",84);
        hexColors.put("#7B68EE",85);
        hexColors.put("#00FA9A",86);
        hexColors.put("#48D1CC",87);
        hexColors.put("#C71585",88);
        hexColors.put("#191970",89);
        hexColors.put("#F5FFFA",90);
        hexColors.put("#FFE4E1",91);
        hexColors.put("#FFE4B5",92);
        hexColors.put("#FFDEAD",93);
        hexColors.put("#000080",94);
        hexColors.put("#FDF5E6",95);
        hexColors.put("#808000",96);
        hexColors.put("#6B8E23",97);
        hexColors.put("#FFA500",98);
        hexColors.put("#FF4500",99);
        hexColors.put("#DA70D6",100);
        hexColors.put("#EEE8AA",101);
        hexColors.put("#98FB98",102);
        hexColors.put("#AFEEEE",103);
        hexColors.put("#DB7093",104);
        hexColors.put("#FFEFD5",105);
        hexColors.put("#FFDAB9",106);
        hexColors.put("#CD853F",107);
        hexColors.put("#FFC0CB",108);
        hexColors.put("#DDA0DD",109);
        hexColors.put("#B0E0E6",110);
        hexColors.put("#800080",111);
        hexColors.put("#FF0000",112);
        hexColors.put("#BC8F8F",113);
        hexColors.put("#4169E1",114);
        hexColors.put("#8B4513",115);
        hexColors.put("#FA8072",116);
        hexColors.put("#F4A460",117);
        hexColors.put("#2E8B57",118);
        hexColors.put("#FFF5EE",119);
        hexColors.put("#A0522D",120);
        hexColors.put("#C0C0C0",121);
        hexColors.put("#87CEEB",122);
        hexColors.put("#6A5ACD",123);
        hexColors.put("#708090",124);
        hexColors.put("#FFFAFA",125);
        hexColors.put("#00FF7F",126);
        hexColors.put("#4682B4",127);
        hexColors.put("#D2B48C",128);
        hexColors.put("#008080",129);
        hexColors.put("#D8BFD8",130);
        hexColors.put("#FF6347",131);
        hexColors.put("#40E0D0",132);
        hexColors.put("#EE82EE",133);
        hexColors.put("#F5DEB3",134);
        hexColors.put("#FFFFFF",135);
        hexColors.put("#F5F5F5",136);
        hexColors.put("#FFFF00",137);
        hexColors.put("#9ACD32",138);
    }


    public static Integer getSizeCode(final Integer width, final Integer height) {
        if (width == null || height == null) return 0;
        final long size = (long) (width * height);
        if (size < 524288) return 1;
        else if (size < 1048576) return 2;
        else if (size < 4194304) return 3;
        else return 4;
    }

    public static Integer getSizeCode(final String imageSize) {
        if (StringUtils.isBlank(imageSize)) return 0;
        switch (imageSize){
            case "small":
                return 1;
            case "medium":
                return 2;
            case "large":
                return 3;
            case "extra_large":
                return 4;
            default:
                return 0;
        }
    }

    public static String getSize(int tag) {
        final int sizeCode = TagEncoding.IMAGE_SIZE.extractValue(tag);
        switch (sizeCode) {
            case 1:
                return "small";
            case 2:
                return "medium";
            case 3:
                return "large";
            case 4:
                return "extra_large";
            default:
                return "";
        }
    }

    public static Integer getColorSpaceCode(final String colorSpace) {
        if (StringUtils.isBlank(colorSpace)) return 0;
        else if (StringUtils.containsIgnoreCase(colorSpace, "rgb")) return 1;
        else if (StringUtils.containsIgnoreCase(colorSpace, "gray") || colorSpace.toLowerCase().contains("grey")) return 2;
        else if (StringUtils.containsIgnoreCase(colorSpace, "cmyk")) return 3;
        else return 0;
    }

    public static Integer getColorSpaceCode(final Boolean imageColor, final Boolean imageGrayScale) {
        if (imageColor == null && imageGrayScale == null) return 0;
        else if (imageGrayScale != null && imageGrayScale) return 2;
        else if (imageColor != null && imageColor) return 1;
        else return 3;
    }

    public static String getColorSpace(int tag) {
        final int colorSpaceCode = TagEncoding.IMAGE_COLOURSPACE.extractValue(tag);
        if (2 == colorSpaceCode) {
            return "false";
        } else if (1 == colorSpaceCode || 3 == colorSpaceCode) {
            return "true";
        }
        return "";
    }

    // used in TagUtils
    public static Integer getAspectRatioCode(final Orientation orientation) {
        if (orientation == null) return 0;
        else if (orientation == Orientation.PORTRAIT) return 1;
        else if (orientation == Orientation.LANDSCAPE) return 2;
        else return 0;
    }

    // EA-1436 switched the values around to fix the mistake
    // before: 10 portrait, 01 landscape
    // now: 01 portrait, 10 landscape
    // used in ModelUtils
    public static String getAspectRatio(int tag) {
        final int aspectRatioCode = TagEncoding.IMAGE_ASPECTRATIO.extractValue(tag);
        switch (aspectRatioCode) {
            case 1:
                return Orientation.getValue(Orientation.PORTRAIT);
            case 2:
                return Orientation.getValue(Orientation.LANDSCAPE);
            default:
                return "";
        }
    }

    public static Integer getColorCode(final String colour) {
        if (StringUtils.isBlank(colour)) return 0;
        else if (hexColors.containsKey(colour)) return hexColors.get(colour);
        else return 0;
    }

    public static String getColor(int tag) {
        final Integer colorCode = TagEncoding.IMAGE_COLOUR.extractValue(tag);
        return StringUtils.defaultIfEmpty(hexColors.inverse().get(colorCode), "");
    }
}
