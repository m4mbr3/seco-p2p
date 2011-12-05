package secop2p;

/**
 * @author eros
 */
public class Service {

    private int id;
    private String name;

    /*
     * Dummy Service class, only contains the name of the service
     */
    public Service(int id, String name){
        this.id = id;
        this.name = name;
    }
    public Service(String name){
        this.name = name;
    }
    public Service(){

    }

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
