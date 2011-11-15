package ub3.packetSeq;

public class ExampleMessage implements IndexedMsg {
	// valueRange can be changed, but don't make it smaller than PacketSequencer.windowsize (or increase that)
	public static final int valueRange = 512;
	private int index;
	
	private byte[] data; // placeholder for any data
	
	public ExampleMessage(int index, byte[] data) {
		this.index = index % valueRange;
		this.data = data;
	}
	
	public int getValueRange() {
		return valueRange;
	}
	public int getIndex() {
		return index;
	}
}
