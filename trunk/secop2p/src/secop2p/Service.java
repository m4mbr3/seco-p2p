package javaapplication1;

/**
 * @author eros
 */
public class Service {

    private String name;

    /*
     * Dummy Service class, only contains the name of the service
     */
    public Service(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
