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
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author eros
 */
public class ServiceRepository {

    private final static String DB_CONN = "jdbc:sqlite:ServiceRepository.sqlite";
    private final Connection conn;
    static private PreparedStatement selectServicesList;
    static private PreparedStatement selectEnginesList;
    static private PreparedStatement selectRelationList;
    static private PreparedStatement selectEngineServices;
    static private PreparedStatement selectServiceEngines;
    static private PreparedStatement addService;
    static private PreparedStatement addEngine;
    static private PreparedStatement addRelation;
    static private PreparedStatement delService;
    static private PreparedStatement delServiceEngines;
    static private PreparedStatement delEngine;
    static private PreparedStatement delEngineServices;
    static private PreparedStatement delRelation;
    static private PreparedStatement selectEngineById;
    static private PreparedStatement selectServiceById;
    static private PreparedStatement updateLastAliveTimestamp;
    static private PreparedStatement selectAliveEngines;
    static private PreparedStatement selectAliveRelations;

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
        selectRelationList = conn.prepareStatement(
                "SELECT * FROM services_to_engines");
        selectEngineServices = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE engine_id = ?");
        selectServiceEngines = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE service_id = ?");
        addService = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO services (name) VALUES (? ) ");
        addEngine = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO engines (name, host, port, alive_port) VALUES (?,?,?,?)" );
        addRelation = conn.prepareStatement(
                "INSERT OR REPLACE INTO service_map (service_id,engine_id) VALUES (?,?)");
        delService = conn.prepareStatement(
                "DELETE FROM services  WHERE id = ?");
        delServiceEngines = conn.prepareStatement(
                "DELETE FROM service_map WHERE service_id = ?");
        delEngine = conn.prepareStatement(
                "DELETE FROM engines  WHERE id = ?");
        delEngineServices = conn.prepareStatement(
                "DELETE FROM service_map WHERE engine_id = ?");
        delRelation = conn.prepareStatement(
                "DELETE FROM service_map WHERE engine_id = ? AND service_id = ?");
        selectEngineById = conn.prepareStatement(
                "SELECT * FROM engines WHERE id = ?");
        selectServiceById = conn.prepareStatement(
                "SELECT * FROM services WHERE id = ?");
        updateLastAliveTimestamp = conn.prepareStatement(
                "UPDATE engines SET last_alive_timestamp = ? WHERE id = ?");
        selectAliveEngines = conn.prepareStatement(
                "SELECT * FROM engines WHERE last_alive_timestamp > ?");
        selectAliveRelations = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE last_alive_timestamp > ?");
       }

    /*
     * Function to get list of ALL services
     */
    public Set<Service> getServicesList() throws SQLException{
        ResultSet rs;
        synchronized(conn){
            rs = selectServicesList.executeQuery();
        }
        List<Service> sl = new ArrayList<Service>();
        while(rs.next()){
            sl.add( new Service( rs.getInt("id"), rs.getString("name") ) );
        }
        rs.close();
        return new TreeSet<Service>(sl);
    }

    /*
     *  Function to get list of ALL engines
     */
    public Set<EngineInfo> getEnginesList() throws SQLException{
        ResultSet rs;
        synchronized(conn){
            rs = selectEnginesList.executeQuery();
        }
        List<EngineInfo> el = new ArrayList<EngineInfo>();
        while(rs.next()){
            el.add( new EngineInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("host"),
                    rs.getInt("port"),
                    rs.getInt("alive_port")) );
        }
        rs.close();
        return new TreeSet<EngineInfo>(el);
    }

    public Set<EngineInfo> getAliveEngines(long lastAliveTimestamp) throws SQLException{
        ResultSet rs;
        synchronized(conn){
            selectAliveEngines.setLong(1, lastAliveTimestamp);
            rs = selectAliveEngines.executeQuery();
        }
        List<EngineInfo> el = new ArrayList<EngineInfo>();
        while(rs.next()){
            el.add( new EngineInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("host"),
                    rs.getInt("port"),
                    rs.getInt("alive_port")) );
        }
        rs.close();
        return new TreeSet<EngineInfo>(el);
    }
    
    public  Set<Relation> getRelationList() throws SQLException{
        ResultSet rs;
        synchronized(conn){
            rs = selectRelationList.executeQuery();
        }
        List<Relation> sl = new ArrayList<Relation>();
        while(rs.next()){
            Service s = new Service(
                rs.getInt("service_id"),
                rs.getString("service_name")
            );
            EngineInfo e = new EngineInfo(
                rs.getInt("engine_id"),
                rs.getString("engine_name"),
                rs.getString("host"),
                rs.getInt("port"),
                rs.getInt("alive_port")
            );
            sl.add( new Relation( s, e ) );
        }
        rs.close();
        return new TreeSet<Relation>(sl);
    }

    public  Set<Relation> getAliveRelationList(long timestamp) throws SQLException{
        ResultSet rs;
        synchronized(conn){
            selectAliveRelations.setLong(1, timestamp);
            rs = selectAliveRelations.executeQuery();
        }
        List<Relation> sl = new ArrayList<Relation>();
        while(rs.next()){
            Service s = new Service(
                rs.getInt("service_id"),
                rs.getString("service_name")
            );
            EngineInfo e = new EngineInfo(
                rs.getInt("engine_id"),
                rs.getString("engine_name"),
                rs.getString("host"),
                rs.getInt("port"),
                rs.getInt("alive_port")
            );
            sl.add( new Relation( s, e ) );
        }
        rs.close();
        return new TreeSet<Relation>(sl);
    }

    /*
     * Function to get the Services of an engine searched by ID
     */
    public Set<Service> getServicesMappedToEngine(EngineInfo eng) throws SQLException{
        ResultSet rs;
        synchronized(conn){
            selectEngineServices.setInt(1, eng.getId());
            rs = selectEngineServices.executeQuery();
        }
        ArrayList<Service> sl = new ArrayList<Service>();
        while(rs.next()){
            sl.add( new Service( rs.getInt("service_id"), rs.getString("service_name") ) );
        }
        rs.close();
        return new TreeSet<Service>(sl);
    }
    /*
     * Function to get the Engines that provide a specific service searched by ID
     */
    public Set<EngineInfo> getEnginesMappedToService(Service ser) throws SQLException{
        ResultSet rs;
        synchronized(conn){
            selectServiceEngines.setInt(1, ser.getId());
            rs = selectServiceEngines.executeQuery();
        }
        ArrayList<EngineInfo> el = new ArrayList<EngineInfo>();
        while(rs.next()){
            el.add( new EngineInfo(
                    rs.getInt("engine_id"),
                    rs.getString("engine_name"),
                    rs.getString("host"),
                    rs.getInt("port"),
                    rs.getInt("alive_port")) );
        }
        rs.close();
        return new TreeSet<EngineInfo>(el);
    }
    /*
     * Function to add a new type of Service into list
     */
    public boolean  addNewService(Service ser) throws SQLException{
        int success;
        synchronized(conn){
            //addService.setInt(1, ser.getId());
            addService.setString(1, ser.getName());
            success = addService.executeUpdate();
            if(success > 0){
                ResultSet rs = addService.getGeneratedKeys();
                rs.next();
                ser.setId( rs.getInt(1) );
                System.out.println("Service aggiunto: "+ser);
            }
            return success > 0;
        }
    }
    /*
     * Function to add a new instance of Engine into list
     */
    public boolean addNewEngine(EngineInfo eng) throws SQLException{
        int success;
        synchronized(conn){
            addEngine.setString(1,eng.getName());
            addEngine.setString(2,eng.getHost());
            addEngine.setInt(3, eng.getPort());
            addEngine.setInt(4, eng.getAlivePort());
            success = addEngine.executeUpdate();
            if(success > 0){
                ResultSet rs = addEngine.getGeneratedKeys();
                rs.next();
                eng.setId( rs.getInt(1) );
            }
            System.out.println("Added engine: "+eng);
            return success > 0;
        }
    }
    /*
     * Function for adding a new relation between a service and an engine
     */
    public boolean addRelServiceEngine(Service s, EngineInfo e) throws SQLException {
        int result;
        synchronized(conn){
            addRelation.setInt(1,s.getId());
            addRelation.setInt(2,e.getId());
            result = addRelation.executeUpdate();
            System.out.println("Relation aggiunta: "+new Relation(s, e)+" with result "+result);
            return result > 0;
        }
    }

    /*
     * Function that delete  a service from the system thinking also to all dependences
     * with engines
     */
    public boolean delService(Service ser) throws SQLException{
            int result;
            delServiceEngines(ser);
            synchronized(conn){
                System.out.println("L'id da eliminare è"+ser.getId());
                delService.setInt(1, ser.getId());
                result = delService.executeUpdate();
                return result > 0;
            }
        }

    /*
     * Function that delete a relation between a service and an engine
     * by ServiceID
     */
    public boolean delServiceEngines(Service ser) throws SQLException{
        int result;
        synchronized(conn){
            delServiceEngines.setInt(1,ser.getId());
            result = delServiceEngines.executeUpdate();
            return result > 0;
         }
    }

    /*
     * Function that delete an engine from the system thinking also to all dependences
     * with engines
     */
    public boolean delEngine(EngineInfo eng) throws SQLException{
        int result;
        delEngineServices(eng);
        synchronized(conn){
            delEngine.setInt(1,eng.getId());
            result = delEngine.executeUpdate();
            return result > 0;
        }
    }

    /*
     * Function that delete a relation between a service and an engine
     * by EngineID
     */
    public boolean delEngineServices(EngineInfo eng) throws SQLException{
        int result;
        synchronized(conn){
            delEngineServices.setInt(1,eng.getId());
            result = delEngineServices.executeUpdate();
            return result > 0;
        }
    }

    public boolean delRelSeviceEngine(Service s, EngineInfo e) throws SQLException{
         int result;
         synchronized(conn){
             delRelation.setInt(1,s.getId());
             delRelation.setInt(2,e.getId());
             result = delRelation.executeUpdate();
             return result > 0;
         }
    }

    /*Nguyen Ho
     Get engine information bases on engineId
     */
    public EngineInfo getEngineById(int engineId) throws SQLException {
        ResultSet resultSet;
        EngineInfo engineInfo = null;
        selectEngineById.setInt(1, engineId);
        synchronized(conn){
            resultSet = selectEngineById.executeQuery();
        }
        if(resultSet.next()){
            engineInfo = new EngineInfo(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("host"),
                resultSet.getInt("port"),
                resultSet.getInt("alive_port")
            ) ;
        }else
            throw new java.util.NoSuchElementException();
        resultSet.close();
        return engineInfo;
    }   
    
    /*Nguyen Ho
     Get service bases on serviceId
     */
    public Service getServiceById(int serviceId) throws SQLException{
        ResultSet resultSet;
        Service service = null;
        synchronized(conn){
            selectServiceById.setInt(1, serviceId);
            resultSet=selectServiceById.executeQuery();
        }
        if(resultSet.next()){
            service = new Service(
                resultSet.getInt("id"),
                resultSet.getString("name")
            );
        }else
            throw new java.util.NoSuchElementException();
        resultSet.close();
        return  service;
    }

    boolean updateLastAliveTimestamp(EngineInfo from, long timestamp) throws SQLException {
        //TODO check id is valid
        synchronized(conn){
            updateLastAliveTimestamp.setLong(1, timestamp);
            updateLastAliveTimestamp.setInt(2, from.getId());
            int res = updateLastAliveTimestamp.executeUpdate();
            return res > 0;
        }
    }
    
}