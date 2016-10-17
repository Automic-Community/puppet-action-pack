package com.automic.puppet.actions;

import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.constants.Constants;
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
        addOption("classesname", true, "Classes");

    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        WebResource webResClient = getClient();

        // get auth token
        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);

        try {
            NodeGroupInfo groupInfo = new NodeGroupInfo(authToken, webResClient);

            String jsonObject = prepareJsonObject(groupInfo.getNodeGroup(nodeGroup));

            String groupId = groupInfo.getGroupId(nodeGroup);
            String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);

            // url to replace class in node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.writeln("Calling URL to replace classes parameter: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(jsonObject, MediaType.APPLICATION_JSON).put(ClientResponse.class);

        } finally {
            // revoke the token
            tHandler.logout(authToken);
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
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

        String classesName = getOptionValue("classesname");
        PuppetValidator.checkNotEmpty(classesName, "Class name");
        classesNameArray = CommonUtil.splitAndTrimSpace(classesName, ",");
        if (classesNameArray.length == 0) {
            throw new AutomicException(
                    String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Classes", classesName));
        }
    }

}
