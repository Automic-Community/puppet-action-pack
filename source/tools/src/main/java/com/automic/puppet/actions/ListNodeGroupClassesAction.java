package com.automic.puppet.actions;

import java.util.List;

import com.automic.puppet.actions.helper.NodeGroupInfo;
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

    public ListNodeGroupClassesAction() {
        addOption("nodegroup", true, "Node group name");
    }

    @Override
    protected void executeSpecific() throws AutomicException {
        prepareInputParameters();
        WebResource webResClient = getClient();

        // generate auth token
        TokenHandler tHandler = new TokenHandler(webResClient);
        String authToken = tHandler.login(username);
        try {
            // Get list of classes
            List<String> classList = new NodeGroupInfo(authToken, webResClient).getClasses(nodeGroup);
            // process response
            prepareOutput(classList);
        } finally {
            // destroy token
            tHandler.logout(authToken);
        }
    }

    // Validating if the given input is not empty
    private void prepareInputParameters() throws AutomicException {
        // get node group
        nodeGroup = getOptionValue("nodegroup");
        PuppetValidator.checkNotEmpty(nodeGroup, "Node Group");

    }

    // print the list of class in AE vara UC4RB_PUP_CLASS_LIST in the job report
    private void prepareOutput(List<String> classList) {
        // write the node group details to job report
        ConsoleWriter.writeln("UC4RB_PUP_CLASS_COUNT::=" + classList.size());        
        ConsoleWriter.writeln("UC4RB_PUP_CLASS_LIST::=" + CommonUtil.listToString(classList,","));
    }

}
