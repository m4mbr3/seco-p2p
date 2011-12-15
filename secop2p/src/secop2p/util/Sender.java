/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import secop2p.EngineInfo;
import secop2p.RemoteEngine;

/**
 *
 * @author eros
 */
public class Sender {

    public static int DEFAULT_INTERVAL = -1;
    public static int DEFAULT_DELAY = 500;
    private Timer t;
    private MyTask mt;
    private Set<RemoteEngine> targets;
    private Method callback;
    private Object callee;

    public Sender(Set<RemoteEngine> targets, Method callback, Object callee){
        this(targets, callback, callee, DEFAULT_INTERVAL);
    }

    public Sender(Set<RemoteEngine> targets, Method callback, Object callee, int interval){
        t = new Timer();
        mt = new MyTask();
        this.targets = Collections.synchronizedSet(targets);
        this.callback = callback;
        this.callee = callee;
        if(interval >= 0){
            this.privateStart(interval);
        }
    }

    private void privateStart(int interval){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                Sender.this.stop();
            }
        });
        t.scheduleAtFixedRate(mt, DEFAULT_DELAY, interval);
    }

    public void start(int interval){
        this.privateStart(interval);
    }

    class MyTask extends TimerTask {

        @Override
        public void run() {
            byte[] msg = "I'm alive".getBytes();
            try {
                msg = (byte[]) callback.invoke(callee);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(RemoteEngine e : targets.toArray(new RemoteEngine[0])){
                try {
                    Socket s = new Socket();
                    s.connect(e.getSocketAddress());
                    s.getOutputStream().write(msg);
                    try{
                        s.getOutputStream().close();
                    }catch(SocketException ex){}
                    try{
                        s.getInputStream().close();
                    }catch(SocketException ex){}
                    try{
                        s.close();
                    }catch(SocketException ex){}
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    public void stop(){
        mt.cancel();
        t.cancel();
        t.purge();
    }
}
