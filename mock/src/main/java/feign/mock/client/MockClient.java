/**
 * Copyright 2012-2018 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package feign.mock.client;

import static feign.Util.UTF_8;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.Util;
import feign.mock.HttpMethod;
import feign.mock.RequestHeaders;
import feign.mock.RequestKey;

public abstract class MockClient implements Client {

  static class RequestResponse {

    final RequestKey requestKey;

    final Response.Builder responseBuilder;

    private RequestResponse(RequestKey requestKey, Response.Builder responseBuilder) {
      this.requestKey = requestKey;
      this.responseBuilder = responseBuilder;
    }

    public static RequestResponse of(RequestKey requestKey, Response.Builder responseBuilder) {
      return new RequestResponse(requestKey, responseBuilder);
    }
  }

  final List<RequestResponse> responses = new ArrayList<>();

  final Map<RequestKey, List<Request>> requests = new HashMap<>();

  MockClient() {}

  @Override
  public abstract Response execute(Request request, Request.Options options) throws IOException;


  public MockClient ok(HttpMethod method, String url, InputStream responseBody) throws IOException {
    return ok(RequestKey.builder(method, url).build(), responseBody);
  }

  public MockClient ok(HttpMethod method, String url, String responseBody) {
    return ok(RequestKey.builder(method, url).build(), responseBody);
  }

  public MockClient ok(HttpMethod method, String url, byte[] responseBody) {
    return ok(RequestKey.builder(method, url).build(), responseBody);
  }

  public MockClient ok(HttpMethod method, String url) {
    return ok(RequestKey.builder(method, url).build());
  }

  public MockClient ok(RequestKey requestKey, InputStream responseBody) throws IOException {
    return ok(requestKey, Util.toByteArray(responseBody));
  }

  public MockClient ok(RequestKey requestKey, String responseBody) {
    return ok(requestKey, responseBody.getBytes(UTF_8));
  }

  public MockClient ok(RequestKey requestKey, byte[] responseBody) {
    return add(requestKey, HttpURLConnection.HTTP_OK, responseBody);
  }

  public MockClient ok(RequestKey requestKey) {
    return ok(requestKey, (byte[]) null);
  }

  public MockClient add(HttpMethod method, String url, int status, InputStream responseBody)
      throws IOException {
    return add(RequestKey.builder(method, url).build(), status, responseBody);
  }

  public MockClient add(HttpMethod method, String url, int status, String responseBody) {
    return add(RequestKey.builder(method, url).build(), status, responseBody);
  }

  public MockClient add(HttpMethod method, String url, int status, byte[] responseBody) {
    return add(RequestKey.builder(method, url).build(), status, responseBody);
  }

  public MockClient add(HttpMethod method, String url, int status) {
    return add(RequestKey.builder(method, url).build(), status);
  }

  /**
   * @param response
   *        <ul>
   *        <li>the status defaults to 0, not 200!</li>
   *        <li>the internal feign-code requires the headers to be set</li>
   *        </ul>
   */
  public MockClient add(HttpMethod method, String url, Response.Builder response) {
    return add(RequestKey.builder(method, url).build(), response);
  }

  public MockClient add(RequestKey requestKey, int status, InputStream responseBody)
      throws IOException {
    return add(requestKey, status, Util.toByteArray(responseBody));
  }

  public MockClient add(RequestKey requestKey, int status, String responseBody) {
    return add(requestKey, status, responseBody.getBytes(UTF_8));
  }

  public MockClient add(RequestKey requestKey, int status, byte[] responseBody) {
    return add(requestKey,
        Response.builder().status(status).reason("Mocked").headers(RequestHeaders.EMPTY)
            .body(responseBody));
  }

  public MockClient add(RequestKey requestKey, int status) {
    return add(requestKey, status, (byte[]) null);
  }

  public MockClient add(RequestKey requestKey, Response.Builder response) {
    responses.add(RequestResponse.of(requestKey, response));
    return this;
  }

  public MockClient add(HttpMethod method, String url, Response response) {
    return this.add(method, url, response.toBuilder());
  }

  public MockClient noContent(HttpMethod method, String url) {
    return add(method, url, HttpURLConnection.HTTP_NO_CONTENT);
  }

  public abstract Request verifyOne(HttpMethod method, String url);

  public abstract List<Request> verifyTimes(final HttpMethod method,
                                            final String url,
                                            final int times);

  public abstract void verifyNever(HttpMethod method, String url);

  public abstract void verifyStatus();

  public abstract void resetRequests();


}
