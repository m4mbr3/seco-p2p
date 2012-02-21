/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.seco.qp.engine.routing;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.seco.qp.engine.routing.util.PortChecker;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Set;
import org.seco.qp.engine.routing.util.Listener;
import org.seco.qp.engine.routing.util.MessageStreamReader;
import org.seco.qp.engine.routing.util.MessageStreamWriter;

/**
 *
 * @author eros
 */
public final class ServiceRepositoryProvider implements Listener.ListenerCallback, MessageStreamReader.MessageReceivedCallback {

    public static final int DEFAULT_PORT = 8000;
    public static final int INVALIDATE_ENGINE_LAST_UPDATE_DELTA = 60*60*1000;
    private final ServiceRepository sr;
    private int port;
    private Listener l;

    public ServiceRepositoryProvider() throws SQLException, ClassNotFoundException, IOException, NoSuchMethodException{
        this(0);
    }

    public ServiceRepositoryProvider(int port) throws SQLException, ClassNotFoundException, IOException, NoSuchMethodException{
        this(port, null);
    }

    public ServiceRepositoryProvider(ServiceRepository sr) throws SQLException, ClassNotFoundException, IOException, NoSuchMethodException{
        this(0, sr);
    }

    public ServiceRepositoryProvider(int port, ServiceRepository sr) throws SQLException, ClassNotFoundException, IOException, NoSuchMethodException{
        if(sr != null)
            this.sr = sr;
        else
            this.sr = new ServiceRepository();
        ServerSocketChannel ssc;
        if(port != 0){
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress("0.0.0.0", port));
            this.port = port;
        }else{
            ssc = PortChecker.getBoundedServerSocketChannel(DEFAULT_PORT);
            this.port = ssc.socket().getLocalPort();
        }
        l = new Listener(ssc, this);
    }

    public LocalMap getLocalMap() throws SQLException {
        long timestamp = System.currentTimeMillis() - INVALIDATE_ENGINE_LAST_UPDATE_DELTA;
        Set<EngineInfo> engines = sr.getAliveEngines(timestamp);
        Set<Service> services = sr.getServicesList();
        Set<Relation> relations = sr.getAliveRelationList(timestamp);
        return new LocalMap(engines, services, relations);
    }

    @Override
    public void handleRequest(SocketChannel client) {
        try {
            MessageStreamReader msr = new MessageStreamReader(client, this);
            msr.run(); // read all object sent from the client
            MessageStreamWriter msw = new MessageStreamWriter(client);
            msw.writeMessage(getLocalMap());
            msw.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServiceRepositoryProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServiceRepositoryProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop(){
        l.close();
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, NoSuchMethodException{
        ServiceRepositoryProvider srp = new ServiceRepositoryProvider();
    }

    @Override
    public void messageReceived(Object o) {
        try {
            if(o instanceof EngineInfo){
                EngineInfo e = (EngineInfo) o;
                if(!sr.getEnginesList().contains(e))
                    sr.addNewEngine(e);
            } else if(o instanceof Service){
                Service s = (Service)o;
                if(!sr.getServicesList().contains(s))
                    sr.addNewService(s);
            } else if(o instanceof Relation){
                Relation r = (Relation) o;
                if(!sr.getRelationList().contains(r))
                    sr.addRelServiceEngine(r.getService(), r.getEngine());
            }else if(o instanceof Message){
                Message m = (Message) o;
                EngineInfo from = m.getEngine();
                if(!sr.getEnginesList().contains(from))
                    sr.addNewEngine(from);
                sr.updateLastAliveTimestamp(from, m.getTimestamp());
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServiceRepositoryProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getPort(){
        return port;
    }

}
