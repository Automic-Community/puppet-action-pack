package com.automic.puppet.actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonParsingException;

import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.validator.PuppetValidator;

/**
 * This class is responsible to add or edit variables to provided node group.
 *
 */
public class AddEditVariableToNodeGroupAction extends UpdateNodeGroupAction {

    /**
     * Name of variable
     */
    private String variableName;

    /**
     * Value of variable
     */
    private String variableValueList;

    public AddEditVariableToNodeGroupAction() {
        addOption("varaname", true, "Variable name");
        addOption("varavalue", true, "Variable value");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        actionSpecificValidation();
        super.executeSpecific();

    }

    // Validating if the given input is not empty
    private void actionSpecificValidation() throws AutomicException {
        // variable name
        variableName = getOptionValue("varaname");
        PuppetValidator.checkNotEmpty(variableName, "Variable Name");

        // value of variable
        variableValueList = getOptionValue("varavalue");
        PuppetValidator.checkNotEmpty(variableValueList, "Variable Value");
    }

    // generate json to add/edit variable to node group
    @Override
    protected String getEntity() {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        // check if the given variable value is a valid json or just a string
        JsonReader reader = Json.createReader(new StringReader(variableValueList));
        JsonStructure jsonstruct = null;

        try {
            jsonstruct = reader.read();
        } catch (JsonParsingException ex) {
        }

        if (jsonstruct == null) {
            objectBuilder.add(variableName, variableValueList);
        } else {
            objectBuilder.add(variableName, jsonstruct);
        }

        JsonObjectBuilder jsonObject = Json.createObjectBuilder();
        jsonObject.add("variables", objectBuilder);
        return jsonObject.build().toString();
    }

}
