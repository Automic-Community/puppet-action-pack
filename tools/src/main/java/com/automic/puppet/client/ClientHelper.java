package com.automic.puppet.client;

import com.automic.puppet.actions.AbstractAction;
import com.automic.puppet.cli.Cli;
import com.automic.puppet.cli.CliOptions;
import com.automic.puppet.constants.Constants;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;

/**
 * Helper class to delegate request to specific Action based on input arguments .
 * */
public class ClientHelper {

    private static final String ABSOLUTEPATH = "com.automic.puppet.actions";

    private ClientHelper() {
    }

    /**
     * Method to delegate parameters to an instance of {@link AbstractAction} based on the value of Action parameter.
     *
     * @param actionParameters
     *            of options with key as option name and value is option value
     * @throws AutomicException
     */

    public static void executeAction(String[] actionParameters) throws AutomicException {
        String actionName = new Cli(new CliOptions(), actionParameters).getOptionValue(Constants.ACTION);

        AbstractAction action = null;
        try {
            Class<?> classDefinition = Class.forName(getCanonicalName(actionName));
            action = (AbstractAction) classDefinition.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            String msg = String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, Constants.ACTION, actionName);
            throw new AutomicException(msg, e);
        }
        action.executeAction(actionParameters);
    }

    private static String getCanonicalName(String clsName) {
        return ABSOLUTEPATH + "." + clsName;
    }
}
