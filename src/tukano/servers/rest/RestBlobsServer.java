package tukano.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tukano.discovery.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RestBlobsServer {

    private static Logger Log = Logger.getLogger(RestBlobsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "blobs";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {

            Discovery discovery = Discovery.getInstance();
        	
            ResourceConfig config = new ResourceConfig();
            config.register(  RestBlobsResource.class );

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

			discovery.announce(SERVICE, serverURI);

            // More code can be executed here...
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }

}