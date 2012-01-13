package alpv.mwp.crawler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import alpv.mwp.commons.Task;

public class CrawlerTask implements Task<ToWorkerMessage, FromWorkerMessage> {
	public static String ADDR = "127.0.0.1";
	public static int PORT = 1099;
	
	public FromWorkerMessage exec(ToWorkerMessage a) {
		
		if(a==null)
			return new FromWorkerMessage(new ArrayList<String>(), new ArrayList<String>(),0);
		
		HttpURL url = new HttpURLImpl(a.url);
		
		Set<String> linkz = lookForLinks(url);
		List<String> urlz = buildURLs(url, linkz);
		
		List<String> emails = getEmails(linkz);
		
		//if depth is 0 dont add more links to the argPool
		if(a.depth==0)
			return new FromWorkerMessage(emails, new ArrayList<String>(),a.depth);
		return new FromWorkerMessage(urlz, emails,a.depth);
	}
	private static Set<String> lookForLinks(HttpURL url) {
		try {
			// load page
			HttpConnection con = url.openConnection();

			String doc = new String();
			InputStream is = con.getContent();

			int c;
			// save page
			while ((c = is.read()) != -1) {
				doc += (char) c;
			}

			Vector<Integer> begins = new Vector<Integer>();
			Vector<Integer> ends = new Vector<Integer>();

			// find links
			 int ind = 0;
			   while ((ind = doc.indexOf("href=\"", ind + 1)) != -1) {
			    begins.add(ind + 6);
			    ends.add(doc.indexOf("\"", ind + 6));
			   }

			if (begins.size() != ends.size()) {
				System.err.println("parsing html: begins & ends size mismatch");
				return null;

			} else {
				Set<String> set = new TreeSet<String>();
				for (int i = 0; i < begins.size(); i++)
					set.add(doc.substring(begins.elementAt(i), ends.elementAt(i)));

				return set;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<String> buildURLs(HttpURL origin, Set<String> links) {
		List<String> result = new ArrayList<String>();

		for (String lnk : links) {
			String url;

			if (lnk.charAt(0) == '/') { // relative link
				url = "http://" + origin.getHost() + lnk;
				result.add(url);

			} else if (lnk.startsWith("http://")) { // absolute link
				url = lnk;
				result.add(url);
			} else
				System.out.println("omitting " + lnk + " from " + origin.getHost() + "/" + origin.getPath());
		}
		return result;
	}

	private List<String> getEmails(Set<String> links){
		List<String> mails = new ArrayList<String>();
		for(String s : links)
			if(s.startsWith("mailto:")){
				//if starts with mailto: get emailadress
				s = s.substring(7);
				int qm,sp,tmp;
				qm = s.indexOf('?');
				sp = s.indexOf(' ');
				if((qm>0||sp>0) &&qm<sp)
						tmp=qm;
					else
						tmp=sp;
				if(qm<0&&sp<0)
					tmp = s.length();
				mails.add(s.substring(0, tmp));
			}
		
		return mails;
	}


}
