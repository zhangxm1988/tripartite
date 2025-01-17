package work.gaigeshen.triparttite.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import work.gaigeshen.triparttite.core.header.DefaultHeaders;
import work.gaigeshen.triparttite.core.header.Headers;
import work.gaigeshen.triparttite.core.interceptor.Execution;
import work.gaigeshen.triparttite.core.interceptor.Interceptor;
import work.gaigeshen.triparttite.core.interceptor.Interceptors;
import work.gaigeshen.triparttite.core.parameter.Parameter;
import work.gaigeshen.triparttite.core.parameter.Parameters;
import work.gaigeshen.triparttite.core.parameter.converter.ParametersConversionException;
import work.gaigeshen.triparttite.core.parameter.converter.ParametersConverter;
import work.gaigeshen.triparttite.core.parameter.creator.ParametersCreator;
import work.gaigeshen.triparttite.core.response.consumer.ResponseConsumer;
import work.gaigeshen.triparttite.core.response.converter.ResponseConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 *
 * @author gaigeshen
 */
public class RestTemplateWebExecutor implements WebExecutor {

  private static final Logger log = LoggerFactory.getLogger(RestTemplateWebExecutor.class);

  private final RestTemplate restTemplate;

  private ParametersConverter parametersConverter;

  protected RestTemplateWebExecutor(RestTemplate template) {
    if (Objects.isNull(template)) {
      throw new IllegalArgumentException("restTemplate cannot be null");
    }
    this.restTemplate = template;
  }

  public static RestTemplateWebExecutor create(RestTemplate template) {
    if (Objects.isNull(template)) {
      throw new IllegalArgumentException("restTemplate cannot be null");
    }
    return new RestTemplateWebExecutor(template);
  }

  public static RestTemplateWebExecutor create(boolean disableSslValidation) {
    if (disableSslValidation) {
      HttpsURLConnectionConfigurer.defaultDisableSSLValidation();
    }
    return create(new RestTemplate());
  }

  public static RestTemplateWebExecutor create() {
    return create(true);
  }

  public void setParametersConverter(ParametersConverter converter) {
    if (Objects.isNull(converter)) {
      throw new IllegalArgumentException("parameters converter cannot be null");
    }
    this.parametersConverter = converter;
  }

  public void setInterceptors(Interceptor... interceptors) {
    if (Objects.isNull(interceptors)) {
      throw new IllegalArgumentException("interceptors cannot be null");
    }
    Interceptors newInterceptors = new Interceptors(interceptors);
    restTemplate.getInterceptors().add(0, new HttpInterceptorInterceptors(newInterceptors));
  }

  @Override
  public <T> T execute(String url, Class<T> responseClass, Object... uriVariables) throws WebException {
    if (Objects.isNull(url)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(responseClass)) {
      throw new IllegalArgumentException("response class cannot be null");
    }
    return executeInternal(url, createResponseExtractor(responseClass), uriVariables);
  }

  @Override
  public <T> T execute(String url, ResponseConverter<T> converter, Object... uriVariables) throws WebException {
    if (Objects.isNull(url)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(converter)) {
      throw new IllegalArgumentException("response converter cannot be null");
    }
    return executeInternal(url, createResponseExtractor(converter), uriVariables);
  }

  @Override
  public void execute(String url, ResponseConsumer consumer, Object... uriVariables) throws WebException {
    if (Objects.isNull(url)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(consumer)) {
      throw new IllegalArgumentException("response consumer cannot be null");
    }
    executeInternal(url, createResponseExtractor(consumer), uriVariables);
  }

