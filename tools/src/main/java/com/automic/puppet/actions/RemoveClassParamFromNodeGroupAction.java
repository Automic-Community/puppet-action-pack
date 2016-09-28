package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
 * This class removes a parameter from the given node group, if it exist.
 * 
 * @author shrutinambiar
 *
 */
public class RemoveClassParamFromNodeGroupAction extends AbstractHttpAction {

    /**
     * Name of the node group
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
        addOption("classname", true, "Class name whose parameter is to be removed");
        addOption("classparam", true, "Parameter to be removed");
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

            // check if class exist or not
            if (!CommonUtil.checkClassExist(jsonobj, className, nodeGroup)) {
                throw new AutomicException("No class found with the name [" + className + "]");
            }

            // url to add the node to node group
            WebResource webresource = webResClient.path("classifier-api").path(apiVersion).path("groups").path(groupId);

            ConsoleWriter.writeln("Calling URL to remove a class parameter: " + webresource.getURI());

            webresource.accept(MediaType.APPLICATION_JSON).header("X-Authentication", authToken)
                    .entity(getNodeJson(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
        } finally {
            // revoke the token
            TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
        }

    }

    private String getNodeJson() {

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
        try {
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node group name");

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
