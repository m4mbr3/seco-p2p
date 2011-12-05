/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author eros
 */
public class ServiceRepository {

    private final static String DB_CONN = "jdbc:sqlite:ServiceRepository.sqlite";
    private Connection conn;
    private PreparedStatement selectServicesList;
    private PreparedStatement selectEnginesList;
    private PreparedStatement selectEngineServices;
    private PreparedStatement selectServiceEngines;
    private PreparedStatement addService;
    private PreparedStatement addEngine;
    private PreparedStatement addServiceEngine;
    private PreparedStatement delService;
    private PreparedStatement delServiceEngine;
    private PreparedStatement delEngine;
    private PreparedStatement delEngineService;
    /*
     * Default constructor, tries to connect to "ServiceRepository.sqlite" 
     * in the same directory (if it exists!)
     */
    public ServiceRepository() throws SQLException, ClassNotFoundException{
        this(DB_CONN);
    }

    /*
     * @param db_conn   jdbc connection string,
     * please use jdbc:sqlite:path-to-file.db
     * if you need to connect to sqlite
     */
    public ServiceRepository(String db_conn) throws SQLException, ClassNotFoundException{
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection( db_conn );
        selectServicesList = conn.prepareStatement(
                "SELECT * FROM services");
        selectEnginesList = conn.prepareStatement(
                "SELECT * FROM engines");
        selectEngineServices = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE engine_id = ?");
        selectServiceEngines = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE service_id = ?");
        addService = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO services (name) VALUES (? ) ");
        addEngine = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO engines (name, host, port) VALUES (?,?,?)" );
        addServiceEngine = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO service_map (service_id,engine_id) VALUES (?,?)");
        delService = conn.prepareStatement(
                "DELETE FROM services  WHERE id = ?");
        delServiceEngine = conn.prepareStatement(
                "DELETE FROM service_map WHERE service_id = ?");
        delEngine = conn.prepareStatement(
                "DELETE FROM engines  WHERE id = ?");
        delEngineService = conn.prepareStatement(
                "DELETE FROM service_map WHERE engine_id = ?");
       }

    /*
     * Function to get list of ALL services
     */
    public Service[] getServicesList() throws SQLException{
        ResultSet rs;
        synchronized(selectServicesList){
            rs = selectServicesList.executeQuery();
        }
        List<Service> sl = new ArrayList<Service>();
        while(rs.next()){
            sl.add( new Service( rs.getInt("id"), rs.getString("name") ) );
        }
        rs.close();
        return sl.toArray(new Service[0]);
    }

    /*
     *  Function to get list of ALL engines
     */
    public EngineInfo[] getEnginesList() throws SQLException{
        ResultSet rs;
        synchronized(selectEnginesList){
            rs = selectEnginesList.executeQuery();
        }
        List<EngineInfo> sl = new ArrayList<EngineInfo>();
        while(rs.next()){
            sl.add( new EngineInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("host"),
                    rs.getInt("host") ) );
        }
        rs.close();
        return sl.toArray(new EngineInfo[0]);
    }

    /*
     * Function to get the Services of an engine searched by ID
     */
    public Service[] getServicesMappedToEngine(EngineInfo eng) throws SQLException{
        ResultSet rs;
        synchronized(selectEngineServices){
            selectEngineServices.setInt(1, eng.getId());
            rs = selectEngineServices.executeQuery();
        }
        ArrayList<Service> sl = new ArrayList<Service>();
        while(rs.next()){
            sl.add( new Service( rs.getInt("service_id"), rs.getString("service_name") ) );
        }
        rs.close();
        return sl.toArray(new Service[0]);
    }
    /*
     * Function to get the Engines that provide a specific service searched by ID
     */
    public EngineInfo[] getEnginesMappedToService(Service ser) throws SQLException{
        ResultSet rs;
        synchronized(selectServiceEngines){
            selectServiceEngines.setInt(1, ser.getId());
            rs = selectServiceEngines.executeQuery();
        }
        ArrayList<EngineInfo> el = new ArrayList<EngineInfo>();
        while(rs.next()){
            el.add( new EngineInfo(
                    rs.getInt("engine_id"),
                    rs.getString("engine_name"),
                    rs.getString("host"),
                    rs.getInt("port")) );
        }
        rs.close();
        return el.toArray(new EngineInfo[0]);
    }
    /*
     * Function to add a new type of Service into list
     */
    public boolean  addNewService(Service ser) throws SQLException{
        boolean result;
        synchronized(addService){
            //addService.setInt(1, ser.getId());
            addService.setString(1, ser.getName());
            result = addService.execute();
            return result;
        }
    }
    /*
     * Function to add a new instance of Engine into list
     */
    public boolean addNewEngine(EngineInfo eng) throws SQLException{
        boolean result;
        synchronized(addEngine){
            addEngine.setString(1,eng.getName());
            addEngine.setString(2,eng.getHost());
            addEngine.setInt(3, eng.getPort());
            result = addEngine.execute();
            return result;
        }
    }
    /*
     * Function for adding a new relation between a service and an engine
     */
     public boolean addRelServiceEngine(int service_id, int engine_id) throws SQLException
     {
         boolean result;
         synchronized(addServiceEngine){
             addServiceEngine.setInt(1,service_id);
             addServiceEngine.setInt(2, engine_id);
             result = addServiceEngine.execute();
             return result;
         }
     }
    /*
     * Function that delete  a service from the system thinking also to all dependences
     * with engines
     */
    public boolean delService(Service ser) throws SQLException{
            boolean result;
            result = delServiceEngine(ser);
            synchronized(delService){
                System.out.println("L'id da eliminare è"+ser.getId());
                delService.setInt(1, ser.getId());
                result = delService.execute();
                return result;
            }
        }

    /*
     * Function that delete a relation between a service and an engine
     * by ServiceID
     */
        public boolean delServiceEngine(Service ser) throws SQLException{
            boolean result;
            synchronized(delServiceEngine){
                delServiceEngine.setInt(1,ser.getId());
                result = delServiceEngine.execute();
                return result;
             }
        }
        /*
         * Function that delete an engine from the system thinking also to all dependences
         * with engines
         */
        public boolean delEngine(EngineInfo eng) throws SQLException{
            boolean result;
            result = delEngineService(eng);
            synchronized(delEngine){
                delEngine.setInt(1,eng.getId());
                result = delEngine.execute();
                return result;
            }
        }
        /*
         * Function that delete a relation between a service and an engine
         * by EngineID
         */
        public boolean delEngineService(EngineInfo eng) throws SQLException{
            boolean result;
            synchronized(delEngineService){
                delEngineService.setInt(1,eng.getId());
                result = delEngineService.execute();
                return result;
            }
        }

}