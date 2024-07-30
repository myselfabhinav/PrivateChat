package com.example.privatechat;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private final int SERVER_PORT = 8080;
    private final MainActivity activity;
    private volatile boolean running = false;

    public ServerThread(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            running = true;
            activity.showMessage("Server started. Waiting for clients...");

            // Accept client connection
            clientSocket = serverSocket.accept();
            activity.showMessage("Client connected: " + clientSocket.getInetAddress());

            // Initialize input/output streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

            // Read messages from client
            String message;
            while (running && (message = input.readLine()) != null) {
                activity.showMessage("Client: " + message);
            }
        } catch (Exception e) {
            Log.e("ServerThread", "Error", e);
            activity.showMessage("Error in server: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            Log.e("ServerThread", "Error closing sockets", e);
            activity.showMessage("Error closing server: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }
}
