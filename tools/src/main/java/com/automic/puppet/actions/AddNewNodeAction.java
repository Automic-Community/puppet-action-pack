package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.GetAuthToken;
import com.automic.puppet.actions.helper.GetGroupId;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

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

        String authToken = GetAuthToken.restResponse(getClient(), username, password, apiVersion);
        WebResource webresource = getClient().path("classifier-api").path(apiVersion).path("groups")
                .path(GetGroupId.restResponse(authToken, getClient(), nodeGroup, nodeName, apiVersion)).path("pin");

        ConsoleWriter.writeln("Calling URL : " + webresource.getURI());

        webresource.header("X-Authentication", authToken).entity(getNodeJson(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
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

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("nodes", nodeName);
        return json.build().toString();
    }
}
