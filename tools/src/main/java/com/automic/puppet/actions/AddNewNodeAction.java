package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
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
 * Class to add a new node to the given node group
 * 
 * @author shrutinambiar
 *
 */
public class AddNewNodeAction extends AbstractHttpAction {

    private String nodeName;
    private String nodeGroup;

    public AddNewNodeAction() {
        addOption("nodename", true, "Node to be added");
        addOption("nodegroup", true, "Node group name to which the node is to be added");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        // get auth token
        WebResource webResClient = getClient();

        String authToken = TokenHandler.getToken(webResClient, username, password, loginApiVersion);
        if (authToken == null) {
            throw new AutomicException("Could not authenticate the user [" + username + "]");
        }

        String groupId = GetGroupId.restResponse(authToken, webResClient, nodeGroup, apiVersion);
        if (groupId == null) {
            throw new AutomicException("No group id found for [" + nodeGroup + "]");
        }

        // url to add the node to node group
        WebResource webresource = getClient().path("classifier-api").path(apiVersion).path("groups").path(groupId)
                .path("pin");

        ConsoleWriter.writeln("Calling URL to add a node to node group: " + webresource.getURI());

        webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                .entity(getNodeJson(), MediaType.APPLICATION_JSON).post(ClientResponse.class);

        // revoke the token
        TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);

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
