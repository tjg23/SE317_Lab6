import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private String senderId;
	private String receiverId;
	private String messageType;
	private Map<String, Object> payload;
	private String messageId;

	public Message(String senderId, String receiverId, String messageType, Map<String, Object> payload) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.messageType = messageType;
		this.payload = payload;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public String getMessageType() {
		return messageType;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public String getMessageId() {
		return messageId;
	}

	public void addData(String key, Object value) {
		if (payload == null) {
			payload = new HashMap<>();
		}
		payload.put(key, value);
	}

	public Object getData(String key) {
		return payload != null ? payload.get(key) : null;
	}
}
