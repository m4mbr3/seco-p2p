/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.seco.qp.engine.routing;

import java.io.Serializable;
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
        int time = (int)(System.currentTimeMillis()/1000);
        return "Engine "+from.getName()+" is alive at "+time+" with metrics "+m;
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
    
}

interface Metrics extends Comparable, Serializable {

    public int evaluate();

}

final class LocalMetrics implements Metrics {

    public static int SCALE_FACTOR = 5000;
    double load;

    /**
     * @param load Should be in range 0 to 1
     */
    public LocalMetrics(double load){
        this.load = load;
    }

    public int evaluate(){
        return (int) Math.round(this.load*SCALE_FACTOR);
    }

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
