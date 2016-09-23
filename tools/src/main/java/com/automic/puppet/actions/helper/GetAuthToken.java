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
 * 
 * Helper class to generate authentication token
 *
 */
public class GetAuthToken {

    private static final String TOKEN = "token";

    public static String restResponse(WebResource webresource, String username, String password, String apiVersion)
            throws AutomicException {
        ClientResponse response = null;

        WebResource webres = webresource.path("rbac-api").path(apiVersion).path("auth").path("token");

        ConsoleWriter.writeln("Calling URL to authenticate the user : " + webres.getURI());

        response = webres.accept(MediaType.APPLICATION_JSON)
                .entity(getJsonAuthentication(username, password), MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        return getToken(CommonUtil.jsonObjectResponse(response.getEntityInputStream()));

    }

    private static String getToken(JsonObject jsonobj) {
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
