package alpv_ws1112.ub1.webradio.proto;

import java.util.List;

import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message;
import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message.Chat;

import com.google.protobuf.ByteString;

public class ProtoBuf {	
	//just a hell of a lot builder so no one hast to type to much
	public static Message buildMessage(String username, List<String> messages, String format, byte[] data) {
		Message.Builder builder = Message.newBuilder();
		if(username!=null && messages.size()!=0)
			builder.setChat(buildChat(username, messages));
		
		if(format != null)
			builder.setFormat(format);
		if(data != null)
			builder.setData(ByteString.copyFrom(data));
		
		Message Message = builder.build();
		assert (Message.isInitialized());
		return Message;
	}
	public static Message buildMessage(int id, List<String> username, List<String> messages, String format, byte[] data, int valueR) {
		Message.Builder builder = Message.newBuilder();
		if(username!=null && messages != null)
			builder.setChat(buildChat(username, messages));
		
		if(format != null)
			builder.setFormat(format);
		if(data != null)
			builder.setData(ByteString.copyFrom(data));
		builder.setId(id);
		builder.setValueRange(valueR);
		
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
	public static Message buildMessage(Integer counter,
			String format, byte[] data, int valuerange) {
Message.Builder builder = Message.newBuilder();
		
		if(format != null)
			builder.setFormat(format);
		if(data != null)
			builder.setData(ByteString.copyFrom(data));
		
		builder.setId(counter);
		builder.setValueRange(valuerange);
		
		Message Message = builder.build();
		assert (Message.isInitialized());
		return Message;
	}
	public static Message buildMessage(String username, String message) {
		Message.Builder builder = Message.newBuilder();
		
		builder.setChat(buildChat(username, message));
		
		Message Message = builder.build();
		assert (Message.isInitialized());
		return Message;	}
	private static Chat buildChat(String username, String message) {
		Chat.Builder builder = Chat.newBuilder();
		
		builder.addUsername(username);
		builder.addMessage(message);
		Chat chat = builder.build();
		return chat;
		
	}
	
}
