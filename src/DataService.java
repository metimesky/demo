import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by zhangxinwei on 7/26/16.
 */
public class DataService {

    private static ComboPooledDataSource ds = new ComboPooledDataSource();
    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        A a = new A(0, 0, "dd");


        DataService dataService = new DataService();

        long s = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            dataService.update(a, A.class);
        }
        System.out.println(System.currentTimeMillis() - s);
    }

    public <T> void update(Object obj, Class<T> clazz) {

        //sql
        try {
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();

            Table t = clazz.getAnnotation(Table.class);
            if (t != null) {
                sb1.append("update ").append(t.value()).append(" set ");
            }

            Field[] fields = clazz.getFields();
            for (Field f : fields) {
                Col annotation1 = f.getAnnotation(Col.class);
                if (annotation1 != null) {
                    String colName = annotation1.value();
                    boolean key = annotation1.key();

                    Method m = clazz.getMethod("getCol" + f.getName());
                    if (key) {
                        sb2.append(" where ").append(colName).append(" = ").append((String) m.invoke(obj));
                    }else {
                        sb1.append(colName).append(" = ").append((String)m.invoke(obj)).append(",");
                    }
                }
            }

            String sql = sb1.substring(0, sb1.length() - 1) + sb2.toString();

            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareCall(sql);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


}





@Table("t_a")
class A {
    @Col(value = "id", key = true)
    public int aId;

    @Col("col_a")
    public int a = 0;

    @Col("col_s")
    public String str = "宝宝吧";

    A(int aId, int a, String str) {
        aId = aId;
        a = a;
        str = str;
    }

    public String getColaId() {
        return aId + "";
    }

    public String getCola() {
        return a + "";
    }

    public String getColstr() {
        return "'" + str + "'";
    }
}



@Retention(RetentionPolicy.RUNTIME)
@interface Table {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface Col {
    String value();
    boolean key() default false;
}
