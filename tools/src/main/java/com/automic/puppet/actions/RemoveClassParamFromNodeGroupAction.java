package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
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
 * This class removes a parameter from the given node group, if it exist.
 * 
 * @author shrutinambiar
 *
 */
public class RemoveClassParamFromNodeGroupAction extends AbstractHttpAction {

    /**
     * Node group name
     */
    private String nodeGroup;

    /**
     * Name of the class
     */
    private String className;

    /**
     * Parameter list to be removed
     */
    private String[] classParamList;

    public RemoveClassParamFromNodeGroupAction() {
        addOption("nodegroup", true, "Node group name");
        addOption("classname", true, "Class");
        addOption("classparam", true, "Parameter Name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {

        WebResource webResClient = getClient();

        // get auth token
        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);

        try {
            prepareInputParameters();

            // get group information
            NodeGroupInfo groupInfo = new NodeGroupInfo(authToken, webResClient);

            String groupId = groupInfo.getGroupId(nodeGroup);

            String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_API_VERSION, Constants.API_VERSION);

            // check if class exist or not
            if (!groupInfo.checkClassExist(className, nodeGroup)) {
                throw new AutomicException("No class found with the name [" + className + "]");
            }

            // url to add the node to node group
            WebResource webresource = getClient().path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.writeln("Calling URL to remove a class parameter: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(getEntity(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
        } finally {
            // revoke the token
            tHandler.logout(authToken);
        }
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

    private void prepareInputParameters() throws AutomicException {

        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

        className = getOptionValue("classname");
        PuppetValidator.checkNotEmpty(className, "Class name");

        String classParameters = getOptionValue("classparam");
        PuppetValidator.checkNotEmpty(classParameters, "Class name");

        classParamList = CommonUtil.splitAndTrimSpace(classParameters, ",");
        if (classParamList.length == 0) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, "Classes",
                    classParameters));
        }
    }

}
