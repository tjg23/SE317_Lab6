import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private int port;
	private String systemId;
	private boolean running;
	private MessageHandler messageHandler;

	public Server(int port, String systemId, MessageHandler handler) {
		this.port = port;
		this.systemId = systemId;
		this.running = false;
		this.messageHandler = handler;
	}

	public void start() {
		if (!running) {
			running = true;
			new Thread(() -> {
				try (ServerSocket serverSocket = new ServerSocket(port)) {
					System.out.println(systemId + " server started on port " + port);
					while (running) {
						try {
							Socket clientSocket = serverSocket.accept();
							new ClientHandler(clientSocket, messageHandler).start();
						} catch (IOException e) {
							if (running) {
								System.err.println("Error accepting connection: " + e.getMessage());
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					running = false;
					System.out.println("Server stopped.");
				}
			}).start();
		} else {
			System.out.println("Server is already running.");
		}
	}

	public void stop() {
		if (running) {
			running = false;
			System.out.println("Stopping server...");
		} else {
			System.out.println("Server is not running.");
		}
	}

	private class ClientHandler extends Thread {
		private Socket socket;
		private MessageHandler handler;

		public ClientHandler(Socket socket, MessageHandler handler) {
			this.socket = socket;
			this.handler = handler;
		}

		@Override
		public void run() {
			try (
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
				Message request = (Message) in.readObject();
				Message response = handler.handleMessage(request);
				out.writeObject(response);
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("Error handling client request: " + e.getMessage());
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.err.println("Error closing socket: " + e.getMessage());
				}
			}
		}
	}

}

interface MessageHandler {
	Message handleMessage(Message message);
}
