import com.github.client.RemoteClient;
import com.github.registry.ServiceDiscovery;
import com.github.service.HelloService;
import org.junit.Test;

import java.util.stream.IntStream;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:34 2018/6/30
 * @desc
 */
public class Benchmark {

    @Test
    public void rpcTest() {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        final RemoteClient remoteClient = new RemoteClient(serviceDiscovery);

        Thread[] threads = new Thread[10];

        IntStream.range(0, 10).forEach(n -> {
            Runnable runnable = () -> {
                IntStream.range(0, 100).forEach(i -> {
                    final HelloService helloService = remoteClient.create(HelloService.class);
                    String result = helloService.hello(Integer.toString(i));
                    System.out.println("rpc request result :" + result);
                });
            };
            threads[n] = new Thread(runnable);
            threads[n].start();
        });

        IntStream.range(0,10).forEach(n -> {
            try {
                threads[n].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        remoteClient.stop();
    }


}
