package feign.mock.client;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import feign.Request;
import feign.Response;
import feign.mock.HttpMethod;
import feign.mock.RequestKey;
import feign.mock.VerificationAssertionError;

public class NonSequentialClient extends MockClient {

  private NonSequentialClient() {}

  public static NonSequentialClient create() {
    return new NonSequentialClient();
  }

  @Override
  public synchronized Response execute(Request request, Request.Options options) {
    RequestKey requestKey = RequestKey.create(request);
    return executeAny(request, requestKey).request(request).build();
  }

  @Override
  public void verifyStatus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resetRequests() {
    requests.clear();
  }

  @Override
  public void verifyNever(HttpMethod method, String url) {
    RequestKey requestKey = RequestKey.builder(method, url).build();
    if (requests.containsKey(requestKey)) {
      throw new VerificationAssertionError("Do not wanted: '%s' but was invoked!", requestKey);
    }
  }

  @Override
  public Request verifyOne(HttpMethod method, String url) {
    return verifyTimes(method, url, 1).get(0);
  }

  @Override
  public List<Request> verifyTimes(final HttpMethod method, final String url, final int times) {
    if (times < 0) {
      throw new IllegalArgumentException("times must be a non negative number");
    }

    if (times == 0) {
      verifyNever(method, url);
      return Collections.emptyList();
    }

    RequestKey requestKey = RequestKey.builder(method, url).build();
    if (!requests.containsKey(requestKey)) {
      throw new VerificationAssertionError("Wanted: '%s' but never invoked!", requestKey);
    }

    List<Request> result = requests.get(requestKey);
    if (result.size() != times) {
      throw new VerificationAssertionError("Wanted: '%s' to be invoked: '%s' times but got: '%s'!",
          requestKey,
          times, result.size());
    }

    return result;
  }

  private Response.Builder executeAny(Request request, RequestKey requestKey) {
    requests.computeIfPresent(requestKey, addRequestToListAndReturn(request));
    requests.computeIfAbsent(requestKey, createRequestListFrom(request));
    return getResponseBuilder(request, requestKey);
  }

  private Response.Builder getResponseBuilder(Request request, RequestKey requestKey) {
    return responses
        .stream()
        .filter(requestResponse -> requestResponse.requestKey.equalsExtended(requestKey))
        .reduce((first, last) -> last)
        .map(req -> req.responseBuilder)
        .orElseGet(() -> Response.builder().status(HttpURLConnection.HTTP_NOT_FOUND)
            .reason("Not mocker").headers(request.headers()));
  }

  private BiFunction<RequestKey, List<Request>, List<Request>> addRequestToListAndReturn(Request request) {
    return (key, list) -> {
      list.add(request);
      return list;
    };
  }

  private Function<RequestKey, List<Request>> createRequestListFrom(Request request) {
    return key -> new ArrayList<>(Arrays.asList(request));
  }

}
