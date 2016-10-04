package com.automic.puppet.actions;

import java.util.List;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.ConsoleWriter;
import com.sun.jersey.api.client.WebResource;

/**
 * This available node groups.
 *
 */
public class ListNodeGroupAction extends AbstractHttpAction {

    /**
     * Node Group Filter
     */
    private String nodeGroupsFilter;

    public ListNodeGroupAction() {
        addOption("nodegroupfilter", false, "Node Group filter");
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
            List<String> nodeGroups = new NodeGroupInfo(authToken, webResClient).getNodeGroups();
            // process response
            prepareOutput(nodeGroups);
        } finally {
            // destroy token
            tHandler.logout(authToken);
        }
    }

    private void prepareInputParameters() throws AutomicException {
        // Initialize node group filter
        nodeGroupsFilter = getOptionValue("nodegroupfilter");
    }

    // print the list of node groups in AE vara UC4RB_PUP_NODE_GROUP_LIST in the job report
    private void prepareOutput(List<String> nodeGroups) {
        // write the node group details to job report
        StringBuilder sb = new StringBuilder();
        if (!nodeGroups.isEmpty()) {
            if (nodeGroupsFilter != null && "".equals(nodeGroupsFilter)) {
                for (String group : nodeGroups) {
                    if (group.matches(nodeGroupsFilter)) {
                        sb.append(group);
                    }
                }
            } else {
                sb.append(nodeGroups.toString());
                sb.deleteCharAt(0);
                sb.deleteCharAt(sb.length() - 1);
            }
        }

        ConsoleWriter.writeln("UC4RB_PUP_NODE_GROUP_LIST::=" + sb);

    }

}
