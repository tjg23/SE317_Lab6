import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private String senderId;
	private String receiverId;
	private Type messageType;
	private Map<String, Object> payload;
	private String correlationId;

	public enum Type {
		DEPOSIT,
		WITHDRAW,
		TRANSFER,
		PAY_BILL,
		CHECK_BALANCE,
		LOGIN,
		LOGOUT,
		SIGNUP,
		SUCCESS,
		DECLINED,
		ERROR
	}

	public Message(String senderId, String receiverId, Type messageType, Map<String, Object> payload) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.messageType = messageType;
		this.payload = payload;
	}

	public Message() {
		this.payload = new HashMap<>();
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public Type getMessageType() {
		return messageType;
	}

	public void setMessageType(Type messageType) {
		this.messageType = messageType;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
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
