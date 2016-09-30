package com.automic.puppet.constants;

/**
 * Constants class contains all the constants.
 *
 */
public class Constants {

    public static final int MINUS_ONE = -1;
    public static final int ZERO = 0;

    public static final String ENV_CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";
    public static final String ENV_READ_TIMEOUT = "READ_TIMEOUT";
    public static final String ENV_LOGIN_API_VERSION = "ENV_LOGINAPI_VERSION";
    public static final String ENV_LOGOUT_API_VERSION = "ENV_LOGOUTAPI_VERSION";
    public static final String ENV_API_VERSION = "ENV_API_VERSION";

    public static final int CONN_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 60000;
    public static final String LOGIN_API_VERSION = "v1";
    public static final String LOGOUT_API_VERSION = "v2";
    public static final String API_VERSION = "v1";

    public static final int IO_BUFFER_SIZE = 4 * 1024;
    public static final String ACTION = "action";
    public static final String HELP = "help";

    public static final String HTTPS = "https";
    public static final String BASE_URL = "baseurl";
    public static final String PUPPET_USERNAME = "username";
    public static final String PUPPET_PASSWORD = "password";
    public static final String SKIP_CERT_VALIDATION = "skipvalidation";

    public static final String YES = "YES";
    public static final String TRUE = "TRUE";
    public static final String ONE = "1";

    public static final String IGNORE_CHECK = "IGNORE-CHECK";

    private Constants() {
    }
}
