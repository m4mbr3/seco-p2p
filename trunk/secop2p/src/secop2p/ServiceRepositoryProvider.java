/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.util.logging.Level;
import java.util.logging.Logger;
import secop2p.util.PortChecker;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import secop2p.util.Listener;
import secop2p.util.ListenerCallback;
import secop2p.util.MessageReceivedCallback;
import secop2p.util.MessageStreamReader;
import secop2p.util.MessageStreamWriter;

/**
 *
 * @author eros
 */
public final class ServiceRepositoryProvider implements ListenerCallback, MessageReceivedCallback {

    private static final int DEFAULT_PORT = 8000;
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
        Set<EngineInfo> engines = new HashSet<EngineInfo>( Arrays.asList(
            sr.getEnginesList()
        ) );
        Set<Service> services = new HashSet<Service>( Arrays.asList(
            sr.getServicesList()
        ) );
        Set<Relation> relations = new HashSet<Relation>( Arrays.asList(
            sr.getRelationList()
        ) );
        return new LocalMap(engines, services, relations);
    }

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

    public void messageReceived(Object o) {
        try {
            if(o instanceof EngineInfo){
                EngineInfo e = (EngineInfo) o;
                if(!Arrays.asList(sr.getEnginesList()).contains(e))
                    sr.addNewEngine(e);
            } else if(o instanceof Service){
                Service s = (Service)o;
                if(!Arrays.asList(sr.getServicesList()).contains(s))
                    sr.addNewService(s);
            } else if(o instanceof Relation){
                Relation r = (Relation) o;
                if(!Arrays.asList(sr.getRelationList()).contains(r))
                    sr.addRelServiceEngine(r.getService(), r.getEngine());
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServiceRepositoryProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
