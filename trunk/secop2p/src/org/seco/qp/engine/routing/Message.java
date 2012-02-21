/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.seco.qp.engine.routing;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.seco.qp.engine.routing.util.Serializer;

/**
 *
 * @author eros
 */
class Message implements Serializable {

    private EngineInfo from;
    private Metrics m;
    private long timestamp;

    public Message(EngineInfo from, Metrics m){
        this.from = from;
        this.m = m;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString(){
        long time = System.currentTimeMillis();
        return "Engine "+from.getName()+" is alive at "+timestampToString(time)+" with metrics "+m.evaluate();
    }

    public EngineInfo getEngine(){
        return from;
    }

    public Metrics getMetrics(){
        return m;
    }

    public long getTimestamp(){
        return timestamp;
    }

    private static String timestampToString(long time){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(c.getTime());
    }
    
}

interface Metrics extends Comparable, Serializable {

    public int evaluate();

}

final class LocalMetrics implements Metrics {

    public static int SCALE_FACTOR = 5000;
    double load;

    public LocalMetrics(){
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        this.load = os.getSystemLoadAverage() / os.getAvailableProcessors();
        if(load < 0)
            load = 0.75;
    }

    /**
     * @param load Should be in range 0 to 1
     */
    public LocalMetrics(double load){
        this.load = load;
    }

    @Override
    public int evaluate(){
        return (int) Math.round(SCALE_FACTOR * Math.pow(Math.E, -load));
    }

    @Override
    public int compareTo(Object t) {
        if(t instanceof Metrics){
            return evaluate()- ((Metrics) t).evaluate();
        }else
            return Integer.MAX_VALUE;
    }

    @Override
    public String toString(){
        return Serializer.toXML(this);
    }

}
