package benblamey.tests;

import com.restfb.DefaultFacebookClient;
import com.restfb.types.User;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UserTest {

    @Test
    public void test() {

        String accessToken = "??????";

        DefaultFacebookClient _facebookClient = new DefaultFacebookClient(accessToken);

//        DefaultFacebookClient _facebookClient = new DefaultFacebookClient(
//                "??????????????//",
//                PipelineContext.getCurrentContext().USE_MYSQL_CACHE
//                ? new CachedDefaultWebRequestor()
//                : new DefaultWebRequestor(), new DefaultJsonMapper());
        User user = (User) _facebookClient.fetchObject("me", User.class);

        assertTrue(user.getEmail() != null);

        System.out.println(user.getEmail());
    }

}
