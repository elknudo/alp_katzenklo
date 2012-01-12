package alpv.mwp.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class HttpConnectionImpl implements HttpConnection {
	private static class Header {
		String field;
		String key;
	}

	protected static String getBegin = "GET /";
	protected static String httpVersion = " HTTP/1.1\n";
	protected static String hostMsg = "Host: ";
	protected static String msgEnd = "\n\n";
	/* how long to wait for response before closing connection */
	protected static final int timeout = 60;

	protected HttpURL url;
	protected boolean connected;

	protected String completeResponse;

	public HttpConnectionImpl(HttpURL url) {
		this.url = url;
		this.connected = false;
	}

	@Override
	public int getResponseCode() {
		/* try to load page if not stored yet */
		if (!connected)
			loadPage();

		/* if page is stored now, parse response code */
		if (connected) {
			String s = completeResponse.substring(9, 12);
			return Integer.parseInt(s);
		}
		return -1;
	}

	@Override
	public String getHeaderFieldKey(int field) {
		if (!connected)
			loadPage();

		/* if page is stored now, parse headers */
		if (connected) {
			return extractHeaders(completeResponse)[field].key;
		}

		return null;
	}

	@Override
	public String getHeaderField(int field) {
		if (!connected)
			loadPage();

		/* if page is stored now, parse headers */
		if (connected) {
			return extractHeaders(completeResponse)[field].field;
		}

		return null;
	}

	@Override
	public InputStream getContent() throws IOException {
		if (!connected)
			loadPage();

		/* if page is stored now, create inputStream */
		if (connected) {
			return new InputStream() {
				private int position = 0;
				private String content = extractContent(completeResponse);

				@Override
				public int read() throws IOException {
					/* return next byte */
					if (position < content.length())
						return content.charAt(position++);
					return -1; // end is reached
				}

			};
		}

		return null;
	}

	private String buildGETmsg(HttpURL url) {
		return getBegin + url.getPath() + httpVersion + hostMsg + url.getHost() + msgEnd;
	}

	private boolean loadPage() {
		boolean timedOut = false;

		/* connect */
		Socket socket = null;
		PrintWriter toServer = null;
		BufferedReader fromServer = null;
		InetAddress addr = null;

		try {
			addr = InetAddress.getByName(url.getHost());
			int port = url.getPort();
			socket = new Socket(addr, port);
			toServer = new PrintWriter(socket.getOutputStream(), true);
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("IO Error");
			System.exit(1);
		}

		/* send get */
		byte[] bMsg = buildGETmsg(this.url).getBytes();
		try {
			socket.getOutputStream().write(bMsg);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/* read page */
		String line = null;

		final long t_start = System.currentTimeMillis();

		// skip null responses
		while ((line = noBlockReadLine(fromServer)) == null) {
			// check for timeout
			if (System.currentTimeMillis() > t_start + timeout * 1000) {
				timedOut = true;
				break;
			} else {
				// stall next test
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// save response until null responses occur
		do {
			completeResponse = completeResponse == null ? line : completeResponse + '\n' + line;
		} while ((line = noBlockReadLine(fromServer)) != null);

		if (timedOut)
			completeResponse = null;

		/* close connection */
		try {
			toServer.close();
			fromServer.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		if (!timedOut) {
			this.connected = true;
			return true;
		} else {
			System.err.println("HttpConnection: connection to " + this.url.getHost() + " timed out");
			this.connected = false;
			return false;
		}
	}

	private static String noBlockReadLine(BufferedReader reader) {
		/* may return null, but does not block */
		try {
			if (reader.ready())
				return reader.readLine();
			else
				return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Header[] extractHeaders(String doc) {
		String str_headers = doc.substring(doc.indexOf('\n') + 1, doc.indexOf("\n\n") + 1);
		String[] headers_lines = str_headers.split("\n");
		Header[] headers = new Header[headers_lines.length];
		for (int i = 0; i < headers.length; i++) {
			headers[i] = new Header();
			headers[i].field = headers_lines[i].substring(0, headers_lines[i].indexOf(':'));
			headers[i].key = headers_lines[i].substring(headers_lines[i].indexOf(':') + 1);
		}
		return headers;
	}
	
	private static String extractContent(String doc) {
		return doc.substring(doc.indexOf("\n\n") + 1);
	}

	public String test() {
		if (!connected)
			loadPage();
		return completeResponse;
	}

	public static void main(String[] args) {
		try {
			String known = "http://www.fu-berlin.de/einrichtungen/";

			HttpConnectionImpl con = (HttpConnectionImpl) new HttpURLImpl(known).openConnection();
			
			System.out.println();
			System.out.println("Content ---->");
			InputStream is = con.getContent();
			int c;
			while((c = is.read()) != -1) 
				System.out.print((char) c);
			
			System.out.println();
			System.out.println("Complete ---->");
			System.out.println();
			System.out.println(con.test());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
