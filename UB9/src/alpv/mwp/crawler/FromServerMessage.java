package alpv.mwp.crawler;

import java.io.Serializable;
import java.util.List;

public class FromServerMessage implements Serializable{

	boolean finished = false;
	List<String> emails;
	
	public FromServerMessage(List<String> emails,boolean fin){
		this.emails = emails;
		this.finished = fin;
	}
	
}
