package alpv.mwp.commons;

import java.net.*;
import java.io.*;
import java.util.*;

public class Main {
	public static Set<String> lookForLinks(String doc) {

		Vector<Integer> begins = new Vector<Integer>();
		Vector<Integer> ends = new Vector<Integer>();

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
	}

	public static void main(String[] args) {
		try {
			// connect
			java.net.URL url = new URL("https://userpage.fu-berlin.de/ximusic/?id=uni");

			java.net.URLConnection con = url.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String inputLine;
			String document = new String();

			// save page
			while ((inputLine = in.readLine()) != null) {
				// System.out.println(inputLine);
				document = document.concat(inputLine + '\n');
			}

			in.close();

			// look for links
			Set<String> linkz = lookForLinks(document);
			
			for (String l : linkz)
				System.out.println(l);

		} catch (Exception e) {
			System.err.println("No");
		}

		/*
		 * Launches the server, 2 workers and a client start mains individually to have seperate consoles
		 * ServerImpl.main(null); WorkerImpl.main(null); WorkerImpl.main(null); //RaytracingClient.main(null);
		 */
	}
}
