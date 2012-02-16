/*
 * @(#)CompositeInvoker.java   1.0   Ago 30, 2010
 *
 * Copyright 2010-2010 Politecnico di Milano. All Rights Reserved.
 *
 * This software is the proprietary information of Politecnico di Milano.
 * Use is subject to license terms.
 *
 * @(#) $Id: CompositeInvoker.java 1670 2011-06-13 14:10:50Z barbieri $
 */
package org.seco.qp.engine.invokers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.seco.qp.engine.Invoker;
import org.seco.qp.engine.routing.EngineInfo;
import org.seco.qp.engine.routing.Service;
import org.seco.qp.engine.routing.ServiceRepositoryProxy;
import org.seco.qp.engine.routing.EngineMonitor;
import org.seco.qp.model.AccessMethod;
import org.slf4j.Logger;

/**
 * A composite invoker.
 * <p>
 * This invoker implements the chain of responsibility pattern. It wraps an ordered list of
 * invokers. An invocation request is handled by querying each wrapped invoker: the first
 * able to handle the request is selected.
 * </p>
 * 
 * @author Francesco Corcoglioniti &lt;francesco.corcoglioniti@gmail.com&gt;
 * @author Salvatore Vadacca &lt;vadacca@elet.polimi.it&gt;
 */
public class CompositeInvoker implements Invoker {

    /** The wrapped invokers. */
    private final List<Invoker> wrappedInvokers;
    private final static Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositeInvoker.class);

    private static Properties properties;
    private static EngineInfo thisEngine;
    private static ServiceRepositoryProxy serviceRepoProxy;
    private static EngineMonitor engineMonitor;

    /**
     * Creates a new composite invoker wrapping the invokers specified.
     * 
     * @param wrappedInvokers
     *           the wrapped invokers, possibly null or empty.
     */
    public CompositeInvoker(final Invoker... wrappedInvokers) {
        this.wrappedInvokers = wrappedInvokers == null ? null : Arrays.asList(wrappedInvokers);
        loadConfig();
    }

    /**
     * Creates a new composite invoker wrapping the invokers specified.
     *
     * @param wrappedInvokers
     *           the wrapped invokers, possibly null or empty.
     */
    public CompositeInvoker(final List<Invoker> wrappedInvokers) {
        this.wrappedInvokers = wrappedInvokers == null ? Collections.<Invoker>emptyList()
                : ImmutableList.copyOf(wrappedInvokers);
        loadConfig();
    }

    private void loadConfig(){
        String propertiesFile = "../../../resources/org/seco/qp/server/default.properties";
        propertiesFile = propertiesFile.replace("/", File.separator);
        if(System.getenv().containsKey("SECO_CONF"))
            propertiesFile = System.getenv("SECO_CONF");
        properties = new Properties();
        try {
            properties.load( new FileInputStream( propertiesFile ) );
        } catch (IOException ex) {
            LOG.warn("Properties file cannot be read by CompositeInvoker");
        }
        assert properties.containsKey("server.name");
        assert properties.containsKey("api.host");
        assert properties.containsKey("api.port");
        assert properties.containsKey("api.alive_port");
        assert properties.containsKey("routing.repo_host");
        assert properties.containsKey("routing.repo_port");
        assert properties.containsKey("routing.engine_id");

        try {
            thisEngine = new EngineInfo( getEngineId(), getEngineName(), getEngineHost(), getEnginePort(), getAlivePort() );
            serviceRepoProxy = new ServiceRepositoryProxy( thisEngine, getRepoHost(), getRepoPort() );
            engineMonitor = new EngineMonitor( thisEngine, serviceRepoProxy );
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CompositeInvoker.class.getName()).log(Level.SEVERE, null, ex);
        }
        assert serviceRepoProxy != null;
        assert engineMonitor != null;
    }

    public static String getEngineName(){
        return properties.getProperty("server.name");
    }

    public static String getEngineHost(){
        return properties.getProperty("api.host");
    }

    public static int getEnginePort(){
        return Integer.parseInt( properties.getProperty("api.port") );
    }

    public static int getEngineId(){
        return Integer.parseInt( properties.getProperty("routing.engine_id") );
    }

    public static int getAlivePort(){
        return Integer.parseInt( properties.getProperty("api.alive_port") );
    }

    public static String getRepoHost(){
        return properties.getProperty("routing.repo_host");
    }

    public static int getRepoPort(){
        return Integer.parseInt( properties.getProperty("routing.repo_port") );
    }

    public static EngineInfo getThisEngineInfo(){
        return thisEngine;
    }

    public static ServiceRepositoryProxy getServiceRepositoryProxy(){
        return serviceRepoProxy;
    }

    public static EngineMonitor getEngineMonitor(){
        return engineMonitor;
    }

    /**
     * {@inheritDoc} The request is supported if there is a wrapped invoker supporting it.
     */
    @Override
    public boolean supports(final Request request) {

        // Check parameters.
        Preconditions.checkNotNull(request);

        // Delegate
        return this.select(request) != null;
    }

    /**
     * {@inheritDoc} This method applies the chain of responsibility pattern until an invoker
     * accepting the request is found; the corresponding cursor will be returned.
     */
    @Override
    public Cursor invoke(final Request request) {

        // Check parameters.
        Preconditions.checkNotNull(request);

        // Select an invoker able to handle the request.
        final Invoker invoker = this.select(request);
        Preconditions.checkArgument(invoker != null, "Unsupported request");

        LOG.trace("invoking {}", invoker);
        // Delegate.
        return invoker.invoke(request);
    }

    /**
     * Selects an invoker among the wrapped ones able to handle the invocation request
     * specified.
     * 
     * @param request
     *           the invocation request, not null.
     * @return the selected invoker, or <tt>null</tt> if a suitable invoker cannot be found.
     */
    private Invoker select(final Request request) {

        // Check parameters.
        assert request != null;

        // Find a suitable invoker for the request.

        final AccessMethod method = request.getAccessMethod(); 
        final String serviceId = method.getAggregatedId();
        // Get the engine name from server properties
        String localEngineName = getEngineName();
        boolean isSupportedLocally = true;
        try {
            isSupportedLocally = isLocal(serviceId, localEngineName);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CompositeInvoker.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (final Invoker wrappedInvoker : this.wrappedInvokers) {
            boolean isRemoteInvoker = wrappedInvoker instanceof RemoteInvoker;
            if(isSupportedLocally && ! isRemoteInvoker){
                if(wrappedInvoker.supports(request))
                   return wrappedInvoker;
            }else if( !isSupportedLocally && isRemoteInvoker){
                if(wrappedInvoker.supports(request))
                    return wrappedInvoker;
            }
        }

        // Return null if the request is unsupported.
        LOG.warn("No suitable service found for serviceId "+serviceId);
        return null;
    }

    /*
    
     */
    private boolean isLocal(final String serviceId, 
            final String localEngineName) throws IOException {
        
        boolean isLocal = false;
        
        Service serv = serviceRepoProxy.getServiceByName(serviceId);
        if(serv == null){
            LOG.warn("Routing table has no engine available for service "+serviceId);
            return false;
        }

        Set<EngineInfo> engines = serviceRepoProxy.getEnginesMappedToService(serv, false);
        isLocal = engines.contains(thisEngine);
        LOG.info("isLocal: "+isLocal+" -- service: "+serviceId+" -- engine: "+localEngineName);
        return isLocal;
    }

    /**
     * {@inheritDoc} The returned string lists the wrapped invokers.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("wrappedInvokers", this.wrappedInvokers).toString();
    }
}
