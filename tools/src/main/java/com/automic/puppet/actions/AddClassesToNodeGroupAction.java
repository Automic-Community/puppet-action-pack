package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.validator.PuppetValidator;

/**
 * This class is responsible to add multiple classes to provided node group.
 *
 */
public class AddClassesToNodeGroupAction extends UpdateNodeGroupAction {

    /**
     * list of classes
     */
    private String[] classList;

    /**
     * 
     */
    public AddClassesToNodeGroupAction() {
        addOption("classes", true, "Classes");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        actionSpecificValidation();
        super.executeSpecific();
    }

    @Override
    protected String getEntity() {
        JsonValue emptyJson = Json.createObjectBuilder().build();

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        for (String classname : classList) {
            objectBuilder.add(classname, emptyJson);
        }
        JsonObjectBuilder classesJson = Json.createObjectBuilder();
        classesJson.add("classes", objectBuilder);
        return classesJson.build().toString();
    }

    private void actionSpecificValidation() throws AutomicException {
        // classes
        String classes = getOptionValue("classes");
        PuppetValidator.checkNotEmpty(classes, "Classes");
        classList = CommonUtil.splitAndTrimSpace(classes, ",");
        if (classList.length == 0) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Classes", classes));
        }
    }

}
