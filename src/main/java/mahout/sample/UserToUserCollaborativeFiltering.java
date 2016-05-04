package mahout.sample;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
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

    public List<RecommendedItem> getRecommendation(int userId, int numberOfRecommendations) throws IOException, TasteException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("data.csv").getFile());

        DataModel model = new FileDataModel(file);

        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(userId, numberOfRecommendations);

        return recommendations;
    }

    public static void main(String[] args) throws IOException, TasteException {

            UserToUserCollaborativeFiltering userToUserCollaborativeFiltering = new UserToUserCollaborativeFiltering();

            List<RecommendedItem> recommendations = userToUserCollaborativeFiltering.getRecommendation(2, 3);

            for (RecommendedItem recommendation : recommendations) System.out.println(recommendation);
    }
}
