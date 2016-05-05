package sample;

import junit.framework.Assert;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by alcohol on 5/4/16.
 */
public class UserToUserCollaborativeFilteringTest {
    @Test
    public void getRecommendationsTest() throws IOException, TasteException {
        UserToUserCollaborativeFiltering userToUserCollaborativeFiltering = new UserToUserCollaborativeFiltering();
        List<RecommendedItem> recommendations = userToUserCollaborativeFiltering.getRecommendation(2, 3);
        Assert.assertEquals(3, recommendations.size());
    }
}
