package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.constants.Constants;
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
     * Name of the node group
     */
    private String nodeGroup;

    /**
     * Name of the node
     */
    private String nodeName;

    public AddNewNodeAction() {
        addOption("nodename", true, "Node");
        addOption("nodegroup", true, "Node group name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        WebResource webResClient = getClient();

        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);
        try {
            String groupId = new NodeGroupInfo(authToken, webResClient).getGroupId(nodeGroup);

            String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId)
                    .path("pin");

            ConsoleWriter.writeln("Calling URL to add a node to node group: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(getNodeJson(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
        } finally {
            // revoke the token
            tHandler.logout(authToken);
        }

    }

    private void prepareInputParameters() throws AutomicException {
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

        nodeName = getOptionValue("nodename");
        PuppetValidator.checkNotEmpty(nodeName, "Node name");
    }

    protected String getNodeJson() {

        JsonArrayBuilder jsonNodeArray = Json.createArrayBuilder();
        jsonNodeArray.add(nodeName);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("nodes", jsonNodeArray);
        return json.build().toString();
    }
}
