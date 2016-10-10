package com.automic.puppet.actions.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.constants.Constants;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
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

    public NodeGroupInfo(String authToken, WebResource webresource) throws AutomicException {
        ClientResponse response = null;

        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);

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
        if (!CommonUtil.checkNotEmpty(groupId)) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Node Group",
                    nodeGroup));
        }
        return groupId;
    }

    /**
     * Get the List of classes in a node group
     * 
     * @param nodeGroup
     * @return
     * @throws AutomicException
     */
    public List<String> getClasses(String nodeGroup) throws AutomicException {
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
        if (!CommonUtil.checkNotNull(obj)) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Node Group",
                    nodeGroup));
        }
        return obj;
    }

    /**
     * Check if the class exist in the given node group
     * 
     * @param jsonArray
     * @param className
     * @param nodeGroup
     * @return
     * @throws AutomicException
     */
    public boolean checkClassExist(String className, String nodeGroup) throws AutomicException {
        JsonObject jsonobj = getNodeGroup(nodeGroup);
        boolean classExist = false;
        if (jsonobj != null) {
            JsonObject classobj = jsonobj.getJsonObject("classes");
            if (classobj.containsKey(className)) {
                classExist = true;
            }
        }
        return classExist;
    }

    /**
     * Get the available node groups
     * 
     * @return List of node groups
     * @throws AutomicException
     */
    public List<String> getNodeGroups() throws AutomicException {
        List<String> nodeGroups = new ArrayList<String>();
        JsonObject obj = null;
        for (int i = 0, arraySize = jsonArray.size(); i < arraySize; i++) {
            obj = jsonArray.getJsonObject(i);
            nodeGroups.add(obj.getString("name"));
        }
        return nodeGroups;
    }
    
    
    /**
     * Get the available node groups id
     * 
     * @return List of node groups
     * @throws AutomicException
     */
    public Map<String,String> getNodeGroupIdAndName() throws AutomicException {
        Map<String,String> nodeGroupMap = new HashMap<String,String>();
        JsonObject obj = null;
        for (int i = 0, arraySize = jsonArray.size(); i < arraySize; i++) {
            obj = jsonArray.getJsonObject(i);
            nodeGroupMap.put( obj.getString("id"),obj.getString("name"));
           
        }
        return nodeGroupMap;
    }

}
