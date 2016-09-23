package com.automic.puppet.actions.helper;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

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
public final class TokenHandler {

    private static final String TOKEN = "token";

    /**
     * Method to get the authentication token
     * 
     * @param webresource
     * @param username
     * @param password
     * @param apiVersion
     * @return
     * @throws AutomicException
     */
    public static String getToken(WebResource webresource, String username, String password, String apiVersion)
            throws AutomicException {
        ClientResponse response = null;

        WebResource webres = webresource.path("rbac-api").path(apiVersion).path("auth").path("token");

        ConsoleWriter.writeln("Calling URL to authenticate the user : " + webres.getURI());

        response = webres.accept(MediaType.APPLICATION_JSON)
                .entity(getJsonAuthentication(username, password), MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        return getAuthToken(CommonUtil.jsonObjectResponse(response.getEntityInputStream()));

    }

    /**
     * Method to revoke the authentication token
     * 
     * @param webresource
     * @param logoutApiVersion
     * @param token
     * @throws AutomicException
     */
    public static void revokeToken(WebResource webresource, String logoutApiVersion, String token)
            throws AutomicException {

        WebResource webres = webresource.path("rbac-api").path(logoutApiVersion).path("tokens");
        ConsoleWriter.writeln("Calling URL to delete the token : " + webres.getURI());
        webres.queryParam("revoke_tokens", token).header("X-Authentication", token).delete(ClientResponse.class);

    }

    private static String getAuthToken(JsonObject jsonobj) {
        String authToken = null;
        if (jsonobj.containsKey(TOKEN)) {
            authToken = jsonobj.getString(TOKEN);
        }
        return authToken;
    }

    private static String getJsonAuthentication(String username, String password) {

        JsonObjectBuilder json = Json.createObjectBuilder();

        json.add("login", username);
        json.add("password", password);

        return json.build().toString();
    }

}
