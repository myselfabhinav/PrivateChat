package com.example.privatechat;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private final String serverIP;
    private final int SERVER_PORT = 8080;
    private final MainActivity activity;
    private volatile boolean running = true; // Flag to control the thread execution

    public ClientThread(String serverIP, MainActivity activity) {
        this.serverIP = serverIP;
        this.activity = activity;
    }

    @Override
    public void run() {
        try {
            // Initialize socket and input/output streams
            socket = new Socket(serverIP, SERVER_PORT);
            activity.showMessage("Connected to server: " + serverIP);

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // Listen for incoming messages
            String message;
            while (running && (message = input.readLine()) != null) {
                activity.showMessage("Server: " + message);
            }
        } catch (SocketException e) {
            Log.e("ClientThread", "SocketException: " + e.getMessage(), e);
            activity.showMessage("Network error: " + e.getMessage());
        } catch (IOException e) {
            Log.e("ClientThread", "IOException: " + e.getMessage(), e);
            activity.showMessage("I/O error: " + e.getMessage());
        } catch (Exception e) {
            Log.e("ClientThread", "Error: " + e.getMessage(), e);
            activity.showMessage("Error in client: " + e.getMessage());
        } finally {
            // Ensure resources are properly closed
            stopClient();
        }
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
            activity.showMessage("You: " + message);
        } else {
            Log.w("ClientThread", "Output stream is null. Cannot send message.");
        }
    }

    public void stopClient() {
        running = false; // Stop the thread loop

        try {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                activity.showMessage("Disconnected from server.");
            }
        } catch (IOException e) {
            Log.e("ClientThread", "Error closing resources", e);
            activity.showMessage("Error closing client: " + e.getMessage());
        }
    }
}
