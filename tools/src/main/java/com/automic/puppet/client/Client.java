package com.automic.puppet.client;

import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.exception.AutomicRuntimeException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;

/**
 * Main Class is the insertion point of Puppet interaction api when called from AE implementation. It delegates the
 * parameters to appropriate action and returns a response code based on output of action.
 *
 * Following response code are returned by java program 0 - Successful response from Puppet API, 1 - An exception
 * occurred/Error in response from Puppet API, 2 - Connection timeout while calling Puppet API
 *
 */

public final class Client {

    private static final int RESPONSE_OK = 0;
    private static final int RESPONSE_NOT_OK = 1;

    private static final String ERRORMSG = "Please check the input parameters.";

    private Client() {
    }

    /**
     * Main method which will start the execution of an action on Puppet. This method will call the
     * ClientHelper class which will trigger the execution of specific action and then if action fails this main method
     * will handle the failed scenario and print the error message and system will exit with the respective response
     * code.
     *
     * @param params
     *            array of parameters
     */
    public static void main(String[] params) {
        ConsoleWriter.writeln("****** Execution starts ******");
        int responseCode = RESPONSE_NOT_OK;        
        try {
            ClientHelper.executeAction(params);
            responseCode = RESPONSE_OK;
        } catch (AutomicException | AutomicRuntimeException e) {
            ConsoleWriter.writeln(CommonUtil.formatErrorMessage(e.getMessage()));
            ConsoleWriter.writeln(CommonUtil.formatErrorMessage(ERRORMSG));
        } catch (Exception e) {
            ConsoleWriter.writeln(e);            
            ConsoleWriter.writeln(CommonUtil.formatErrorMessage(ERRORMSG));
        }        
        ConsoleWriter.writeln("****** Execution ends with response code : " + responseCode);
        ConsoleWriter.flush();
        System.exit(responseCode);
    }
}
