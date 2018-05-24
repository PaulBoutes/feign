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
package feign.mock;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestHeaders {

  public static class Builder {

    private Map<String, Collection<String>> headers = new HashMap<>();

    private Builder() {}

    public Builder add(String key, Collection<String> values) {
      headers.computeIfPresent(key, (k, list) -> {
        list.addAll(values);
        return list;
      });
      headers.putIfAbsent(key, values);
      return this;
    }

    public Builder add(String key, String value) {
      headers.computeIfPresent(key, (k, list) -> {
        list.add(value);
        return list;
      });
      headers.putIfAbsent(key, new ArrayList<>(Arrays.asList(value)));
      return this;
    }

    public RequestHeaders build() {
      return new RequestHeaders(this);
    }

  }

  public static final Map<String, Collection<String>> EMPTY = Collections.emptyMap();

  public static Builder builder() {
    return new Builder();
  }

  public static RequestHeaders of(Map<String, Collection<String>> headers) {
    return new RequestHeaders(headers);
  }

  private Map<String, Collection<String>> headers;

  private RequestHeaders(Builder builder) {
    this.headers = builder.headers;
  }

  private RequestHeaders(Map<String, Collection<String>> headers) {
    this.headers = headers;
  }

  public int size() {
    return headers.size();
  }

  public int sizeOf(String key) {
    return headers.getOrDefault(key, Collections.emptyList()).size();
  }

  public Collection<String> fetch(String key) {
    return headers.get(key);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RequestHeaders other = (RequestHeaders) obj;
    return this.headers.equals(other.headers);
  }

  @Override
  public String toString() {
    return headers
        .entrySet()
        .stream()
        .map(Map.Entry::toString)
        .collect(collectingAndThen(joining(", "), s -> s.length() > 0 ? s : "no"));
  }

}
