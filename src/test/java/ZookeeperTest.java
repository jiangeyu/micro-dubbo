import com.github.registry.ServiceDiscovery;
import com.github.registry.ServiceRegistry;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:59 2018/6/29
 * @desc
 */
public class ZookeeperTest {

    @Test
    public void zookeeperRegistryTest() {
        ServiceRegistry registry = new ServiceRegistry("127.0.0.1:2181");
        ZooKeeper zk = registry.registry("myZookeeper");
        registry.addRootNode(zk);
        registry.createNode(zk, "micro-dubbo");

        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        ZooKeeper zk2 = serviceDiscovery.connectServer();

        serviceDiscovery.watchNode(zk);
        serviceDiscovery.watchNode(zk2);

    }
}
