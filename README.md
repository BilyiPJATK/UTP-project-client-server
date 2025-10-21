# Java Chat Server & Client

## Overview
This project implements a multi-client chat system in Java using sockets.  
It supports public messages (broadcast to all clients), private messages, messages to all except certain users, banned phrase filtering, and configurable server settings via a properties file.

## Features
- Multi-client support: multiple clients can connect and chat simultaneously.
- Banned phrase filtering: messages containing banned words are blocked.
- Private messaging: `/msg to:<user1,user2> <message>`
- Exclude users messaging: `/msg except:<user1,user2> <message>`
- View connected clients list.
- Configurable server name, port, and banned words in `config.bin`.

## Setup & Running

### Server
1. Configure server settings in `src/config.bin` (properties file):
    ```
    port=6565
    name=ChatServer
    bannedWords=itn,test,fail
    ```
2. Compile server classes:
    ```bash
    javac Server.java ChatHandler.java
    ```
3. Run server:
    ```bash
    java Server
    ```
4. The server will start and listen on the configured port.

### Client
1. Edit `Client.java` if needed to change server IP or port:
    ```java
    String serverAddress = "192.168.0.17";
    int serverPort = 6565;
    ```
2. Compile client:
    ```bash
    javac Client.java
    ```
3. Run client:
    ```bash
    java Client
    ```
4. Follow prompts to enter your username and start chatting.

## Usage

### Commands
- Send public message: just type your message and press Enter.
- Send private message: `/msg to:<user1,user2> <your message>`
- Send to everyone except certain users: `/msg except:<user1,user2> <your message>`
- View banned words: `/banned`

### Notes
- Usernames must be unique; duplicates are rejected.
- Messages containing banned phrases are blocked and the sender is notified.
- Server prints connected clients in console when someone joins or leaves.

## Project Structure
- `Server.java` – Main server application.
- `ChatHandler.java` – Handles individual client connections.
- `Client.java` – Client application.
- `config.bin` – Server configuration file (port, name, bannedWords).

## Example

**Client 1:**
Enter your username: Alice
Send a message to everyone: Hello everyone!


**Client 2:**
Enter your username: Bob
Alice has joined the chat.
Bob (private): Hi Alice!
