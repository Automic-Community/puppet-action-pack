package com.automic.puppet.actions;

import java.net.URI;
import java.net.URISyntaxException;

import com.automic.puppet.config.HttpClientConfig;
import com.automic.puppet.constants.Constants;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.filter.GenericResponseFilter;
import com.automic.puppet.util.CommonUtil;
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
     * version of puppet REST api to login
     */
    protected String loginApiVersion;

    /**
     * version of puppet REST api to destroy the token
     */
    protected String logoutApiVersion;

    /**
     * version of puppet REST api to perform various services
     */
    protected String apiVersion;

    /**
     * Username to connect to Puppet
     */
    protected String username;

    /**
     * Password to Puppet username
     */
    protected String password;

    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeOut;

    /**
     * Read timeout in milliseconds
     */
    private int readTimeOut;

    /**
     * Option to skip validation
     */
    private String skipCertValidation;

    /**
     * Service end point
     */
    private Client client;

    public AbstractHttpAction() {
        addOption(Constants.BASE_URL, true, "Puppet Base URL");
        addOption(Constants.PUPPET_USERNAME, true, "Puppet username");
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
            this.connectionTimeOut = CommonUtil.parseEnvIntValue(Constants.ENV_CONNECTION_TIMEOUT,
                    Constants.CONN_TIMEOUT);
            this.readTimeOut = CommonUtil.parseEnvIntValue(Constants.ENV_READ_TIMEOUT, Constants.READ_TIMEOUT);
            this.baseUrl = new URI(temp);
            this.username = getOptionValue(Constants.PUPPET_USERNAME);
            this.password = System.getenv("UC4_DECRYPTED_PWD");
            this.loginApiVersion = CommonUtil.parseEnvStringValue(Constants.ENV_LOGIN_API_VERSION,
                    Constants.LOGIN_API_VERSION);
            this.logoutApiVersion = CommonUtil.parseEnvStringValue(Constants.ENV_LOGOUT_API_VERSION,
                    Constants.LOGOUT_API_VERSION);
            this.apiVersion = CommonUtil.parseEnvStringValue(Constants.ENV_API_VERSION, Constants.API_VERSION);
            this.skipCertValidation = getOptionValue(Constants.SKIP_CERT_VALIDATION);

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
            client = HttpClientConfig.getClient(baseUrl.getScheme(), this.connectionTimeOut, this.readTimeOut,
                    this.skipCertValidation);
            client.addFilter(new GenericResponseFilter());
        }
        return client.resource(baseUrl);
    }

}
