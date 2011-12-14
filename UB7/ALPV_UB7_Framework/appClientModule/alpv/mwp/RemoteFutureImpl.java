package alpv.mwp;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Vector;

public class RemoteFutureImpl implements RemoteFuture,Serializable {

	Object data = null;
	
	public Object get() throws RemoteException {
		if(data!=null)
		synchronized (data) {
			return data;
		}
		return null;
	}

	public void set(Object o){
		synchronized (data) {
			data = o;	
		}
	}
}
