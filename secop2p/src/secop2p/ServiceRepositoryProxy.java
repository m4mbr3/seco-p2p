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
import java.util.TreeMap;
import java.util.TreeSet;
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
    public static long BAN_TIME = 5*60;

    private final EngineInfo thisEngine;
    private final InetSocketAddress repoAddr;
    private LocalMap map = null;
    private long lastUpdateTime = 0;
    private Map<EngineInfo, Long> bannedEngines;

    public ServiceRepositoryProxy(final EngineInfo thisEngine) throws IOException{
        this(thisEngine, "127.0.0.1", ServiceRepositoryProvider.DEFAULT_PORT);
    }

    public ServiceRepositoryProxy(final EngineInfo thisEngine, String host, int port) throws IOException{
        this(thisEngine, host, port, DEFAULT_INTERVAL);
    }

    public ServiceRepositoryProxy(final EngineInfo thisEngine, String host, int port, int interval) throws IOException{
        this(thisEngine, new InetSocketAddress(host, port), interval);
    }

    public ServiceRepositoryProxy(final EngineInfo thisEngine, final InetSocketAddress repoAddr) throws IOException{
        this(thisEngine, repoAddr, DEFAULT_INTERVAL);
    }

    public ServiceRepositoryProxy(final EngineInfo thisEngine, final InetSocketAddress repoAddr, int interval) throws IOException{
        this.repoAddr = repoAddr;
        this.thisEngine = thisEngine;
        bannedEngines = new TreeMap<EngineInfo, Long>();
        updateLocalMap();
    }

    @SuppressWarnings("SleepWhileHoldingLock")
    private void updateLocalMap() throws IOException{
        final int timeout = 5000;
        final long callTime = System.currentTimeMillis();
        final SocketChannel sc = SocketChannel.open(repoAddr);
        final MessageStreamWriter msw = new MessageStreamWriter(sc);
        //TODO get real metrics
        Metrics m = new LocalMetrics(0.5);
        msw.writeMessage(new AliveMessage(thisEngine, m));
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
        return getEnginesMappedToService(s, true);
    }

    public Set<EngineInfo> getEnginesMappedToService(Service s, boolean filtering) throws IOException{
        updateIfNecessary();
        Map<Service,Set<EngineInfo>> servMap = map.getServicesMap();
        if(servMap.containsKey(s)){
            if(filtering)
                return filterBanned(servMap.get(s));
            else
                servMap.get(s);
        }
        return null;
    }

    public void banEngine(EngineInfo ei){
        if(bannedEngines.containsKey(ei))
            bannedEngines.remove(ei);
        bannedEngines.put(ei, System.currentTimeMillis()+BAN_TIME);
    }

    private Set<EngineInfo> filterBanned(final Set<EngineInfo> list){
        final Set<EngineInfo> set = new TreeSet<EngineInfo>(list);
        final long now = System.currentTimeMillis();
        for(EngineInfo ei : bannedEngines.keySet())
            if(bannedEngines.get(ei) < now)
                bannedEngines.remove(ei);
            else if(set.contains(ei))
                set.remove(ei);
        return set;
    }

    public void messageReceived(Object o) {
        if(o instanceof LocalMap){
            this.map = (LocalMap) o;
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) throws IOException{
        /*EngineInfo
        ServiceRepositoryProxy srp = new ServiceRepositoryProxy(ei,"127.0.0.1",8000);
        for(Service s : srp.getServices())
            for(EngineInfo e : srp.getEnginesMappedToService(s))
                System.out.println(e.getName()+" supports "+s.getName());
         * 
         */
    }

}
