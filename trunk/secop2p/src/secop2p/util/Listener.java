/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketException;
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

    private int port;
    private boolean stopped = false;
    private Method callback;
    private Object callee;

    public Listener(int port, Method callback, Object callee){
        this(port, callback, callee, true);
    }

    public Listener(int port, Method callback, Object callee, boolean autostart){
        this.port = port;
        this.callback = callback;
        this.callee = callee;
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
        try{
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    Listener.this.stopped = true;
                }
            });
            Selector selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(port));
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
            }
        }

    }

}

