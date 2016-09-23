package com.automic.puppet.actions;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.actions.helper.GetGroupId;
import com.automic.puppet.actions.helper.TokenHandler;
import com.automic.puppet.exception.AutomicException;
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

	private String nodeGroup;
	private String className;
	private String classParameter;

	public RemoveClassParamFromNodeGroupAction() {
		addOption("nodegroup", true, "Node group name");
		addOption("classname", true,
				"Class name whose parameter is to be removed");
		addOption("classparam", true, "Parameter to be removed");
	}

	@Override
	protected void executeSpecific() throws AutomicException {
		WebResource webResClient = getClient();
		// get auth token
		String authToken = TokenHandler.getToken(webResClient, username,
				password, loginApiVersion);
		if (authToken == null) {
			throw new AutomicException("Could not authenticate the user ["
					+ username + "]");
		}

		try {
			prepareInputParameters();

			String groupId = GetGroupId.restResponse(authToken, webResClient,
					nodeGroup, apiVersion);
			if (groupId == null) {
				throw new AutomicException("No group id found for ["
						+ nodeGroup + "]");
			}

			// url to add the node to node group
			WebResource webresource = getClient().path("classifier-api")
					.path(apiVersion).path("groups").path(groupId);

			ConsoleWriter.writeln("Calling URL to remove a class parameter: "
					+ webresource.getURI());

			webresource.accept(MediaType.APPLICATION_JSON)
					.header("X-Authentication", authToken)
					.entity(getNodeJson(), MediaType.APPLICATION_JSON)
					.post(ClientResponse.class);
		} finally {
			// revoke the token
			TokenHandler.revokeToken(webResClient, logoutApiVersion, authToken);
		}

	}

	private String getNodeJson() {

		JsonObjectBuilder jsonParamField = Json.createObjectBuilder();
		jsonParamField.addNull(classParameter);

		JsonObjectBuilder jsonClass = Json.createObjectBuilder();
		jsonClass.add(className, jsonParamField);

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

			classParameter = getOptionValue("classparam");
			PuppetValidator.checkNotEmpty(classParameter, "Class name");

		} catch (AutomicException e) {
			ConsoleWriter.write(e.getMessage());
			throw e;
		}
	}

}
