package org.seco.qp.engine.routing;


import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;
import org.seco.qp.engine.routing.util.Serializer;


public final class LocalMap implements Serializable{
       
    private Set<EngineInfo> engines;
    private Map<Service, Set<EngineInfo>> services;

    public LocalMap(){
        engines = Collections.EMPTY_SET;
        services = Collections.EMPTY_MAP;
    }

    public  LocalMap(Set<EngineInfo> engines, Set<Service> services,Set<Relation> relations ){
        this.engines  = engines;
        this.services = new TreeMap<Service, Set<EngineInfo>>();
        for(Service s : services)
            this.services.put(s, new TreeSet<EngineInfo>());
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

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("<LocalMap>\n");
        sb.append("\t<EngineList>");
        for(EngineInfo ei : engines)
            sb.append(ei.getName()+" ");
        sb.append("</EngineList>\n");
        sb.append("\t<ServiceList>\n");
        for(Service s : services.keySet()){
            sb.append("\t\t"+s.getName()+" -> ");
            for(EngineInfo ei : services.get(s))
                sb.append(ei.getName()+", ");
            sb.append("\n");
        }
        sb.append("\t</ServiceList>\n");
        sb.append("</LocalMap>");
        //return Serializer.toXML(this);
        return sb.toString();
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, NoSuchMethodException {
        ServiceRepositoryProvider srp = new ServiceRepositoryProvider();
        System.out.println( srp.getLocalMap() );
    }
    
}