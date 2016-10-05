package com.automic.puppet.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.constants.Constants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class is used to get all the existing nodes.
 * 
 * @author shrutinambiar
 *
 */
public class ListNodesAction extends AbstractHttpAction {

    /**
     * Atching criteria for the existing nodes
     */
    private String filter;

    public ListNodesAction() {
        addOption("filter", false, "Filter");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        
        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_DB_API_VERSION, Constants.DB_API_VERSION);
        
        WebResource webResource = getClient().path("pdb").path("query").path(apiVersion).path("nodes");

        // check if filter is provided or not
        filter = getOptionValue("filter");
        if (CommonUtil.checkNotEmpty(filter)) {

            // create json array ["~","certname","<filter>"]
            JsonArrayBuilder json = Json.createArrayBuilder();
            json.add("~");
            json.add("certname");
            json.add(filter);

            webResource = webResource.queryParam("query", json.build().toString());
        }

        ConsoleWriter.writeln("Calling URL : " + webResource.getURI());

        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        prepareOutput(CommonUtil.jsonArrayResponse(response.getEntityInputStream()));

    }

    // prepare the output - list of nodes and total count
    private void prepareOutput(JsonArray jsonArray) {
        List<String> nodeList = getNodes(jsonArray);

        ConsoleWriter.writeln("UC4RB_PUP_NODE_COUNT::=" + nodeList.size());
        String list = nodeList.toString();
        ConsoleWriter.writeln("UC4RB_PUP_NODE_LIST::=" + list.substring(1, list.length() - 1));
    }

    // get the list of nodes
    private List<String> getNodes(JsonArray jsonArray) {
        ArrayList<String> nodeList = new ArrayList<>();

        if (CommonUtil.checkNotNull(jsonArray)) {
            JsonObject obj = null;

            for (int i = 0, size = jsonArray.size(); i < size; i++) {
                obj = jsonArray.getJsonObject(i);
                Set<String> entries = obj.keySet();
                for (String node : entries) {
                    if ("certname".equalsIgnoreCase(node)) {
                        nodeList.add(obj.getString("certname"));
                    }
                }
            }

        }
        return nodeList;
    }
}
