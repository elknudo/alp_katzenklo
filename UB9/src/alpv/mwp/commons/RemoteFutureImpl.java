package alpv.mwp.commons;


import java.io.Serializable;
import java.rmi.RemoteException;

public class RemoteFutureImpl<ReturnObject> implements RemoteFuture<ReturnObject>,Serializable {
	private static final long serialVersionUID = 6783486735362822761L;
	ReturnObject data = null;
	
	synchronized public ReturnObject get() throws RemoteException {
		// block if result is not there yet
		
		return data;
	}

	synchronized public void set(ReturnObject o){
		// result received - wake up waiting threads
		data = o;
		notifyAll();
	}
}
