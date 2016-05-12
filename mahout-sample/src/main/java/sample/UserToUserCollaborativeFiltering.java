package sample;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
            File file = new File("/user/user01/EventRecommendationEngine/OUT/part-r-00000");

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

        List<RecommendedItem> recommendations = recommender.recommend(userId, numberOfRecommendations, rescorer);

        return recommendations;
    }

    public void evaluateResults() throws TasteException {
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
        RecommenderBuilder builder = new MyRecommenderBuilder();
        double result = evaluator.evaluate(builder, null, model, 0.999, 1.0);
        System.out.println(result);
    }
    
    private class MyRecommenderBuilder implements RecommenderBuilder {

        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new LogLikelihoodSimilarity(dataModel);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
            return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
    }

    public static void main(String[] args) throws IOException, TasteException {
        UserToUserCollaborativeFiltering userToUserCollaborativeFiltering = new UserToUserCollaborativeFiltering();

        List<RecommendedItem> recommendations = userToUserCollaborativeFiltering.getRecommendation(4223811312L, 3);

        for (RecommendedItem recommendation : recommendations) System.out.println(recommendation);

        userToUserCollaborativeFiltering.evaluateResults();
    }
}