package alpv.mwp;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.print.attribute.standard.JobSheets;

public class MasterImpl implements Master, Server {

	static int PORT = 1099;
	
	//MasterFunktionen
	static Vector<Worker> workers = new Vector<Worker>(); 
	
	
	public void registerWorker(Worker w) throws RemoteException {
		synchronized (workers) {
			workers.add(w);
		}
		System.out.println("new Worker registered");
	}

	public void unregisterWorker(Worker w) throws RemoteException {
		synchronized (workers) {
			int i = workers.indexOf(w);
			workers.remove(i);
		}

	}

	//Serverfunktionen
	public <Argument, Result, ReturnObject> RemoteFuture<ReturnObject> doJob(
			Job<Argument, Result, ReturnObject> j) throws RemoteException {
			
		System.out.println("doJob called");
		if(workers.size()==0)
			throw new RemoteException();
		
		Pool<Argument> argPool = new PoolImpl<Argument>();
		Pool<Result> resPool = new PoolImpl<Result>();
		
		j.split(argPool, workers.size());
		
		synchronized (resPools) {
			argPools.add(argPool);
			resPools.add(resPool);
			parts.add(argPool.size());
			
			jobs.add(j);
		}
		
		System.out.println("exiting doJob "+jobs.size());
		return j.getFuture();
	}
	
	//Jobs, Pools and nr of parts in which the arguments are divided
	static List<Job> jobs = new ArrayList<Job>();
	static List<Pool> argPools = new ArrayList<Pool>();
	static List<Pool> resPools = new ArrayList<Pool>();
	static List<Integer> parts = new ArrayList<Integer>();
	
	static boolean paused = true;
	
	public static void main(String args[]){
		System.out.println("started master");
		try {
			Registry reg = LocateRegistry.createRegistry(PORT);
			Master stub = (Master) UnicastRemoteObject.exportObject(new MasterImpl(),0);
			reg.bind("master", stub);
			
			Server stub2 = (Server) UnicastRemoteObject.exportObject(new MasterImpl(),0);
			reg.bind("server", stub2);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){

//			System.out.println("Number of Workers: "+workers.size());
			if(jobs.size()!=0)
				System.out.println("Number of Jobs: "+jobs.size() );
//			
			if(argPools.size()!=0){
				System.out.println("job");
				Job j;
				Pool argPool;
				Pool resPool;
				int part;
				synchronized (resPools) {
					j = jobs.remove(0);
					System.out.println("removing jobs");
					argPool = argPools.remove(0); 
					resPool = resPools.remove(0);
					part = parts.remove(0);	
				}
				
				int parts = part;
				
				for(Worker w : workers){
					try {
						System.out.println("giving jobs to workers");
						w.start(j.getTask(), argPool, resPool);
						parts--;
						if(parts==0) break;
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	 			
				try {
					while(resPool.size()!=part) {
						System.out.println(resPool.size() + "!=" +part );
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("merging");
				j.merge(resPool);
				
			}
		}
		
		
	}
	
}
