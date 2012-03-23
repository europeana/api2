package eu.europeana.api2;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Api2Runner {
	
	public static void main(String[] args) throws Exception {
		
		//ApplicationContext context = new ClassPathXmlApplicationContext( "api2-test.xml" );
		
		String webappDirLocation = "src/main/webapp/";
		
        Server server = new Server(8080);

        WebAppContext root = new WebAppContext();
        root.setServer(server);
	    
	    root.setContextPath("/");
	    root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
	    root.setResourceBase(webappDirLocation);
	    root.setParentLoaderPriority(true);		
		
        server.setHandler(root);
		server.start();
		server.join();
	}

}
