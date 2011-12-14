package alpv.mwp;

import java.rmi.RemoteException;
import java.util.Vector;

public class PoolImpl<T> implements Pool<T> {

	Vector<T> ts = new Vector<T>();
	
	@Override
	public void put(T t) throws RemoteException {
		synchronized (ts) {
			ts.add(t);
		}
	}

	@Override
	public T get() throws RemoteException {
		synchronized (ts) {
			return ts.remove(0);
		}
	}

	@Override
	public int size() throws RemoteException {
		synchronized (ts) {
			return ts.size();
		}
	}

}
