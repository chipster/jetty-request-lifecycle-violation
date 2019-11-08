package jettyrequestlifecycleviolation;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;

public class ChannelLogger implements HttpChannel.Listener {

	private static final Logger logger = LogManager.getLogger();

	public void onDispatchFailure(Request request, Throwable failure) {
		logger.warn("onDispatchFailure " + request.getMethod() + " " + request.getRequestURI() + "\n" + ExceptionUtils.getStackTrace(failure));
	}

	public void onRequestFailure(Request request, Throwable failure) {
		logger.warn("onRequestFailure " + request.getMethod() + " " + request.getRequestURI() + "\n" + ExceptionUtils.getStackTrace(failure));
	}

	public void onResponseFailure(Request request, Throwable failure) {
		logger.warn("onResponseFailure " + request.getMethod() + " " + request.getRequestURI() + "\n" + ExceptionUtils.getStackTrace(failure));            	
	}
}
