package com.automic.puppet.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.json.JsonArray;
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

        ConsoleWriter.writeln("Calling URL : " + webResource.getURI());

        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        prepareOutput(CommonUtil.jsonArrayResponse(response.getEntityInputStream()));

    }

    // prepare the output - list of nodes and total count
    private void prepareOutput(JsonArray jsonArray) {
        List<String> nodeList = getNodes(jsonArray);
        List<String> filterNodeList = null;
        Pattern pt = null;
        // filtering data
        if (CommonUtil.checkNotEmpty(filter) && !nodeList.isEmpty()) {
            filterNodeList = new ArrayList<String>();
            try {
                pt = Pattern.compile(filter);
            } catch (PatternSyntaxException pe) {
                pt = Pattern.compile(Pattern.quote(filter));
            }
            for (String group : nodeList) {
                if (pt.matcher(group).matches()) {
                    filterNodeList.add(group);
                }
            }
        } else {
            filterNodeList = nodeList;
        }
        // preparing output of filtered data
        StringBuilder sb = new StringBuilder();
        for (String nodeGroup : filterNodeList) {
            sb.append(nodeGroup).append(",");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }

        ConsoleWriter.writeln("UC4RB_PUP_NODE_COUNT::=" + filterNodeList.size());
        ConsoleWriter.writeln("UC4RB_PUP_NODE_LIST::=" + sb.toString());

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
