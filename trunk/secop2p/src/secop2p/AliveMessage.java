/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.io.Serializable;
import secop2p.util.Serializer;

/**
 *
 * @author eros
 */
public class AliveMessage implements Serializable {

    private final EngineInfo from;
    private final long timestamp;
    private final Metrics metrics;

    public AliveMessage(final EngineInfo ei, final Metrics m){
        from = ei;
        timestamp = System.currentTimeMillis();
        metrics = m;
    }

    public EngineInfo getFrom() {
        return from;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toString(){
        return Serializer.toXML(this);
    }

}
