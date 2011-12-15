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
		
		this.notify();
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
		register(this,threads,addr,port);
	
	}
	
	public WorkerImpl(int threads) throws RemoteException{
		this.threads = threads;
		register(this,threads);
	}
	
	public WorkerImpl() throws RemoteException{
		register(this,threads); 
	}
	
	private static void register(WorkerImpl wi,int threads){
		for(int i = 0; i<threads;i++)
				try {
					Registry registry = LocateRegistry.getRegistry(ADDR,PORT);
					master = (Master) registry.lookup("master");
					master.registerWorker(wi);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
	}
	
	private static void register(WorkerImpl wi,int threads,String addr, int port){
		for(int i = 0; i<threads;i++)
				try {
					Registry registry = LocateRegistry.getRegistry(addr,port);
					master = (Master) registry.lookup("master");
					master.registerWorker(wi);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
	}
	
	public static void main(String[] args){
		
		for(int i = 0;i<threads;i++){
			Runnable thread;
			try {
				thread = new WorkerImpl();
				thread.run();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
