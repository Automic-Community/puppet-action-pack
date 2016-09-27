package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
 * Class to add a new node to the given node group
 * 
 * @author shrutinambiar
 *
 */
public class AddNewNodeAction extends AbstractHttpAction {

    /**
     * Name of the node
     */
    private String nodeName;

    /**
     * Name of the node group
     */
    private String nodeGroup;

    public AddNewNodeAction() {
        addOption("nodename", true, "Node to be added");
        addOption("nodegroup", true, "Node group name to which the node is to be added");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        // get auth token
        WebResource webResClient = getClient();

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
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId)
                    .path("pin");

            ConsoleWriter.writeln("Calling URL to add a node to node group: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(getNodeJson(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
        } finally {
            // revoke the token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }

    }

    private void prepareInputParameters() throws AutomicException {
        try {
            nodeName = getOptionValue("nodename");
            PuppetValidator.checkNotEmpty(nodeName, "Node name");

            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

        } catch (AutomicException e) {
            ConsoleWriter.write(e.getMessage());
            throw e;
        }
    }

    private String getNodeJson() {

        JsonArrayBuilder jsonNodeArray = Json.createArrayBuilder();
        jsonNodeArray.add(nodeName);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("nodes", jsonNodeArray);
        return json.build().toString();
    }
}
