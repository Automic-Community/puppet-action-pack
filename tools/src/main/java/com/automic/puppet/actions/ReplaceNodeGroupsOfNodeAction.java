/**
 * 
 */
package com.automic.puppet.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
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
 * @author sumitsamson
 *
 */
public class ReplaceNodeGroupsOfNodeAction extends AbstractHttpAction {

    private String nodeName;
    private String[] nodeGroupNameList;
    private Map<String, String> nodeGroupMap;

    public ReplaceNodeGroupsOfNodeAction() {
        addOption("nodename", true, "Node");
        addOption("nodegroups", true, "Node group name");
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
            nodeGroupMap = new NodeGroupInfo(authToken, webResClient).getNodeGroupIdAndName();
            // 1 get groups for a given node
            List<String> unpinNodeGroupIdList = getPresentGroupList(webResClient, authToken);

            // 2 get the ids for the user provided node group names
            List<String> pinNodeGroupIdList = getIdForUserProvidedGroups();

            // 3 compare the newNodeGroupList with the presentNodeGroupList
            updatePinAndUnpinLists(unpinNodeGroupIdList, pinNodeGroupIdList);

            String nodeJsonString = getNodeJson();

            // 4 un-pin the groups
            if (unpinNodeGroupIdList.size() > 0) {
                for (String groupId : unpinNodeGroupIdList) {
                    WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups")
                            .path(groupId).path("unpin");
                    ConsoleWriter.writeln("Calling action specific URL: " + webresource.getURI());
                    webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                            .entity(nodeJsonString, MediaType.APPLICATION_JSON).post(ClientResponse.class);
                }
            }

            // 5 pin the groups
            if (pinNodeGroupIdList.size() > 0) {
                for (String groupId : pinNodeGroupIdList) {
                    WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups")
                            .path(groupId).path("pin");
                    ConsoleWriter.writeln("Calling action specific URL: " + webresource.getURI());
                    webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                            .entity(nodeJsonString, MediaType.APPLICATION_JSON).post(ClientResponse.class);
                }
            }
        } finally {
            // revoke the token
            tokenHandler.logout(authToken);
        }
    }

    private void updatePinAndUnpinLists(List<String> unpinNodeGroupIdList, List<String> pinNodeGroupIdList) {
        for (String groupName : new ArrayList<String>(pinNodeGroupIdList)) {
            if (unpinNodeGroupIdList.contains(groupName)) {
                pinNodeGroupIdList.remove(groupName);
                unpinNodeGroupIdList.remove(groupName);
            }
        }
        ConsoleWriter.writeln("Nodes to be removed " + getNodeGroupNames(unpinNodeGroupIdList));
        ConsoleWriter.writeln("Nodes to be added " + getNodeGroupNames(pinNodeGroupIdList));
    }

    private List<String> getIdForUserProvidedGroups() throws AutomicException {
        List<String> newNodeGroupIdList = new ArrayList<>();
        for (String groupName : nodeGroupNameList) {
            boolean found = false;
            for (String key : nodeGroupMap.keySet()) {
                if (groupName.equals(nodeGroupMap.get(key))) {
                    newNodeGroupIdList.add(key);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER,
                        "Node group name ", groupName));
            }
        }
        return newNodeGroupIdList;
    }

    private void prepareInputParameters() throws AutomicException {

        nodeName = getOptionValue("nodename");
        PuppetValidator.checkNotEmpty(nodeName, "Node name");

        // nodes
        String nodeGroups = getOptionValue("nodegroups");
        PuppetValidator.checkNotEmpty(nodeGroups, "Node groups");
        nodeGroupNameList = CommonUtil.splitAndTrimSpace(nodeGroups, ",");
        if (nodeGroupNameList.length == 0) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Node group(s)",
                    nodeGroups));
        }

    }

    // get json entity
    private String getNodeJson() {
        JsonArrayBuilder jsonarrayBuilder = Json.createArrayBuilder();
        jsonarrayBuilder.add(nodeName);
        JsonObjectBuilder nodesJson = Json.createObjectBuilder();
        nodesJson.add("nodes", jsonarrayBuilder);
        return nodesJson.build().toString();
    }

    private List<String> getPresentGroupList(WebResource webResClient, String authToken) {
        List<String> presentNodeGroupList = new ArrayList<String>();

        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);
        WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("classified").path("nodes")
                .path(nodeName);
        ConsoleWriter.writeln("Calling url for getting the present node groups of a node : " + webresource.getURI());

        ClientResponse response = webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                .get(ClientResponse.class);

        JsonArray jArray = CommonUtil.jsonObjectResponse(response.getEntityInputStream()).getJsonArray("groups");

        for (int i = 0; i < jArray.size(); i++) {
            presentNodeGroupList.add(jArray.getString(i));
        }
        ConsoleWriter.writeln("[INFO] Current Node Group Names ::= " + getNodeGroupNames(presentNodeGroupList));
        return presentNodeGroupList;
    }

    private String getNodeGroupNames(List<String> list) {
        StringBuilder temp = new StringBuilder();
        for (String grpId : list) {
            temp.append(nodeGroupMap.get(grpId)).append(",");
        }
        if (temp.length() > 0) {
            temp.setLength(temp.length() - 1);
        }
        return temp.toString();
    }

}
