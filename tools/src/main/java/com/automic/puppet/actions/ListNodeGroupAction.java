package com.automic.puppet.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.automic.puppet.actions.helper.NodeGroupInfo;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
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
    private Pattern nodeGroupsFilter;

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
        String temp = getOptionValue("nodegroupfilter");
        if (CommonUtil.checkNotEmpty(temp)) {
            try {
                nodeGroupsFilter = Pattern.compile(temp);
            } catch (PatternSyntaxException pe) {
                nodeGroupsFilter = Pattern.compile(Pattern.quote(temp));
            }
        }
    }

    // print the list of node groups in AE vara UC4RB_PUP_NODE_GROUP_LIST in the
    // job report
    private void prepareOutput(List<String> nodeGroups) {
        // write the node group details to job report
        List<String> filterNodeGroups = new ArrayList<String>();
        if (CommonUtil.checkNotNull(nodeGroupsFilter)) {
            for (String group : nodeGroups) {
                if (nodeGroupsFilter.matcher(group).matches()) {
                    filterNodeGroups.add(group);
                }
            }
        } else {
            filterNodeGroups = nodeGroups;
        }
        
        ConsoleWriter.writeln("UC4RB_PUP_NODE_GROUP_LIST::=" + CommonUtil.listToString(filterNodeGroups, ","));
        ConsoleWriter.writeln("UC4RB_PUP_NODE_GROUP_COUNT::=" + filterNodeGroups.size());
    }

}
