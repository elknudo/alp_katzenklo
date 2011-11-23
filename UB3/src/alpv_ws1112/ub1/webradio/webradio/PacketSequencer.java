package alpv_ws1112.ub1.webradio.webradio;

import java.util.Vector;
import alpv_ws1112.ub1.webradio.proto.PacketProtos.*;

public class PacketSequencer<T extends Message> {
	private int valueRange = -1;
	private int current_index = -1;
	private int window_size = 128; // cannot exceed valueRange
	private Vector<T> storage;

	public PacketSequencer() {
		storage = new Vector<T>(window_size);
		storage.setSize(window_size);
	}

	/* store and retreive messages in order */
	public boolean hasNext() {
		return true;
	}

	/* get following message */
	public Message next() {
		if(current_index == -1) // not initialized
			return null;
		T a = storage.firstElement();
		storage.remove(0);
		storage.add(null);
		
		current_index = (current_index + 1) % valueRange;
		checkIntegrity();
		return a;
	}

	/* put new message in sequence */
	public boolean put(T elem) {
		if (elem == null)
			return false;

		if (current_index == -1) { // init
			current_index = elem.getId();
			valueRange = elem.getValueRange();
		}

		final int relative_position = getRelativePosition(elem.getId());
		// drop elem if beyond window (Variante 1)
		if (relative_position >= window_size)
			return false;

		// put elem in at 'getRelPosi'
		storage.removeElementAt(relative_position);
		storage.insertElementAt(elem, relative_position);
		
		checkIntegrity();
		return true;
	}

	public int getRelativePosition(int index) {
		/*
		 * Returns big values if index is before current position returns small
		 * values if index is in front of current position
		 */
		if (index < current_index)
			return (valueRange - current_index + index);
		else
			return index - current_index;
	}

	private boolean checkIntegrity() {
		for (int i = 0; i < storage.size(); i++) {
			T elem = storage.elementAt(i);
			if (elem != null && i != getRelativePosition(elem.getId())) {
				System.err.println("No integrity");
				return false;
			}
		}
		return true;
	}
}