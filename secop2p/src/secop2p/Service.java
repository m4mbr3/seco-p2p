package secop2p;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("<"+this.getClass().getSimpleName());
        for(Field f : this.getClass().getDeclaredFields()){
            try {
                sb.append(" "+f.getName() + "='" + f.get(this) + "'");
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(EngineInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(EngineInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sb.append(">");
        return sb.toString();
    }

}
