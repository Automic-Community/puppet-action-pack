/**
 *
 */
package com.automic.puppet.actions;

import java.net.URI;
import java.net.URISyntaxException;

import com.automic.puppet.config.HttpClientConfig;
import com.automic.puppet.constants.Constants;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.filter.GenericResponseFilter;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

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
    private String username;

    /**
     * Password to Puppet username
     */
    private String password;

    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeOut;

    /**
     * Read timeout in milliseconds
     */
    private int readTimeOut;

    /**
     * Service end point
     */
    private Client client;


    public AbstractHttpAction() {
        addOption(Constants.READ_TIMEOUT, true, "Read timeout");
        addOption(Constants.CONNECTION_TIMEOUT, true, "connection timeout");
        addOption(Constants.BASE_URL, true, "Base URL of Puppet classifier");
        addOption(Constants.PUPPET_USERNAME, true, "Username for Puppet Authentication");
        addOption(Constants.PUPPET_PASSWORD, true, "Password for Puppet user");
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
            this.connectionTimeOut = CommonUtil.parseStringValue(getOptionValue(Constants.CONNECTION_TIMEOUT),
                    Constants.MINUS_ONE);
            PuppetValidator.lessThan(connectionTimeOut, Constants.ZERO, "Connect Timeout");
            this.readTimeOut = CommonUtil.parseStringValue(getOptionValue(Constants.READ_TIMEOUT), Constants.MINUS_ONE);
            PuppetValidator.lessThan(readTimeOut, Constants.ZERO, "Read Timeout");
            this.baseUrl = new URI(temp);
            this.username = getOptionValue(Constants.PUPPET_USERNAME);
            this.password = getOptionValue(Constants.PUPPET_PASSWORD);
        } catch (AutomicException e) {
            throw e;
        } catch (URISyntaxException e) {
            String msg = String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "URL", temp);
            throw new AutomicException(msg, e);
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
            client = HttpClientConfig.getClient(baseUrl.getScheme(), this.connectionTimeOut, this.readTimeOut);
            client.addFilter(new HTTPBasicAuthFilter(username, password));
            client.addFilter(new GenericResponseFilter());
        }
        return client.resource(baseUrl);
    }

}
