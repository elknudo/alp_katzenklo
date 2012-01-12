package alpv.mwp.crawler;

import java.io.IOException;
import java.net.UnknownHostException;

public class HttpURLImpl implements HttpURL {
	protected String host;
	protected String path;
	protected int port = 80;

	public HttpURLImpl(String url) {
		if (url.startsWith("http://")) {
			final int hostBegin = 7;
			final int hostEnd = url.indexOf('/', hostBegin);
			this.host = url.substring(hostBegin, hostEnd);
			final int pathBegin = hostEnd + 1;
			final int pathEnd = url.length();
			this.path = url.substring(pathBegin, pathEnd);
		} else {
			System.err.println("Url-String is not a http url: " + url);
		}
	}

	@Override
	public HttpConnection openConnection() throws UnknownHostException, IOException {
		return new HttpConnectionImpl(this);
	}

	/* Set and Get: host, path, port */

	@Override
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static void main(String[] args) {
		/* Test */
		HttpURL url = new HttpURLImpl("http://www.astra.de/office/index.html");
		System.out.println(url.getHost() + "  " + url.getPath() + "  " + url.getPort());
		/* error -> */
		new HttpURLImpl("www.noHttp.info");
	}

}
