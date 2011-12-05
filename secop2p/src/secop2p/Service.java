package javaapplication1;

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

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

}
