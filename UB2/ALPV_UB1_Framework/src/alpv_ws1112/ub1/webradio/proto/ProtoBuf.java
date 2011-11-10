package alpv_ws1112.ub1.webradio.proto;

import java.util.List;

import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message;
import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message.Chat;

import com.google.protobuf.ByteString;

public class ProtoBuf {	
	public static Message buildMessage(String username, List<String> messages, String format, byte[] data) {
		Message.Builder builder = Message.newBuilder();
		if(username!=null && messages != null)
			builder.setChat(buildChat(username, messages));
		
		if(format != null)
			builder.setFormat(format);
		if(data != null)
			builder.setData(ByteString.copyFrom(data));
		
		Message Message = builder.build();
		assert (Message.isInitialized());
		return Message;
	}
	public static Message buildMessage(List<String> username, List<String> messages, String format, byte[] data) {
		Message.Builder builder = Message.newBuilder();
		if(username!=null && messages != null)
			builder.setChat(buildChat(username, messages));
		
		if(format != null)
			builder.setFormat(format);
		if(data != null)
			builder.setData(ByteString.copyFrom(data));
		
		Message Message = builder.build();
		assert (Message.isInitialized());
		return Message;
	}
	public static Message buildMessage(String format, byte[] data) {
		Message.Builder builder = Message.newBuilder();
		
		if(format != null)
			builder.setFormat(format);
		if(data != null)
			builder.setData(ByteString.copyFrom(data));
		
		Message Message = builder.build();
		assert (Message.isInitialized());
		return Message;
	}
	
	public static Message buildMessage(String username, List<String> messages) {
		return buildMessage(username, messages, null,null);
	}
	private static Chat buildChat(String username, List<String> messages) {
		Chat.Builder builder = Chat.newBuilder();
		
		builder.addAllMessage(messages);
		for(int i=0;i<messages.size();i++)
			builder.addUsername(username);
		
		return builder.build();
	}
	private static Chat buildChat(List<String> username, List<String> messages) {
		Chat.Builder builder = Chat.newBuilder();
		
		builder.addAllMessage(messages);
		for(int i=0;i<messages.size();i++)
			builder.addAllUsername(username);
		
		return builder.build();
	}
	
}
