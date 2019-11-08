package jettyrequestlifecycleviolation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class FileServer {
	
	private final Logger logger = LogManager.getLogger();
		
	private Server server;

    public Server startServer(URI baseUri) throws Exception {    	    	    	
    	
		File storage = new File("storage");		
		storage.mkdir();
                
    	server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(baseUri.getPort());
        connector.setHost(baseUri.getHost());
        
        connector.addBean(new ChannelLogger());
        
        server.addConnector(connector);
		                
		ServletContextHandler contextHandler = new ServletContextHandler(server, "/", false, false);
		contextHandler.setResourceBase(storage.getPath());
				
		FileServlet fileServlet = new FileServlet(storage);
		contextHandler.addServlet(new ServletHolder(fileServlet), "/*");
		
		CustomRequestLog requestLog = new CustomRequestLog("logs/request.log", "%t %{client}a \"%r\" %k %X %s %{ms}T ms %{CLF}I B %{CLF}O B %{connection}i %{connection}o");
		server.setRequestLog(requestLog);
        
        server.start();       
        
        return server;
    }
    
    public void stop() throws Exception {
    	server.stop();
    }

    public static void main(String[] args) throws Exception {
    	new FileServer().run();
    }
    
    public void run() throws Exception {    	
    	
    	URI baseUri = URI.create("http://localhost:8000/");
    	
    	Server server = this.startServer(baseUri);
    	
    	try {
    		this.test(baseUri);
    	} catch (Exception e) {
    		logger.error("server error", e);
    	} finally {    		
    		server.stop();
    	}
    }
    
    public void writeFile(File file, String lineContent, long lineCount) throws IOException {
    	try (FileWriter fw = new FileWriter(file)) {
    		
    		for (int i = 0; i < lineCount; i++) {
    			fw.write("" + i + "\t" + lineContent + "\n");
    		}
    	}
    }

	private void test(URI baseUri) throws Exception {
				
		FileClient client = new FileClient(baseUri);
		
		File uploadFile = new File("upload-test-file.txt");
		File downloadFile = new File("download-test-file.txt");
		
		this.writeFile(uploadFile, "test-file-conntent", 100_000);		
    	
		int n = 100;
		
		logger.info("uplaod and download file '" + uploadFile + "' " + n + " times");
				
    	for (int i = 0; i < n; i++) {			
    		logger.info("test " + i);
			client.uploadFile("jettyUrlPath", uploadFile);
			client.downloadFile("jettyUrlPath", downloadFile);
			downloadFile.delete();
    	}
    	logger.info("done");
	}
}

