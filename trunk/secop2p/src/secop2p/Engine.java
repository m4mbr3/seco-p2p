/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import secop2p.util.MessageReceivedCallback;
import secop2p.util.MessageStreamReader;
import secop2p.util.MessageStreamWriter;

/**
 *
 * @author eros
 */
public class Engine extends EngineInfo implements MessageReceivedCallback {

    public static String DEFAULT_LISTEN_HOST = "0.0.0.0";
    public static int DEFAULT_LISTEN_PORT = 9000;
    public static String DEFAULT_REPO_HOST = "127.0.0.1";
    public static int DEFAULT_REPO_PORT = 8000;
    public static long DEFAULT_MAP_TIMEOUT = 60*60*1000;

    private Set<Service> supported_services;
    private final InetSocketAddress servRepo;
    private LocalMap map;
    private long lastUpdate = 0;

    public Engine(String name) throws ClassNotFoundException, IOException{
        this(
            name,
            new InetSocketAddress(DEFAULT_LISTEN_HOST, DEFAULT_LISTEN_PORT),
            new InetSocketAddress(DEFAULT_REPO_HOST, DEFAULT_REPO_PORT)
        );
    }

    public Engine(String name, String host, int port) throws ClassNotFoundException, IOException{
        this(
            name,
            new InetSocketAddress(host, port),
            new InetSocketAddress(DEFAULT_REPO_HOST, DEFAULT_REPO_PORT)
        );
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public Engine(String name, InetSocketAddress listenTo, InetSocketAddress servRepo) throws ClassNotFoundException, IOException{
        super(name, listenTo.getHostName(), listenTo.getPort());
        this.servRepo = servRepo;
        if( ! getLocalMap().getEngines().contains(this) ){
            SocketChannel sc = SocketChannel.open(servRepo);
            MessageStreamWriter msw = new MessageStreamWriter(sc);
            msw.writeMessage((EngineInfo)this);
            msw.close();
            MessageStreamReader msr = new MessageStreamReader(sc, this);
            msr.run();
            sc.close();
        }
    }

    public void addService(Service s) throws UnknownHostException, IOException, ClassNotFoundException {
        supported_services = new HashSet<Service>();
        supported_services.add(s);
        SocketChannel sc = SocketChannel.open(servRepo);
        MessageStreamWriter msw = new MessageStreamWriter(sc);
        if(!getLocalMap().getServices().contains(s)){
            msw.writeMessage(s);
        }
        msw.writeMessage(new Relation(s, (EngineInfo)this));
        msw.close();
        MessageStreamReader msr = new MessageStreamReader(sc, this);
        msr.run();
        sc.close();
    }

    public synchronized void updateLocalMap() throws IOException, ClassNotFoundException{
        //Socket sc = new Socket();
        SocketChannel sc = SocketChannel.open(servRepo);
        MessageStreamWriter msw = new MessageStreamWriter(sc);
        msw.close();
        MessageStreamReader msr = new MessageStreamReader(sc, this);
        msr.run(); //Run synchonously in this thread
        sc.close();
    }

    private synchronized LocalMap getLocalMap() throws IOException, ClassNotFoundException{
        if(map == null || lastUpdate < System.currentTimeMillis() - DEFAULT_MAP_TIMEOUT){
            updateLocalMap();
        }
        return map;
    }

    public void messageReceived(Object o) {
        if(o instanceof LocalMap){
            this.map = (LocalMap) o;
            lastUpdate = System.currentTimeMillis();
        }
    }

}
