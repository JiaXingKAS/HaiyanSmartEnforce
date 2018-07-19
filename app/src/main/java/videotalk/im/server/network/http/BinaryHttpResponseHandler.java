/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package videotalk.im.server.network.http;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Used to intercept and handle the responses from requests made using {@link AsyncHttpClient}.
 * Receives response body as byte array with a content-type whitelist. (e.g. checks Content-Type
 * against allowed list, Content-length). <p>&nbsp;</p> For example: <p>&nbsp;</p>
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient();
 * String[] allowedTypes = new String[] { "image/png" };
 * client.get("http://www.example.com/image.png", new BinaryHttpResponseHandler(allowedTypes) {
 *     &#064;Override
 *     public void onSuccess(byte[] imageData) {
 *         // Successfully got a response
 *     }
 *
 *     &#064;Override
 *     public void onFailure(Throwable e, byte[] imageData) {
 *         // Response failed :(
 *     }
 * });
 * </pre>
 */
public class BinaryHttpResponseHandler extends AsyncHttpResponseHandler {

    private String[] mAllowedContentTypes = new String[] {
        "image/jpeg",
        "image/png"
    };

    /**
     * Method can be overriden to return allowed content types, can be sometimes better than passing
     * data in constructor
     *
     * @return array of content-types or Pattern string templates (eg. '.*' to match every response)
     */
    public String[] getAllowedContentTypes() {
        return mAllowedContentTypes;
    }

    /**
     * Creates a new BinaryHttpResponseHandler
     */
    public BinaryHttpResponseHandler() {
        super();
    }

    /**
     * Creates a new BinaryHttpResponseHandler, and overrides the default allowed content types with
     * passed String array (hopefully) of content types.
     *
     * @param allowedContentTypes content types array, eg. 'image/jpeg' or pattern '.*'
     */
    public BinaryHttpResponseHandler(String[] allowedContentTypes) {
        this();
        mAllowedContentTypes = allowedContentTypes;
    }


    //
    // Callbacks to be overridden, typically anonymously
    //

    /**
     * Fired when a request returns successfully, override to handle in your own code
     *
     * @param binaryData the body of the HTTP response from the server
     */
    public void onSuccess(byte[] binaryData) {
    }

    /**
     * Fired when a request returns successfully, override to handle in your own code
     *
     * @param statusCode the status code of the response
     * @param binaryData the body of the HTTP response from the server
     */
    public void onSuccess(int statusCode, byte[] binaryData) {
        onSuccess(binaryData);
    }

    /**
     * Fired when a request returns successfully, override to handle in your own code
     *
     * @param statusCode response HTTP statuse code
     * @param headers    response headers, if any
     * @param binaryData the response body, if any
     */

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
        onSuccess(statusCode, binaryData);
    }

    /**
     * Fired when a request fails to complete, override to handle in your own code
     *
     * @param statusCode response HTTP statuse code
     * @param headers    response headers, if any
     * @param binaryData the response body, if any
     * @param error      the underlying cause of the failure
     */

    @SuppressWarnings("deprecation")
    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
        onFailure(statusCode, error, null);
    }

    //
    // Pre-processing of messages (in original calling thread, typically the UI thread)
    //

    // Interface to AsyncHttpRequest
    @Override
    public final void sendResponseMessage(HttpResponse response) throws IOException {
        StatusLine status = response.getStatusLine();
        Header[] contentTypeHeaders = response.getHeaders("Content-Type");
        if (contentTypeHeaders.length != 1) {
            //malformed/ambiguous HTTP Header, ABORT!
            sendFailureMessage(status.getStatusCode(), response.getAllHeaders(), null, new HttpResponseException(status.getStatusCode(), "None, or more than one, Content-Type Header found!"));
            return;
        }
        Header contentTypeHeader = contentTypeHeaders[0];
        boolean foundAllowedContentType = false;
        for (String anAllowedContentType : getAllowedContentTypes()) {
            try {
                if (Pattern.matches(anAllowedContentType, contentTypeHeader.getValue())) {
                    foundAllowedContentType = true;
                }
            } catch (PatternSyntaxException e) {
            }
        }
        if (!foundAllowedContentType) {
            //Content-Type not in allowed list, ABORT!
            sendFailureMessage(status.getStatusCode(), response.getAllHeaders(), null, new HttpResponseException(status.getStatusCode(), "Content-Type not allowed!"));
            return;
        }
        super.sendResponseMessage(response);
    }
}
