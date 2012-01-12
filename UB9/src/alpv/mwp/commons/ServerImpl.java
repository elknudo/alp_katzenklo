package alpv.mwp.commons;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ServerImpl implements Server, Master, Runnable {
	// env
	Vector<Worker> workers = new Vector<Worker>();

	// state
	private boolean active = true;

	// for the job
	private Pool argPool = null;
	private Pool resPool = null;
	private Job currentJob = null;
	private RemoteFutureImpl currentFuture = null;

	@Override
	synchronized public <Argument, Result, ReturnObject> RemoteFuture<ReturnObject> doJob(
			Job<Argument, Result, ReturnObject> j) throws RemoteException {

		System.out.println(System.currentTimeMillis() + "  doJob() start");

		/* create & export the pools */
		argPool = (Pool) UnicastRemoteObject.exportObject(new PoolImpl<Argument>(), 0);
		resPool = (Pool) UnicastRemoteObject.exportObject(new PoolImpl<Result>(), 0);
		/* save the job and create a future */
		currentJob = j;
		currentFuture = new RemoteFutureImpl<ReturnObject>();

		// wake up run()
		notifyAll();

		System.out.println(System.currentTimeMillis() + "  doJob() end");

		// RMI: export future - client and server work on the 'same' object
		RemoteFuture<ReturnObject> returnStub = (RemoteFuture<ReturnObject>) UnicastRemoteObject.exportObject(
				currentFuture, 0);

		return returnStub;
	}

	synchronized public void run() {
		while (active) {
			if (argPool != null && resPool != null && currentJob != null && currentFuture != null) {
				// a job arrived!
				try {
					System.out.println(System.currentTimeMillis() + "  A Job Arrived");

					// split
					currentJob.split(argPool, workers.size());
					System.out.println(System.currentTimeMillis() + "  argPool size after split(): " + argPool.size());

					// execute task
					Task task = currentJob.getTask();
					System.out.println(System.currentTimeMillis() + "  launching task on workers");
					for (Worker w : workers)
						w.start(task, argPool, resPool);

					// merge
					System.out.println(System.currentTimeMillis() + "  Merging job");

					currentJob.merge(resPool);

					System.out.println(System.currentTimeMillis() + "  Job merged");

					// set clients future - 'return result'
					currentFuture.set(currentJob.getFuture().get());

					// reset job state - ready for next job
					argPool = null;
					resPool = null;
					currentJob = null;
					currentFuture = null;

				} catch (RemoteException e) {
					System.out.println(System.currentTimeMillis() + "  Error during execution of job");
					e.printStackTrace();
				}
			} else {
				// no jobs - go to sleep
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			final int PORT = 1099;

			Registry reg = LocateRegistry.createRegistry(PORT);
			ServerImpl server = new ServerImpl();

			Remote remote = UnicastRemoteObject.exportObject(server, 0);
			Server stub = (Server) remote;
			Master mstub = (Master) remote;
			reg.bind("server", stub);
			reg.bind("master", mstub);

			new Thread(server).start();

			System.out.println("started masterserver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Master methods */
	@Override
	public void registerWorker(Worker w) throws RemoteException {
		workers.add(w);
	}

	@Override
	public void unregisterWorker(Worker w) throws RemoteException {
		workers.remove(w);
	}
}
