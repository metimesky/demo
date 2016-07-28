import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ORMDao {
	
	/**
	 * 获取实体的ID值
	 * 
	 * @param entity
	 * @return
	 */
	public String getIdField(Object entity) {

		String idField = null;
		try {
			Field[] fields = Class.forName("NewsClass")

			.getDeclaredFields();

			System.out.println(fields.length);

			Annotation[] annotations;

			for (Field field : fields) {

				annotations = field.getDeclaredAnnotations();

				for (Annotation a : annotations) {
//					System.out.println();
					if (a.annotationType().getSimpleName().equals("Id")) {
						idField = field.getName();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return idField;
	}

	/**
	 * 对象查询
	 * 
	 * @param entity
	 * @param pageNo
	 * @param pageSize
	 * @param orderType
	 * @return
	 * @throws Exception
	 */
	public void query(Object entity){

		Field[] fields = entity.getClass().getDeclaredFields();

		String sqlSelect = "";

		String sqlWhere = "";

		String sql = "";

		Class cl = entity.getClass();

		boolean firstFlag = true;

		ArrayList<String> cols = new ArrayList<String>();
		
		String id = this.getIdField(entity);
		
		String className = entity.getClass().getSimpleName();

		for (int i = 0; i < fields.length; i++) {

			String fieldName = fields[i].getName();
			if (i != fields.length - 1) {
				sqlSelect += fieldName + ",";
			} else {
				sqlSelect += fieldName;
			}

			cols.add(fieldName);

			String methodName = "get" + fieldName.substring(0, 1).toUpperCase()
					+ fieldName.substring(1, fieldName.length());
			

			

			try{
				if (cl.getMethod(methodName).invoke(entity) != null) {
					if (firstFlag) {
						sqlWhere += " where " + fieldName + "='"
								+ cl.getMethod(methodName).invoke(entity) + "'";
						firstFlag = false;
					} else {
						sqlWhere += " and " + fieldName + "='"
								+ cl.getMethod(methodName).invoke(entity) + "'";
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}

		
		sql = "select " + sqlSelect + " from "
					+ className + sqlWhere;

		System.out.println("the final sql :" + sql);

	}

	public static void main(String[] args) {
		ORMDao dao = new ORMDao();
		NewsClass newsclass = new NewsClass();
		newsclass.setId("1");
		newsclass.setName("国际新闻");
		dao.query(newsclass);

	}



}

class NewsClass {

	@Id
	String id;

	@ColName("c_name")
	String name;

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}



	public String getName() {
		return name;
	}
}



@Retention(RetentionPolicy.RUNTIME)
 @interface Id {
	public String name = "id";
}

@Retention(RetentionPolicy.RUNTIME)
@interface ColName {
	String value() default "";
}