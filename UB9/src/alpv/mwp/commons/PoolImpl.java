package alpv.mwp.commons;

import java.rmi.RemoteException;
import java.util.Vector;

public class PoolImpl<T> implements Pool<T> {

	private Vector<T> ts = new Vector<T>();

	@Override
	synchronized public void put(T t) throws RemoteException {
		ts.add(t);
	}

	@Override
	synchronized public T get() throws RemoteException {
		try {
			return ts.remove(0);
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	synchronized public int size() throws RemoteException {
		return ts.size();
	}
	
	synchronized public void putAt(int i, T t){
		ts.add(i, t);
	}
	
	
}
