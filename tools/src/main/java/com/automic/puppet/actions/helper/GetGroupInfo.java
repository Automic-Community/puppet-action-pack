package com.automic.puppet.actions.helper;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

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
public final class GetGroupInfo {

    /**
     * Method to get the group id
     * 
     * @param authToken
     * @param webresource
     * @param nodeGroup
     * @param apiVersion
     * @return
     * @throws AutomicException
     */
    public static JsonObject restResponse(String authToken, WebResource webresource, String nodeGroup, String apiVersion)
            throws AutomicException {
        ClientResponse response = null;

        WebResource webres = webresource.path("classifier-api").path(apiVersion).path("groups");

        ConsoleWriter.writeln("Calling URL to get the group info : " + webres.getURI());

        response = webres.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                .get(ClientResponse.class);

        return getJsonObject(CommonUtil.jsonArrayResponse(response.getEntityInputStream()), nodeGroup);

    }
    
    /**
     * Get the jsonobject of the group, if found
     * 
     * @param jsonarray
     * @param nodeGroup
     * @return
     */
    private static JsonObject getJsonObject(JsonArray jsonarray, String nodeGroup) {
        JsonObject obj = null;
        for (int i = 0, arraySize = jsonarray.size(); i < arraySize; i++) {
            obj = jsonarray.getJsonObject(i);
            if (nodeGroup.equals(obj.getString("name"))) {
                break;
            } else {
                obj = null;
            }
        }
        return obj;
    }
    
}
