// JmriServer.java
package jmri.jmris;

import java.net.*;
import java.io.*;
//import java.lang.*;
import java.util.Vector;

// imports for ZeroConf.
import jmri.util.zeroconf.ZeroConfService;

/**
 * This is the main JMRI Server implementation.
 *
 * It starts a thread for each client.
 *
 */
public class JmriServer {

    protected int portNo = 3000; // Port to listen to for new clients.
    protected ServerSocket connectSocket;
    protected ZeroConfService service;
    private Thread listenThread = null;
    private Vector<clientListener> connectedClientThreads = new Vector<clientListener>();
    private static JmriServer _instance = null;

    public synchronized static JmriServer instance() {
        if (_instance == null) {
            _instance = new JmriServer();
        }
        return _instance;
    }

    // Create a new server using the default port
    public JmriServer() {
        this(3000);
    }

    // Create a new server using a given port
    public JmriServer(int port) {
        super();
        // Try registering the server on the given port
        try {
            connectSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Failed to connect to port " + port);
        }

        this.portNo = port;

    }

    // Maintain a vector of connected clients
    // Add a new client
    private synchronized void addClient(clientListener client) {
        if (!connectedClientThreads.contains(client)) {
            connectedClientThreads.addElement(client);
            client.start();
        }
    }

    //Remove a client
    private synchronized void removeClient(clientListener client) {
        if (connectedClientThreads.contains(client)) {
            connectedClientThreads.removeElement(client);
        }
    }

    public void start() {
        /* Start the server thread */
        if (listenThread == null) {
            listenThread = new Thread(new newClientListener(connectSocket));
            listenThread.start();
            advertise();
        }
    }

    // Advertise the service with ZeroConf
    protected void advertise() {
        service = ZeroConfService.create("_jmri._tcp.local.", portNo);
        service.publish();
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        listenThread.stop();
        service.stop();
    }

    // Internal thread to listen for new connections
    class newClientListener implements Runnable {

        ServerSocket listenSocket = null;
        boolean running = true;

        public newClientListener(ServerSocket socket) {

            listenSocket = socket;
        }

        public void run() {
            // Listen for connection requests
            try {
                while (running) {
                    Socket clientSocket = listenSocket.accept();
                    if (log.isDebugEnabled()) {
                        log.debug(" Client Connected from IP " + clientSocket.getInetAddress() + " port " + clientSocket.getPort());
                    }
                    addClient(new clientListener(clientSocket));
                }
            } catch (IOException e) {
                log.error("IOException while Listening for clients");
            }
        }

        public void stop() {
            //super.stop();
            running = false;
            try {
                listenSocket.close();
                if (log.isDebugEnabled()) {
                    log.debug("Listen Socket closed");
                }
            } catch (IOException e) {
                log.error("socket in ThreadedServer won't close");
                return;
            }
        }
    } // end of newClientListener class

    // Internal class to handle a client
    class clientListener implements Runnable {

        Socket clientSocket = null;
        DataInputStream inStream = null;
        DataOutputStream outStream = null;
        Thread clientThread = null;

        public clientListener(Socket socket) {
            if (log.isDebugEnabled()) {
                log.debug("Starting new Client");
            }
            clientSocket = socket;
            try {
                inStream = new DataInputStream(clientSocket.getInputStream());
                outStream = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                log.error("Error obtaining I/O Stream from socket.");
            }
        }

        public void start() {
            clientThread = new Thread(this);
            clientThread.start();
        }

        public void run() {
            // handle a client.
            try {
                handleClient(inStream, outStream);
            } catch (IOException e1) {
                // When we get an IO exception here, we're done
                if (log.isDebugEnabled()) {
                    log.debug("Server Exiting");
                }
                // Unregister with the server
                removeClient(this);
                return;
            } catch (java.lang.NullPointerException e2) {
                // When we get an IO exception here, we're done with this client
                if (log.isDebugEnabled()) {
                    log.debug("Client Disconnect");
                }
                // Unregister with the server
                removeClient(this);
                return;
            }
        }
    } // end of clientListener class.

    // Handle communication to a client through inStream and outStream
    public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        // Listen for commands from the client until the connection closes
        byte cmd[] = new byte[100];
        int count;
        while (true) {
            // Read the command from the client
            count = inStream.read(cmd);
            // Echo the input back to the client
            if (count != 0) {
                outStream.write(cmd);
            }
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriServer.class.getName());
}
