package com.automic.puppet.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.config.HttpClientConfig;
import com.automic.puppet.constants.Constants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.filter.GenericResponseFilter;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class list all the nodes for the specified node group
 * 
 * @author anuragupadhyay
 *
 */
public class ListNodesForNodeGroupAction extends AbstractHttpAction {

    /**
     * Name of Node Group
     */
    private String nodeGroup;

  
    public ListNodesForNodeGroupAction() {
        addOption("nodegroup", true, "Node group name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {

        prepareInputParameters();

        WebResource webResClient = getClient();

        // generate auth token
        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);
        //
        JsonObject jsonobj = new NodeGroupInfo(authToken, webResClient).getNodeGroup(nodeGroup);
        JsonArray rule = getRule(jsonobj);
        // Calling translator URL
        JsonObject ruleQueryObject = getQueryObject(authToken, webResClient, rule);
        prepareOutput(ruleQueryObject);

    }

    private JsonObject getQueryObject(String authToken, WebResource webresource, JsonArray rule) {

        ClientResponse response = null;

        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);

        WebResource webres = webresource.path("classifier-api").path(apiVersion).path("rules").path("translate");

        ConsoleWriter.writeln("Calling URL to translate rule into query : " + webres.getURI());

        response = webres.accept(MediaType.APPLICATION_JSON).entity(rule.toString(), MediaType.APPLICATION_JSON)
                .header("X-Authentication", authToken).post(ClientResponse.class);

        JsonObject ruleQueryObject = CommonUtil.jsonObjectResponse(response.getEntityInputStream());

        return ruleQueryObject;

    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        // get node group
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

    }

    private JsonArray getRule(JsonObject jsonobj) {
        JsonArray rule = null;
        if (jsonobj != null && jsonobj.containsKey("rule")) {

            rule = jsonobj.getJsonArray("rule");
        }

        return rule;

    }

    private void prepareOutput(JsonObject ruleQueryObject) {

        ConsoleWriter.writeln("DB_QUERY_OBJECT::=" + ruleQueryObject.toString());

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
