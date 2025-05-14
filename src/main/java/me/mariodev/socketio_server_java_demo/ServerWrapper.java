package me.mariodev.socketio_server_java_demo;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoServer;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

public final class ServerWrapper {
    private static AtomicInteger PORT_START = null;

    private final int mPort;
    private final Server mServer;
    private final EngineIoServerOptions eioOptions;
    private final EngineIoServer mEngineIoServer;
    private final SocketIoServer mSocketIoServer;

    public ServerWrapper(String ip, int port, String[] allowedCorsOrigins) {
        PORT_START = new AtomicInteger(port);
        
        mPort = PORT_START.getAndIncrement();
        mServer = new Server(new InetSocketAddress(ip, mPort));
        eioOptions = EngineIoServerOptions.newFromDefault();
        eioOptions.setAllowedCorsOrigins(allowedCorsOrigins);
        
        mEngineIoServer = new EngineIoServer(eioOptions);
        mSocketIoServer = new SocketIoServer(mEngineIoServer);

        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addFilter(RemoteAddrFilter.class, "/socket.io/*", EnumSet.of(
            DispatcherType.REQUEST));
        
        /*
        An alternative way of handling the CORS.
        Must set eioOptions.setCorsHandlingDisabled(true) if you want to use the below method
        
        FilterHolder cors = new FilterHolder(new CrossOriginFilter());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,POST,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Cache-Control");
        cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");
        servletContextHandler.addFilter(cors, "/socket.io/*", EnumSet.of(DispatcherType.REQUEST));
        */
        
        servletContextHandler.addServlet(new ServletHolder(new HttpServlet() {

            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                mEngineIoServer.handleRequest(new HttpServletRequestWrapper(request) {
                    @Override
                    public boolean isAsyncSupported() {
                        return true;
                    }
                }, response);
            }
        }), "/socket.io/*");

        // Ensure that JettyWebSocketServletContainerInitializer is initialized,
// to setup the JettyWebSocketServerContainer for this web application context.
        JettyWebSocketServletContainerInitializer.configure(servletContextHandler, null);

// Add a WebSocket-initializer Servlet to register WebSocket endpoints.
        final ServletHolder holder = new ServletHolder(new SocketIoJettyWebsocketInitializerServlet(mEngineIoServer));
        servletContextHandler.addServlet(holder, "/*");

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] { servletContextHandler });
        mServer.setHandler(handlerList);
    }

    public void startServer() throws Exception {
        mServer.start();
    }

    public void stopServer() throws Exception {
        mServer.stop();
    }

    public int getPort() {
        return mPort;
    }

    public SocketIoServer getSocketIoServer() {
        return mSocketIoServer;
    }
    
}
