package com.automic.puppet.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

public class ListNodesForNodeGroupAction extends AbstractHttpAction {

    /**
     * Atching criteria for the existing nodes
     */
    /**
     * Name of Node Group
     */
    private String nodeGroup;

    /**
     * Name of variable
     */
    private String dbURL;

    /**
     * Service end point
     */
    private Client client;

    public ListNodesForNodeGroupAction() {
        addOption("nodegroup", true, "Node group name");
        addOption("dburl", true, "Puppet DB url");
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
        // calling puppet db to resolve Query
        JsonArray jsonArray = getNodes(authToken, ruleQueryObject);
        prepareOutput(jsonArray);

    }

    /**
     * Method to initialize Client instance.
     * 
     * @throws AutomicException
     * 
     */
    private WebResource getDbClient() throws AutomicException {
        if (client == null) {
            client = HttpClientConfig.getClient(this.skipCertValidation);
            client.addFilter(new GenericResponseFilter());
        }
        return client.resource(dbURL);
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

    private JsonArray getNodes(String authToken, JsonObject ruleQueryObject) throws AutomicException {

        String dbApiVersion = CommonUtil.getEnvParameter(Constants.ENV_DB_API_VERSION, Constants.DB_API_VERSION);

        WebResource webResource = getDbClient().path("pdb").path("query").path(dbApiVersion).path("nodes");

        ConsoleWriter.writeln("Calling URL : " + webResource.getURI());

        ClientResponse dbApiResponse = webResource.accept(MediaType.APPLICATION_JSON)
                .entity(ruleQueryObject.toString(), MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                .post(ClientResponse.class);

        JsonArray jsonArray = CommonUtil.jsonArrayResponse(dbApiResponse.getEntityInputStream());
        return jsonArray;

    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        // get node group
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

        // variable name
        dbURL = getOptionValue("dburl");
        PuppetValidator.checkNotEmpty(dbURL, "DB URL");
    }

    private JsonArray getRule(JsonObject jsonobj) {
        JsonArray rule = null;
        if (jsonobj != null && jsonobj.containsKey("rule")) {

            rule = jsonobj.getJsonArray("rule");
        }

        return rule;

    }

    // prepare the output - list of nodes and total count
    private void prepareOutput(JsonArray jsonArray) {
        List<String> nodeList = getNodes(jsonArray);

        // preparing output of filtered data
        StringBuilder sb = new StringBuilder();
        for (String nodeGroup : nodeList) {
            sb.append(nodeGroup).append(",");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }

        ConsoleWriter.writeln("UC4RB_PUP_NODE_COUNT::=" + nodeList.size());
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
