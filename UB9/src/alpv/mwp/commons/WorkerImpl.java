package alpv.mwp.commons;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class WorkerImpl implements Worker, Runnable {

	public static String ADDR = "127.0.0.1";
	public static int PORT = 1099;

	private boolean jobToDo = false;
	private Pool argPool = null;
	private Pool resPool = null;
	private Task t = null;

	public WorkerImpl() {
		try {
			/* find server */
			Registry registry = LocateRegistry.getRegistry(ADDR, PORT);
			Master server = (Master) registry.lookup("master");

			/* export me */
			Worker wstub = (Worker) UnicastRemoteObject.exportObject(this, 0);

			/* add me to the masters list of workers */
			server.registerWorker(wstub);
			System.out.println("Worker: registered");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

		// start worker thread
		new Thread(this).start();
	}

	synchronized public void run() {
		while (true) {
			if (jobToDo) {
				System.out.println("Worker: starting exec of a job");
				try {
					// do it
					Object input = argPool.get();
					Object output = t.exec(input);
					resPool.put(output);
					jobToDo = false;
					System.out.println("Worker: finished my job");
				} catch (Exception e) {
					System.out.println("Worker: error during exec of a job");
					e.printStackTrace();
				}
			} else {
				try {
					// sleep
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	synchronized public <Argument, Result> void start(Task<Argument, Result> t, Pool<Argument> argpool,
			Pool<Result> respool) throws RemoteException {

		System.out.println("Worker.start(): saving job info for run");
		this.argPool = argpool;
		this.resPool = respool;
		this.t = t;
		jobToDo = true;

		notifyAll();
	}

	public static void main(String[] args) {
		new WorkerImpl();
	}
}