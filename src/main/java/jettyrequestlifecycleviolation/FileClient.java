package jettyrequestlifecycleviolation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

public class FileClient {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private URI host;
	private HttpClient jettyClient;

	public FileClient(URI host) throws Exception {		
		this.host = host;

		jettyClient = new HttpClient();
		jettyClient.start();
	}

	public void uploadFile(String path, File file) throws IOException, ServletException, InterruptedException, TimeoutException, ExecutionException, URISyntaxException {

		URI uri = host.resolve(path);
		ContentResponse response = jettyClient.newRequest(uri)
				.method(HttpMethod.PUT)
				.file(file.toPath(), "text/plain")
				.send();

		if (response.getStatus() >= 300) {
			throw new RuntimeException("client error: upload failed " + uri + " " + response.getStatus());
		}
	}

	public void downloadFile(String path, File destFile) throws IOException, ServletException, InterruptedException, TimeoutException, ExecutionException, URISyntaxException {


		URI uri = host.resolve(path);
		InputStreamResponseListener listener = new InputStreamResponseListener();		
		jettyClient.newRequest(uri).send(listener);		

		Response response = listener.get(Long.MAX_VALUE, TimeUnit.SECONDS);
		
		if(response.getStatus() == HttpStatus.OK_200) {

			try (InputStream inStream = listener.getInputStream()) {
				Files.copy(inStream, destFile.toPath());
			}
		} else {
			throw new RuntimeException("client error: download failed " + uri + " " + response.getStatus());
		}
	}	
}
