/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.seco.qp.engine.routing;

import org.seco.qp.engine.routing.util.PortChecker;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.seco.qp.engine.routing.util.Listener;
import org.seco.qp.engine.routing.util.ListenerCallback;
import org.seco.qp.engine.routing.util.Sender;
import org.seco.qp.engine.routing.util.Serializer;

/**
 *
 * @author eros
 */
public class EngineMonitor implements ListenerCallback, Sender.MessageGenerator, Sender.TargetGenerator {

    private final EngineInfo engineInfo;
    private final ServiceRepositoryProxy srp;
    private Map<EngineInfo, Message> messages;


    public EngineMonitor(EngineInfo ei) throws IOException{
        this(ei, new ServiceRepositoryProxy(ei));
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public EngineMonitor(EngineInfo ei, ServiceRepositoryProxy srp){
        engineInfo = ei;
        this.srp = srp;
        messages = new TreeMap<EngineInfo, Message>();
        ServerSocketChannel ssc = PortChecker.getBoundedServerSocketChannelOrNull(ei.getAlivePort());
        new Listener(ssc, this);
        new Sender(this, this);
    }

    public byte[] generateMessage(Object... args){
        //return this.getName()+" - I'm alive at "+(int)(System.currentTimeMillis()/1000);
        Metrics m = new LocalMetrics(0.5);
        try{
            byte[] msg = Serializer.serialize(new Message(this.engineInfo, m));
            return msg;
        }catch(IOException ex){
            Logger.getLogger(EngineMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }catch(Exception ex){
            Logger.getLogger(EngineMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void handleRequest(SocketChannel client) {
        synchronized(System.out){
            Message m;
            try {
                m = Serializer.deserialize(client.socket().getInputStream(), Message.class);
                System.out.println();
                System.out.println("Engine "+engineInfo+" has received message:\n\t"+m);
                System.out.println();
                messages.put(m.getEngine(), m);
            } catch (IOException ex) {
                Logger.getLogger(EngineMonitor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(EngineMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Set<InetSocketAddress> getTargetsList(Object... args) {
        Set<InetSocketAddress> targets = new HashSet<InetSocketAddress>();
        try {
            for (EngineInfo ei : srp.getEngines()) {
                if( ei.compareTo(this.engineInfo) != 0)
                    targets.add(ei.getAliveSocketAddress());
            }
        } catch (IOException ex) {
            Logger.getLogger(EngineMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return targets;
    }

    private Set<EngineInfo> filterEngines(Set<EngineInfo> set){
        Set<EngineInfo> engines = new TreeSet(set);
        for(EngineInfo e : engines){
            if(!messages.containsKey(e)){
                srp.banEngine(e);
                engines.remove(e);
            } else {
                long timeout_time = messages.get(e).getTimestamp() + ServiceRepositoryProxy.BAN_TIME;
                if(timeout_time < System.currentTimeMillis()){
                    srp.banEngine(e);
                    engines.remove(e);
                }
            }
        }
        return engines;
    }

    public Set<EngineInfo> getAliveEngines() throws IOException{
        return filterEngines(srp.getEngines());
    }

    public Set<EngineInfo> getAliveEnginesMappedToService(Service s) throws IOException{
        return filterEngines(srp.getEnginesMappedToService(s));
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) throws IOException{
        int i = 0;
        int id = Integer.parseInt(args[i++]);
        int port = Integer.parseInt(args[i++]);
        int alive_port = Integer.parseInt(args[i++]);
        EngineInfo ei = new EngineInfo(id, "eng"+id, "127.0.0.1", port, alive_port);
        new EngineMonitor(ei);
    }

    public EngineInfo getBestSuitedEngine(Service s) throws IOException{
        Set<EngineInfo> engs = getAliveEnginesMappedToService(s);
        EngineInfo best = null;
        Metrics m = null;
        for(EngineInfo ei : engs){
            if(messages.containsKey(ei)){
                Metrics mt = messages.get(ei).getMetrics();
                if(best == null){
                    best = ei;
                    m = mt;
                }else if(mt.evaluate() > m.evaluate()){
                    best = ei;
                    m = mt;
                }
            }
        }
        return best;
    }

}



