package com.automic.puppet.actions.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Class to get the group Id based on the group name provided
 * 
 * @author shrutinambiar
 *
 */
public class NodeGroupInfo {

    private JsonArray jsonArray;

    public NodeGroupInfo(String authToken, WebResource webresource, String apiVersion) throws AutomicException {
        ClientResponse response = null;

        WebResource webres = webresource.path("classifier-api").path(apiVersion).path("groups");

        ConsoleWriter.writeln("Calling URL to get the group info : " + webres.getURI());

        response = webres.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                .get(ClientResponse.class);

        jsonArray = CommonUtil.jsonArrayResponse(response.getEntityInputStream());

    }

    /**
     * Get the group Id for a given group name
     * 
     * @param nodeGroup
     * @return
     * @throws AutomicException
     */
    public String getGroupId(String nodeGroup) throws AutomicException {
        String groupId = getNodeGroup(nodeGroup).getString("id");
        PuppetValidator.checkNotEmpty(groupId, "Node Group");
        return groupId;
    }

    /**
     * Get the List of classes in a node group
     * 
     * @param nodeGroup
     * @return
     * @throws AutomicException
     */
    public List<String> getGroupJson(String nodeGroup) throws AutomicException {
        ArrayList<String> classList = new ArrayList<>();
        Set<String> entries = getNodeGroup(nodeGroup).getJsonObject("classes").keySet();

        // iterate over all classes
        for (String className : entries) {
            classList.add(className);
        }

        return classList;

    }

    /**
     * Get the specified nodegroup json object
     * 
     * @param nodeGroup
     * @return
     * @throws AutomicException
     */
    public JsonObject getNodeGroup(String nodeGroup) throws AutomicException {
        JsonObject obj = null;
        for (int i = 0, arraySize = jsonArray.size(); i < arraySize; i++) {
            obj = jsonArray.getJsonObject(i);
            if (nodeGroup.equals(obj.getString("name"))) {
                break;
            } else {
                obj = null;
            }
        }
        PuppetValidator.checkNotNull(obj, "Node Group");
        return obj;
    }

}
