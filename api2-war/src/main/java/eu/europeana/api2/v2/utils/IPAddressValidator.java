package eu.europeana.api2.v2.utils;

import java.util.regex.Pattern;

/**
 * Class to validate Ip address
 * This is temp class for the ticket EA-4053
 * Should be removed later
 */
public class IPAddressValidator {

    private static final String ipRegex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

    /**
     * Our valid IPv4 address is in the form “x.x.x.x”,
     * where each x is a number in the range 0 <= x <= 255, does not have leading zeros,
     * and is separated by a dot.
     * @param ip
     * @return
     */
    public static boolean isValidIPAddress(String ip) {
        return Pattern.compile(ipRegex).matcher(ip).matches();
    }

}
