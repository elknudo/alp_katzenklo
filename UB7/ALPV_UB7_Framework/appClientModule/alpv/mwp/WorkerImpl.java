package alpv.mwp;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WorkerImpl implements Worker,Runnable,Serializable {

	private static int threads = 4;
	private static Master master = null;
	private static String ADDR = "127.0.0.1";
	private static int PORT = 1099;
	
	public <Argument, Result> void start(Task<Argument, Result> t,
			Pool<Argument> argpool, Pool<Result> respool)
			throws RemoteException {
		
		Argument a;
		synchronized (argpool) {
			a = argpool.get();	
		}
		Result result = t.exec(a);
		synchronized (respool) {
			respool.put(result);
		}
		
	}

	public WorkerImpl(String addr,int port, int threads) throws RemoteException{
		this.threads = threads;
		this.ADDR = addr;
		this.PORT = port;
	}
	
	public WorkerImpl(int threads) throws RemoteException{
		this.threads = threads;
	}
	
	public WorkerImpl() throws RemoteException{
	}
	
	public static void main(String[] args){
		
		for(int i = 0;i<threads;i++){
			Runnable thread;
			try {
				thread = new WorkerImpl();
				thread.run();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	@Override
	public void run() {
		try {
			Registry registry = LocateRegistry.getRegistry(ADDR,PORT);
			master = (Master) registry.lookup("master");
			master.registerWorker(new WorkerImpl());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
}
