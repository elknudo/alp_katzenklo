package alpv.mwp;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class Client {
	
	public static String ADDR = "127.0.0.1";
	public static int PORT = 1099;

	public static void main(String[] args) {
		Registry registry;
		Server master;
		try {
			registry = LocateRegistry.getRegistry(ADDR,PORT);
			master = (Server) registry.lookup("server");
			
			RemoteFuture rf = master.doJob(new JobImpl());
			
			while(rf.get()==null){
//				System.out.println("waiting");
				
			}
			List<Integer> r = (List<Integer>) rf.get();
			for(Integer i : r)
				System.out.println(i);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
