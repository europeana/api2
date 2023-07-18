package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.enums.Profile;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProfileUtilsTest {

    private static String SINGLE_VALID_PROFILE_1 = "minimal";
    private static String SINGLE_VALID_PROFILE_2 = "translate";


    private static String MULTIPLE_VALID_PROFILE_1 = "minimal hits debug";
    private static String MULTIPLE_VALID_PROFILE_2 = "minimal,translate,debug,hits";
    private static String MULTIPLE_VALID_PROFILE_3 = "hits%2Cdebug%2Ctranslate";

    private static String MULTIPLE_INVALID_PROFILE = "test%2Chello pro,happy";

    private static String MULTIPLE_MIX_PROFILE_1 = "minimal,translate hits,debug  srishti%2Chello%2Ctest";
    private static String MULTIPLE_MIX_PROFILE_2 = "standard, hits,debug  %2Chello%2Ctest";


    @Test
    public void testSingleValidProfile() {
        Set<Profile> profiles = ProfileUtils.getProfiles(SINGLE_VALID_PROFILE_1);
        assertEquals(1, profiles.size());
        assertTrue(profiles.contains(Profile.MINIMAL));

        profiles.clear();

        profiles = ProfileUtils.getProfiles(SINGLE_VALID_PROFILE_2);
        assertEquals(1, profiles.size());
        assertTrue(profiles.contains(Profile.TRANSLATE));

    }

    @Test
    public void testSingleInValidProfile() {
        Set<Profile> profiles = ProfileUtils.getProfiles("test");
        assertEquals(0, profiles.size());
    }

    @Test
    public void testMultipleValidProfile_1() {
        Set<Profile> profiles = ProfileUtils.getProfiles(MULTIPLE_VALID_PROFILE_1);
        assertEquals(3, profiles.size());
        assertTrue(profiles.contains(Profile.MINIMAL));
        assertTrue(profiles.contains(Profile.HITS));
        assertTrue(profiles.contains(Profile.DEBUG));
    }

    @Test
    public void testMultipleValidProfile_2() {
        Set<Profile> profiles = ProfileUtils.getProfiles(MULTIPLE_VALID_PROFILE_2);
        assertEquals(4, profiles.size());
        assertTrue(profiles.contains(Profile.MINIMAL));
        assertTrue(profiles.contains(Profile.HITS));
        assertTrue(profiles.contains(Profile.DEBUG));
        assertTrue(profiles.contains(Profile.TRANSLATE));
    }

    @Test
    public void testMultipleValidProfile_3() {
        Set<Profile> profiles = ProfileUtils.getProfiles(MULTIPLE_VALID_PROFILE_3);
        assertEquals(3, profiles.size());
        assertTrue(profiles.contains(Profile.HITS));
        assertTrue(profiles.contains(Profile.DEBUG));
        assertTrue(profiles.contains(Profile.TRANSLATE));
    }

    @Test
    public void testMultipleInValidProfile() {
        Set<Profile> profiles = ProfileUtils.getProfiles(MULTIPLE_INVALID_PROFILE);
        assertEquals(0, profiles.size());
    }

    @Test
    public void testMultipleInValidAndValidProfile_1() {
        Set<Profile> profiles = ProfileUtils.getProfiles(MULTIPLE_MIX_PROFILE_1);
        assertEquals(4, profiles.size());
        assertTrue(profiles.contains(Profile.MINIMAL));
        assertTrue(profiles.contains(Profile.HITS));
        assertTrue(profiles.contains(Profile.DEBUG));
        assertTrue(profiles.contains(Profile.TRANSLATE));
    }

    @Test
    public void testMultipleInValidAndValidProfile_2() {
        Set<Profile> profiles = ProfileUtils.getProfiles(MULTIPLE_MIX_PROFILE_2);
        assertEquals(3, profiles.size());
        assertTrue(profiles.contains(Profile.STANDARD));
        assertTrue(profiles.contains(Profile.HITS));
        assertTrue(profiles.contains(Profile.DEBUG));
    }

}
