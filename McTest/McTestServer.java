import java.net.*;

public class McTestServer {

	public static void main(String[] args) {
		try {
			InetAddress group = InetAddress.getByName("225.0.0.0");
			final int port = 6667;

			MulticastSocket ms = new MulticastSocket(port);
			ms.joinGroup(group);

			Thread.sleep(5000);
			// SRV
			for (int i = 0; i < 10; i++) {
				String str = "Hallo" + String.valueOf(i) + " ";

				DatagramPacket msg = new DatagramPacket(str.getBytes(),
						str.length(), group, port);

				System.out.println("Out: " + str);
				ms.send(msg);
			}
			// common
			ms.leaveGroup(group);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
