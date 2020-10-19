package com.automic.puppet.actions;

import javax.json.JsonObject;
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
 * This class list all the nodes for the specified node group
 * 
 * @author anuragupadhyay
 *
 */
public class TranslateNodeGroupRuleAction extends AbstractHttpAction {

    /**
     * Name of Node Group
     */
    private String nodeGroup;

    public TranslateNodeGroupRuleAction() {
        addOption("nodegroup", true, "Node group name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {

        prepareInputParameters();
        WebResource webResClient = getClient();

        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);
        String rule = new NodeGroupInfo(authToken, webResClient).getRules(nodeGroup);

        if (rule != null) {
            String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);
            WebResource webres = webResClient.path("classifier-api").path(apiVersion).path("rules").path("translate");
            ConsoleWriter.writeln("Calling URL to translate rule into query : " + webres.getURI());

            ClientResponse response = webres.accept(MediaType.APPLICATION_JSON)
                    .entity(rule.toString(), MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .post(ClientResponse.class);

            JsonObject ruleQueryObject = CommonUtil.jsonObjectResponse(response.getEntityInputStream());
            ConsoleWriter.writeln("DB_QUERY_OBJECT::=" + ruleQueryObject.toString());
        }
    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        // get node group
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

    }

}
