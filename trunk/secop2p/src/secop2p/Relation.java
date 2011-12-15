package secop2p;

import java.io.Serializable;



public class Relation implements Serializable, Comparable {

    private Service s;
    private EngineInfo e;

    public Relation(Service s, EngineInfo e) {
        this.s = s;
        this.e = e;
    }

    public Service getService(){
        return s;
    }

    public EngineInfo getEngine(){
        return e;
    }

    public int compareTo(Object t) {
        if(t instanceof Relation){
            Relation r = (Relation)t;
            int upper = e.getId() - r.e.getId();
            int lower = s.getId()-r.s.getId();
            return (upper << Integer.SIZE/2) + lower;
        }else
            return Integer.MAX_VALUE;
    }

    @Override
    public String toString(){
        return "<Relation>"+e+s+"</Relation>";
    }

}