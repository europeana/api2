package eu.europeana.api2.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Registers the endpoints eg. /info .This is to fix broken endpoint functionality
 *  With spring-boot-actuator on version 1.3.8.RELEASE  and  spring mvc upgrade to 5.3.16 version from 4.3.23.RELEASE
 * @author Shwetambara Nazare
 * Created on 03-01-2024
 */
public class EndpointHandlerMappingCustom extends RequestMappingHandlerMapping {

  Logger log = LogManager.getLogger(EndpointHandlerMappingCustom.class);

  private final Set<MvcEndpoint> endpoints;
  private final CorsConfiguration corsConfiguration;
  private final String prefix;
  private boolean disabled;

  private MvcEndpoint  handler;

  public EndpointHandlerMappingCustom(Collection<? extends MvcEndpoint> endpoints) {
    this(endpoints, null);
  }

  public EndpointHandlerMappingCustom(Collection<? extends MvcEndpoint> endpoints, CorsConfiguration corsConfiguration) {
    this.prefix = "";
    this.disabled = false;
    this.endpoints = new HashSet<>(endpoints);
    this.corsConfiguration = corsConfiguration;
    this.setOrder(-100);
  }


  @Override
  protected boolean isHandler(Class<?> beanType) {
    return false;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public boolean isDisabled() {
    return this.disabled;
  }


  @Override
  protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingInfo mappingInfo) {
    return this.corsConfiguration;
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    if (!this.disabled) {
      Iterator<MvcEndpoint> endpoint = this.endpoints.iterator();
      while (endpoint.hasNext()) {
        handler = endpoint.next();
        log.info("Registering handler mapping for : {}",handler.getPath());
        this.detectHandlerMethods(handler);
      }
    }
  }

  /** To get endpoints URL same as pre spring-mvc version upgrade **/
  @Override
  protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    RequestMappingInfo mapping =  super.getMappingForMethod(method,handlerType);
    if (mapping != null) {
      String[] patterns = this.getPatterns(handler, mapping);
      return this.withNewPatterns(mapping, patterns);
    }
    return null;
  }

  private String[] getPatterns(Object handler, RequestMappingInfo mapping) {
    String path = this.getPath(handler);
    String prefixVal = StringUtils.hasText(this.prefix) ? this.prefix + path : path;

    PatternsRequestCondition patternsCondition = mapping.getPatternsCondition();
    Set<String> defaultPatterns = patternsCondition != null ? patternsCondition.getPatterns() : new HashSet<>();
    if (defaultPatterns.isEmpty()) {
      return new String[]{prefixVal, prefixVal + ".json"};
    } else {
      List<String> patterns = new ArrayList<>(defaultPatterns);
      for(int i = 0; i < patterns.size(); ++i) {
        patterns.set(i, prefixVal +  patterns.get(i));
      }
      return  patterns.toArray(new String[patterns.size()]);
    }
  }

  private String getPath(Object handler) {
    ApplicationContext applicationContext = this.getApplicationContext();
    if (applicationContext != null && handler instanceof String) {
      handler = applicationContext.getBean((String)handler);
    }
    return handler instanceof MvcEndpoint ? ((MvcEndpoint)handler).getPath() : "";
  }

  private RequestMappingInfo withNewPatterns(RequestMappingInfo mapping, String[] patternStrings) {
    PatternsRequestCondition patterns = new PatternsRequestCondition(patternStrings, null, null, this.useSuffixPatternMatch(),
            this.useTrailingSlashMatch(), null);
    return new RequestMappingInfo(patterns,
            mapping.getMethodsCondition(),
            mapping.getParamsCondition(),
            mapping.getHeadersCondition(),
            mapping.getConsumesCondition(),
            mapping.getProducesCondition(),
            mapping.getCustomCondition());
  }
}
