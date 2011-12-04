package javaapplication1;

/**
 * @author eros
 *
 * Dummy class that represents an engine
 */
public class EngineInfo {

    private String name;
    private String host;
    private int port;

    public EngineInfo(String name, String host, int port){
        this.name=name;
        this.host=host;
        this.port=port;
    }

    public String getName(){
        return name;
    }

}
