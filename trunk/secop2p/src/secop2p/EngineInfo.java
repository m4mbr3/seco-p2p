package secop2p;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import secop2p.util.Serializer;

/**
 * @author eros
 *
 * Dummy class that represents an engine
 */
public class EngineInfo implements Serializable, RemoteEngine, Comparable {

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
        try {
            InetAddress ia = InetAddress.getByName(host);
            return new InetSocketAddress(ia, port);
        } catch (UnknownHostException ex) {
            Logger.getLogger(EngineInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public InetSocketAddress getAliveSocketAddress() {
        try {
            InetAddress ia = InetAddress.getByName(host);
            return new InetSocketAddress(ia, alivePort);
        } catch (UnknownHostException ex) {
            Logger.getLogger(EngineInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

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
