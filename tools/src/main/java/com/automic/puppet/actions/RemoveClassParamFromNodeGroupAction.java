package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;

/**
 * This class removes a parameter from the given node group, if it exist.
 * 
 * @author shrutinambiar
 *
 */
public class RemoveClassParamFromNodeGroupAction extends UpdateNodeGroupAction {

    /**
     * Name of the class
     */
    private String className;

    /**
     * Parameter list to be removed
     */
    private String[] classParamList;

    public RemoveClassParamFromNodeGroupAction() {
        addOption("classname", true, "Class name whose parameter is to be removed");
        addOption("classparam", true, "Parameter to be removed");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        actionSpecificValidation();
        super.executeSpecific();

    }

    protected String getEntity() {

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        for (String paramName : classParamList) {
            objectBuilder.addNull(paramName);
        }

        JsonObjectBuilder jsonClass = Json.createObjectBuilder();
        jsonClass.add(className, objectBuilder);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("classes", jsonClass);

        return json.build().toString();
    }

    private void actionSpecificValidation() throws AutomicException {
        try {

            className = getOptionValue("classname");
            PuppetValidator.checkNotEmpty(className, "Class name");

            String classParameters = getOptionValue("classparam");
            PuppetValidator.checkNotEmpty(classParameters, "Class name");

            classParamList = classParameters.split(",");
            if (classParamList.length == 0) {
                throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Classes",
                        classParameters));
            }
        } catch (AutomicException e) {
            ConsoleWriter.write(e.getMessage());
            throw e;
        }
    }

}
