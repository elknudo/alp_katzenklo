import java.net.*;

public class McTestClient {

	public static void main(String args[]) throws SocketException {
		try {
			InetAddress group = InetAddress.getByName("225.0.0.0");
			final int port = 6667;

			MulticastSocket ms = new MulticastSocket(port);
			ms.joinGroup(group);

			// CLIENT
			byte[] buf = new byte[32];
			DatagramPacket incoming = new DatagramPacket(buf, buf.length);
			while (true) {
				ms.receive(incoming);

				byte dt[] = incoming.getData();
				String str = new String(dt);
				System.out.print("In: " + str);
				System.out.println(" From: " + incoming.getAddress() + " "
						+ incoming.getPort());
			}
			// common
			// ms.leaveGroup(group);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

// eth3
