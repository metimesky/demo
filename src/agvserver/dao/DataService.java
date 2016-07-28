package agvserver.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by zhangxinwei on 7/26/2016.
 */
public class DataService {

    public <T>  void update(Object obj, Class<T> clazz) {
        T o = (T) obj;



        Connection con = null;
        try {
            Field[] fields = clazz.getFields();
            Method m = clazz.getMethod("sql", null);
            String sql = (String) m.invoke(obj, null);

            Type type = fields[0].getType();


            PreparedStatement ps = con.prepareStatement(sql);
            int ret = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        A a = new A(0, 0);
        DataService dataService = new DataService();
        dataService.update(a, A.class);
    }
}

class A {
    int id;
    int a;
    String b;

    A(int a, int b) {
        a = a;
        b = b;
    }

    public String sql() {
        return "update table set a = ?, b = ? where id = ?";
    }
}