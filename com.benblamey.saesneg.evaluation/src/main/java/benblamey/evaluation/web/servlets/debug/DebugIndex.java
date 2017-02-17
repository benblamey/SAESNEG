package benblamey.evaluation.web.servlets.debug;

import com.benblamey.core.FileUtil;
import benblamey.evaluation.web.debug.EvaluationSessionManager;
import benblamey.saesneg.ExperimentUserContext;
import benblamey.saesneg.Users;
import benblamey.saesneg.experiments.DatumsScope;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.serialization.LifeStoryInfo;
import benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import com.mongodb.DBObject;
import com.restfb.types.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import socialworld.model.SocialWorldUser;

/**
 * Servlet implementation class Index
 */
public class DebugIndex extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!EvaluationSessionManager.ensureAdminLoggedOn(request, response)) {
            return;
        }

        System.out.println("Loading /debug");

        PrintWriter resp = response.getWriter();

        resp.println("<html>");
        resp.println("<head>");
        resp.println("<title>Debug-Index</title>");
        resp.println("</head>");
        resp.println("<body>");

        for (DBObject userObj : Users.getAllUsers()) {
            SocialWorldUser user = new SocialWorldUser(userObj, true);

            
            resp.println(user.getName());

            resp.println(" ");

            if (null != user.getValue(LifeStoryInfo.LIFE_STORY_INFOS)) {
                resp.println(" (LS) ");

                if (null != user.getValue(LifeStoryJsonSerializer.GROUND_TRUTH_EVENTS)) {
                    resp.println("<a href=\"ViewGroundTruthEvents?id="
                            + user.getValue(SocialWorldUser.FACEBOOK_USER_ID)
                            + "\">View Life Story Events</a> ");

                    resp.println("<a href=\"ViewGroundTruthEvents?processed=true&id="
                            + user.getValue(SocialWorldUser.FACEBOOK_USER_ID)
                            + "\">View Life Story Events -- Processed</a> ");

                    resp.println("<a href=\"ViewGroundTruthEvents?processed=true&all=true&id="
                            + user.getValue(SocialWorldUser.FACEBOOK_USER_ID)
                            + "\">View Life Story Events -- Processed (ALL)</a> ");

                    resp.println("<a href=\"ComputeEventClusters?id="
                            + user.getValue(SocialWorldUser.FACEBOOK_USER_ID)
                            + "\">Compute Events</a> ");

                    resp.println("<a href=\"CompareClusterAccuracy?id="
                            + user.getValue(SocialWorldUser.FACEBOOK_USER_ID)
                            + "\">Compare Accuracy</a> ");
                }

                String allDatumFileName = ExperimentUserContext.getClusterOutputFilename(
                        UserContext.getOutputDirectoryWithTrailingSlashForUser(user.getPrettyName()));
                if (FileUtil.exists(allDatumFileName)) {
                    resp.println("<a href=\"LifeLogInterface?"
                            + "id=" + user.getValue(SocialWorldUser.FACEBOOK_USER_ID)
                            + "&scope=" + DatumsScope.AllDatums.name()
                            + "\">LifeLog (all)</a> ");
                }

                String gtOnlyFileName = ExperimentUserContext.getClusterOutputFilename(
                        UserContext.getOutputDirectoryWithTrailingSlashForUser(user.getPrettyName()));
                if (FileUtil.exists(gtOnlyFileName)) {
                    resp.println("<a href=\"LifeLogInterface?"
                            + "id=" + user.getValue(SocialWorldUser.FACEBOOK_USER_ID)
                            + "&scope=" + DatumsScope.DatumsUsedInGroundTruth.name()
                            + "\">LifeLog (gt only)</a> ");
                }

            }

            resp.println("<br/>");
        }

        resp.println("</body>");
        resp.println("</html>");

        System.out.println("Done.");
    }

}
