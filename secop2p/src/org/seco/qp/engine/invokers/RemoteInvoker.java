/*
 * RemoteInvoker implementation
 * Check support
 * Establish the http connection
 */
package org.seco.qp.engine.invokers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.seco.common.data.Tuple;
import org.seco.common.data.TupleType;
import org.seco.qp.model.methods.SPARQLService.Prefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Set;
import org.seco.common.serialization.Deserializer;
import org.seco.common.serialization.StreamReader;
import org.seco.common.serialization.json.JSONStreamReader;
import org.seco.qp.engine.routing.EngineInfo;
import org.seco.qp.engine.routing.EngineMonitor;
import org.seco.qp.engine.routing.Service;
import org.seco.qp.engine.routing.ServiceRepositoryProxy;
import org.seco.qp.model.AccessMethod;
import org.seco.qp.model.AccessModifiers;

/**
 * Remote invoker.
 * <p>
 * This class extends {@link HttpInvoker} and allows accessing a remote endpoint as an
 * <em>exact</em> service. An instance of this class may be configured by providing a default
 * endpoint URL to be used with services not providing their own, and a default list of
 * prefixes to be merged with the ones specified in the service descriptor.
 * </p>
 * 
 * @author Nguyen Ho, Eros Lever, Andrea Mambre
 */
public class RemoteInvoker extends HttpInvoker {

   /** Logger. */
   private static final Logger LOG = LoggerFactory.getLogger(RemoteInvoker.class);

   /** The default endpoint URL to be used for services not providing their own. */
   //private final String defaultEndpointURL;

   /** The default prefixes, to be merged with the ones provided by the service. */
   //private final List<Prefix> defaultPrefixes;

   /**
    * Creates a new instance for the scheduler, HTTP client and default endpoint URL and
    * prefixes specified.
    * 
    * @param scheduler
    *           the scheduler used to implement modifiers, not null.
    * @param client
    *           the HTTP client, not null.
    * @param defaultEndpointURL
    *           the default URL of the endpoint to be used for services not specifying their
    *           own, possibly null.
    * @param defaultPrefixes
    *           a list of default prefixes to be merged with the ones provided by the service
    *           (the latter having priority).
    */
   public RemoteInvoker(final ScheduledExecutorService scheduler, final HttpClient client){
         //final String defaultEndpointURL, final List<Prefix> defaultPrefixes) {

      // Invoke parent class constructor.
      super(scheduler, client);

      // Initialize the object.
      /*this.defaultEndpointURL = defaultEndpointURL;
      this.defaultPrefixes = defaultPrefixes == null ? Collections.<Prefix> emptyList()
            : ImmutableList.copyOf(defaultPrefixes);*/
   }

    /**
     * {@inheritDoc} Checks that the service is a SPARQL service.
     */
    @Override
    public boolean supports(final Request request) {
        Preconditions.checkNotNull(request);
        //return request.getAccessMethod() instanceof SPARQLService;
        EngineInfo ei = CompositeInvoker.getThisEngineInfo();
        ServiceRepositoryProxy srp = CompositeInvoker.getServiceRepositoryProxy();
        EngineMonitor em = CompositeInvoker.getEngineMonitor();
        String serviceName = request.getAccessMethod().getAggregatedId();
        try {
            Service s = srp.getServiceByName(serviceName);
            if (s == null) {
                LOG.warn("Service "+serviceName+" is not present in the routing table");
                return false;
            }else{
                Set<EngineInfo> engines = em.getAliveEnginesMappedToService(s);
                int minSize = engines.contains(ei) ? 1 : 0;
                if(engines.size() > minSize){
                    return true;
                }else{
                    LOG.warn("No engine available for service "+serviceName);
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RemoteInvoker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

   /**
    * {@inheritDoc} Performs a GET request to the URL returned by
    * {@link #computeURL(SPARQLService, Tuple)}.
    */
   @Override
   protected void initSubmitExchange(final Request request, final int fetchIndex,
         final ContentExchange submitExchange) throws Throwable {

      // Check parameters.
      assert request != null;
      assert fetchIndex >= 0;
      assert submitExchange != null;
     
      //get the support engine
      final AccessMethod accessMethod = request.getAccessMethod();
      String sourceId = accessMethod.getSource().getId(); //how to get the serviceId?      
      String accessMethodId = accessMethod.getId(); //how to get the serviceId?
      
      // Port
      ServiceRepositoryProxy sr = CompositeInvoker.getServiceRepositoryProxy();
      Service s = sr.getServiceByName( accessMethod.getAggregatedId() );
      if(s == null)
          LOG.warn("Service "+accessMethod.getAggregatedId() + " not in the routing table");
      EngineMonitor em = CompositeInvoker.getEngineMonitor();
      EngineInfo ei = em.getBestSuitedEngine(s);
      String host = ei.getHost();
      int port = ei.getPort();
      //compute submit URL
      String submitURL = "http://" + host + ":" + port + "/engine/invoke?";
      
      submitURL += "_sourceId=" + sourceId + "&";
      submitURL += "_accessMethodId=" + accessMethodId + "&";
      
      final Tuple inputTuple = request.getInputTuple();
      
      final TupleType inputTupleType = inputTuple.getType();
      
      for (String fieldName : inputTupleType.fieldNames()) {
         submitURL += fieldName + "=" + URLEncoder.encode((String)inputTuple.get(fieldName),"UTF-8") + "&";
      }
     
      final AccessModifiers modifiers = request.getAccessMethod().getAggregatedModifiers();
      final Integer maxFetches = modifiers.getMaxFetches();
      final Integer maxTuples = modifiers.getMaxTuples();
      
      if (maxFetches != null) {
        submitURL += "_maxFetches=" + maxFetches + "&";
      }
      if (maxTuples != null) {
        submitURL += "_maxTuples=" + maxTuples + "&";
      }
      
      submitURL = submitURL.substring(0, submitURL.length() - 1);

      LOG.info("Requesting URL: "+submitURL);
      
      submitExchange.setMethod("GET");
      submitExchange.setURL(submitURL);
      submitExchange.setRequestHeader("Accept", "application/json"); //???what is the header
   }

   /**
    * {@inheritDoc} Parses the SPARQL XML response. A single chunk is expected.
    */
   @Override
   protected Results parseResults(final Request request, final int fetchIndex,
         final ContentExchange exchange) throws Throwable {

      // Check parameters.
      assert request != null;
      assert fetchIndex >= 0;
      assert exchange != null;

      // Check content type.
      final String contentType = exchange.getResponseFields().getStringField("Content-Type");
      if (contentType == null || !contentType.startsWith("application/json")) {
         LOG.warn("Unexpected content type {}: "
               + "will proceed, but deserialization may fail", contentType);
      }
      
      final String content = exchange.getResponseContent().replace("\"EOF\" : ", "\"eof\" : ");
      
      StreamReader reader;
      Object resultToSend = null;
      try {
        reader = new JSONStreamReader(new StringReader(content));
        final Deserializer deserializer = new Deserializer(reader);
        resultToSend = deserializer.read(Results.class);
      } catch (IOException ex) {
        LOG.debug("JSON parser was unable to parse the results: "+ex.getMessage());
        //ex.printStackTrace();
        return null;
      }
      
      // Return fetch results.
      return (Results) resultToSend;
   }

}
