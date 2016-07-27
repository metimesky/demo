
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class HtmlTemplate {

	static Logger logger = Logger.getLogger(HtmlTemplate.class.getName());
	
	/*皆是静态方法，存方法就行*/
	static Map<String, Method> methodMap = new HashMap<>();
	static List<String> classNameList = new ArrayList<>();
	
	public static void forPage(String page, RequestContent req, ResponseContent resp) {
		String className = delSuffix(page);
		Method m = methodMap.get(className);
		try {
			if(m == null){
//				resp.sendRedirect("login.html");
//				resp.fillContent("no found");
			}else {
				m.invoke(null, req, resp);
			}

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/*服务每次启动就会重新解析并编译一次，此后不再检查*/
	public static void parseAndCompile() {
		/*解析*/
		Parse.start();
		logger.info("htmlpage html文件解析完毕");
		
		/*编译*/
		compile();
		logger.info("htmlpage java文件编译完毕");
	}
	
	public static void compile() {
		JavaCompiler c = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = c.getStandardFileManager(null,null,null);

        Iterable<JavaFileObject> files = getJavaFiles();
        Iterable<String> options = Arrays.asList("-d", Parse.pagedirPath);
		JavaCompiler.CompilationTask task = c.getTask(null, fileManager, null, options, null, files);
        Boolean result = task.call();
        try {
			fileManager.close();

			for (String className : classNameList) {
				URL[] urls = new URL[] {new URL("file:"+ Parse.pagedirPath)};
				URLClassLoader loader = new URLClassLoader(urls);
				Class clazz = loader.loadClass(Parse.packInfo+"."+className);
				Method m = clazz.getMethod("process", new Class[]{RequestContent.class, ResponseContent.class});
				methodMap.put(className, m);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/*java*/
	public static ArrayList<JavaFileObject> getJavaFiles() {
		ArrayList<JavaFileObject> fs = new ArrayList<>();

		try {
			File f = new File(Parse.pagedirPath);

			File[] fileList = f.listFiles();

			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isFile()) {
					 String name = fileList[i].getName();
					 if(name.endsWith(".java")) {
						 String className = delSuffix(name);

						 classNameList.add(className);
						 StringBuffer sb = new StringBuffer();
						 BufferedReader br = new BufferedReader(new FileReader(fileList[i]));
						 String st = "";
						 while((st = br.readLine()) != null) {
							 sb.append(st);
						 }
						 JavaStringObject jso = new JavaStringObject(className, sb.toString());
						 fs.add(jso);
					 }
				}
			}
		} catch (Exception e) {

		}


		return fs;
	}
	
	public static String delSuffix(String name) {
		int index = name.indexOf(".");
		if (index != -1) {
			name = name.substring(0, index);
		}

		return name;
	}
	
	static class JavaStringObject extends SimpleJavaFileObject {  
        private String code;  
  
        public JavaStringObject(String name, String code) {  
            super(URI.create(name + ".java"), Kind.SOURCE);  
            this.code = code;  
        }  
  
        @Override  
        public CharSequence getCharContent(boolean ignoreEncodingErrors)  
                throws IOException {  
            return code;  
        }  
    }  
}

class Parse {
	static Logger logger = Logger.getLogger(Parse.class.getName());

	static String packInfo = "htmltemplate";
	public static String pagedirPath = null;

	static StringBuffer sb_html = new StringBuffer();
//	static String outDir = pagedirPath;
	static String package_funParamTypeImportInfo_Str = packageInfo();
	static String nameStr = "";
	static StringBuffer importStr = new StringBuffer();
	static StringBuffer htmlStr = new StringBuffer();

	static File[] fs = null;

	public static void main(String[] args) {
		start();
	}


	public static String jarDir() {
		String jarPath = Parse.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if(jarPath.lastIndexOf(".jar") == -1) {
			/*调试 xxx/bin/ */
			char c = jarPath.charAt(jarPath.length()-1);
			if(c == '/') {
				String s2 = jarPath.substring(0, jarPath.length()-1);
				int index = s2.lastIndexOf("/");
				String dir = s2.substring(0, index+1);
				return dir;
			}else {
			}

		}
		int index = jarPath.lastIndexOf("/");
		String dir = jarPath.substring(0, index+1);
		return dir;
	}

	/**
	 * 子文件以sub_为前缀
	 */
	public static void start() {
		removeJavaFile();


		fs = filesInDir("");
		for (int i = 0; i < fs.length; i++) {
			nameStr = fs[i].getName();
			if(nameStr.startsWith("sub_")){
				continue;
			}

			logger.info("开始解析"+nameStr);
			parseFile(fs[i]);
			assembleStr();

			sb_html = new StringBuffer();
			importStr = new StringBuffer();
			htmlStr = new StringBuffer();
		}
	}

	private static String packageInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("package ").append(packInfo).append(";\r\n\r\n");
		sb.append("import java.io.IOException;\r\n");
//		sb.append("import RequestContent;\r\n");
//		sb.append("import ResponseContent;\r\n\r\n");

		return sb.toString();
	}

	private static void parseFile(File f) {
		if(f.isDirectory())
			return;
		ByteReader br = new ByteReader(f);

		parseImportStr(br);
		parseHtmlStr(br);

	}

	private static void assembleStr() {
		sb_html.append(package_funParamTypeImportInfo_Str);
		sb_html.append(importStr);

		sb_html.append("\r\npublic class ").append(delSuffix(nameStr)).append("{\r\n");
		sb_html.append("\r\n\tpublic static void process(RequestContent req, ResponseContent resp) throws IOException {\r\n");

		sb_html.append(htmlStr);

		sb_html.append("\t}\r\n}");

		logger.info("写入"+pagedirPath +File.separator + delSuffix(nameStr) + ".java");
		FileWriter fw;
		try {
			fw = new FileWriter(pagedirPath +File.separator + delSuffix(nameStr) + ".java");
			fw.write(sb_html.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static String parseHtmlStr(ByteReader br) {
		int premark = 0;

		byte[] bs = br.readLine();
		String ts= new String(bs);


		//premark 0上次是表达式 1上次是输出 2上次是包含文件
		while (bs != null) {
			int i = ByteReader.indexOf(bs, (byte)'{');
			if (i != -1) {
				char ch = (char)bs[i+1];
				switch (ch) {
					case '%':
						// {% for %}
						if(premark != 0) {
							htmlStr.append("\");\r\n");
						}
						premark = 0;
						parseExpression(bs, i, br);
						break;
					case '{':
						// 输出
						if(premark == 1) {

						}else {
							htmlStr.append("\t\tresp.fillContent(\"");
						}
						premark = 1;
						parseOutString(bs, i, br);
						break;
					case '(':
						//包含 html
						if(premark == 1) {
							htmlStr.append("\");\r\n");
						}
						parseSubHtmlFile(bs, i, br);
						premark = 2;
						break;
					default:
						break;
				}
			} else {
				if(premark == 1) {

				}else {

					htmlStr.append("\t\tresp.fillContent(\"");
				}
				premark = 1;
				parsePureHtmlLine(bs);
			}

			bs = br.readLine();
		}
		if(premark == 1) {
			htmlStr.append("\");\r\n");
		}

		return null;
	}

	/**
	 * 解析纯标签行
	 * @param bs
	 */
	private static void parsePureHtmlLine(byte[] bs) {
		String s = new String(bs);
		s = s.replace("\"", "\\\"");
		htmlStr.append(s);
	}

	/**
	 * 解析{( ... )}包含文件字符串嵌入
	 * @param bs
	 * @param i
	 * @param br
	 */
	private static void parseSubHtmlFile(byte[] bs, int i, ByteReader br) {
		int index = ByteReader.indexOf(bs, ")}".getBytes(), i+2);
		byte[] bs_t = ByteReader.trim(Arrays.copyOfRange(bs, i+2, index));
		if(bs_t == null || bs_t.length == 0) {
			return;
		}
		String fileName = new String(bs_t);
		for (int j = 0; j < fs.length; j++) {
			String name = fs[j].getName();
			if(name.equals(fileName)) {
				parseFile(fs[j]);
				break;
			}
		}
	}

	/**
	 * 解析{{ ... }}
	 * @param bs
	 * @param i
	 * @param br
	 */
	private static void parseOutString(byte[] bs, int i, ByteReader br) {
//		ArrayList al = indexs(s, "{{");
		ArrayList al = ByteReader.indexOfTotal(bs, "{{".getBytes(), 0);
		int count = al.size();
		int preIndex = 0;
		int endStr = 0;
		for (int j = 0; j < count; j++) {
			int var_index = (int) al.get(j);
			int i2 = ByteReader.indexOf(bs, "}}".getBytes(), var_index);

			if (i2 != -1) {
				String var = new String(Arrays.copyOfRange(bs, var_index+2,	i2));
				String s_t = new String(Arrays.copyOfRange(bs, preIndex, var_index));
				preIndex = i2 + 2;

				s_t = s_t.replace("\"", "\\\"");
				htmlStr.append(s_t);
				htmlStr.append("\"+");
				htmlStr.append(var);

				//bug,此行仅是{{ ... }}


				if (j + 1 != count) {
					htmlStr.append("+\"");
				}

				endStr = i2 + 2;
			} else {
				logger.warning("error");
			}
		}

		// }} 后的字符串
		if (endStr < bs.length) {
			String end = new String(Arrays.copyOfRange(bs, endStr, bs.length));
			if(end.trim().length() == 0 ) {
				htmlStr.append("+\"");
			}else {
				end = end.replace("\"", "\\\"");
				htmlStr.append("+\"").append(end);
			}

		} else {
			htmlStr.append("+\"");
		}
	}

	/**
	 * 解析{% ... %}
	 * @param bs
	 * @param i
	 * @param br
	 */
	private static void parseExpression(byte[] bs, int i, ByteReader br) {
		int isExpEnd = ByteReader.indexOf(bs, "%}".getBytes(), i+2);
		StringBuffer sb2 = new StringBuffer();

		while (isExpEnd == -1) {
			// old
			sb2.append(new String(Arrays.copyOfRange(bs, i+2, bs.length))).append("\r\n");

			// new line
			bs = br.readLine();
			if (bs == null || bs.length == 0) {
				System.out.println("error and html end");
				return;
			} else {
				isExpEnd = ByteReader.indexOf(bs, "%}".getBytes());
			}
		}

		if (sb2.length() <= 0) {
			htmlStr.append("\t");
			htmlStr.append(new String(Arrays.copyOfRange(bs, i+2, isExpEnd))).append("\r\n");


		} else {
			htmlStr.append("\t");
			htmlStr.append(sb2.toString()).append(new String(Arrays.copyOfRange(bs, 0, isExpEnd))).append("\r\n");

		}
	}

	/**
	 * import头需写在最前
	 * @param br
	 */
	private static void parseImportStr(ByteReader br) {
		byte[] data = br.readLine();

		//过滤空行
		while(ByteReader.lenWhenTrim(data) == 0) {
			data = br.readLine();
		}

		while(ByteReader.indexOf(data, "import".getBytes()) != -1) {
			importStr.append(new String(data));
			importStr.append("\r\n");
			data = br.readLine();
			while(ByteReader.lenWhenTrim(data) == 0) {
				data = br.readLine();
			}
		}

		if(br.charAtIndex(br.curIndex() - 2) == '\r') {
			br.setIndex(br.curIndex() - data.length - 2);

		}else {
			br.setIndex(br.curIndex() - data.length - 1);

		}
	}


	/**
	 * html文件与jar同级
	 * @param dir
	 * @return
     */
	public static File[] filesInDir(String dir) {

		logger.info("getJarDir : " + jarDir() + "webpage");

		File f = null;
		if (pagedirPath == null) {
			f = new File(jarDir() + "webpage");
		}else {
			f = new File(pagedirPath);
		}

		File[] fileList = f.listFiles();
		ArrayList<File> as = new ArrayList<>();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isFile()) {
				String name = fileList[i].getName();
				if(name.endsWith(".html")) {
					as.add(fileList[i]);
				}
			}
		}

		fileList = as.toArray(new File[as.size()]);
		return fileList;
	}

	/**
	 * 每次编译前,除旧
	 */
	public static void removeJavaFile() {
		File[] fileList = filesInDir("");
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isFile()) {
				String name = fileList[i].getName();
				if(name.endsWith(".java")) {
					if(fileList[i].delete()) {
						logger.info(name + "删除");
					}
				}
			}
		}
	}

	public static String delSuffix(String name) {
		int index = name.indexOf(".");
		if (index != -1) {
			name = name.substring(0, index);
		}

		return name;
	}

}
