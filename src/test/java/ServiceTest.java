import com.github.client.RemoteClient;
import com.github.client.RemoteFuture;
import com.github.client.proxy.AsyncObjectProxy;
import com.github.server.RemoteServer;
import com.github.service.HelloService;
import com.github.service.Person;
import com.github.service.PersonService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class ServiceTest {

    @Autowired
    private RemoteClient rpcClient;
    @Autowired
    private RemoteServer remoteServer;


    @Test
    public void helloTest1() {
        remoteServer.start();
        HelloService helloService = rpcClient.create(HelloService.class);
        String result = helloService.hello("World");
        System.out.println(result + "------------------------");
        Assert.assertEquals("Hello! World", result);
    }

    @Test
    public void helloTest2() {
        HelloService helloService = rpcClient.create(HelloService.class);
        Person person = new Person("Yong", "Huang");
        String result = helloService.hello(person);
        Assert.assertEquals("Hello! Yong Huang", result);
    }

    @Test
    public void helloPersonTest() {
        PersonService personService = rpcClient.create(PersonService.class);
        int num = 5;
        List<Person> persons = personService.GetTestPerson("xiaoming", num);
        List<Person> expectedPersons = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            expectedPersons.add(new Person(Integer.toString(i), "xiaoming"));
        }
        assertThat(persons, equalTo(expectedPersons));

        for (int i = 0; i < persons.size(); ++i) {
            System.out.println(persons.get(i));
        }
    }

    @Test
    public void helloFutureTest1() {
        remoteServer.start();
        System.out.println(remoteServer.getBeanMap().toString());
        AsyncObjectProxy helloService = rpcClient.createAsyns(HelloService.class);
        RemoteFuture result = helloService.call("hello", "World");
        System.out.println(result.get());
        Assert.assertEquals("Hello! World", result.get());
    }

    @Test
    public void helloFutureTest2() throws ExecutionException, InterruptedException {
        AsyncObjectProxy helloService = rpcClient.createAsyns(HelloService.class);
        Person person = new Person("Yong", "Huang");
        RemoteFuture result = helloService.call("hello", person);
        Assert.assertEquals("Hello! Yong Huang", result.get());
    }

    @Test
    public void helloPersonFutureTest1() throws ExecutionException, InterruptedException {
        AsyncObjectProxy helloPersonService = rpcClient.createAsyns(PersonService.class);
        int num = 5;
        RemoteFuture result = helloPersonService.call("GetTestPerson", "xiaoming", num);
        List<Person> persons = (List<Person>) result.get();
        List<Person> expectedPersons = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            expectedPersons.add(new Person(Integer.toString(i), "xiaoming"));
        }
        assertThat(persons, equalTo(expectedPersons));

        for (int i = 0; i < num; ++i) {
            System.out.println(persons.get(i));
        }
    }

    @After
    public void setTear() {
        if (rpcClient != null) {
            rpcClient.stop();
        }
    }

}
