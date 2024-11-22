import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static int port;
    private static String name;
    private static List<String> bannedWords;
    private static final Map<String, ChatHandler> clients = new HashMap<>();

    public static void main(String[] args) {

        if (!loadConfiguration("src/config.bin")) {
            System.err.println("Failed to load configuration. Exiting...");
            return;
        }

        System.out.println("Starting " + name + " on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(name + " is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ChatHandler clientHandler = new ChatHandler(clientSocket, clients, bannedWords);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static boolean loadConfiguration(String configFilePath) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
            port = Integer.parseInt(properties.getProperty("port"));
            name = properties.getProperty("name");
            bannedWords = Arrays.asList(properties.getProperty("bannedWords").split(","));
            return true;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            return false;
        }
    }

    public static void printConnectedClients() {
        System.out.println("Currently connected clients:");
        if (clients.isEmpty()) {
            System.out.println("No clients connected.");
        } else {
            for (String clientName : clients.keySet()) {
                System.out.println(clientName);
            }
        }
    }
}
