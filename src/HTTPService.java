import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HTTPService {

    HttpServerProvider provider = HttpServerProvider.provider();
    HttpServer server;

    public HTTPService() {
        try {
            server = provider.createHttpServer(new InetSocketAddress(8080), 0);
            server.setExecutor(Executors.newCachedThreadPool());

            server.createContext("/", new HomeHttpHandler());
            server.createContext("/login", new LoginHttpHandler());
            server.start();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new HTTPService();
    }
}

class HomeHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {


    }
}

class LoginHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("test");
        InputStream is = httpExchange.getRequestBody();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String str = br.readLine();
        System.out.println(httpExchange.getRequestURI());

        String username = "";
        String password = "";

//        User u2 = new User(1, username, password);
//        HttpSession.setSession("userSession", u2, httpExchange);
//
//
//
////        httpExchange.sendResponseHeaders(200, 0);
////        Headers headers = httpExchange.getResponseHeaders();
//////        headers.set("Content-Length");
////
////        OutputStream out = httpExchange.getResponseBody();
////        out.write("hello world\n".getBytes());
////        out.close();
//
        User u = (User) HttpSession.getObjFromSession(httpExchange, "userSession");
        if (u == null) {
            //重新登陆
            HtmlTemplate.forPage("login.html", new RequestContent(httpExchange), new ResponseContent(httpExchange));
        }else {
            //跳转到成功登陆后界面

        }
    }
}
