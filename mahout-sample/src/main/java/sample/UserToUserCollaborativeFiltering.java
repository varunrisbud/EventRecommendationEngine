package sample;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by alcohol on 5/4/16.
 */
public class UserToUserCollaborativeFiltering {

    public List<RecommendedItem> getRecommendation(long userId, int numberOfRecommendations) throws IOException, TasteException {

        File file = new File("/media/alcohol/Study/CS_286-James_Casaletto/Event_Recommendation/OUT/part-r-00000");

        DataModel model = new FileDataModel(file);

        UserSimilarity similarity = new LogLikelihoodSimilarity(model);

        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        FriendsRescorer rescorer = new FriendsRescorer();

        List<RecommendedItem> recommendations = recommender.recommend(userId, numberOfRecommendations, rescorer);

        return recommendations;
    }

    public static void main(String[] args) throws IOException, TasteException {

        UserToUserCollaborativeFiltering userToUserCollaborativeFiltering = new UserToUserCollaborativeFiltering();

        List<RecommendedItem> recommendations = userToUserCollaborativeFiltering.getRecommendation(1918795690L, 3);

        for (RecommendedItem recommendation : recommendations) System.out.println(recommendation);
    }

}
