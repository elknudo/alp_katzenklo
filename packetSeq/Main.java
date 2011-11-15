package ub3.packetSeq;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExampleMessage ems[] = { 
				new ExampleMessage(508, null),
				new ExampleMessage(509, null),
				new ExampleMessage(510, null),
				new ExampleMessage(511, null), // over the
				new ExampleMessage(512, null), // gap (..510, 511, 0, 1..)
				new ExampleMessage(513, null),
				new ExampleMessage(514, null),
				// 2 lost packets
				new ExampleMessage(517, null),
				new ExampleMessage(518, null),
				new ExampleMessage(519, null),
		};
		PacketSequencer<ExampleMessage> ps = new PacketSequencer<ExampleMessage>();
		for(int i =0; i < ems.length; i++) {
			ps.put(ems[i]);
		}
		System.out.println("out");
		for(int i =0; i < ems.length; i++) {
			ExampleMessage ms = ps.next();
			if(ms == null)
				System.out.println("i = " + i + "   null element (lost packet)");
			else
				System.out.println("i = " + i + "   Msg index = " + ms.getIndex());
		}
	}

}
