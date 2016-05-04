package mapreduce.preparedata;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/**
 * Created by alcohol on 5/2/16.
 */
public class UserEventValueMatrixMapper extends Mapper<Text, Text, Text, Text> {
    private static final String yesValue = "3";
    private static final String maybeValue = "2";
    private static final String noValue = "1";

    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        String[] valueParts = key.toString().split(",");
        if(valueParts.length == 5) {
            emitUserEventValueTriplets(valueParts[1], valueParts[0], yesValue, context);
            emitUserEventValueTriplets(valueParts[2], valueParts[0], maybeValue, context);
            emitUserEventValueTriplets(valueParts[4], valueParts[0], noValue, context);
        }
    }

    private void emitUserEventValueTriplets(String userIdListString, String eventId, String value, Context context) throws IOException, InterruptedException {
        String[] userIdList = userIdListString.split(" ");
        for(String userId: userIdList) {
            StringBuilder mapValue = new StringBuilder();
            mapValue.append(userId).append("|").append(value);
            context.write(new Text(eventId), new Text(mapValue.toString()));
        }
    }

}
