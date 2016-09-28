package com.automic.puppet.actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.GetGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This action adds or edits a parameter to the given class in a noge group if it exist.
 * 
 * @author anuragupadhyay
 *
 */
public class AddClassParameterAction extends AbstractHttpAction {

    /**
     * Name of the node group
     */
    private String nodeGroup;

    /**
     * Name of the class to which parameter will be added
     */
    private String className;

    /**
     * Class parameter to be add
     */
    private String classParameter;

    /**
     * Parameter value for the given class parameter.
     */
    private String paramValues;

    public AddClassParameterAction() {
        addOption("nodegroup", true, "Node group name");
        addOption("classname", true, "Class name to add/edit the parameter");
        addOption("classparam", true, "Parameter to be added/updated");
        addOption("paramvalue", true, "Value of the parameter");
    }

    protected void executeSpecific() throws AutomicException {
        WebResource webResClient = getClient();
        // get auth token
        String authToken = TokenHandler.getToken(webResClient, username, password, loginApiVersion);
        if (authToken == null) {
            throw new AutomicException("Could not authenticate the user [" + username + "]");
        }

        try {
            prepareInputParameters();

            JsonObject jsonobj = GetGroupInfo.restResponse(authToken, webResClient, nodeGroup, apiVersion);

            String groupId = CommonUtil.getGroupId(jsonobj, nodeGroup);
            if (groupId == null) {
                throw new AutomicException("No group id found for [" + nodeGroup + "]");
            }

            // url to add the node to node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.writeln("Calling URL to add/edit a parameter: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(getNodeJson(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
        } finally {
            // revoke the token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }

    }

    private String getNodeJson() {

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        // check if the given parameter value is a valid json or just a string
        JsonReader reader = Json.createReader(new StringReader(paramValues));
        JsonStructure jsonstruct = null;

        try {
            jsonstruct = reader.read();
        } catch (JsonParsingException ex) {
        }

        if (jsonstruct == null) {
            objectBuilder.add(classParameter, paramValues);
        } else {
            objectBuilder.add(classParameter, jsonstruct);
        }

        JsonObjectBuilder jsonParamField = Json.createObjectBuilder();
        jsonParamField.add(className, objectBuilder);

        JsonObjectBuilder jsonClass = Json.createObjectBuilder();
        jsonClass.add("classes", jsonParamField);

        return jsonClass.build().toString();
    }

    private void prepareInputParameters() throws AutomicException {
        try {
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

            className = getOptionValue("classname");
            PuppetValidator.checkNotEmpty(className, "Class name");

            classParameter = getOptionValue("classparam");
            PuppetValidator.checkNotEmpty(classParameter, "Class name");

            paramValues = getOptionValue("paramvalue");
            PuppetValidator.checkNotEmpty(paramValues, "Class name");

        } catch (AutomicException e) {
            ConsoleWriter.write(e.getMessage());
            throw e;
        }
    }

}
