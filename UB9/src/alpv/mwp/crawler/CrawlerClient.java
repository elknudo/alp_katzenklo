package alpv.mwp.crawler;

import alpv.mwp.commons.Job;
import alpv.mwp.commons.RemoteFuture;
import alpv.mwp.commons.Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.List;


public class CrawlerClient {
	public static String ADDR = "127.0.0.1";
	public static int PORT = 1099;
	private static List<String> ret;	

	public static void main(String[] args) {
		Registry registry;
		Server jobServer;
		try {
			/* find server */
			registry = LocateRegistry.getRegistry(ADDR, PORT);
			jobServer = (Server) registry.lookup("server");
			Job<ToWorkerMessage, FromWorkerMessage, FromServerMessage> theJob = new CrawlerJob("http://www.fu-berlin.de/einrichtungen");
			
			System.out.println(System.currentTimeMillis() + "  doJob() launch");
			final RemoteFuture<FromServerMessage> returnedFuture = jobServer.doJob(theJob);
			System.out.println(System.currentTimeMillis() + "  doJob() finish");
			new Thread(new Runnable() {

				@Override
				public void run() {
					FromServerMessage result = null;
					try {
						/* wait for depth to be reached*/						
						while(result == null || result.finished==false){
							result = returnedFuture.get();
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					System.out.println(System.currentTimeMillis() + "  returnedFuture");
					for(String email : result.emails)
						System.out.println(email);
				}
			}).start();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	
	}
	
	
}
