package com.automic.puppet.constants;

public class ExceptionConstants {

    public static final String UNABLE_TO_WRITE = "Error writing on console";
    public static final String UNABLE_TO_FLUSH_STREAM = "Error while flushing stream";
    public static final String INVALID_ARGS = "Improper Args. Possible cause : %s";

    public static final String INVALID_INPUT_PARAMETER = "Invalid value for parameter [%s] : [%s]";

    public static final String SSLCONTEXT_ERROR = "Unable to build secured context.";
    public static final String INVALID_KEYSTORE = "Invalid KeyStore.";

    public static final String ERROR_READING_FILE = "Error occured during reading of file [%s],probably"
            + " file doesn't exists ";
    public static final String ERROR_SKIPPING_CERT = "Error skipping the certificate validation";

    private ExceptionConstants() {
    }
}
