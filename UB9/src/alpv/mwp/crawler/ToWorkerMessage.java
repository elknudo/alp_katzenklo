package alpv.mwp.crawler;

import java.io.Serializable;

public class ToWorkerMessage implements Serializable{

	int depth;
	String url;
	public ToWorkerMessage(int i, String startURL) {
		depth = i;
		url = startURL;
	}
	
}
