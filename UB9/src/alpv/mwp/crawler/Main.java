package alpv.mwp.crawler;

import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class Main {
	/* exercise b) */
	public static Set<String> lookForLinks(HttpURL url) {
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
			while ((ind = doc.indexOf("<a href=\"", ind + 1)) != -1) {
				begins.add(ind + 9);
				ends.add(doc.indexOf("\"", ind + 9));
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

	public static Vector<HttpURL> buildURLs(HttpURL origin, Set<String> links) {
		Vector<HttpURL> result = new Vector<HttpURL>();

		for (String lnk : links) {
			String url;

			if (lnk.charAt(0) == '/') { // relative link
				url = "http://" + origin.getHost() + lnk;
				result.add(new HttpURLImpl(url));

			} else if (lnk.startsWith("http://")) { // absolute link
				url = lnk;
				result.add(new HttpURLImpl(url));
			} else
				System.out.println("omitting " + lnk + " from " + origin.getHost() + "/" + origin.getPath());
		}
		return result;
	}

	public static void main(String[] args) {

		HttpURL url = new HttpURLImpl("http://www.fu-berlin.de/einrichtungen");

		// look for links
		System.out.println("Linkz: ");
		System.out.println();

		Set<String> linkz = lookForLinks(url);

		for (String l : linkz)
			System.out.println(l);

		// build URLs for crawling
		System.out.println();
		System.out.println("Urlz: ");
		System.out.println();

		Vector<HttpURL> urlz = buildURLs(url, linkz);

		System.out.println();

		for (HttpURL lurl : urlz)
			System.out.println(lurl.getHost() + "/" + lurl.getPath());

	}

}
