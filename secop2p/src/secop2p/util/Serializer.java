/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eros
 */
public class Serializer {

    public static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        synchronized(o){
            oos.writeObject(o);
            oos.close();
        }
        baos.close();
        return baos.toByteArray();
    }

    public static <E>E deserialize(byte[] serialized, Class<E> type) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        ois.close();
        bais.close();
        return type.cast(o);
    }
    public static <E>E deserialize(InputStream in, Class<E> type) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(in);
        Object o = ois.readObject();
        ois.close();
        return type.cast(o);
    }

    public static String toXML(Object o){
        Set<String> fields = new HashSet<String>();
        for(Field f : o.getClass().getDeclaredFields()){
            fields.add(f.getName());
        }
        return toXML(o,fields.toArray(new String[0]));
    }

    public static String toXML(Object o, String... fields){
        Set<String> fieldset = new HashSet<String>(Arrays.asList(fields));
        StringBuilder sb = new StringBuilder();
        sb.append("<"+o.getClass().getName()+">");
        for(Field f : o.getClass().getDeclaredFields()){
            if(fieldset.contains( f.getName() )){
                boolean acc = f.isAccessible();
                if(!acc) // getting rid of "private" modifiers
                    f.setAccessible(true);
                try {
                    String s = f.get(o).toString();
                    if(!s.startsWith("<")){
                        String type = f.getType().toString();
                        if(type.startsWith("class "))
                            type = type.substring(6);
                        else if(type.startsWith("interface "))
                            type = type.substring(10);
                        if(type.startsWith("java.lang."))
                            type = type.substring(10);
                        s = "<"+type+">"+s+"</"+type+">";
                    }
                    sb.append(s);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Serializer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Serializer.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(!acc)
                    f.setAccessible(false);
            }
        }
        sb.append("</"+o.getClass().getName()+">");
        return sb.toString();
    }

}
