package com.automic.puppet.actions;

import java.util.Map.Entry;

import javax.json.Json;
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
 * This class is used to replace existing node group classes with provided classes.
 * 
 * @author anuragupadhyay
 *
 */
public class ReplaceNodeGroupClassesAction extends AbstractHttpAction {

    private String nodeGroup;
    private String[] classesNameArray;

    public ReplaceNodeGroupClassesAction() {
        addOption("nodegroup", true, "Node group name");
        addOption("classesname", true, "Classes to be replace");

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

            // get group information
            JsonObject jsonobj = GetGroupInfo.restResponse(authToken, webResClient, nodeGroup, apiVersion);
            String groupId = CommonUtil.getGroupId(jsonobj, nodeGroup);
            if (groupId == null) {
                throw new AutomicException("No group id found for [" + nodeGroup + "]");
            }
            String jsonObject = prepareJsonObject(jsonobj);

            // url to replace class in node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.writeln("Calling URL to replace classes parameter: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(jsonObject, MediaType.APPLICATION_JSON).put(ClientResponse.class);

        } finally {
            // revoke the token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }

    }

    private String prepareJsonObject(JsonObject jsonobj) {

        JsonValue emptyJson = Json.createObjectBuilder().build();

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        for (String classname : classesNameArray) {
            objectBuilder.add(classname, emptyJson);
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Entry<String, JsonValue> entry : jsonobj.entrySet()) {
            if (!"classes".equals(entry.getKey())) {
                builder.add(entry.getKey(), entry.getValue());
            } else {
                builder.add("classes", objectBuilder.build());
            }
        }

        return builder.build().toString();
    }

    private void prepareInputParameters() throws AutomicException {
        try {
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

            String classesName = getOptionValue("classesname");
            PuppetValidator.checkNotEmpty(classesName, "Class name");
            classesNameArray = classesName.split(",");
            if (classesNameArray.length == 0) {
                throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Classes",
                        classesName));
            }

        } catch (AutomicException e) {
            ConsoleWriter.write(e.getMessage());
            throw e;
        }
    }

}
