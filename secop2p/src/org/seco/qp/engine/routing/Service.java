package org.seco.qp.engine.routing;

import java.io.Serializable;
import org.seco.qp.engine.routing.util.Serializer;

/**
 * @author eros
 */
public class Service implements Serializable, Comparable {

    private int id;
    private String name;

    /*
     * Dummy Service class, only contains the name of the service
     */
    public Service(int id, String name){
        this.id = id;
        this.name = name;
    }

    public Service(String name){
        this.name = name;
    }
    
    public Service(){

    }

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return Serializer.toXML(this);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Service){
            Service s = (Service) o;
            if(s.id == this.id)
                return true;
            if(s.name.equals(this.name))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.id;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object t) {
        if(t instanceof Service){
            Service s = (Service) t;
            return name.compareTo(s.name);
        }else
            return Integer.MAX_VALUE;
    }

}
