import org.junit.Test;

import java.util.stream.IntStream;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午1:24 2018/6/30
 * @desc
 */
public class Java8ApiTest {

    @Test
    public  void rangeTest() {
        int length = 4;
        IntStream.range(0, length).forEach(i -> System.out.println(i));
    }
}