  @Override
  public <T> T execute(String url, Parameters parameters, Class<T> responseClass, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(parameters)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(responseClass)) {
      throw new IllegalArgumentException("response class cannot be null");
    }
    return executeInternal(url, parameters, createResponseExtractor(responseClass), uriVariables);
  }

  @Override
  public <T> T execute(String url, Parameters parameters, ResponseConverter<T> converter, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(parameters)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(converter)) {
      throw new IllegalArgumentException("response converter cannot be null");
    }
    return executeInternal(url, parameters, createResponseExtractor(converter), uriVariables);
  }

  @Override
  public void execute(String url, Parameters parameters, ResponseConsumer consumer, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(parameters)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(consumer)) {
      throw new IllegalArgumentException("response consumer cannot be null");
    }
    executeInternal(url, parameters, createResponseExtractor(consumer), uriVariables);
  }

  @Override
  public <T> T execute(String url, Object parameters, Class<T> responseClass, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(parameters)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(responseClass)) {
      throw new IllegalArgumentException("response class cannot be null");
    }
    return execute(url, convertParameters(parameters), responseClass, uriVariables);
  }

  @Override
  public <T> T execute(String url, Object parameters, ResponseConverter<T> converter, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(parameters)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(converter)) {
      throw new IllegalArgumentException("response converter cannot be null");
    }
    return execute(url, convertParameters(parameters), converter, uriVariables);
  }

  @Override
  public void execute(String url, Object parameters, ResponseConsumer consumer, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(parameters)) {
      throw new IllegalArgumentException("url and parameters cannot be null");
    }
    if (Objects.isNull(consumer)) {
      throw new IllegalArgumentException("response consumer cannot be null");
    }
    execute(url, convertParameters(parameters), consumer, uriVariables);
  }

  @Override
  public <T> T execute(String url, ParametersCreator creator, Class<T> responseClass, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(creator)) {
      throw new IllegalArgumentException("url and parameters creator cannot be null");
    }
    if (Objects.isNull(responseClass)) {
      throw new IllegalArgumentException("response class cannot be null");
    }
    return execute(url, creator.create(), responseClass, uriVariables);
  }

  @Override
  public <T> T execute(String url, ParametersCreator creator, ResponseConverter<T> converter, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(creator)) {
      throw new IllegalArgumentException("url and parameters creator cannot be null");
    }
    if (Objects.isNull(converter)) {
      throw new IllegalArgumentException("response converter cannot be null");
    }
    return execute(url, creator.create(), converter, uriVariables);
  }

  @Override
  public void execute(String url, ParametersCreator creator, ResponseConsumer consumer, Object... uriVariables) throws WebException {
    if (Objects.isNull(url) || Objects.isNull(creator)) {
      throw new IllegalArgumentException("url and parameters creator cannot be null");
    }
    if (Objects.isNull(consumer)) {
      throw new IllegalArgumentException("response consumer cannot be null");
    }
    execute(url, creator.create(), consumer, uriVariables);
  }

  private <T> T executeInternal(String url, ResponseExtractor<T> extractor, Object... uriVariables) throws WebExecutionException {
    try {
      return restTemplate.execute(url, GET, wrapParametersRequestCallback(null), extractor, uriVariables);
    } catch (RestClientException e) {
      throw new WebExecutionException(e.getMessage(), e);
    }
  }

  private <T> T executeInternal(String url, Parameters parameters, ResponseExtractor<T> extractor, Object... uriVariables) throws WebExecutionException {
    try {
      return restTemplate.execute(url, POST, wrapParametersRequestCallback(parameters), extractor, uriVariables);
    } catch (RestClientException e) {
      throw new WebExecutionException(e.getMessage(), e);
    }
  }

  private Parameters convertParameters(Object parameters) throws ParametersConversionException {
    if (Objects.isNull(parameters)) {
      throw new IllegalArgumentException("parameters cannot be null");
    }
    if (Objects.isNull(parametersConverter)) {
      throw new ParametersConversionException("missing parameters converter");
    }
    return parametersConverter.convert(parameters);
  }

  private <T> ResponseExtractor<T> createResponseExtractor(Class<T> responseClass) {
    if (Objects.isNull(responseClass)) {
      throw new IllegalArgumentException("response class cannot be null");
    }
    return new HttpMessageConverterExtractor<>(responseClass, restTemplate.getMessageConverters());
  }

  private <R> ResponseExtractor<R> createResponseExtractor(ResponseConverter<R> converter) {
    if (Objects.isNull(converter)) {
      throw new IllegalArgumentException("response converter cannot be null");
    }
    return new ResponseConverterExtractor<>(converter);
  }

  private ResponseExtractor<Void> createResponseExtractor(ResponseConsumer consumer) {
    if (Objects.isNull(consumer)) {
      throw new IllegalArgumentException("response consumer cannot be null");
    }
    return new ResponseConsumerExtractor(consumer);
  }

  private RequestCallback wrapParametersRequestCallback(Parameters parameters) {
    if (Objects.isNull(parameters)) {
      return restTemplate.httpEntityCallback(null);
    }
    HttpHeaders hHeaders = new HttpHeaders();
    if (parameters.getType() == Parameters.Type.JSON) {
      hHeaders.setContentType(MediaType.APPLICATION_JSON);
      return restTemplate.httpEntityCallback(new HttpEntity<>(extractParameters(parameters), hHeaders));
    } else {
      if (parameters.getType() == Parameters.Type.PARAMETERS) {
        hHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      }
      else if (parameters.getType() == Parameters.Type.MULTIPART_PARAMETERS) {
        hHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
      }
      MultiValueMap<String, Object> converted = new LinkedMultiValueMap<>();
      for (Parameter<?> parameter : parameters) {
        if (!(parameter instanceof Parameters)) {
          converted.put(parameter.getName(), Collections.singletonList(parameter.getValue()));
        }
      }
      return restTemplate.httpEntityCallback(new HttpEntity<>(converted, hHeaders));
    }
  }

  private Map<String, Object> extractParameters(Parameters parameters) {
    if (Objects.isNull(parameters)) {
      return null;
    }
    Map<String, Object> extracted = new HashMap<>();
    for (Parameter<?> parameter : parameters) {
      // ParametersParameter
      if (parameter.getValue() instanceof Parameters) {
        extracted.put(parameter.getName(), extractParameters((Parameters) parameter.getValue()));
      }
      // CollectionParameter
      else if (parameter.getValue() instanceof Collection) {
        Collection<Object> theValue = new ArrayList<>();
        for (Object item : ((Collection<?>) parameter.getValue())) {
          if (item instanceof Boolean || item instanceof String || item instanceof Number || item instanceof byte[]) {
            theValue.add(item);
            continue;
          }
          theValue.add(extractParameters((Parameters) item));
        }
        extracted.put(parameter.getName(), theValue);
      }
      // other Parameter
      else {
        extracted.put(parameter.getName(), parameter.getValue());
      }
    }
    return extracted;
  }

  /**
   *
   * @author gaigeshen
   */
  private static class ResponseConverterExtractor<R> implements ResponseExtractor<R> {

    private final ResponseConverter<R> converter;

    private ResponseConverterExtractor(ResponseConverter<R> converter) {
      if (Objects.isNull(converter)) {
        throw new IllegalArgumentException("response converter cannot be null");
      }
      this.converter = converter;
    }

    @Override
    public R extractData(ClientHttpResponse response) throws IOException {
      Headers headers = new DefaultHeaders();
      response.getHeaders().forEach(headers::addValues);
      return converter.convert(response.getBody(), headers);
    }
  }

  /**
   *
   * @author gaigeshen
   */
  private static class ResponseConsumerExtractor implements ResponseExtractor<Void> {

    private final ResponseConsumer consumer;

    private ResponseConsumerExtractor(ResponseConsumer consumer) {
      if (Objects.isNull(consumer)) {
        throw new IllegalArgumentException("response consumer cannot be null");
      }
      this.consumer = consumer;
    }

    @Override
    public Void extractData(ClientHttpResponse response) throws IOException {
      Headers headers = new DefaultHeaders();
      response.getHeaders().forEach(headers::addValues);
      consumer.consume(response.getBody(), headers);
      return null;
    }
  }

  /**
   *
   * @author gaigeshen
   */
  private static class HttpInterceptorInterceptors implements ClientHttpRequestInterceptor {

    private final Interceptors interceptors;

    private HttpInterceptorInterceptors(Interceptors interceptors) {
      if (Objects.isNull(interceptors)) {
        throw new IllegalArgumentException("interceptors cannot be null");
      }
      this.interceptors = interceptors;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution execution) throws IOException {
      log.info(">>>> URI: [{}] {}", httpRequest.getMethod(), httpRequest.getURI());
      log.info(">>>> Headers: {}", httpRequest.getHeaders());
      log.info(">>>> Body: {}", new String(body, StandardCharsets.UTF_8));
      if (!interceptors.hasInterceptors()) {
        ClientHttpResponse httpResponse = execution.execute(httpRequest, body);
        log.info("<<<< Status: {}", httpResponse.getStatusCode());
        log.info("<<<< Headers: {}", httpResponse.getHeaders());
        return httpResponse;
      }
      HttpRequestRequest request = new HttpRequestRequest(httpRequest, body);
      Interceptor.Response response = interceptors.intercept(request, new HttpRequestExecution(execution));
      if (Objects.isNull(response)) {
        throw new WebExecutionException("response from interceptor cannot be null");
      }
      if (response instanceof HttpResponseResponse) {
        HttpResponseResponse httpResponse = (HttpResponseResponse) response;
        log.info("<<<< Status: {}", httpResponse.getStatusCode());
        log.info("<<<< Headers: {}", httpResponse.getHeaders());
        return httpResponse;
      }
      throw new WebExecutionException("please don't produce response instance by interceptor");
    }
  }

  /**
   *
   * @author gaigeshen
   */
  private static class HttpRequestExecution implements Execution {

    private final ClientHttpRequestExecution execution;

    private HttpRequestExecution(ClientHttpRequestExecution execution) {
      if (Objects.isNull(execution)) {
        throw new IllegalArgumentException("http request execution cannot be null");
      }
      this.execution = execution;
    }

    @Override
    public Interceptor.Response execute(Interceptor.Request request) throws WebExecutionException {
      if (!(request instanceof HttpRequestRequest)) {
        throw new IllegalArgumentException("can only support HttpRequestRequest");
      }
      HttpRequest httpRequest = ((HttpRequestRequest) request).httpRequest;
      try {
        ClientHttpResponse httpResponse = execution.execute(httpRequest, request.bodyBytes());
        return new HttpResponseResponse(httpResponse);
      } catch (IOException e) {
        throw new WebExecutionException("could not execute: " + request, e);
      }
    }
  }

  /**
   *
   * @author gaigeshen
   */
  private static class HttpRequestRequest implements Interceptor.Request {

    private final HttpRequest httpRequest;

    private final byte[] bodyBytes;

    private HttpRequestRequest(HttpRequest httpRequest, byte[] bodyBytes) {
      if (Objects.isNull(httpRequest)) {
        throw new IllegalArgumentException("http request cannot be null");
      }
      if (Objects.isNull(bodyBytes)) {
        throw new IllegalArgumentException("body bytes cannot be null");
      }
      this.httpRequest = httpRequest;
      this.bodyBytes = bodyBytes;
    }

    @Override
    public String url() {
      return httpRequest.getURI().toString();
    }

    @Override
    public String method() {
      return httpRequest.getMethodValue();
    }

    @Override
    public Headers headers() {
      return new HttpHeadersHeaders(httpRequest.getHeaders());
    }

    @Override
    public byte[] bodyBytes() {
      return bodyBytes;
    }

    @Override
    public String toString() {
      return httpRequest.toString();
    }
  }

  /**
   *
   * @author gaigeshen
   */
  private static class HttpResponseResponse extends AbstractClientHttpResponse implements Interceptor.Response {

    private final ClientHttpResponse httpResponse;

    private byte[] buffer;

    private HttpResponseResponse(ClientHttpResponse httpResponse) {
      if (Objects.isNull(httpResponse)) {
        throw new IllegalArgumentException("http response cannot be null");
      }
      this.httpResponse = httpResponse;
    }

    @Override
    public Headers headers() {
      return new HttpHeadersHeaders(httpResponse.getHeaders());
    }

    @Override
    public void buffered(byte[] bodyBytes) {
      buffer = bodyBytes;
    }

    @Override
    public InputStream bodyStream() throws IOException {
      if (Objects.isNull(buffer)) {
        return httpResponse.getBody();
      }
      return new ByteArrayInputStream(buffer);
    }

    @Override
    public int getRawStatusCode() throws IOException {
      return httpResponse.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
      return httpResponse.getStatusText();
    }

    @Override
    public void close() {
      httpResponse.close();
    }

    @Override
    public InputStream getBody() throws IOException {
      if (Objects.isNull(buffer)) {
        return httpResponse.getBody();
      }
      return new ByteArrayInputStream(buffer);
    }

    @Override
    public HttpHeaders getHeaders() {
      return httpResponse.getHeaders();
    }
  }

  /**
   *
   * @author gaigeshen
   */
  private static class HttpHeadersHeaders implements Headers {

    private final HttpHeaders httpHeaders;

    private HttpHeadersHeaders(HttpHeaders httpHeaders) {
      if (Objects.isNull(httpHeaders)) {
        throw new IllegalArgumentException("http headers cannot be null");
      }
      this.httpHeaders = httpHeaders;
    }

    @Override
    public void addValue(String name, String value) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      if (Objects.isNull(value)) {
        throw new IllegalArgumentException("value cannot be null");
      }
      addValues(name, Collections.singletonList(value));
    }

    @Override
    public void addValues(String name, List<String> values) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      if (Objects.isNull(values) || values.isEmpty()) {
        throw new IllegalArgumentException("values cannot be null or empty");
      }
      httpHeaders.addAll(name, values);
    }

    @Override
    public void putValue(String name, String value) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      if (Objects.isNull(value)) {
        throw new IllegalArgumentException("value cannot be null");
      }
      putValues(name, Collections.singletonList(value));
    }

    @Override
    public void putValues(String name, List<String> values) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      if (Objects.isNull(values) || values.isEmpty()) {
        throw new IllegalArgumentException("values cannot be null or empty");
      }
      httpHeaders.put(name, values);
    }

    @Override
    public List<String> getValues(String name) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      return httpHeaders.get(name);
    }

    @Override
    public String getValue(String name) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      return httpHeaders.getFirst(name);
    }

    @Override
    public boolean contains(String name) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      return httpHeaders.containsKey(name);
    }

    @Override
    public void remove(String name) {
      if (Objects.isNull(name)) {
        throw new IllegalArgumentException("name cannot be null");
      }
      httpHeaders.remove(name);
    }

    @Override
    public String toString() {
      return httpHeaders.toString();
    }
  }
}
