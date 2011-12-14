package alpv.mwp;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class JobImpl implements Job<List<Integer>, List<Integer>, RemoteFutureImpl> {

	Task<List<Integer>, List<Integer>> task = (Task<List<Integer>, List<Integer>>) new TaskImpl();
	
	Vector<Integer> args = new Vector<Integer>();
	RemoteFutureImpl rfi = new RemoteFutureImpl();
	
	public Task<List<Integer>, List<Integer>> getTask() {
		return task;
	}

	@Override
	public RemoteFuture<RemoteFutureImpl> getFuture() {
		return rfi;
	}

	@Override
	public void split(Pool<List<Integer>> argPool, int workerCount) {
		int len = args.size()/workerCount;
		int i =0;
		
		try {
			if(i+2*len<args.size())
				argPool.put(args.subList(i, i+=len));	
			else
				argPool.put(args.subList(i, args.size()-1));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void merge(Pool<List<Integer>> resPool) {
		List<Integer> list = new ArrayList<Integer>();
		try {
			while(resPool.size()>0){
				list.addAll(resPool.get());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		task.exec(list);
		
	}

	public JobImpl(){
		args.add(1);
		args.add(2);
		args.add(55);
		args.add(234);args.add(21);
		args.add(4);
		args.add(55);
		args.add(257);args.add(11);
		args.add(2);
		args.add(3);
		args.add(4);args.add(41);
		args.add(2);
		args.add(3);
		args.add(4);args.add(21);
		args.add(6);
		args.add(7);
		args.add(1);
	}
	
}
