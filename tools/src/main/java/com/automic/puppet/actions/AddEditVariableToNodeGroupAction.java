/**
 * 
 */
package com.automic.puppet.actions;

import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.GetGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.constants.ExceptionConstants;
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
    private List<String> variableValueList;

    /**
     * 
     */
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
            String variableValues = getOptionValue("varavalue");
            PuppetValidator.checkNotEmpty(variableValues, "varavalue");
            variableValueList = Arrays.asList(variableValues.split(","));
            if (variableValueList.size() == 0) {
                throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "varavalue",
                        variableValues));
            }
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
        if (variableValueList.size() == 1) {
            objectBuilder.add(variableName, variableValueList.get(0));
        } else {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (String value : variableValueList) {
                arrayBuilder.add(value);
            }
            objectBuilder.add(variableName, arrayBuilder.build());
        }
        JsonObjectBuilder jsonObject = Json.createObjectBuilder();
        jsonObject.add("variables", objectBuilder);
        return jsonObject.build().toString();

    }

}
