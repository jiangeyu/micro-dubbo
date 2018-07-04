import com.github.registry.ServiceRegistry;
import com.github.server.RemoteServer;
import com.github.service.HelloService;
import com.github.service.HelloServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcBootstrapWithoutSpring {
    private static final Logger logger = LoggerFactory.getLogger(RpcBootstrapWithoutSpring.class);

    public static void main(String[] args) throws InterruptedException {
        String serverAddress = "127.0.0.1:18866";
        ServiceRegistry serviceRegistry = new ServiceRegistry("127.0.0.1:2181");
        RemoteServer rpcServer = new RemoteServer(serverAddress, serviceRegistry);
        rpcServer.start();
        HelloService helloService = new HelloServiceImpl();
        rpcServer.addService("com.nettyrpc.test.client.HelloService", helloService);
        System.out.println(helloService.hello("----------------------------"));
        try {
            rpcServer.start();
        } catch (Exception ex) {
            logger.error("Exception: {}", ex);
        }
    }
}
