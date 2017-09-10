package com.benblamey.evaluation.web.servlets.debug;

import com.benblamey.core.FileUtil;
import com.benblamey.core.facebook.FacebookIDs;
import com.benblamey.evaluation.web.GroundTruthLifeStoryViewModel;
import com.benblamey.evaluation.web.debug.EvaluationSessionManager;
import com.benblamey.saesneg.ExperimentUserContext;
import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.serialization.LifeStoryInfo;
import com.benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import com.benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import socialworld.model.CrossAppletSession;
import socialworld.model.SocialWorldUser;

/**
 * Servlet implementation class LifeStory
 */
public class LifeLogInterface extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DateTime start = DateTime.now();

        if (!EvaluationSessionManager.ensureAdminLoggedOn(request, response)) {
            return;
        }

        String userStoryToShow;
        if (request.getParameter("id") != null) {
            userStoryToShow = (String) request.getParameter("id");
        } else {
            CrossAppletSession cas = new CrossAppletSession(request);
            userStoryToShow = cas.getFacebookIDOrNull();
        }

        request.setAttribute("USERID", userStoryToShow);

        System.out.println("Loading story for user: " + userStoryToShow);

        SocialWorldUser user = new SocialWorldUser(
                userStoryToShow,
                false);
        UserContext uc = UserContext.fromSocialWorldUser(user);

        LifeStoryInfo latestInfo = LifeStoryInfo.getLatestLifeStory(user);
        if (latestInfo == null) {
            System.err.append("There is no valid life story according to records in mongo, user: " + userStoryToShow);
            response.sendRedirect("./");
            return;
        } else if (!latestInfo.success) {
            System.err.append("Latest life story has an error, user: " + userStoryToShow);
            response.sendRedirect("./");
            return;
        }

        LifeStory ls = LifeStoryXMLSerializer.DeserializeLifeStory(latestInfo.filename, uc);
        request.setAttribute("LIFESTORYINFO", latestInfo);
        request.setAttribute("LIFESTORY", ls);


        // Replace the event-set on the deserialized life-story with any existing ground-truth.
        ls.EventsGolden = LifeStoryJsonSerializer.getGroundTruthEvents(ls, user);

        // The view-model creates the pseudo-events.
        GroundTruthLifeStoryViewModel lsvm = new GroundTruthLifeStoryViewModel(ls);

        request.setAttribute("LIFESTORYVIEWMODEL", lsvm);

        RequestDispatcher dispatcher = request
                .getRequestDispatcher("WEB-INF/lifeLogInterface.jsp");
        dispatcher.forward(request, response);

        System.out.println("Served LifeLogInterface for user " + userStoryToShow
                + " - done after " + Seconds.secondsBetween(start, DateTime.now()).getSeconds() + "secs.");


//        String clusterOutputFilename = ExperimentUserContext.getClusterOutputFilename(UserContext.getOutputDirectoryWithTrailingSlashForUser(user.getPrettyName()));
//        if (!FileUtil.exists(clusterOutputFilename)) {
//            throw new IllegalArgumentException("events json file does not exist.");
//        }
//        response.getOutputStream().print(clusterOutputFilename);

    }

}
