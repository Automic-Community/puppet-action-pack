/**
 * 
 */
package com.automic.puppet.actions;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
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
        addOption("classname", true, "Class name");

    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();

        WebResource webResClient = getClient();

        // generate auth token
        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);

        try {

            JsonObject ngJson = new NodeGroupInfo(authToken, webResClient).getNodeGroup(nodeGroup);

            prepareOutput(ngJson);

        } finally {
            // destroy token
            tHandler.logout(authToken);
        }

    }

    private void prepareOutput(JsonObject ngJson) throws AutomicException {
        if (ngJson != null) {
            JsonObject classObj = ngJson.getJsonObject("classes");
            if (classObj.containsKey(className)) {

                JsonObject paramObj = classObj.getJsonObject(className);

                Set<String> keys = paramObj.keySet();
                StringBuilder sb = new StringBuilder();
                sb.append("UC4RB_PUP_CLASS_PARAM_COUNT::=");
                sb.append(keys.size());
                sb.append("\n");

                for (String key : paramObj.keySet()) {
                    sb.append("UC4RB_PUP_CLASS_PARAM_" + key + "::=");

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
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

        className = getOptionValue("classname");
        PuppetValidator.checkNotEmpty(className, "Class name");
    }

}
