package com.automic.puppet.actions;

import java.util.List;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
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

    public ListNodeGroupClassesAction() {
        addOption("nodegroup", true, "Name of the node group");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        WebResource webResClient = getClient();

        ConsoleWriter.newLine();
        ConsoleWriter.writeln("**************************************************");
        ConsoleWriter.writeln("    Execution starts for action      ");
        ConsoleWriter.writeln("**************************************************");
        ConsoleWriter.newLine();

        // generate auth token
        String authToken = TokenHandler.getToken(webResClient, username, password, loginApiVersion);
        if (authToken == null) {
            throw new AutomicException("Could not authenticate the user [" + username + "]");
        }

        try {
            // Get list of classes
            List<String> classList = new NodeGroupInfo(authToken, webResClient, apiVersion).getGroupJson(nodeGroup);
            // process response
            prepareOutput(classList);
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

        } catch (AutomicException e) {
            ConsoleWriter.writeln(e);
            throw e;
        }
    }

    // print the list of class in AE vara UC4RB_PUP_CLASS_LIST in the job report
    private void prepareOutput(List<String> classList) throws AutomicException {
        // write the node group details to job report
        ConsoleWriter.writeln("UC4RB_PUP_CLASS_COUNT::=" + classList.size());
        String list = classList.toString();
        ConsoleWriter.writeln("UC4RB_PUP_CLASS_LIST::=" + list.substring(1, list.length() - 1));

    }

}
