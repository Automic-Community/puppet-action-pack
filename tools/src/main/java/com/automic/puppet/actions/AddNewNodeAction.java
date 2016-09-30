package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;

/**
 * Class to add a new node to the given node group
 * 
 * @author shrutinambiar
 *
 */
public class AddNewNodeAction extends UpdateNodeGroupAction {

    /**
     * Name of the node
     */
    private String nodeName;

    public AddNewNodeAction() {
        addOption("nodename", true, "Node to be added");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        actionSpecificValidation();
        super.executeSpecific();

    }

    private void actionSpecificValidation() throws AutomicException {
        try {
            nodeName = getOptionValue("nodename");
            PuppetValidator.checkNotEmpty(nodeName, "Node name");

        } catch (AutomicException e) {
            ConsoleWriter.write(e.getMessage());
            throw e;
        }
    }

    protected String getNodeJson() {

        JsonArrayBuilder jsonNodeArray = Json.createArrayBuilder();
        jsonNodeArray.add(nodeName);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("nodes", jsonNodeArray);
        return json.build().toString();
    }
}
