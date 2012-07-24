package com.rackspace.papi.components.ratelimit;

import com.rackspace.papi.commons.config.manager.UpdateListener;

import com.rackspace.papi.components.ratelimit.write.ActiveLimitsWriter;
import com.rackspace.papi.components.ratelimit.write.CombinedLimitsWriter;
import com.rackspace.papi.filter.logic.AbstractConfiguredFilterHandlerFactory;

import com.rackspace.papi.service.datastore.Datastore;

import com.rackspace.repose.service.ratelimit.RateLimitingServiceFactory;
import com.rackspace.repose.service.ratelimit.cache.ManagedRateLimitCache;
import com.rackspace.repose.service.ratelimit.cache.RateLimitCache;
import com.rackspace.repose.service.ratelimit.config.RateLimitingConfiguration;
import com.rackspace.repose.service.ratelimit.RateLimitingService;

import java.util.*;
import java.util.regex.Pattern;

public class RateLimitingHandlerFactory extends AbstractConfiguredFilterHandlerFactory<RateLimitingHandler> {

   private final RateLimitCache rateLimitCache;
   //Volatile
   private Pattern describeLimitsUriRegex;
   private RateLimitingConfiguration rateLimitingConfig;
   private RateLimitingService service;

   public RateLimitingHandlerFactory(Datastore datastore) {
      rateLimitCache = new ManagedRateLimitCache(datastore);
   }

   @Override
   protected Map<Class, UpdateListener<?>> getListeners() {
      final Map<Class, UpdateListener<?>> listenerMap = new HashMap<Class, UpdateListener<?>>();
      listenerMap.put(RateLimitingConfiguration.class, new RateLimitingConfigurationListener());

      return listenerMap;
   }

   private class RateLimitingConfigurationListener implements UpdateListener<RateLimitingConfiguration> {

      @Override
      public void configurationUpdated(RateLimitingConfiguration configurationObject) {
         service = RateLimitingServiceFactory.createRateLimitingService(rateLimitCache, configurationObject);
         describeLimitsUriRegex = Pattern.compile(configurationObject.getRequestEndpoint().getUriRegex());
         rateLimitingConfig = configurationObject;
      }
   }

   @Override
   protected RateLimitingHandler buildHandler() {

      final ActiveLimitsWriter activeLimitsWriter = new ActiveLimitsWriter(rateLimitingConfig.getRequestEndpoint().getLimitsFormat());
      final CombinedLimitsWriter combinedLimitsWriter = new CombinedLimitsWriter(rateLimitingConfig.getRequestEndpoint().getLimitsFormat());
      final RateLimitingServiceHelper serviceHelper = new RateLimitingServiceHelper(service, activeLimitsWriter, combinedLimitsWriter);

      return new RateLimitingHandler(serviceHelper, rateLimitingConfig.getRequestEndpoint().isIncludeAbsoluteLimits(),
                                     rateLimitingConfig.isDelegation(), describeLimitsUriRegex);
   }
}
