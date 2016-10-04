package com.automic.puppet.actions;

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
 * This action is used to update the node group according to the provided json
 * 
 * @author shrutinambiar
 *
 */
public class UpdateNodeGroupAction extends AbstractHttpAction {

    /**
     * Name of the node group
     */
    protected String nodeGroup;

    public UpdateNodeGroupAction() {
        addOption("nodegroup", true, "Node group name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

        WebResource webResClient = getClient();

        // get auth token
        TokenHandler tokenHandler = new TokenHandler(webResClient);
        String authToken = tokenHandler.login(username);

        try {
            // get group information
            String groupId = new NodeGroupInfo(authToken, webResClient).getGroupId(nodeGroup);

            String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);

            // url to add the node to node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.newLine();
            ConsoleWriter.writeln("Calling action specific URL: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(getEntity(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
            ConsoleWriter.newLine();
        } finally {
            // revoke the token
            tokenHandler.logout(authToken);
        }
    }

    protected String getEntity() {
        return "";
    }
}
