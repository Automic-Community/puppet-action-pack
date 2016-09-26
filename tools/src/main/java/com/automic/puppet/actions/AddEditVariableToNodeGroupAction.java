/**
 * 
 */
package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.GetGroupId;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
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
    private String variableValue;

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
        String groupId = GetGroupId.restResponse(authToken, webResClient, nodeGroup, apiVersion);
        if (groupId == null) {
            throw new AutomicException("No group id found for [" + nodeGroup + "]");
        }
        // json object with classes
        JsonObject jsonObject = buildJson();

        // Create POST request
        WebResource webResource = getClient().path("classifier-api").path(apiVersion).path("groups").path(groupId);
        ConsoleWriter.writeln("Calling url " + webResource.getURI());
        ClientResponse response = webResource.header("X-Authentication", authToken)
                .entity(jsonObject.toString(), MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);
        // process response
        prepareOutput(response);

        // destroy token
        TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
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
            variableValue = getOptionValue("varavalue");
            PuppetValidator.checkNotEmpty(variableValue, "varavalue");
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
    private JsonObject buildJson() {

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add(variableName, variableValue);
        return Json.createObjectBuilder().add("variables", objectBuilder).build();

    }

}
