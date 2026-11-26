package dev.portpocket;
import java.net.*;
import java.io.IOException;

final class PortCheck {
    static ServerSocket bind(int port) throws IOException {
        ServerSocket socket = new ServerSocket();
        try {
            socket.setReuseAddress(false);
            socket.bind(new InetSocketAddress("127.0.0.1", port));
            return socket;
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }
    static String status(int port) {
        try (ServerSocket available = bind(port)) {
            return "Available";
        } catch (BindException e) {
            String reason = String.valueOf(e.getMessage()).toLowerCase(java.util.Locale.ROOT);
            return reason.contains("in use") || reason.contains("eaddrinuse") ? "Occupied" : "Restricted";
        } catch (IOException | SecurityException e) {
            return "Restricted";
        }
    }
}
