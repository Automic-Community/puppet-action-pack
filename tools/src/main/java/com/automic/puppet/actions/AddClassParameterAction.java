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
 * This action adds or edits a parameter to the given class in a noge group if it exist.
 * 
 * @author anuragupadhyay
 *
 */
public class AddClassParameterAction extends UpdateNodeGroupAction {

    /**
     * Name of the class to which parameter will be added
     */
    private String className;

    /**
     * Class parameter to be add
     */
    private String classParameter;

    /**
     * Parameter value for the given class parameter.
     */
    private String paramValues;

    public AddClassParameterAction() {
        addOption("classname", true, "Class");
        addOption("classparam", true, "Parameter");
        addOption("paramvalue", true, "Value");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        actionSpecificValidation();
        super.executeSpecific();

    }

    @Override
    protected String getEntity() {

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        // check if the given parameter value is a valid json or just a string
        JsonReader reader = Json.createReader(new StringReader(paramValues));
        JsonStructure jsonstruct = null;

        try {
            jsonstruct = reader.read();
        } catch (JsonParsingException ex) {
        }

        if (jsonstruct == null) {
            objectBuilder.add(classParameter, paramValues);
        } else {
            objectBuilder.add(classParameter, jsonstruct);
        }

        JsonObjectBuilder jsonParamField = Json.createObjectBuilder();
        jsonParamField.add(className, objectBuilder);

        JsonObjectBuilder jsonClass = Json.createObjectBuilder();
        jsonClass.add("classes", jsonParamField);

        return jsonClass.build().toString();
    }

    private void actionSpecificValidation() throws AutomicException {
        className = getOptionValue("classname");
        PuppetValidator.checkNotEmpty(className, "Class name");

        classParameter = getOptionValue("classparam");
        PuppetValidator.checkNotEmpty(classParameter, "Class name");

        paramValues = getOptionValue("paramvalue");
        PuppetValidator.checkNotEmpty(paramValues, "Class name");
    }

}
