package sample;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alcohol on 5/7/16.
 */
public class FriendsRescorer implements IDRescorer {

    private long userId;
    public static HashMap<String, String> friendsList = new HashMap<>(2000);
    private static boolean isInitialized = false;
    private HashMap<Long, Integer> usersFriendsEventList = new HashMap<>();
    private int numberOfFriend = 0;
    GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBY6isFfznblaG45uuUemQKFVeH7ZJl7EI");

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

            File file = new File("/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/input/user_friends.csv");

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

   /*public void getUserInTrainDataSetWhoHaveFriendsList() throws FileNotFoundException, UnsupportedEncodingException, TasteException {
       PrintWriter writer = new PrintWriter("/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/input/usersWithLocations.txt", "UTF-8");
       LongPrimitiveIterator userIdList = UserToUserCollaborativeFiltering.model.getUserIDs();
       Map<Long, String> userMap = new HashMap<>();
       BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/input/users.csv")));
       String line = null;
       try {
           while((line = br.readLine()) != null) {
               String[] split = line.split(",");
               //System.out.println(line);
               if(split.length > 5 && split[0] != null && split[5] != null) {
                    userMap.put(Long.parseLong(split[0]), split[5]);
               }
           }
           while(userIdList.hasNext()) {
                long userId = userIdList.nextLong();
                //System.out.println("Inside while: " + userId);
                if(userMap.containsKey(userId)) writer.print(userId + "\n");
            }
       } catch (IOException e) {
           e.printStackTrace();
       }
       writer.close();
    }*/

    @Override
    public double rescore(long id, double originalScore) {
        //System.out.println("ID: " + id + "\tScore: " + originalScore);

        double newScore;

        if(usersFriendsEventList.containsKey(id)) {
            newScore = ((double) usersFriendsEventList.get(id) / numberOfFriend);
            if(!Double.isNaN(originalScore)) newScore += originalScore;
            //System.out.println("ID: " + id + "\t Friends Attending: "+ usersFriendsEventList.get(id) +"\t Original Score: " + originalScore + "\t NewScore: " + newScore);
            return newScore;
        }

        return originalScore;
    }

    @Override
    public boolean isFiltered(long id) {
        return false;
    }


    public long computeDistance(String eventID, String userID) {
        boolean LAT_LANG_PRESENT = true;
        boolean ZIP_PRESENT = true;
        boolean CITY_PRESENT = true;
        boolean STATE_PRESENT = true;
        boolean COUNTRY_PRESENT = true;

        String eventLineLookupFile = "/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/input/eventAndLineLookup.csv";
        String cleanedEventFile = "/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/input/cleanedEventData.csv";
        String userFile = "/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/input/users.csv";

        String USER_LOCATION = findUser(userFile, userID);
        if (USER_LOCATION.isEmpty()) {
            //System.out.println("User location empty");
            return Long.MAX_VALUE;
        } else {
            //System.out.println("User location not empty");
            int lineID = lookupEvent(eventLineLookupFile, eventID);

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(cleanedEventFile));
                String line;
                int lineCounter = 0;
                while ((line = br.readLine()) != null) {
                    lineCounter++;
                    if (lineCounter > lineID) {
                        //System.out.println("Event ID: " + eventID + "\t Line" + line);
                        break;
                    }
                }

            //try (Stream<String> lines = Files.lines(Paths.get(cleanedEventFile))) {
                //String line = lines.skip(lineID).findFirst().get();
                String[] eventDetails = line.split(",");
                if (eventDetails[5].trim().isEmpty() || eventDetails[6].trim().isEmpty()) {
                    LAT_LANG_PRESENT = false;
                }
                if (eventDetails[1].trim().isEmpty()) {
                    CITY_PRESENT = false;
                }
                if (eventDetails[3].trim().isEmpty()) {
                    ZIP_PRESENT = false;
                }
                if (eventDetails[2].trim().isEmpty()) {
                    STATE_PRESENT = false;
                }
                if (eventDetails[4].trim().isEmpty()) {
                    COUNTRY_PRESENT = false;
                }
                if (LAT_LANG_PRESENT) {
                    //System.out.println("Lat lang present");
                    return calculateDistance(USER_LOCATION, eventDetails[5].trim(), eventDetails[6].trim());
                } else {
                    if (CITY_PRESENT || STATE_PRESENT || ZIP_PRESENT || COUNTRY_PRESENT) {
                        return calculateDistance(USER_LOCATION, eventDetails[1], eventDetails[2], eventDetails[3],
                                eventDetails[4]);

                    } else {
                        return Long.MAX_VALUE;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //} catch (Exception e) {
               // e.printStackTrace();
            //}
            return Long.MAX_VALUE;
        }
    }

    private long calculateDistance(String userLocation, String city, String state, String zip, String country) {
        GeocodingResult[] results;
        try {
            String eventLocation = city + " " + state + " " + zip + " " + country;

            results = GeocodingApi.geocode(context, userLocation).await();
            LatLng origin = results[0].geometry.location;

            results = GeocodingApi.geocode(context, eventLocation).await();
            LatLng destination = results[0].geometry.location;

            DistanceMatrix matrix = DistanceMatrixApi.newRequest(context).origins(origin).destinations(destination)
                    .await();
            if (matrix.rows[0].elements[0].status.toString() == "OK") {
                Long distanceInMeters = matrix.rows[0].elements[0].distance.inMeters;
                //System.out.println(distanceInMeters);
                return (distanceInMeters / 1000L);
            } else {
                return Long.MAX_VALUE;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Long.MAX_VALUE;
    }

    private long calculateDistance(String userLocation, String latitude, String longitude) {
        GeocodingResult[] results;
        try {
            results = GeocodingApi.geocode(context, userLocation).await();
            LatLng origin = results[0].geometry.location;
            LatLng destination = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            DistanceMatrix matrix = DistanceMatrixApi.newRequest(context).origins(origin).destinations(destination)
                    .await();
            if (matrix.rows[0].elements[0].status.toString() == "OK") {
                Long distanceInMeters = matrix.rows[0].elements[0].distance.inMeters;
                //System.out.println(distanceInMeters);
                return (distanceInMeters / 1000L);
            } else {
                return Long.MAX_VALUE;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Long.MAX_VALUE;
    }

    private int lookupEvent(String eventLineLookupFile, String eventID) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(eventLineLookupFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] eventDetails = line.split(",");
                if (eventID.equals(eventDetails[0])) {
                    return Integer.parseInt(eventDetails[1]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    private String findUser(String userFile, String userID) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(userFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] userDetails = line.split(",");
                if (userDetails[0].equals(userID)) {
                    String location = userDetails[5];
                    if (location.isEmpty()) {
                        return "";
                    } else {
                        return location;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    public int getNumberOfFriendAttendingTheEvent(long eventId) {
        if(usersFriendsEventList.containsKey(eventId)) return usersFriendsEventList.get(eventId);
        return 0;
    }
}
