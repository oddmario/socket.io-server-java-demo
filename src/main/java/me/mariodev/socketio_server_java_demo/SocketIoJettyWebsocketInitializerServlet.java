package me.mariodev.socketio_server_java_demo;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.JettyWebSocketHandler;
import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;

public class SocketIoJettyWebsocketInitializerServlet extends HttpServlet {

  private final EngineIoServer mEngineIoServer;

  public SocketIoJettyWebsocketInitializerServlet(EngineIoServer engineIoServer) {
    this.mEngineIoServer = engineIoServer;
  }

  public void init() {
    // Retrieve the JettyWebSocketServerContainer.
    JettyWebSocketServerContainer container = JettyWebSocketServerContainer.getContainer(
        getServletContext());

    // Configure the max message size to 128kb.
    container.setMaxTextMessageSize(128 * 1024);

    // Simple registration of our SocketIO WebSocket endpoint
    container.addMapping(
        "/socket.io/*",
        (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(mEngineIoServer));
  }

}
