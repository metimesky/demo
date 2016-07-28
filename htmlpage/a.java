package htmltemplate;

import java.io.IOException;

public class a{

	public static void process(RequestContent req, ResponseContent resp) throws IOException {
		resp.fillContent("<!DOCTYPE html><html><head>	<title></title></head><body>"+ a +"<p id=\"\"> </p>");
	 if(1 > 0) {
		resp.fillContent("<p>xxx</p>");
	}
		resp.fillContent("</body></html>");
	}
}