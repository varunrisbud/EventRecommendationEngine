package sample;

import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.math.neighborhood.HashedVector;

import java.io.*;
import java.util.HashMap;

/**
 * Created by alcohol on 5/7/16.
 */
public class FriendsRescorer implements IDRescorer{

    HashMap<String, String> userFriends = new HashMap<>(2000);

    public void populateUsersFriendFromFile() throws IOException {
        File file = new File("/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/user_friends.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        while((line = br.readLine()) != null) {
            String[] split = line.split(",");
            if(split.length == 2) userFriends.put(split[0], split[1]);
        }
    }

    @Override
    public double rescore(long id, double originalScore) {
        System.out.println("ID: " + id + "\tScore: " + originalScore);
        return originalScore;
    }

    @Override
    public boolean isFiltered(long id) {
        return false;
    }

}
