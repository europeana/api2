package eu.europeana.api2.v2.utils;

/**
 * This is a (slightly revised) copy of the MockFullBean class taken form Corelib schema.org module
 *
 * constants for MockFullBean
 *
 * @author Srishti Singh
 * <p>
 * Created on 09-05-2020
 */

import java.util.Date;

public class MockBeanConstants {

    public static final String COMMON_ABOUT             = "2021618/internetserver_Details_kunst_25027";
    public static final String DEF                      = "def";
    public static final String IT                       = "it";
    public static final String EN                       = "en";
    public static final String PL                       = "pl";
    public static final String FR                       = "fr";
    public static final String DE                       = "de";
    public static final String NO                       = "no";
    public static final String NL                       = "nl";
    public static final String EN_GB                    = "en-GB";
    public static final String NL_NL                    = "nl-NL";
    public static final String DE_NL                    = "de-NL";

    public static final String  URL_PREFIX               = "http://data.europeana.eu";
    public static final String  ABOUT                    = "/item/" + COMMON_ABOUT ;
    public static final String  EDM_IS_SHOWN_AT          = "http://teylers.adlibhosting.com/internetserver/Details/kunst/25027";
    public static final String  EDM_IS_SHOWN_BY          = "http://teylers.adlibhosting.com/wwwopacx/wwwopac.ashx?command=getcontent&server=images&value=TvB%20G%203674.jpg";
    public static final String  EDM_PREVIEW              = "http://edmPreview.jpg";
    public static final String  DC_TYPE_1                = "graph";
    public static final String  DC_TYPE_2                = "http://data.europeana.eu/concept/base/48";
    public static final String  DC_COVERAGE_ABOUT        = "01 December 1950";  //invalid date hence will be added as about
    public static final String  DC_COVERAGE_TEMPORAL     = "http://semium.org/time/1977" ;
    public static final String  DC_CREATOR_1             = "Calamatta, Luigi (1801-1869)";
    public static final String  DC_CREATOR_2             = "Leonardo da Vinci (1452 - 1519)";
    public static final String  DC_CREATOR_3             = "graveur";
    public static final String  DC_CREATOR_4             = "http://data.europeana.eu/concept/base/190";
    public static final String  DC_CREATOR_5             = "http://data.europeana.eu/agent/base/103195";
    public static final String  DC_CREATOR_6             = "http://data.europeana.eu/agent/base/6";
    public static final String  DC_CREATOR_7             = "#place_Paris";
    public static final String  DC_CREATOR_8             = "http://data.europeana.eu/agent/base/146741";
    public static final String  DC_CREATOR_9             = "Europeana";
    public static final String  DC_CREATOR_10             = "http://thesaurus.europeanafashion.eu/thesaurus/testing";
    public static final String  DC_CREATOR_11            = "/direct/testing_realtiveUri";
    public static final String  DC_TERMS_CREATED         = "01 November 1928";
    public static final String  EDM_COUNTRY              = "netherlands";
    public static final String  DC_TERMS_TEMPORAL_1      = "http://semium.org/time/19xx";
    public static final String  DC_TERMS_TEMPORAL_2      = "http://semium.org/time/1901";
    public static final String  DC_TERMS_TEMPORAL_3      = "1981-1990";
    public static final String  DC_TERMS_TEMPORAL_4      = "1930";
    public static final String  EDM_PROVIDER_1           = "Teylers Museum";
    public static final String  EDM_PROVIDER_2           = "Digitale Collectie";
    public static final String  DC_TITLE                 = "Mona Lisa";
    public static final String  DC_DESCRIPTION           = "https://data.europeana.eu/test";
    public static final String  DC_RELATION1             = "http://data.europeana.eu/agent/base/146741";
    public static final String  DC_RELATION2             = "#place_Paris";
    public static final String  DC_ALTERNATIVE           = "Mona Lisa Gemälde";
    public static final long    TIMESTAMP_CREATED        = 1395759054479L;
    public static final long    TIMESTAMP_UPDATED        = 1395759054479L;
    public static final String  MIME_TYPE_VIDEO          = "video/mp4";
    public static final String  MIME_TYPE_IMAGE          = "image/jpeg";
    public static final String  LANGUAUGE_NL             = "nl";
    public static final String  EDM_RIGHTS               = "http://creativecommons.org/licenses/by-nc/3.0/nl/";
    public static final String  EUROPEANA_COLLECTION     = "2021618_Ag_NL_DigitaleCollectie_TeylersKunst";
    public static final String  AGGREGATION_ABOUT        = "/aggregation/provider/" + COMMON_ABOUT ;
    public static final String  DC_DATE                  = "1821 - 1869";
    public static final String  DC_IDENTIFIER            = "TvB G 3674";
    public static final String  EUROPEANA_AGG_ABOUT      = "/aggregation/europeana/" + COMMON_ABOUT ;

    // proxy
    public static final String PROVIDER_PROXY = "/proxy/provider/" + COMMON_ABOUT;
    public static final String EUROPEANA_PROXY = "/proxy/europeana/" + COMMON_ABOUT;
    public static final String PROXY1_DC_FORMAT1_DEF    = "paper: height: 675 mm";
    public static final String PROXY1_DC_FORMAT2_DEF    = "paper: width: 522 mm";
    public static final String PROXY1_DC_FORMAT1_NL     = "papier: hoogte: 675 mm";
    public static final String PROXY1_DC_FORMAT2_NL     = "papier: breedte: 522 mm";
    public static final String PROXY1_DC_TERMS_MEDIUM_EN = "canvas";
    public static final String PROXY1_DC_TERMS_MEDIUM_IT = "tela";
    public static final String PROXY1_DC_TERMS_MEDIUM_NL = "doek";
    public static final String PROXY1_DC_DESCRIPTION_NL = "Meisje met de parel partial";
    public static final String PROXY1_DC_DESCRIPTION_DE = "Meisje met de parel exact match";


