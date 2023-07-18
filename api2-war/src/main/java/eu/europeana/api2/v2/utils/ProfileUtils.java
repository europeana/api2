package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.enums.Profile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils class for profiles
 * @author srishti singh
 * @since 14 July 2023
 */
public class ProfileUtils {

    private void Profile() {
        //
    }

    /**
     * There are multiple ways profile can be passed.
     * It can contain -
     *        '+'    : detected as space in spring boot
     *        ','    : detected as comma
     *       '%20'   : detected as space in spring boot
     *   OR  '%252C' : detected as %2C in spring boot
     *
     * hence the pattern contains , space and %2C to split the multiple profiles
     */
    private static String multipleProfiles = "|,| |%2C|";
    private static String splitMultipleProfile = "[^,\\s%2C?]+";

    private  static final Pattern multipleProfilesPattern = Pattern.compile(multipleProfiles);
    private  static final Pattern splitProfilesPattern = Pattern.compile(splitMultipleProfile);


    /**
     * Returns the profile list from the given profile string
     *
     * @param profile
     * @return
     */
    public static Set<Profile> getProfiles(String profile) {
        Set<Profile> profiles = new HashSet<>();
        if (hasMultipleProfiles(profile)) {
            Matcher m = splitProfilesPattern.matcher(profile);
            while (m.find()) {
                addProfiles(profiles, m.group());
            }
        } else {
            addProfiles(profiles, profile);
        }
        return profiles;
    }

    /**
     * Add the given profiles to the profiles list
     * only if it's a valid profile
     *
     * @param profiles
     * @param profile
     */
    private static void addProfiles(Set<Profile> profiles, String profile) {
        if (Profile.isValid(profile)) {
            profiles.add(Profile.getValue(profile));
        }
    }

    /**
     * Checks if the profile string contains multiple profiles
     * @param profile
     * @return
     */
    public static boolean hasMultipleProfiles(String profile) {
       return multipleProfilesPattern.matcher(profile).find();
    }
}
