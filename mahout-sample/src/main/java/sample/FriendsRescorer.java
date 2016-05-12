package sample;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by alcohol on 5/7/16.
 */
public class FriendsRescorer implements IDRescorer {

    private long userId;
    public static HashMap<String, String> friendsList = new HashMap<>(2000);
    private static boolean isInitialized = false;
    private HashMap<Long, Integer> usersFriendsEventList = new HashMap<>();
    private int numberOfFriend = 0;

    public FriendsRescorer(long userId) throws IOException, TasteException {
        this.userId = userId;

        this.populateFriendsFromFile();

        this.getEventsUsersFriendsAreGoing(userId);
    }

    private void getEventsUsersFriendsAreGoing(long userId) throws TasteException {
        //String userIdStr = String.valueOf(userId);
       // System.out.println("UserIdStr: " + userIdStr);
        if(friendsList.containsKey(String.valueOf(userId))) {
            String usersFriends = friendsList.get(String.valueOf(userId));

            String[] usersFriendList = usersFriends.split(" ");

            //System.out.println("Users Friend: " + usersFriendList.length + "\n" + Arrays.toString(usersFriendList));

            numberOfFriend = usersFriendList.length;

            this.populateUsersFriendsEventList(usersFriendList);
        }
    }

    private void populateUsersFriendsEventList(String[] usersFriendList) {
        for(String friendId: usersFriendList) {
            try {
                FastIDSet friendsEventList = UserToUserCollaborativeFiltering.model.getItemIDsFromUser(Long.valueOf(friendId));

                this.addEventToUsersFriendEventList(friendsEventList, friendId);

            } catch (TasteException e) {
                //e.printStackTrace();
            }
        }
    }

    private void addEventToUsersFriendEventList(FastIDSet friendsEventList, String friendId) {
        for(long eventId: friendsEventList) {

            try {
                float eventPreference = UserToUserCollaborativeFiltering.model.getPreferenceValue(Long.valueOf(friendId), eventId);
                //System.out.println("Event Preference: " + eventPreference + "\t Event ID: " + eventId + "Friend ID: " + friendId);
                if(eventPreference > 2.0) {
                    int frequency = 1;

                    if (usersFriendsEventList.containsKey(eventId)) frequency += usersFriendsEventList.get(eventId);

                    usersFriendsEventList.put(eventId, frequency);
                }
            } catch (TasteException e) {

            }
        }
    }

    public void populateFriendsFromFile() throws IOException, TasteException {
        if(!isInitialized) {

            File file = new File("/user/use01/EventRecommendationEngine/DATA/user_friends.csv");

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line = null;

            while ((line = br.readLine()) != null) {

                String[] split = line.split(",");

                if (split.length == 2) friendsList.put(split[0], split[1]);
            }

            br.close();

            //this.getUserInTrainDataSetWhoHaveFriendsList();

            isInitialized = true;
        }
    }

   /* private void getUserInTrainDataSetWhoHaveFriendsList() throws FileNotFoundException, UnsupportedEncodingException, TasteException {
        PrintWriter writer = new PrintWriter("/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/usersWithFriends.txt", "UTF-8");
        LongPrimitiveIterator userIdList = UserToUserCollaborativeFiltering.model.getUserIDs();
        while(userIdList.hasNext()) {
            long userId = userIdList.nextLong();
            //System.out.println("Inside while: " + userId);
            if(friendsList.containsKey(String.valueOf(userId))) writer.print(userId + "\n");
        }
        writer.close();
    }
*/
    @Override
    public double rescore(long id, double originalScore) {
        //System.out.println("ID: " + id + "\tScore: " + originalScore);

        double newScore;

        if(usersFriendsEventList.containsKey(id)) {
            newScore = ((double) usersFriendsEventList.get(id) / numberOfFriend);
            if(!Double.isNaN(originalScore)) newScore += originalScore;
            System.out.println("ID: " + id + "\t Friends Attending: "+ usersFriendsEventList.get(id) +"\t Original Score: " + originalScore + "\t NewScore: " + newScore);
            return newScore;
        }

        return originalScore;
    }

    @Override
    public boolean isFiltered(long id) {
        return false;
    }

}
