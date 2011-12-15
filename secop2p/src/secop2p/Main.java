/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.sql.SQLException;
import java.util.Scanner;

/**
 *
 * @author eros
 */


public class Main {

    public static void printMenu() {
        System.out.println("**************************SeCo P2P***************************");
        System.out.println("Insert the number of  you choice");
        System.out.println("01) Print All Services");
        System.out.println("02) Print All Engines");
        System.out.println("03) Print All Engines Mapped to a Service");
        System.out.println("04) Print All Services of an Engine ");
        System.out.println("05) Print All the Relations between Services & Engines");
        System.out.println("06) Add a new Service");
        System.out.println("07) Add a new Engine");
        System.out.println("08) Add Relation between a Service and an Engine");
        System.out.println("09) Delete a Service");
        System.out.println("10) Delete an Engine");
        System.out.println("11) Delete all Relations of an Engine");
        System.out.println("12) Delete all Relations of a Service");
        System.out.println("13) Delete a Relation between a Service and an Engine");
        System.out.println("14) Exit");
        System.out.println("**************************SeCo P2P***************************");
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Scanner reader = new Scanner(System.in);
        int choice=1;
        ServiceRepository sr = new ServiceRepository();
        Service s;
        EngineInfo e;
        while ( choice != 14){
            printMenu();
            choice = reader.nextInt();
            switch(choice){
                case 1:
                    for(Service srv : sr.getServicesList())
                        System.out.println( srv );
                    break;
                case 2:
                    for(EngineInfo eng : sr.getEnginesList())
                        System.out.println( eng );
                    break;
                case 3:
                    System.out.println("Insert the ID of the service");
                    s = sr.getServiceById( reader.nextInt() );
                    for(EngineInfo eng : sr.getEnginesMappedToService(s))
                        System.out.println( eng );
                    break;
                case 4:
                    System.out.println("Give the ID of the Engine");
                    e = sr.getEngineById( reader.nextInt() );
                    for(Service srv : sr.getServicesMappedToEngine(e))
                        System.out.println( srv );
                    break;
                case 5:
                    for (Relation r : sr.getRelationList())
                        System.out.println(r);
                    break;
                case 6:
                    s = new Service();
                    System.out.println("Insert the name of the new Services");
                    reader.nextLine();
                    s.setName(reader.nextLine());
                    sr.addNewService(s);
                    break;
                case 7:
                    e = new EngineInfo();
                    System.out.println("Insert the name of the new Engine");
                    reader.nextLine();
                    e.setName(reader.nextLine());
                    System.out.println("Insert the host address of the new Engine");
                    e.setHost(reader.nextLine());
                    System.out.println("Insert the port number of the new Engine");
                    e.setPort(reader.nextInt());
                    sr.addNewEngine(e);
                    break;
                case 8:
                    System.out.println("Insert l'id del servizio");
                    s = sr.getServiceById(reader.nextInt());
                    System.out.println("Insert l'id dell'Engine");
                    e = sr.getEngineById(reader.nextInt());
                    sr.addRelServiceEngine(s, e);
                    break;
                case 9:
                    System.out.println("Give the ID of the service");
                    s = sr.getServiceById(reader.nextInt());
                    sr.delService(s);
                    break;
                case 10:
                    System.out.println("Give the ID of the Engine");
                    e = sr.getEngineById(reader.nextInt());
                    sr.delEngine(e);
                    break;
                case 11:
                    System.out.println("Give the ID of the Engine");
                    e = sr.getEngineById(reader.nextInt());
                    sr.delEngineServices(e);
                    break;
                case 12:
                    System.out.println("Give the ID of the Service");
                    s = sr.getServiceById(reader.nextInt());
                    sr.delServiceEngines(s);
                    break;
                case 13:
                    System.out.println("Give the ID of the Service");
                    s = sr.getServiceById( reader.nextInt() );
                    System.out.println("Give the ID of the Engine");
                    e = sr.getEngineById( reader.nextInt() );
                    sr.delRelSeviceEngine(s, e);
                    break;
            }
        }
    }
}
