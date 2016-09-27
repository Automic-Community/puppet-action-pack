/**
 * 
 */
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
 * This class is responsible to add or edit variables to provided node group.
 *
 */
public class AddEditVariableToNodeGroupAction extends AbstractHttpAction {

    /**
     * Name of Node Group
     */
    private String nodeGroup;

    /**
     * Name of variable
     */
    private String variableName;

    /**
     * Value of variable
     */
    private String variableValueList;

    public AddEditVariableToNodeGroupAction() {
        addOption("nodegroup", true, "Name of the node group");
        addOption("varaname", true, "Name of variable to add/edit");
        addOption("varavalue", true, "Value of variable");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        WebResource webResClient = getClient();
        // generate auth token
        String authToken = TokenHandler.getToken(webResClient, username, password, apiVersion);
        // get group id based on node group name

        try {

            JsonObject jsonobj = GetGroupInfo.restResponse(authToken, webResClient, nodeGroup, apiVersion);

            String groupId = CommonUtil.getGroupId(jsonobj, nodeGroup);
            if (groupId == null) {
                throw new AutomicException("No group id found for [" + nodeGroup + "]");
            }

            // Create POST request
            WebResource webResource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);
            ConsoleWriter.writeln("Calling url " + webResource.getURI());
            ClientResponse response = webResource.header("X-Authentication", authToken)
                    .entity(buildJson(), MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                    .post(ClientResponse.class);
            // process response
            prepareOutput(response);
        } finally {
            // destroy token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }
    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        try {
            // get node group
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

            // variable name
            variableName = getOptionValue("varaname");
            PuppetValidator.checkNotEmpty(variableName, "varaname");

            // value of variable
            variableValueList = getOptionValue("varavalue");
            PuppetValidator.checkNotEmpty(variableValueList, "varavalue");

        } catch (AutomicException e) {
            ConsoleWriter.writeln(e);
            throw e;
        }
    }

    // process response and print the response in the job report
    private void prepareOutput(ClientResponse response) throws AutomicException {
        // write the node group details to job report
        ConsoleWriter.writeln("Node Group details");
        ConsoleWriter.writeln(response.getEntity(String.class));
    }

    // generate json to add/edit variable to node group
    private String buildJson() {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        // check if the given variable value is a valid json or just a string
        JsonReader reader = Json.createReader(new StringReader(variableValueList));
        JsonStructure jsonstruct = null;

        try {
            jsonstruct = reader.read();
        } catch (JsonParsingException ex) {
        }

        if (jsonstruct == null) {
            objectBuilder.add(variableName, variableValueList);
        } else {
            objectBuilder.add(variableName, jsonstruct);
        }

        JsonObjectBuilder jsonObject = Json.createObjectBuilder();
        jsonObject.add("variables", objectBuilder);
        return jsonObject.build().toString();

    }

}