    //agent Person
    public static final String AGENT1_PREF_LABEL_EN      = "Science and inventions of Leonardo da Vinci";
    public static final String AGENT1_PREF_LABEL_PL      = "Wynalazki i konstrukcje Leonarda da Vinci";
    public static final String AGENT1_BIRTH_DATE         = "1452-04-15";
    public static final String AGENT1_DEATH_DATE         = "1519-05-02";
    public static final String AGENT1_BIRTH_PLACE_DEF1   = "http://dbpedia.org/resource/Republic_of_Florence";
    public static final String AGENT1_BIRTH_PLACE_DEF2   = "http://dbpedia.org/resource/Vinci,_Tuscany";
    public static final String AGENT1_BIRTH_PLACE_EN     = "Vinci, Republic of Florence";
    public static final String AGENT1_DEATH_PLACE_1      = "http://dbpedia.org/resource/Indre-et-Loire";
    public static final String AGENT1_DEATH_PLACE_2      = "http://dbpedia.org/resource/Amboise";

    //Organization
    public static final String DISOLUTION_DATE           = "2019-05-02";

    //Place
    public static final String  PLACE_PREF_LABEL         = "Paris";
    public static final String  PLACE_ALT_LABEL          = "Parigi";
    public static final String  PLACE_NOTE               = "Probably in Popicourt";
    public static final String  PLACE_SAME_OWL_AS        = "http://www.somewhere.eu/place/5";
    public static final String  PLACE_HAS_PART           = "http://www.somewhere.eu/place/2";
    public static final String  PLACE_IS_PART            = "http://www.somewhere.eu/place/3";

    //Concept
    public static final String CONCEPT_PREF_LABEL_NO     = "Landbruk";
    public static final String CONCEPT_PREF_LABEL_DE   = "Landwirtschaft";
    public static final String CONCEPT_PREF_LABEL_IT      = "carta";
    public static final String CONCEPT_NOTE_1            = "Landbruk er en fellesbetegnelse for jordbruk og skogbruk som begge er primærnæringer, og omfatter en rekke næringsgrener der foredling av jord til kulturplanter eller beite er grunnleggende for produksjonen. Ordet har samme betydning som agrikultur, fra latin ager («åker») og cultura («dyrking»).I Norge blir 2,8 % av landarealet brukt til jordbruk (2005).";
    public static final String CONCEPT_NOTE_2            = "Als Landwirtschaft wird der Wirtschaftsbereich der Urproduktion bezeichnet. Das Ziel der Urproduktion ist die zielgerichtete Herstellung pflanzlicher oder tierischer Erzeugnisse auf einer zu diesem Zweck bewirtschafteten Fläche. In der Wissenschaft sowie der fachlichen Praxis ist synonym der Begriff Agrarwirtschaft gebräuchlich.Die Landwirtschaft stellt einen der ältesten Wirtschaftsbereiche der Menschheit dar. Heute beläuft sich die landwirtschaftlich genutzte Fläche auf 48.827.330 km2, dies sind 9,6 % der Erdoberfläche. Somit wird etwa ein Drittel der Landfläche der Erde landwirtschaftlich genutzt.Der Wirtschaftsbereich Agrarwirtschaft wird zumeist in die beiden Sektoren Pflanzenproduktion Tierproduktioneingeteilt und dann weiter untergliedertDer Anbau von Nutzpflanzen dient zuallererst der Nahrungsmittelproduktion direkt wie indirekt. In letzterem Fall erfolgt die Herstellung von Rohstoffen zur weiteren Verarbeitung in Teilen des nachgelagerten Wirtschaftsbereichs des sogenannten Agribusiness (z. B. Weiterverarbeitung von Getreide zu Mehl für die Brotherstellung). Darüber hinaus werden landwirtschaftliche Rohstoffe (u. a. Faserpflanzen wie Baumwolle oder Leinen) auch in der Bekleidungsindustrie weiter veredelt.Die Haltung von Nutztieren dient in erster Linie der Nahrungsmittelproduktion (z. B. Milch, Eier), in zweiter Linie der Herstellung von Rohstoffen für die Herstellung von Bekleidung. Vor der Produktion von Kunstfasern schufen die Menschen noch ihre gesamte Bekleidung u. a. aus den tierischen Produkten Leder, Pelz und Wolle.Die Verwertung der durch die Agrarwirtschaft, zum Teil erst seit kürzerer Zeit, angebauten Biomasse aus nachwachsenden Rohstoffen (u. a. Holz, Mais) in Form von Vergasung, Karbonisierung und Raffinierung stellt eine erst seit kurzer Zeit mitunter stark zunehmende Form der Veredelung dar.Die Landwirtschaft ist Teilwirtschaftszweig eines größeren Gesamtsystems mit vor- und nachgelagerten Sektoren.Eine Person, die Landwirtschaft betreibt, bezeichnet man als Landwirt. Neben berufspraktischen Ausbildungen bestehen an zahlreichen Universitäten und Fachhochschulen eigene landwirtschaftliche Fachbereiche. Das dort gelehrte und erforschte Fach Agrarwissenschaft bereitet sowohl auf die Führung von landwirtschaftlichen Betrieben als auch auf Tätigkeiten in verwandten Wirtschaftsbereichen vor und ist ein ingenieurwissenschaftliches Fach.";
}
