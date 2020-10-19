package com.automic.puppet.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
     * Attaching criteria for the existing nodes
     */
    private Pattern ptrn;
    private String filter;

    public ListNodesAction() {
        addOption("operator", false, "Filter operator");
        addOption("key", false, "Filter key");
        addOption("value", false, "Filter value");
        addOption("filterjson", false, "Filter json object");
        filter = "";
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_DB_API_VERSION, Constants.DB_API_VERSION);
        WebResource webResource = getClient().path("pdb").path("query").path(apiVersion).path("nodes");

        ConsoleWriter.writeln("Using Filter " + filter);
        ConsoleWriter.writeln("Calling URL : " + webResource.getURI());

        ClientResponse response = webResource.entity(filter, MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).post(ClientResponse.class);
        prepareOutput(CommonUtil.jsonArrayResponse(response.getEntityInputStream()));
    }

    // prepare the output - list of nodes and total count
    private void prepareOutput(JsonArray jsonArray) {
        List<String> nodeList = getNodes(jsonArray);
        List<String> filterNodeList = null;
        // filtering data
        if (CommonUtil.checkNotNull(ptrn) && !nodeList.isEmpty()) {
            filterNodeList = new ArrayList<String>();
            for (String node : nodeList) {
                if (ptrn.matcher(node).matches()) {
                    filterNodeList.add(node);
                }
            }
        } else {
            filterNodeList = nodeList;
        }
        // Sorting Filter data on name
        Collections.sort(filterNodeList);

        ConsoleWriter.writeln("UC4RB_PUP_NODE_COUNT::=" + filterNodeList.size());
        ConsoleWriter.writeln("UC4RB_PUP_NODE_LIST::=" + CommonUtil.listToString(filterNodeList, ","));
    }

    private void prepareInputParameters() {
        // check if filter is provided or not
        String filterJson = getOptionValue("filterjson");
        if (CommonUtil.checkNotEmpty(filterJson)) {
            filter = filterJson;
        } else {

            String opr = getOptionValue("operator");
            String key = getOptionValue("key");
            String val = getOptionValue("value");
            if (CommonUtil.checkNotEmpty(opr) && CommonUtil.checkNotEmpty(key) && CommonUtil.checkNotEmpty(val)) {
                if ("~".equals(opr)) {
                    try {
                        ptrn = Pattern.compile(val);
                    } catch (PatternSyntaxException pe) {
                        opr = "=";
                    }
                }
                JsonArrayBuilder filterArray = Json.createArrayBuilder();
                filterArray.add(opr);
                filterArray.add(key);
                filterArray.add(val);
                filter = Json.createObjectBuilder().add("query", filterArray).build().toString();
            }
        }
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
