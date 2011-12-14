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

    public static void printAllServices() throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        Service[] sl = sr.getServicesList();
        for(Service s : sl)
            System.out.println( s.getId()+"-"+s.getName());
    }
    public static void printAllEngines() throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        EngineInfo[] el = sr.getEnginesList();
        for(EngineInfo e : el)
            System.out.println( e.getId()+"-"+e.getName() );
    }
    public static void printAllRelations() throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        Relation[] re = sr.getRelationList();
        for (Relation r : re)
            System.out.println(r.getTuple()[0]+"-"+r.getTuple()[1]);
    }

    public static void printServiceofEngine(EngineInfo el) throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        Service[] sl = sr.getServicesList();
        sl = sr.getServicesMappedToEngine(el);
        for(Service s : sl)
            System.out.println( s.getId()+"-"+s.getName() );
    }

    public static void printEnginesMappedToService(Service sl) throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        EngineInfo[] el = sr.getEnginesList();
        el = sr.getEnginesMappedToService(sl);
         for(EngineInfo e : el)
            System.out.println( e.getId()+"-"+e.getName() );
    }
    public static void printMenu()
    {
        System.out.println("**************************SeCo P2P***************************");
        System.out.println("Insert the number of  you choice");
        System.out.println("1 ) Print All Services");
        System.out.println("2 ) Print All Engines");
        System.out.println("3 ) Print All Engines Mapped to a Service");
        System.out.println("4 ) Print All Services of an Engine ");
        System.out.println("5 ) Print All the Relations between Services & Engines");
        System.out.println("6 ) Add a new Service");
        System.out.println("7 ) Add a new Engine");
        System.out.println("8) Add Relation between a Service & an Engine");
        System.out.println("9) Delete a Service");
        System.out.println("10) Delete an Engine");
        System.out.println("11) Delete Relation between a Service & an Engine by eng");
        System.out.println("12) Delete Relation between a Service & an Engine by ser");
        System.out.println("13) Exit");
        System.out.println("**********************************************************************");
    }
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        
        Scanner reader = new Scanner(System.in);
        int choice=1;
         while ( choice != 13)
         {
             printMenu();
             choice = reader.nextInt();
             if( choice == 1) printAllServices();
             else if (choice == 2) printAllEngines();
             else if (choice == 3)
             {
                 Service sl = new Service ();
                 System.out.println("Give the ID of the service");
                 sl.setId(reader.nextInt());
                 printEnginesMappedToService(sl);
             }
             else if (choice == 4)
             {
                 EngineInfo eI = new EngineInfo();
                 System.out.println("Give the ID of the Engine");
                 eI.setId(reader.nextInt());
                 printServiceofEngine(eI);
             }
             else if(choice == 5)    printAllRelations();
             else if (choice == 6)
             {
                 ServiceRepository sr = new ServiceRepository();
                 Service sl = new Service();
                 System.out.println("Insert the name of the new Services");
                 reader.nextLine();
                 sl.setName(reader.nextLine());
                 sr.addNewService(sl);
             }
             else if (choice == 7)
             {
                 ServiceRepository sr = new ServiceRepository();
                 EngineInfo eI = new EngineInfo();
                 System.out.println("Insert the name of the new Engine");
                 reader.nextLine();
                 eI.setName(reader.nextLine());
                 System.out.println("Insert the host address of the new Engine");                 
                 eI.setHost(reader.nextLine());
                 System.out.println("Insert the port number of the new Engine");                
                 eI.setPort(reader.nextInt());
                 sr.addNewEngine(eI);
              }
             else if(choice == 8)
             {
                   ServiceRepository sr = new ServiceRepository();
                   int idService, idEngine;
                   System.out.println("Insert l'id del servizio");
                   idService = reader.nextInt();
                   System.out.println("Insert l'id dell'Engine");
                   idEngine = reader.nextInt();
                   sr.addRelServiceEngine(idService, idEngine);
             }
             else if (choice == 9)
             {
                 ServiceRepository sr = new ServiceRepository();
                 Service sl = new Service();
                 System.out.println("Give the ID of the service");
                 sl.setId(reader.nextInt());
                 sr.delService(sl);
             }
             else if (choice == 10)
             {
                 ServiceRepository sr = new ServiceRepository();
                 EngineInfo eI = new EngineInfo();
                 System.out.println("Give the ID of the Engine");
                 eI.setId(reader.nextInt());
                 sr.delEngine(eI);
             }
             else if (choice == 11)
             {
                 ServiceRepository sr = new ServiceRepository();
                 EngineInfo eI = new EngineInfo();
                 System.out.println("Give the ID of the Engine");
                 eI.setId(reader.nextInt());
                 sr.delEngineServices(eI);
             }
             else if (choice == 12)
             {
                 ServiceRepository sr = new ServiceRepository();
                 Service sl = new Service();
                 System.out.println("Give the ID of the Service");
                 sl.setId(reader.nextInt());
                 sr.delServiceEngines(sl);
             }
         }
    }
}
