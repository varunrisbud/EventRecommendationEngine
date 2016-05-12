package mapreduce.preparedata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Created by alcohol on 5/2/16.
 */
public class UserEventValueMatrixDriver extends Configured implements Tool {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set("mapreduce.output.textoutputformat.separator", ",");
        System.exit(ToolRunner.run(conf, new UserEventValueMatrixDriver(), args));
    }

    public int run(String[] args) throws Exception {
        Job job = new Job(getConf(), "CreateUserEventValueMatrix");
        job.setJarByClass(UserEventValueMatrixDriver.class);
        job.setMapperClass(UserEventValueMatrixMapper.class);
        job.setReducerClass(UserEventValueMatrixReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(KeyValueTextInputFormat.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        return job.waitForCompletion(true) ? 0:1;
    }
}
