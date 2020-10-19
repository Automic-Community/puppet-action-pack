package com.automic.puppet.actions;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.WebResource;

/**
 * This class is responsible to read node group variable.
 *
 */
public class ReadNodeGroupVariableAction extends AbstractHttpAction {

    /**
     * Name of Node Group
     */
    private String nodeGroup;

    /**
     * Name of variable
     */
    private String variableName;

    public ReadNodeGroupVariableAction() {
        addOption("nodegroup", true, "Node group name");
        addOption("varaname", true, "Variable name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {

        prepareInputParameters();

        WebResource webResClient = getClient();

        // generate auth token
        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);

        try {
            // get group id based on node group name
            JsonObject jsonobj = new NodeGroupInfo(authToken, webResClient).getNodeGroup(nodeGroup);
            if (jsonobj != null) {
                JsonObject variableObj = jsonobj.getJsonObject("variables");
                if (variableObj.containsKey(variableName)) {
                    JsonValue varValue = variableObj.get(variableName);
                    if (varValue.getValueType() == JsonValue.ValueType.STRING) {
                        ConsoleWriter.writeln("UC4RB_PUP_VARA_VALUE::=" + variableObj.getString(variableName));
                    } else {
                        ConsoleWriter.writeln("UC4RB_PUP_VARA_VALUE::=" + varValue);
                    }
                } else {
                    throw new AutomicException("No variable found with name [" + variableName + "]");
                }
            }
        } finally {
            // destroy token
            tHandler.logout(authToken);
        }
    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        // get node group
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

        // variable name
        variableName = getOptionValue("varaname");
        PuppetValidator.checkNotEmpty(variableName, "Variable Name");
    }
}
