package com.automic.puppet.filter;

import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.exception.AutomicRuntimeException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * This class acts as a filter and intercept the response to validate it.
 *
 */

public class GenericResponseFilter extends ClientFilter {

    private static final int HTTP_SUCCESS_START = 200;
    private static final int HTTP_SUCCESS_END = 299;


    private static final String RESPONSE_CODE = "Response Code [%s]";
    private static final String RESPONSE_MSG = RESPONSE_CODE + " Message : [%s]";

    @Override
    public ClientResponse handle(ClientRequest request) {

        ClientResponse response = getNext().handle(request);
        String msg = null;
        if (CommonUtil.checkNotEmpty(response.getClientResponseStatus().getReasonPhrase())) {
            msg = String.format(RESPONSE_MSG, response.getStatus(), response.getClientResponseStatus()
                    .getReasonPhrase());
        } else {
            msg = String.format(RESPONSE_CODE, response.getStatus());
        }
        if (!(response.getStatus() >= HTTP_SUCCESS_START && response.getStatus() <= HTTP_SUCCESS_END)) {
            try {
				ConsoleWriter.writeln(CommonUtil.formatErrorMessage(msg));
			} catch (AutomicException e) {
				e.printStackTrace();
			}
            String responseMsg = response.getEntity(String.class);
            throw new AutomicRuntimeException(responseMsg);

        } else {
            try {
				ConsoleWriter.writeln(msg);
			} catch (AutomicException e) {
				e.printStackTrace();
			}
        }
        return response;
    }

}
