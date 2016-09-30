package com.automic.puppet.actions;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class is responsible to read node group variable.
 *
 */
public class ReadNodeGroupVariableAction extends AbstractHttpAction {

    /**
     * Name of Node Group
     */
    private String nodeGroup;

    /**
     * Name of variable
     */
    private String variableName;

    public ReadNodeGroupVariableAction() {
        addOption("nodegroup", true, "Name of the node group");
        addOption("varaname", true, "Name of the variable to read from given node group");
    }

    @Override
    protected void executeSpecific() throws AutomicException {

        prepareInputParameters();

        WebResource webResClient = getClient();

        ConsoleWriter.newLine();
        ConsoleWriter.writeln("**************************************************");
        ConsoleWriter.writeln("    Execution starts for action      ");
        ConsoleWriter.writeln("**************************************************");
        ConsoleWriter.newLine();

        // generate auth token
        String authToken = TokenHandler.getToken(webResClient, username, password, apiVersion);
        if (authToken == null) {
            throw new AutomicException("Could not authenticate the user [" + username + "]");
        }

        try {
            // get group id based on node group name
            String groupId = new NodeGroupInfo(authToken, webResClient, apiVersion).getGroupId(nodeGroup);

            WebResource webResource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);
            ConsoleWriter.writeln("Calling url " + webResource.getURI());
            ClientResponse response = webResource.header("X-Authentication", authToken)
                    .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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

        } catch (AutomicException e) {
            ConsoleWriter.writeln(e);
            throw e;
        }
    }

    // process response and print the response in the job report
    private void prepareOutput(ClientResponse response) throws AutomicException {
        // write the node group variable details to job report
        JsonObject jsonobj = CommonUtil.jsonObjectResponse(response.getEntityInputStream());

        if (jsonobj != null) {
            JsonObject variableObj = jsonobj.getJsonObject("variables");
            if (variableObj.containsKey(variableName)) {

                JsonValue varValue = variableObj.get(variableName);
                if (varValue.getValueType() == JsonValue.ValueType.STRING) {
                    ConsoleWriter.writeln("UC4RB_PUP_VARA_VALUE::=" + variableObj.getString(variableName));
                } else {
                    ConsoleWriter.writeln("UC4RB_PUP_VARA_VALUE::=" + varValue);
                }

            } else {
                throw new AutomicException("No variable found with name [" + variableName + "]");

            }

        }

    }

}
