/**
 * 
 */
package com.automic.puppet.actions;

import java.util.Set;

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
 * This class is responsible to get a class parameters for a given node group
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

            // url to get parameters
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
            JsonObject classObj = jsonobj.getJsonObject("classes");
            if (classObj.containsKey(className)) {

                JsonObject paramObj = classObj.getJsonObject(className);
               // ConsoleWriter.writeln("UC4RB_PUP_CLASS_PARAMS::=" + paramObj);
                
                Set<String>keys = paramObj.keySet();
                StringBuilder sb = new StringBuilder();
                sb.append("UC4RB_PUP_CLASS_PARAM_COUNT::=");
                sb.append(keys.size());
                sb.append("\n");
                
                for(String key:paramObj.keySet()){
                    sb.append("UC4RB_PUP_CLASS_PARAM_"+key+"::=");
                    
                    JsonValue varValue = paramObj.get(key);
                    if (varValue.getValueType() == JsonValue.ValueType.STRING) {
                        sb.append(paramObj.getString(key));
                    } else {
                        sb.append(paramObj.get(key));
                    }
                        sb.append("\n");
                }
               
                ConsoleWriter.writeln(sb.toString());

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
