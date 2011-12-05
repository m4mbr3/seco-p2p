package javaapplication1;

/**
 * @author eros
 *
 * Dummy class that represents an engine
 */
public class EngineInfo {

    private int id;
    private String name;
    private String host;
    private int port;

    public EngineInfo(int id, String name, String host, int port){
        this.id = id;
        this.name=name;
        this.host=host;
        this.port=port;
    }

    public String getName(){
        return name;
    }

    public int getId(){
        return id;
    }

    public String getHost(){
        return host;
    }

    public int getPort(){
        return port;
    }

}
