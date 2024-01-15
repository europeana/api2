package eu.europeana.api2.v2.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.solr.bean.impl.BriefBeanImpl;

import java.util.ArrayList;
import java.util.List;

public class MockSearchBeanResults {


    public static List<BriefBean> mockSearchResults(boolean translations) {
        List<BriefBean> beans = new ArrayList<>();

        // will add 6 serach results
        beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcTitleLangAware\": { \"proxy_dc_title.def\": [ \"Paris.\" ]}, \"id\": \"/2048426/item_THLS7Z3QU7MC3WDKASSSKJ3SNIDHG5D4\" }"));

        beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcCreatorLangAware\": { \"proxy_dc_creator.def\": [ \"Trithemius, Johannes\" ] }, " +
                "\"dcTitleLangAware\": { \"proxy_dc_title.def\": [ \"De laudibus sanctissim[a]e matris Ann[a]e Johañes Tritemius\" ] }" +
                ", \"id\": \"/495/item_AVXXGPHVXBUSKT5VP6RHJNP5EFJQY3FO\" }"));

        beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcDescriptionLangAware\": { \"proxy_dc_description.def\": [ \"Alois Jirásek\" ] }, " +
                "\"dcTitleLangAware\": { \"proxy_dc_title.def\": [ \"Pan Johanes: pohádka o čtyřech jednáních\" ], \"proxy_dc_title.en\": [ \"Mr. Johanes: a fairy tale about four negotiations\" ] }, " +
                "\"id\": \"/320/uuid_74bf9a07_87de_4cbc_b580_74a8b44d4ecb\" }"));

        beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcTitleLangAware\": { \"proxy_dc_title.def\": [ \"Básník a myslitel Johanes Schlaf zemřel\" ]}, " +
                "\"id\": \"/336/uuid_37a254c6_8e08_4ec5_aef0_142209f94f8c\" }"));

        beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcDescriptionLangAware\": { \"proxy_dc_description.def\": [ \"Kamerstuk Tweede Kamer 1975-1976 kamerstuknummer 13866 ondernummer 3\" ]}," +
                " \"dcTitleLangAware\": { \"proxy_dc_title.def\": [ \"Naturalisatie van Boediono, Johanes en 24 anderen\" ] }, " +
                "\"id\": \"/9200401/BibliographicResource_1000056388705\" }"));

        beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcDescriptionLangAware\": { \"proxy_dc_description.def\": [ \"Kamerstuk Tweede Kamer 1975-1976 kamerstuknummer 13866 ondernummer 5\" ]}, " +
                "\"dcTitleLangAware\": { \"proxy_dc_title.def\": [ \"Naturalisatie van Boediono, Johanes en 24 anderen\" ] }," +
                " \"id\": \"/9200401/BibliographicResource_1000056388559\" }"));

        if (translations) {
            beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcDescriptionLangAware\": { \"proxy_dc_description.sv\": [ \"testing\" ], \"proxy_dc_description.fr\": [ \"Bonjour\" ]}, " +
                    "\"dcTitleLangAware\": { \"proxy_dc_title.nl\": [ \"Naturalisatie van Boediono, Johanes en 24 anderen\" ] , \"proxy_dc_title.fr\": [ \"French titile\" ] }," +
                    " \"id\": \"/9200401/BibliographicResource_1000056388679\" }"));

            beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcDescriptionLangAware\": { \"proxy_dc_description.sv\": [ \"Sv lang value\" ], \"proxy_dc_description.fr\": [ \"Bonjour madam\" ]}, " +
                    "\"dcTitleLangAware\": { \"proxy_dc_title.nl\": [ \"Naturalisatie van Boediono, Johanes en 24 anderen\" ] , \"proxy_dc_title.sv\": [ \"sv titile\" ] }," +
                    " \"id\": \"/9200401/BibliographicResource_100005468388679\" }"));

            beans.add(addBasicData("{ \"contentTier\": \"4\", \"dcDescriptionLangAware\": { \"proxy_dc_description.sv\": [ \"en value already present\" ], \"proxy_dc_description.en\": [ \"madam\" ]}, " +
                    "\"dcTitleLangAware\": { \"proxy_dc_title.nl\": [ \"Naturalisatie van Boediono, Johanes en 24 anderen\" ]}," +
                    " \"id\": \"/9200401/BibliographicResource_100005468388679\" }"));

        }
        return beans;

    }

    private static BriefBeanImpl addBasicData(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            BriefBeanImpl bean = mapper.readValue(json, BriefBeanImpl.class);
            return bean;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return  null;
    }

}
