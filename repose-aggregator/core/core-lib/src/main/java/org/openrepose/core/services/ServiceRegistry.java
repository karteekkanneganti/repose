package org.openrepose.core.services;

import org.openrepose.core.services.context.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Deprecated
public class ServiceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistry.class);
    private final List<ServiceContext> boundServiceContexts;

    public ServiceRegistry() {
        boundServiceContexts = new LinkedList<ServiceContext>();
    }
    
    public void addService(ServiceContext context) {
        LOG.debug("Registering Context with service registry: " + context.getServiceName());
        boundServiceContexts.add(context);
    }
    
    public List<ServiceContext> getServices() {
        return boundServiceContexts;
    }
}
