package com.automic.puppet.constants;

/**
 * Exception constants used in the application
 * 
 * @author shrutinambiar
 *
 */
public class ExceptionConstants {

    public static final String UNABLE_TO_FLUSH_STREAM = "Error while flushing stream";
    public static final String INVALID_ARGS = "Improper Args. Possible cause : %s";

    public static final String INVALID_INPUT_PARAMETER = "Invalid value for parameter [%s] : [%s]";

    public static final String INVALID_FILE = "Invalid file [%s], possibly file doesn't exists";
    public static final String GENERIC_ERROR_MSG = "System Error occured.";

    public static final String ERROR_SKIPPING_CERT = "Error skipping the certificate validation";
    public static final String PUPPET_CERTIFICATE_MISSING = "Puppet certificate(s) are missing";
    public static final String INVALID_PUPPET_CERITIFCATES = "Invalid Puppet certificates";
    
    private ExceptionConstants() {
    }
}
