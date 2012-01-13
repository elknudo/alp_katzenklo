package alpv.mwp.crawler;

import java.io.Serializable;
import java.util.List;

import alpv.mwp.commons.RemoteFuture;

public class FromWorkerMessage implements Serializable {

	List<String> urls;
	List<String> emails;
	int depth;
	
	public FromWorkerMessage(List<String> futures, List<String> emails, int depth){
		this.urls = futures;
		this.emails = emails;
		this.depth = depth;
	}
	
}
