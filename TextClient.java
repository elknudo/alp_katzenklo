import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TextClient {
	public static final int port = 222;

	Socket socket = null;
	PrintWriter toServer = null;
	BufferedReader fromServer = null;
	InetAddress addr = null;

	public void connect() {
		try {
			addr = InetAddress.getLocalHost();
			socket = new Socket(addr, port);
			toServer = new PrintWriter(socket.getOutputStream(), true);
			fromServer = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("IO Error");
			System.exit(1);
		}

	}

	public void receive() {
		try {
			String line = null;
			while ((line = fromServer.readLine()) == null)
				;
			System.out.println("Client: message received: " + line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			toServer.close();
			fromServer.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		try {
			TextClient client = new TextClient();
			client.connect();
			client.receive();
			Thread.sleep(2000);
			client.close();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

}
