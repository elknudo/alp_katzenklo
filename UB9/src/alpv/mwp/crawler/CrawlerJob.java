package alpv.mwp.crawler;

import java.rmi.RemoteException;
import java.util.ArrayList;

import alpv.mwp.commons.Job;
import alpv.mwp.commons.Pool;
import alpv.mwp.commons.RemoteFuture;
import alpv.mwp.commons.RemoteFutureImpl;
import alpv.mwp.commons.Task;

public class CrawlerJob implements Job<ToWorkerMessage, FromWorkerMessage, FromServerMessage> {
	Task<ToWorkerMessage, FromWorkerMessage> task;
	RemoteFutureImpl<FromServerMessage> rf;
	String startURL = "";
	Pool<ToWorkerMessage> argPool;
	
	public CrawlerJob(String u) {
		this.startURL = u;
		rf = new RemoteFutureImpl<FromServerMessage>();
		task = new CrawlerTask();
	}

	@Override
	public Task<ToWorkerMessage, FromWorkerMessage> getTask() {
		return task;
	}

	@Override
	public RemoteFuture<FromServerMessage> getFuture() {
		return rf;
	}

	@Override
	public void split(Pool<ToWorkerMessage> argPool, int workerCount) {
		try {
			this.argPool = argPool;
			argPool.put(new ToWorkerMessage(1,startURL));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void merge(Pool<FromWorkerMessage> resPool) {
		try {
			while(resPool.size()>0){
				FromWorkerMessage fwm = resPool.get();
				if (rf.get()==null)
					rf.set(new FromServerMessage(new ArrayList<String>(), false));
				for(String t : fwm.urls)
					argPool.put(new ToWorkerMessage(fwm.depth-1, t));
				rf.get().emails.addAll(fwm.emails);
			}
			if(resPool.size()==0&&argPool.size()==0)
				rf.set(new FromServerMessage(rf.get().emails, true));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
