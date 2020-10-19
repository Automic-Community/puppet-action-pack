/**
 * 
 */
package com.automic.puppet.actions;

import java.util.Map;

import javax.json.JsonArray;
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
 * @author sumitsamson This class is used to get all the node groups a node belong to
 *
 */
public class ListNodeGroupsOfNodeAction extends AbstractHttpAction {

    private String nodeName;

    public ListNodeGroupsOfNodeAction() {
        addOption("nodename", true, "Node name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();

        WebResource webResClient = getClient();

        // get auth token
        TokenHandler tokenHandler = new TokenHandler(webResClient);
        String authToken = tokenHandler.login(username);

        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);

        try {

            Map<String, String> nodeGroupMap = new NodeGroupInfo(authToken, webResClient).getNodeGroupIdAndName();

            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("classified")
                    .path("nodes").path(nodeName);
            ConsoleWriter
                    .writeln("Calling url for getting the present node groups of a node : " + webresource.getURI());

            ClientResponse response = webresource.accept(MediaType.APPLICATION_JSON)
                    .header("X-Authentication", authToken).get(ClientResponse.class);

            prepareOutput(response, nodeGroupMap);

        } finally {
            // revoke the token
            tokenHandler.logout(authToken);
        }

    }

    private void prepareOutput(ClientResponse response, Map<String, String> nodeGroupMap) {
        JsonArray jArray = CommonUtil.jsonObjectResponse(response.getEntityInputStream()).getJsonArray("groups");
        ConsoleWriter.writeln("UC4RB_PUP_NODE_GROUP_COUNT::=" + jArray.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jArray.size(); i++) {
            sb.append(nodeGroupMap.get(jArray.getString(i))).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        ConsoleWriter.writeln("UC4RB_PUP_NODE_GROUP_LIST::=" + sb.toString());
    }

    private void prepareInputParameters() throws AutomicException {
        nodeName = getOptionValue("nodename");
        PuppetValidator.checkNotEmpty(nodeName, "Node name");
    }

}
