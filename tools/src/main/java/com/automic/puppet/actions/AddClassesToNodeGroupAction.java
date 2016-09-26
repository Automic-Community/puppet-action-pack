/**
 * 
 */
package com.automic.puppet.actions;

import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.GetGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class is responsible to add multiple classes to provided node group.
 *
 */
public class AddClassesToNodeGroupAction extends AbstractHttpAction {

    /**
     * Name of Node Group
     */
    private String nodeGroup;

    /**
     * list of classes
     */
    private List<String> classList;

    /**
     * 
     */
    public AddClassesToNodeGroupAction() {
        addOption("nodegroup", true, "Name of the node group");
        addOption("classes", true, "Json file containing json");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        WebResource webResClient = getClient();

        // generate auth token
        String authToken = TokenHandler.getToken(webResClient, username, password, apiVersion);
        try {
            // get group id based on node group name
            JsonObject jsonobj = GetGroupInfo.restResponse(authToken, webResClient, nodeGroup, apiVersion);

            String groupId = CommonUtil.getGroupId(jsonobj, nodeGroup);

            if (groupId == null) {
                throw new AutomicException("No group id found for [" + nodeGroup + "]");
            }
            // json object with classes
            JsonObject jsonObject = buildJson();

            // Create POST request
            WebResource webResource = getClient().path("classifier-api").path(apiVersion).path("groups").path(groupId);
            ConsoleWriter.writeln("Calling url " + webResource.getURI());
            ClientResponse response = webResource.header("X-Authentication", authToken)
                    .entity(jsonObject.toString(), MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                    .post(ClientResponse.class);
            // process response
            prepareOutput(response);
        } finally {

            // destroy token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }
    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        try {
            // get node group
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

            // classes
            String classes = getOptionValue("classes");
            PuppetValidator.checkNotEmpty(classes, "Classes");
            classList = Arrays.asList(classes.split(","));
            if (classList.size() == 0) {
                throw new AutomicException(
                        String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Classes", classes));
            }
        } catch (AutomicException e) {
            ConsoleWriter.writeln(e);
            throw e;
        }
    }

    // process response and print the response in the job report
    private void prepareOutput(ClientResponse response) throws AutomicException {
        // write the node group details to job report
        ConsoleWriter.writeln("Node Group details");
        ConsoleWriter.writeln(response.getEntity(String.class));
    }

    // generate json to add classes to node group
    private JsonObject buildJson() {

        JsonValue emptyJson = Json.createObjectBuilder().build();

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        for (String classname : classList) {
            objectBuilder.add(classname, emptyJson);
        }
        return Json.createObjectBuilder().add("classes", objectBuilder).build();

    }

}
