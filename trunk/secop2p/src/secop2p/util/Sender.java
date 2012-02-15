/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eros
 */
public class Sender {

    public static int DEFAULT_INTERVAL = -1;
    public static int DEFAULT_DELAY = 500;
    private Timer t;
    private MyTask mt;
    private TargetGenerator targetGenerator;
    private MessageGenerator messageGenerator;

    public Sender(TargetGenerator tg, MessageGenerator mg){
        this(tg, mg, DEFAULT_INTERVAL);
    }

    public Sender(TargetGenerator tg, MessageGenerator mg, int interval){
        t = new Timer();
        mt = new MyTask();
        this.targetGenerator = tg;
        this.messageGenerator = mg;
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
            byte[] msg;
            try {
                msg = messageGenerator.generateMessage();
                Set<InetSocketAddress> targets = targetGenerator.getTargetsList();
                for(InetSocketAddress isa : targets){
                    Socket s = new Socket();
                    s.connect(isa);
                    s.getOutputStream().write(msg);
                    s.getOutputStream().close();
                    s.getInputStream().close();
                    s.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void stop(){
        mt.cancel();
        t.cancel();
        t.purge();
    }
}

interface MessageGenerator {
    public byte[] generateMessage(Object... args);
}

interface TargetGenerator {
    public Set<InetSocketAddress> getTargetsList(Object... args);
}