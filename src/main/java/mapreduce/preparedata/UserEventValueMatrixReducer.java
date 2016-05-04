package mapreduce.preparedata;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created by alcohol on 5/2/16.
 */
public class UserEventValueMatrixReducer extends Reducer<Text,Text,Text,Text> {

    public void reduce(Text eventId, Iterable<Text> userIdValuePairs, Context context) throws IOException, InterruptedException {
        for(Text userIdValuePair: userIdValuePairs) {
            String value = userIdValuePair.toString();
            String[] userIdValuePairParts = value.split("\\|");
            StringBuilder reducerValue = new StringBuilder();
            reducerValue.append(eventId.toString()).append(",").append(userIdValuePairParts[1]);
            context.write(new Text(userIdValuePairParts[0] + ","), new Text(reducerValue.toString()));
        }
    }

}
