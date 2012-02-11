/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import secop2p.util.MessageReceivedCallback;
import secop2p.util.MessageStreamReader;
import secop2p.util.MessageStreamWriter;

/**
 *
 * @author eros
 */
public class ServiceRepositoryProxy implements MessageReceivedCallback {

    public static int DEFAULT_INTERVAL = 60*60;

    private final InetSocketAddress repoAddr;
    private LocalMap map = null;
    private long lastUpdateTime = 0;

    public ServiceRepositoryProxy(String host, int port) throws IOException{
        this(host, port, DEFAULT_INTERVAL);
    }

    public ServiceRepositoryProxy(String host, int port, int interval) throws IOException{
        this(new InetSocketAddress(host, port), interval);
    }

    public ServiceRepositoryProxy(final InetSocketAddress repoAddr) throws IOException{
        this(repoAddr, DEFAULT_INTERVAL);
    }

    public ServiceRepositoryProxy(final InetSocketAddress repoAddr, int interval) throws IOException{
        this.repoAddr = repoAddr;
        updateLocalMap();
    }

    @SuppressWarnings("SleepWhileHoldingLock")
    private void updateLocalMap() throws IOException{
        final int timeout = 5000;
        final long callTime = System.currentTimeMillis();
        final SocketChannel sc = SocketChannel.open(repoAddr);
        final MessageStreamWriter msw = new MessageStreamWriter(sc);
        msw.close();
        final MessageStreamReader msr = new MessageStreamReader(sc, this);
        msr.run();
        sc.close();

        do{
            try {
                Thread.sleep(100);
                if(System.currentTimeMillis() - callTime > timeout){
                    updateLocalMap();
                    return;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceRepositoryProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }while(lastUpdateTime < callTime);
    }

    private void updateIfNecessary() throws IOException{
        if(System.currentTimeMillis() - lastUpdateTime > DEFAULT_INTERVAL)
            updateLocalMap();
    }

    public Set<EngineInfo> getEngines() throws IOException{
        updateIfNecessary();
        return map.getEngines();
    }

    public Set<Service> getServices() throws IOException{
        updateIfNecessary();
        return map.getServices();
    }

    public EngineInfo getEngineByName(String name) throws IOException{
        updateIfNecessary();
        for(EngineInfo e : map.getEngines())
            if(e.getName().equals(name))
                return e;
        return null;
    }

    public Service getServiceByName(String name) throws IOException{
        updateIfNecessary();
        for(Service s : map.getServices())
            if(s.getName().equals(name))
                return s;
        return null;
    }

    public Set<EngineInfo> getEnginesMappedToService(Service s) throws IOException{
        updateIfNecessary();
        Map<Service,Set<EngineInfo>> servMap = map.getServicesMap();
        if(servMap.containsKey(s))
            return servMap.get(s);
        return null;
    }

    public void messageReceived(Object o) {
        if(o instanceof LocalMap){
            this.map = (LocalMap) o;
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) throws IOException{
        ServiceRepositoryProxy srp = new ServiceRepositoryProxy("127.0.0.1",8000);
        for(Service s : srp.getServices())
            for(EngineInfo e : srp.getEnginesMappedToService(s))
                System.out.println(e.getName()+" supports "+s.getName());
    }

}
