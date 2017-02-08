import com.andersen.ClientTCP;
import com.andersen.ServerTCP;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ClientServerIntegrationTest {

    private Logger logger = Logger.getLogger(ClientServerIntegrationTest.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    private String host = "localhost";

    private int port = 3333;

    private String response = null;

    @Test
    public void integrationTest() throws InterruptedException {
        logger.info("Test started.");
        final List<String> requests = new ArrayList<String>();
        requests.add("Request 1");
        requests.add("Request 2");
        requests.add("Request 3");
        requests.add("Request 4");
        requests.add("Request 5");
        requests.add("Request 6");
        requests.add("Request 7");
        requests.add("Request 8");
        requests.add("Request 9");
        requests.add("Request 10");
        ServerTCP serverTCP = new ServerTCP(host, port);
        Thread serverThread = new Thread(serverTCP);
        serverThread.start();
        for (int i = 0; i < 1000 ; i++) {
            executor.execute((new Runnable() {
                public void run() {
                    ClientTCP clientTCP = new ClientTCP(host, port);
                    response = clientTCP.sendAndReceive(requests.get((int)(Math.random()*10-1)));
                    System.out.println("\n" + "Request: " + clientTCP.getRequest() + ". Response: " + response + "\n");
                    assertEquals(clientTCP.getRequest() + ServerTCP.SERVER_ECHO, response);
                }
            }));
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
     }

}
