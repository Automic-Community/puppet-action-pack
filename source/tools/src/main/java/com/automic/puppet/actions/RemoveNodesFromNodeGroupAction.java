/**
 * 
 */
package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.constants.Constants;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class is responsible to remove multiple nodes from provided node group.
 *
 */
public class RemoveNodesFromNodeGroupAction extends AbstractHttpAction {

    /**
     * list of classes
     */
    private String[] nodeList;

    /**
     * Name of the node group
     */
    private String nodeGroup;

    /**
     * 
     */
    public RemoveNodesFromNodeGroupAction() {
        addOption("nodegroup", true, "Node group name");
        addOption("nodes", true, "Nodes");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        WebResource webResClient = getClient();

        // get auth token
        TokenHandler tokenHandler = new TokenHandler(webResClient);
        String authToken = tokenHandler.login(username);

        try {
            // get group information
            String groupId = new NodeGroupInfo(authToken, webResClient).getGroupId(nodeGroup);

            String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);

            // url to remove the nodes to node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId)
                    .path("unpin");

            ConsoleWriter.writeln("Calling action specific URL: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(getEntity(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
        } finally {
            // revoke the token
            tokenHandler.logout(authToken);
        }
    }

    // validate the inputs
    private void prepareInputParameters() throws AutomicException {
        // validate node group
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");
        // nodes
        String nodes = getOptionValue("nodes");
        PuppetValidator.checkNotEmpty(nodes, "Nodes");
        nodeList = CommonUtil.splitAndTrimSpace(nodes, ",");
        if (nodeList.length == 0) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Nodes", nodes));
        }
    }

    // get json entity
    protected String getEntity() {

        JsonArrayBuilder jsonarrayBuilder = Json.createArrayBuilder();
        for (String nodename : nodeList) {
            jsonarrayBuilder.add(nodename);
        }
        JsonObjectBuilder nodesJson = Json.createObjectBuilder();
        nodesJson.add("nodes", jsonarrayBuilder);
        return nodesJson.build().toString();
    }

}
