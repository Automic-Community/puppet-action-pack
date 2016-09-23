package com.automic.puppet.exception.util;

import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.exception.AutomicRuntimeException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;

/**
 * This class handles some specific cases like connection timeout/unable to connect and return the response code. In
 * addition to this, it also writes the exception message to console.
 */

public class ExceptionHandler {
    private static final int RESPONSE_NOT_OK = 1;

    private static final String ERRORMSG = "Please check the input parameters. For more details refer job report";

    private ExceptionHandler() {

    }

    /**
     * This method handles some specific cases like connection timeout/unable to connect and return the response code.
     * In addition to this, it also writes the exception message to console.
     * 
     * @throws AutomicException
     */
    public static int handleException(Exception ex) throws AutomicException {
        int responseCode = RESPONSE_NOT_OK;
        String errorMsg;
        Throwable th = ex;
        while (th.getCause() != null) {
            th = th.getCause();
        }
        if (th instanceof AutomicException || th instanceof AutomicRuntimeException) {
            errorMsg = th.getMessage();
        } else {
            ex.printStackTrace();
            errorMsg = th.getMessage();
        }

        if (CommonUtil.checkNotEmpty(errorMsg)) {
            ConsoleWriter.writeln(CommonUtil.formatErrorMessage(errorMsg));
        }
        ConsoleWriter.writeln(CommonUtil.formatErrorMessage(ERRORMSG));
        return responseCode;
    }
}
