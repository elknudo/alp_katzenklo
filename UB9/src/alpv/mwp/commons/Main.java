package alpv.mwp.commons;

public class Main {

	public static void main(String[] args) {
		// Launches the server, 2 workers and a client start mains individually to have seperate consoles
		ServerImpl.main(null);
		WorkerImpl.main(null);
		WorkerImpl.main(null);
		// RaytracingClient.main(null);

	}
}
