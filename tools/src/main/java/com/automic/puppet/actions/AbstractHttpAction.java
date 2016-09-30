package com.automic.puppet.actions;

import java.net.URI;
import java.net.URISyntaxException;

import com.automic.puppet.config.HttpClientConfig;
import com.automic.puppet.constants.Constants;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.filter.GenericResponseFilter;
import com.automic.puppet.util.ConsoleWriter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * This class defines the execution of any action.It provides some initializations and validations on common inputs .The
 * child actions will implement its executeSpecific() method as per their own need.
 */
public abstract class AbstractHttpAction extends AbstractAction {

    /**
     * Service end point
     */
    protected URI baseUrl;

    /**
     * Username to connect to Puppet
     */
    protected String username;

    /**
     * Option to skip validation
     */
    private String skipCertValidation;

    /**
     * Service end point
     */
    private Client client;

    public AbstractHttpAction() {
        addOption(Constants.BASE_URL, true, "Puppet URL");
        addOption(Constants.PUPPET_USERNAME, true, "Username");
        addOption(Constants.SKIP_CERT_VALIDATION, true, "Skip SSL validation");
    }

    /**
     * This method initializes the arguments and calls the execute method.
     * 
     * @throws AutomicException
     *             exception while executing an action
     */
    public final void execute() throws AutomicException {
        prepareCommonInputs();
        try {
            executeSpecific();
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }

    private void prepareCommonInputs() throws AutomicException {
        String temp = getOptionValue(Constants.BASE_URL);
        try {
            this.baseUrl = new URI(temp);
            this.username = getOptionValue(Constants.PUPPET_USERNAME);
            this.skipCertValidation = getOptionValue(Constants.SKIP_CERT_VALIDATION);
        } catch (URISyntaxException e) {
            ConsoleWriter.writeln(e);
            String msg = String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "URL", temp);
            throw new AutomicException(msg);
        }
    }

    /**
     * Method to execute the action.
     * 
     * @throws AutomicException
     */
    protected abstract void executeSpecific() throws AutomicException;

    /**
     * Method to initialize Client instance.
     * 
     * @throws AutomicException
     * 
     */
    protected WebResource getClient() throws AutomicException {
        if (client == null) {
            client = HttpClientConfig.getClient(this.skipCertValidation);
            client.addFilter(new GenericResponseFilter());
        }
        return client.resource(baseUrl);
    }

}
