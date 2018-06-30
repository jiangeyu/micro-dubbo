import com.github.client.RemoteClient;
import com.github.client.RemoteFuture;
import com.github.client.proxy.AsyncObjectProxy;
import com.github.registry.ServiceDiscovery;
import com.github.service.HelloService;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:38 2018/6/30
 * @desc
 */
public class BenchmarkAsyns {

    @Test
    public void rpcAsyncTest() {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        final RemoteClient remoteClient = new RemoteClient(serviceDiscovery);

        Thread[] threads = new Thread[10];

        IntStream.range(0, 10).forEach(n -> {
            Runnable runnable = () -> {
                IntStream.range(0, 100).forEach(i -> {
                    try {
                        AsyncObjectProxy client = remoteClient.createAsyns(HelloService.class);
                        RemoteFuture future = client.call("hello", Integer.toString(i));
                        String result = (String) future.get(3000, TimeUnit.MICROSECONDS);
                        System.out.println("rpc request result :" + result);
                    } catch (Exception e) {

                    }

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
