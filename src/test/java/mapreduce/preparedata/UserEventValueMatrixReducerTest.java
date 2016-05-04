package mapreduce.preparedata;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alcohol on 5/3/16.
 */
public class UserEventValueMatrixReducerTest {
    @Test
    public void reduceTest() throws IOException {
        ReduceDriver<Text, Text, Text, Text> reduceDriver;
        UserEventValueMatrixReducer userEventValueMatrixReducer = new UserEventValueMatrixReducer();
        reduceDriver = ReduceDriver.newReduceDriver(userEventValueMatrixReducer);

        List<Text> userIdValuePairs = new ArrayList<>(3);
        userIdValuePairs.add(new Text("187558548|3"));
        userIdValuePairs.add(new Text("187558549|4"));
        userIdValuePairs.add(new Text("187558547|1"));
        Text eventId = new Text("123456");
        reduceDriver.withInput(eventId, userIdValuePairs);

        List<Pair<Text, Text>> result = reduceDriver.run();
        Assert.assertEquals(3, result.size());
    }
}
