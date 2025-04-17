import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class Client {
	private Map<String, Integer> systemPorts;

	public Client() {
		this.systemPorts = Map.of(
				"BANK", 8080,
				"UTIL", 8081);
	}

	public Message sendMessage(Message message) {
		String receiverId = message.getReceiverId();
		if (!systemPorts.containsKey(receiverId)) {
			System.out.println("Receiver ID not recognized: " + receiverId);
			return null;
		}

		int port = systemPorts.get(receiverId);

		try (
				Socket socket = new Socket("localhost", port);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
			out.writeObject(message);
			out.flush();

			Message response = (Message) in.readObject();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
