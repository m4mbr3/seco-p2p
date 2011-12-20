/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p.util;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eros
 */
public class Listener extends Thread {

    private ServerSocketChannel ssc;
    private boolean stopped = true;
    private ListenerCallback callback;
    private Object callee;

    public Listener(int port, ListenerCallback callback){
        this(port, callback, true);
    }

    public Listener(int port, ListenerCallback callback, boolean autostart){
        this(PortChecker.getBoundedServerSocketChannelOrNull(port), callback, autostart);
    }

    public Listener(ServerSocketChannel ssc, ListenerCallback callback){
        this(ssc, callback, true);
    }

    public Listener(ServerSocketChannel ssc, ListenerCallback callback, boolean autostart){
        this.callback = callback;
        this.ssc = ssc;
        if(autostart){
            new Timer().schedule(
                new TimerTask(){
                    @Override
                    public void run() {
                        Listener.this.start();
                    }
                }, 0
            );
        }
    }

    @Override
    public void run(){
        stopped = false;
        try{
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    Listener.this.stopped = true;
                }
            });
            Selector selector = Selector.open();
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while(!stopped){
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                while(!keys.isEmpty()){
                    SelectionKey k = keys.iterator().next();
                    keys.remove(k);
                    SocketChannel s = ssc.accept();
                    if(s != null){
                        new RequestHandler(s).start();
                    }
                }
            }
            ssc.close();
        }catch(Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close(){
        stopped = true;
    }

    class RequestHandler extends Thread {

        private SocketChannel sc;

        public RequestHandler(SocketChannel sc){
            this.sc = sc;
        }

        @Override
        public void run(){
            try {
                callback.handleRequest(sc);
            } finally {
                try{
                    sc.close();
                }catch(IOException e){
                    //Do nothing
                }
            }
                /*
                try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int rbytes;
                while( (rbytes = sc.socket().getInputStream().read(buf)) > -1){
                baos.write(buf,0,rbytes);
                }
                baos.close();
                try{
                sc.socket().getInputStream().close();
                }catch(SocketException e){}
                try{
                sc.socket().getOutputStream().close();
                }catch(SocketException e){}
                try{
                sc.socket().close();
                }catch(SocketException e){}
                try{
                sc.close();
                }catch(SocketException e){}
                // invoke the callback with the received text
                callback.invoke(callee, baos.toByteArray());
                } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex.getCause());
                }*/
        }

    }

}

