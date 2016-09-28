/**
 * 
 */
package com.automic.puppet.actions;

import java.util.Set;

import javax.json.JsonObject;

import com.automic.puppet.actions.helper.GetGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.automic.puppet.util.validator.PuppetValidator;
import com.sun.jersey.api.client.WebResource;

/**
 * This class is responsible to list all classes present in a node group.
 *
 */
public class ListNodeGroupClassesAction extends AbstractHttpAction {

    /**
     * Name of Node Group
     */
    private String nodeGroup;

    /**
     * 
     */
    public ListNodeGroupClassesAction() {
        addOption("nodegroup", true, "Name of the node group");
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

            // Get list of classes
            String classList = getListOfClasses(jsonobj);
            // process response
            prepareOutput(classList);
        } finally {
            // destroy token
            try {
                TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        try {
            // get node group
            nodeGroup = getOptionValue("nodegroup");
            PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

        } catch (AutomicException e) {
            ConsoleWriter.writeln(e);
            throw e;
        }
    }

    // print the list of class in AE vara UC4RB_PUP_CLASS_LIST in the job report
    private void prepareOutput(String classList) throws AutomicException {
        // write the node group details to job report
        ConsoleWriter.writeln("UC4RB_PUP_CLASS_LIST::=" + classList);
    }

    // read json to get list of classes in node group
    private String getListOfClasses(JsonObject nodeGroupJson) {

        final StringBuilder stringBuilder = new StringBuilder();

        Set<String> entries = nodeGroupJson.getJsonObject("classes").keySet();
        int pointer = 1, size = entries.size();
        // iterate over all classes
        for (String className : entries) {
            stringBuilder.append(className);
            if (pointer < size) {
                stringBuilder.append(",");
                pointer++;
            }
        }

        return stringBuilder.toString();

    }
}
