package com.automic.puppet.actions.helper;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.automic.puppet.constants.Constants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Class to get the authentication token and revoke the authentication token
 * 
 * @author shrutinambiar
 *
 */
public class TokenHandler {

    private static final String TOKEN = "token";
    private WebResource resource;

    public TokenHandler(WebResource webresource) {
        this.resource = webresource;
    }

    /**
     * Method to get the authentication token
     * 
     * @param username
     * @return
     * @throws AutomicException
     */
    public String login(String username) throws AutomicException {
        ClientResponse response = null;

        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_LOGIN_API_VERSION, Constants.LOGIN_API_VERSION);
        String password = System.getenv("UC4_DECRYPTED_PWD");

        WebResource webres = resource.path("rbac-api").path(apiVersion).path("auth").path("token");

        ConsoleWriter.newLine();
        ConsoleWriter.writeln("Calling URL to authenticate the user : " + webres.getURI());
        
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("login", username);
        json.add("password", password);

        response = webres.accept(MediaType.APPLICATION_JSON)
                .entity(json.build().toString(), MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);
        ConsoleWriter.newLine();

        return getAuthToken(CommonUtil.jsonObjectResponse(response.getEntityInputStream()));
    }

    /**
     * Method to revoke the authentication token
     * 
     * @param token
     * @throws AutomicException
     */
    public void logout(String token) throws AutomicException {

        String apiVersion = CommonUtil.getEnvParameter(Constants.ENV_LOGOUT_API_VERSION, Constants.LOGOUT_API_VERSION);
        WebResource webres = resource.path("rbac-api").path(apiVersion).path("tokens");

        ConsoleWriter.newLine();
        ConsoleWriter.writeln("Calling URL to delete the token : " + webres.getURI());

        webres.queryParam("revoke_tokens", token).header("X-Authentication", token).header("IGNORE-CHECK", "true")
                .delete(ClientResponse.class);
        ConsoleWriter.newLine();
    }

    private static String getAuthToken(JsonObject jsonobj) throws AutomicException {
        String authToken = null;
        if (jsonobj.containsKey(TOKEN)) {
            authToken = jsonobj.getString(TOKEN);
        }
        if (!CommonUtil.checkNotEmpty(authToken)) {
            throw new AutomicException("Could not authenticate the user.");
        }

        return authToken;
    }

}
