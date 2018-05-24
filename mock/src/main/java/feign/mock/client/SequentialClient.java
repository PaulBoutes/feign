package feign.mock.client;

import java.util.Iterator;
import java.util.List;
import feign.Request;
import feign.Response;
import feign.mock.HttpMethod;
import feign.mock.RequestKey;
import feign.mock.VerificationAssertionError;

public class SequentialClient extends MockClient {

  private Iterator<RequestResponse> responseIterator;

  private SequentialClient() {}

  public static SequentialClient create() {
    return new SequentialClient();
  }

  @Override
  public synchronized Response execute(Request request, Request.Options options) {
    RequestKey requestKey = RequestKey.create(request);
    return executeSequential(requestKey).request(request).build();
  }

  @Override
  public Request verifyOne(HttpMethod method, String url) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Request> verifyTimes(HttpMethod method, String url, int times) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void verifyNever(HttpMethod method, String url) {
    throw new UnsupportedOperationException();
  }


  /**
   * To be called in an &#64;After method:
   *
   * <pre>
   * &#64;After
   * public void tearDown() {
   *   mockClient.verifyStatus();
   * }
   * </pre>
   */
  @Override
  public void verifyStatus() {
    boolean unopenedIterator = responseIterator == null && !responses.isEmpty();
    if (unopenedIterator || responseIterator.hasNext()) {
      throw new VerificationAssertionError("More executions were expected");
    }
  }

  @Override public void resetRequests() {
    responses.clear();
  }



  private Response.Builder executeSequential(RequestKey requestKey) {
    if (responseIterator == null) {
      responseIterator = responses.iterator();
    }
    if (!responseIterator.hasNext()) {
      throw new VerificationAssertionError("Received excessive request %s", requestKey);
    }

    RequestResponse expectedRequestResponse = responseIterator.next();
    if (!expectedRequestResponse.requestKey.equalsExtended(requestKey)) {
      throw new VerificationAssertionError("Expected %s, but was %s",
          expectedRequestResponse.requestKey,
          requestKey);
    }

    return expectedRequestResponse.responseBuilder;
  }

}
