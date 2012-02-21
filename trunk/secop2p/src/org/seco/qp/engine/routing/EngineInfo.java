package org.seco.qp.engine.routing;

import java.io.Serializable;
import java.net.InetSocketAddress;
import org.seco.qp.engine.routing.util.Serializer;

/**
 * @author eros
 *
 * Dummy class that represents an engine
 */
public class EngineInfo implements Serializable, Comparable {

    private int id;
    private String name;
    private String host;
    private int port;
    private int alivePort;

    public EngineInfo(int id, String name, String host, int port, int aPort){
        this.id = id;
        this.name=name;
        this.host=host;
        this.port=port;
        this.alivePort = aPort;
    }

    public EngineInfo(String name, String host, int port){
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public EngineInfo(){
        //Nothing to check
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAlivePort(int port) {
        this.alivePort = port;
    }

    public String getName(){
        return name;
    }

    public int getId(){
        return id;
    }

    public String getHost(){
        return host;
    }

    public int getPort(){
        return port;
    }

    public int getAlivePort(){
        return alivePort;
    }

    @Override
    public String toString(){
        return Serializer.toXML(this);
    }

    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public InetSocketAddress getAliveSocketAddress() {
        return new InetSocketAddress(host, alivePort);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + this.id;
        hash = 43 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 43 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 43 * hash + this.port;
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof EngineInfo){
            EngineInfo ei = (EngineInfo) o;
            if( ei.host.equals(this.host) && ei.port == this.port )
                return true;
            else if( ei.name.equals(this.name))
                return true;
            else if( ei.id == this.id )
                return true;
        }
        return false;
    }

    @Override
    public int compareTo(Object t) {
        if(t instanceof EngineInfo){
            EngineInfo e = (EngineInfo) t;
            if(e.host.equals(this.host)){
                return this.port - e.port;
            }else
                return e.getHost().compareTo(this.host);
        }else
            return Integer.MAX_VALUE;
    }

}
