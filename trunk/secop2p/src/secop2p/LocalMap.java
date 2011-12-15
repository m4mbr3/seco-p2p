package secop2p;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;


public final class LocalMap implements Serializable{
       
    
       private Set<EngineInfo> engines;
       private Map<Service, Set<EngineInfo>> services;

       public LocalMap(){
               engines = Collections.EMPTY_SET;
               services = Collections.EMPTY_MAP;
       }
       public  LocalMap(Set<EngineInfo> engines, Set<Service> services,Set<Relation> relations ){
                this.engines  = engines;
                for(Service s : services)
                    this.services.put(s, new HashSet<EngineInfo>());
                for(Relation r : relations)
                    this.services.get(r.getService()).add(r.getEngine());
       }
       public Set<EngineInfo> getEngines() {
                return engines;
       }

      public Map<Service, Set<EngineInfo>> getServicesMap() {
                return services;
      }

      public Set<Service> getServices() {
                return services.keySet();
      }
}