/**
 * 
 */
package com.automic.puppet.actions;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.GetGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
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
public class ListNodeGroupClassParamtersAction extends AbstractHttpAction {

    /**
     * Name of the node group
     */
    private String nodeGroup;

    /**
     * Name of the class
     */
    private String className;

    public ListNodeGroupClassParamtersAction() {
        addOption("nodegroup", true, "Node group name");
        addOption("classname", true, "Class name whose parameters need to be retrieved");

    }

    @Override
    protected void executeSpecific() throws AutomicException {
        WebResource webResClient = getClient();
        // get auth token
        String authToken = TokenHandler.getToken(webResClient, username, password, loginApiVersion);
        if (authToken == null) {
            throw new AutomicException("Could not authenticate the user [" + username + "]");
        }

        try {
            prepareInputParameters();

            JsonObject jsonobj = GetGroupInfo.restResponse(authToken, webResClient, nodeGroup, apiVersion);

            String groupId = CommonUtil.getGroupId(jsonobj, nodeGroup);
            if (groupId == null) {
                throw new AutomicException("No group id found for [" + nodeGroup + "]");
            }

            // url to add the node to node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.writeln("Calling URL to get parameter(s): " + webresource.getURI());

            ClientResponse response = webresource.accept(MediaType.APPLICATION_JSON)
                    .header("X-Authentication", authToken).get(ClientResponse.class);
            prepareOutput(response);
        } finally {
            // revoke the token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }

    }

    private void prepareOutput(ClientResponse response) throws AutomicException {
        JsonObject jsonobj = CommonUtil.jsonObjectResponse(response.getEntityInputStream());

        if (jsonobj != null) {
            JsonObject variableObj = jsonobj.getJsonObject("classes");
            if (variableObj.containsKey(className)) {

                JsonValue varValue = variableObj.get(className);
                if (varValue.getValueType() == JsonValue.ValueType.STRING) {
                    ConsoleWriter.writeln("UC4RB_PUP_CLASS_PARAMS::=" + variableObj.getString(className));
                } else {
                    ConsoleWriter.writeln("UC4RB_PUP_CLASS_PARAMS::=" + varValue);
                }

            } else {
                throw new AutomicException("No class found with name [" + className + "]");

            }

        }

    }

    private void prepareInputParameters() throws AutomicException {
        try {
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

            className = getOptionValue("classname");
            PuppetValidator.checkNotEmpty(className, "Class name");

        } catch (AutomicException e) {
            ConsoleWriter.write(e.getMessage());
            throw e;
        }
    }

}
