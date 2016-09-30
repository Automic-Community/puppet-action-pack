package com.automic.puppet.actions;

import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
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
        try {
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");
        } catch (AutomicException e) {
            ConsoleWriter.writeln(e);
            throw e;
        }
        WebResource webResClient = getClient();

        ConsoleWriter.newLine();
        ConsoleWriter.writeln("**************************************************");
        ConsoleWriter.writeln("    Execution starts for action      ");
        ConsoleWriter.writeln("**************************************************");
        ConsoleWriter.newLine();

        // get auth token
        String authToken = TokenHandler.getToken(webResClient, username, password, loginApiVersion);
        if (authToken == null) {
            throw new AutomicException("Could not authenticate the user [" + username + "]");
        }

        try {

            // get group information
            String groupId = new NodeGroupInfo(authToken, webResClient, apiVersion).getGroupId(nodeGroup);

            // url to add the node to node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.newLine();
            ConsoleWriter.writeln("Calling action specific URL: " + webresource.getURI());

            ClientResponse response = webresource.accept(MediaType.APPLICATION_JSON)
                    .header("X-Authentication", authToken).entity(getEntity(), MediaType.APPLICATION_JSON)
                    .post(ClientResponse.class);
            ConsoleWriter.newLine();

            prepareOutput(response);
        } finally {
            // revoke the token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }
    }

    protected String getEntity() {
        return "";
    }

    /**
     * Prepare the output for th egiven action
     * 
     * @param response
     * @throws AutomicException
     */
    protected void prepareOutput(ClientResponse response) throws AutomicException {
        // write the node group details to job report
        ConsoleWriter.writeln("Node Group details");
        ConsoleWriter.writeln(response.getEntity(String.class));

    }

}
