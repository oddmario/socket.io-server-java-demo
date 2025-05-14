package me.mariodev.socketio_server_java_demo;

import io.socket.engineio.server.Emitter;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;

public class Main {
    public static void main (String[] args) {
        
        final ServerWrapper serverWrapper = new ServerWrapper("0.0.0.0", 1081, null); // null means "allow all" as stated in https://github.com/socketio/engine.io-server-java/blob/f8cd8fc96f5ee1a027d9b8d9748523e2f9a14d2a/engine.io-server/src/main/java/io/socket/engineio/server/EngineIoServerOptions.java#L26
        try {
            serverWrapper.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SocketIoServer server = serverWrapper.getSocketIoServer();
        SocketIoNamespace ns = server.namespace("/");
        ns.on("connection", emitter -> {
            SocketIoSocket socket = (SocketIoSocket) emitter[0];
            System.out.println("Client " + socket.getId() + " (" + socket.getInitialHeaders().get("remote_addr") + ") has connected.");

            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args1) {
                    System.out.println("[Client " + socket.getId() + "] " + args1);
                    socket.send("message", "test message", 1);
                }
            });

        });
        
    }
}
