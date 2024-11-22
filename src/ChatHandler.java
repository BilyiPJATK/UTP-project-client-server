import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ChatHandler implements Runnable {
    private final List<String> bannedWords;
    private final Socket clientSocket;
    private final Map<String, ChatHandler> clients;
    private PrintWriter out;
    private String clientName;

    public ChatHandler(Socket socket, Map<String, ChatHandler> clients, List<String> bannedWords) {
        this.clientSocket = socket;
        this.clients = clients;
        this.bannedWords = bannedWords;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Enter your username:");
            clientName = in.readLine();
            synchronized (clients) {
                if (clients.containsKey(clientName)) {
                    out.println("Username already taken. Disconnecting...");
                    clientSocket.close();
                    return;
                }
                clients.put(clientName, this);
                Server.printConnectedClients();
                broadcast(clientName + " has joined the chat.");

                out.println("\u001B[32m" + "Send a message to everyone: Just type your message and press Enter. \n" +
                        "Send a private message to specific users: /msg to:<user1,user2> <your message>\n" +
                        "Send a message to everyone except specific users: /msg except:<user1,user2> <your message>\n" +
                        "View the list of banned phrases: /banned" + "\u001B[0m");

                out.println("Currently connected clients:");
                if (clients.isEmpty()) {
                    out.println("No clients connected.");
                } else {
                    for (String clientName : clients.keySet()) {
                        out.println(clientName);
                    }
                }
            }

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/banned")) {
                    sendBannedWords();
                } else if (message.startsWith("/msg")) {
                    processTargetedMessage(message);
                } else {
                    processMessage(message);
                }
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            synchronized (clients) {
                if (clientName != null && clients.containsKey(clientName)) {
                    clients.remove(clientName);
                    Server.printConnectedClients();
                    broadcast(clientName + " has left the chat.");
                }
            }

            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void processMessage(String message) {
        for (String bannedPhrase : bannedWords) {
            if (message.contains(bannedPhrase)) {
                out.println("Message contains banned phrase: " + bannedPhrase);
                return;
            }
        }
        broadcast(clientName + ": " + message);
    }

    private void processTargetedMessage(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            out.println("Invalid command format. Use /msg <options> <message>");
            return;
        }

        String optionsAndMessage = parts[1];
        String[] optionParts = optionsAndMessage.split(" ", 2);
        if (optionParts.length < 2) {
            out.println("Invalid command format. Use /msg <options> <message>");
            return;
        }

        String options = optionParts[0];
        String message = optionParts[1];

        Set<String> recipients = new HashSet<>();
        if (options.startsWith("to:")) {
            String[] targets = options.substring(3).split(",");
            for (String target : targets) {
                if (clients.containsKey(target)) {
                    recipients.add(target.trim());
                } else {
                    out.println("User not found: " + target);
                }
            }
        } else if (options.startsWith("except:")) {
            String[] exclusions = options.substring(7).split(",");
            recipients.addAll(clients.keySet());
            for (String excluded : exclusions) {
                recipients.remove(excluded.trim());
            }
        } else {
            out.println("Invalid options format. Use to:<usernames> or except:<usernames>");
            return;
        }

        synchronized (clients) {
            for (String recipient : recipients) {
                ChatHandler client = clients.get(recipient);
                if (client != null) {
                    client.sendMessage(clientName + " (private): " + message);
                }
            }
        }
    }

    private void sendBannedWords() {
        out.println("Banned phrases: " + String.join(", ", bannedWords));
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ChatHandler client : clients.values()) {
                if (client != this) {
                    client.sendMessage(message);
                }
            }
        }
    }

    private void sendMessage(String message) {
        out.println(message);
    }
}
