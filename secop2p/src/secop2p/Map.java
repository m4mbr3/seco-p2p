package secop2p;
import java.util.ArrayList;

public final class Map{
       ArrayList<EngineInfo> engineInfoList;
       ArrayList<Service> service;
       ArrayList<Relation> relation;

       public  Map(ArrayList<EngineInfo> engineInfoList, ArrayList<Service> service,ArrayList<Relation> relation )
       {
            this.engineInfoList  = engineInfoList;
            this.service = service;
            this.relation = relation;
       }
       public Map()
       {
           engineInfoList = null;
           service = null;
           relation = null;
       }

    public ArrayList<EngineInfo> getEngineInfoList() {
        return engineInfoList;
    }

    public void setEngineInfoList(ArrayList<EngineInfo> engineInfoList) {
        this.engineInfoList = engineInfoList;
    }

    public ArrayList<Relation> getRelation() {
        return relation;
    }

    public void setRelation(ArrayList<Relation> relation) {
        this.relation = relation;
    }

    public ArrayList<Service> getService() {
        return service;
    }

    public void setService(ArrayList<Service> service) {
        this.service = service;
    }
    
}