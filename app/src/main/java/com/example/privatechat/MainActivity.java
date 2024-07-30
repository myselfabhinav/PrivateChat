package com.example.privatechat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private TextView tvChatLog;
    private EditText etMessage;
    private Button btnSend;
    private Button btnStartServer;
    private Button btnConnectClient;
    private ServerThread serverThread;
    private ClientThread clientThread;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvChatLog = findViewById(R.id.tvChatLog);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnStartServer = findViewById(R.id.btnStartServer);
        btnConnectClient = findViewById(R.id.btnConnectClient);

       
        if (!checkPermissions()) {
            requestPermissions();
        }

        btnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    startServer();
                } else {
                    requestPermissions();
                }
            }
        });

        btnConnectClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    connectClient();
                } else {
                    requestPermissions();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startServer() {
        if (serverThread == null) {
            serverThread = new ServerThread(this);
            executorService.execute(serverThread);
            showMessage("Server started. Waiting for clients...");
        } else {
            Toast.makeText(this, "Server is already running", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectClient() {
        if (clientThread == null) {
            String serverIP = getIpAddress();
            if (serverIP != null) {
                clientThread = new ClientThread(serverIP, this);
                executorService.execute(clientThread);
            } else {
                Toast.makeText(this, "Unable to get IP address", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Already connected as client", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serverThread != null) {
            
            executorService.execute(() -> serverThread.sendMessage(message));
        } else if (clientThread != null) {
           
            executorService.execute(() -> clientThread.sendMessage(message));
        } else {
            Toast.makeText(this, "No active connection", Toast.LENGTH_SHORT).show();
        }

        showMessage("Me: " + message);
        etMessage.setText("");
    }

    public void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvChatLog.append(message + "\n");
            }
        });
    }

    private String getIpAddress() {
        try {

            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addresses = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addresses) {
                    if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
