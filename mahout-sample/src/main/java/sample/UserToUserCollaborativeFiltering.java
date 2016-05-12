package sample;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.math.ssvd.SequentialOutOfCoreSvd;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by alcohol on 5/4/16.
 */
public class UserToUserCollaborativeFiltering {

    public static DataModel model;
    private static UserSimilarity similarity;
    private static UserNeighborhood neighborhood;
    private static UserBasedRecommender recommender;

    static {
        try {
            File file = new File("/user/user01/EventRecommendationEngine/DATA/part-r-00000");

            model = new FileDataModel(file);

            similarity = new LogLikelihoodSimilarity(model);

            neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

            recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<RecommendedItem> getRecommendation(long userId, int numberOfRecommendations) throws IOException, TasteException {
        FriendsRescorer rescorer = new FriendsRescorer(userId);

        List<RecommendedItem> recommendations = recommender.recommend(userId, numberOfRecommendations + 15, rescorer);
        //System.out.println("Size: " + recommendations.size());
        /*for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }*/
        Map<Long, Long> eventDistanceMap = new HashMap<>(recommendations.size());
        for(RecommendedItem recommendedItem: recommendations) {
            long distance = rescorer.computeDistance(String.valueOf(recommendedItem.getItemID()), String.valueOf(userId));
            eventDistanceMap.put(recommendedItem.getItemID(), distance);
        }

        //System.out.println("Map Size: " + eventDistanceMap.size());
        /*for(Map.Entry<Long, Long> entry : eventDistanceMap.entrySet()) {
            System.out.println("Key: " +entry.getKey() + "\t " + entry.getValue());
        }*/


        List<Map.Entry<Long, Long>> distanceEntries = new ArrayList(eventDistanceMap.entrySet());
        Collections.sort(distanceEntries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        List<Map.Entry<Long, Long>> topEventsByDistance = distanceEntries.subList(0, Math.min(distanceEntries.size(), numberOfRecommendations));

        //System.out.println("Sorted Distance");
        /*for (Map.Entry<Long, Long> longLongEntry : topEventsByDistance) {
            System.out.println(longLongEntry.getKey() + "\t " + longLongEntry.getValue());
        }*/

        List<RecommendedItem> finalRecommendations = new ArrayList<>(Math.min(recommendations.size(), numberOfRecommendations));
        for(int i = 0, j = 0; i < Math.min(recommendations.size(), numberOfRecommendations); i++) {
            if(topEventsByDistance.get(i).getValue() < Long.MAX_VALUE) {
                long toFindEventId = topEventsByDistance.get(i).getKey();
                finalRecommendations.add(findEventById(recommendations, toFindEventId));
            }
            else finalRecommendations.add(recommendations.get(j++));
        }

        System.out.println("*****************************************  RECOMMENDATIONS  *********************************\n");
        for (RecommendedItem finalRecommendation : finalRecommendations) {
            long eventId = finalRecommendation.getItemID();
            String distance;
            if(eventDistanceMap.containsKey(eventId) && (eventDistanceMap.get(eventId) < Long.MAX_VALUE)) {
                distance = String.valueOf(eventDistanceMap.get(eventId));
            }
            else distance = "Unknown";
            System.out.println("[Event ID: " + eventId + "\t Score: " + finalRecommendation.getValue() + "\t Distance: " + distance + "kms" +"\t Friends Attending:" + rescorer.getNumberOfFriendAttendingTheEvent(finalRecommendation.getItemID()) + "]");
        }
        System.out.println("\n********************************************************************************************\n");
        return finalRecommendations;
    }

    private RecommendedItem findEventById(List<RecommendedItem> recommendations, long eventId) {
        for(RecommendedItem recommendedItem: recommendations) {
            if(eventId == recommendedItem.getItemID()) return recommendedItem;
        }
        return recommendations.get(0);
    }

    public void evaluateResults() throws TasteException {
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
        RecommenderBuilder builder = new MyRecommenderBuilder();
        double result = evaluator.evaluate(builder, null, model, 0.999, 1.0);
        //System.out.println(result);
    }
    
    private class MyRecommenderBuilder implements RecommenderBuilder {

        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new LogLikelihoodSimilarity(dataModel);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
            return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
    }

    public static void main(String[] args) {
        String userId;
        int numberOfRecommendations;
        Scanner sc = new Scanner(System.in);

        System.out.println("*********************************************************************************************");
        System.out.println("*********************************  USER-TO-USER EVENT'S RECOMMENDATION  *********************");
        System.out.println("*********************************************************************************************\n");
        System.out.print("ENTER USER ID: ");
        userId = sc.nextLine();
        System.out.print("\nENTER NO. OF RECOMMENDATIONS: ");
        numberOfRecommendations = sc.nextInt();

        UserToUserCollaborativeFiltering userToUserCollaborativeFiltering = new UserToUserCollaborativeFiltering();

        try {
            userToUserCollaborativeFiltering.getRecommendation(Long.parseLong(userId), numberOfRecommendations);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            System.out.println("Could not provide recommendations for this user");
        }


        //userToUserCollaborativeFiltering.evaluateResults();
    }
}