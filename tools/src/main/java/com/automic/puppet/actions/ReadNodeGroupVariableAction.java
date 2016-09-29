/**
 * 
 */
package com.automic.puppet.actions;

import javax.json.JsonObject;
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
    private boolean endSuccessfully;

    public ReadNodeGroupVariableAction() {
        addOption("nodegroup", true, "Name of the node group");
        addOption("varaname", true, "Name of the variable to read from given node group");
        addOption("endSuccessfully", true, "End successfully when variable not found");
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

            // Create GET request
            WebResource webResource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);
            ConsoleWriter.writeln("Calling url " + webResource.getURI());
            ClientResponse response = webResource.header("X-Authentication", authToken)
                    .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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

            endSuccessfully = "YES".equalsIgnoreCase(getOptionValue("endSuccessfully")) ? true : false;

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
                ConsoleWriter.writeln("UC4RB_PUP_VARA_VALUE::=" + variableObj.get(variableName));

            } else {
                if (!endSuccessfully) {
                    throw new AutomicException("No variable found with name [" + variableName + "]");
                }
                ConsoleWriter.writeln("[INFO] No variable found with name [" + variableName + "]");
            }

        }

    }

}
