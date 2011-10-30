import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class TextServer implements Runnable {
	private static final int port = 222;
	private static final String welcomemsg = "Hello World.";

	private class ConnectionHandler implements Runnable {
		private Socket socket = null;
		private BufferedReader fromClient = null;
		private PrintWriter toClient = null;
		
		public ConnectionHandler(Socket s) {
			socket = s;
			

			try {
				fromClient = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				toClient = new PrintWriter(socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}

		public void run() {
			// communication
			toClient.println(welcomemsg);
			// close?
			close();
		}
		
		private void close() {
			try {
			toClient.close();
			fromClient.close();
			socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			while (true) {
				// listen

				new Thread( new ConnectionHandler(ss.accept()) ).start(); // blocks
				System.out.println("Server: client accepted");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public static void main(String[] args) {

		// launch server
		Thread t = new Thread(new TextServer());
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
