package secop2p;



public class Relation {

    private int[] tuple;

    public Relation(int id_Service, int id_EngineInfo)
    {
        tuple = new int[2];
        tuple[0] = id_Service;
        tuple[1] = id_EngineInfo;
    }

    public int[] getTuple() {
        return tuple;
    }

    public void setTuple(int[] tuple) {
        this.tuple = tuple;
    }
    
    




}