package com.reactiveworks.learning.afresco.outh;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * Runs a Jetty server on a free port, waiting for OAuth to redirect to it with
 * the verification Code
 * 
 * @author Md Noorshid
 *
 */
public class LocalServerReceiver implements VerificationCodeReceiver {
	static Logger logger = Logger.getLogger(LocalServerReceiver.class);

	private static final String CALLBACK_PATH = "/Callback";
	private static final String LOCALHOST = "127.0.0.1";
	private static final int PORT = 8080;

	volatile String code;

	private Server server;

	@Override
	public String getRedirectUri() throws Exception {
		server = new Server();
		for (Connector c : server.getConnectors()) {
			c.setHost(LOCALHOST);
		}

		server.addHandler(new CallbackHandler());
		server.start();
		return "http://" + LOCALHOST + ":" + PORT + CALLBACK_PATH;
	}

	@Override
	public String waitForCode() {
		try {
			this.wait();
		} catch (InterruptedException e) {

		}
		return null;
	}

	@Override
	public void stop() throws Exception {
		if (server != null) {
          server.stop();
		}
	}

	/**
	 * Jetty handler that takes the verifier token passed over from the OAuth
	 * provider and stashes it where {@link #waitForCode} will find it.
	 */
	class CallbackHandler extends AbstractHandler {

		@Override
		public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
				throws IOException, ServletException {
			if (!CALLBACK_PATH.equals(target)) {
				return;
			}

			writeLandingHtml(response);
			response.flushBuffer();
			((Request) request).setHandled(true);

			String error = request.getParameter("error");
			if (error != null) {
				logger.debug("Authentication failed. Error:: " + error);
				logger.debug("QUITTING....");
				System.exit(1);
			}
			code = request.getParameter("code");
			synchronized (LocalServerReceiver.this) {
				LocalServerReceiver.this.notify();
			}

		}

		private void writeLandingHtml(HttpServletResponse response) throws IOException {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");

			PrintWriter doc = response.getWriter();
			doc.println("<html>");
			doc.println("<head><title>OAuth 2.0 Authentication Token Recieved</title></head>");
			doc.println("<body>");
			doc.println("Received verification code. Closing...");
			doc.println("<script type='text/javascript'>");
			// We open "" in the same window to trigger JS ownership of it,
			// which lets
			// us then close it via JS, at least in Chrome.
			doc.println("window.setTimeout(function() {");
			doc.println("    window.open('', '_self', ''); window.close(); }, 1000);");
			doc.println("if (window.opener) { window.opener.checkToken(); }");
			doc.println("</script>");
			doc.println("</body>");
			doc.println("</HTML>");
			doc.flush();
		}

	}

}
