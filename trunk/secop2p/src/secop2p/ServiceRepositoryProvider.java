/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import secop2p.util.PortChecker;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import secop2p.util.Listener;
import secop2p.util.ListenerCallback;
import secop2p.util.Serializer;

/**
 *
 * @author eros
 */
public final class ServiceRepositoryProvider implements ListenerCallback {

    private static final int DEFAULT_PORT = 8000;
    private final ServiceRepository sr;
    private int port;
    Listener l;

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

    public void handleRequest(InputStream in, OutputStream out) {
        try {
            LocalMap map = getLocalMap();
            out.write( Serializer.serialize(map) );
        } catch (IOException ex) {
            Logger.getLogger(ServiceRepositoryProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ServiceRepositoryProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, NoSuchMethodException{
        ServiceRepositoryProvider srp = new ServiceRepositoryProvider();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {}
        Socket s = new Socket("127.0.1.1", srp.port);
        LocalMap map = Serializer.deserialize( s.getInputStream(), LocalMap.class );
        System.out.println( map );
    }

}
