package eu.europeana.api2.v2.web.controller;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum class to handle I18n error keys for Authroization
 */
public enum I18nErrorMessageKeys {

    //error keys for the exceptions messages
    ERROR_APIKEY_EMPTY( "error.empty_apikey", "Empty API key provided!"),
    ERROR_OPERATION_NOT_AUTHORISED("error.operation_not_authorized", "The user is not authorized to perform the given operation!"),
    ERROR_INVALID_APIKEY ("error.invalid_apikey", "Invalid API key provided!"),
    ERROR_MISSING_APIKEY("error.missing_apikey" , "The apiKey must be provided in the request!"),
    ERROR_INVALID_JWT_TOKEN( "error.invalid_jwttoken" , "Invalid jwt token!");

    private final String In8Key;
    private final String message;

    I18nErrorMessageKeys(String in8Key, String message) {
        In8Key = in8Key;
        this.message = message;
    }

    /**
     * Returns the error messages for the Key
     *
     * @param in8Key
     * @return
     */
    public static String getMessageForKey(String in8Key) {
        for (I18nErrorMessageKeys e : values()) {
            if (StringUtils.equalsIgnoreCase(e.In8Key, in8Key)) {
                return e.message;
            }
        }
        return null;
    }

}