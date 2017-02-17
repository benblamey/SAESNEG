package benblamey.evaluation.web.servlets.debug;

import benblamey.evaluation.web.CompareClusterAccuracyOutput;
import benblamey.evaluation.web.debug.DevelopmentFlags;
import benblamey.evaluation.web.debug.EvaluationSessionManager;
import benblamey.evaluation.web.debug.ProcessedLifeStoryCache;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.phaseB.SVMEdgeClassifier;
import benblamey.saesneg.phaseB.eval.EventMatcher;
import benblamey.saesneg.serialization.LifeStoryInfo;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import socialworld.model.CrossAppletSession;
import socialworld.model.SocialWorldUser;

/**
 * Servlet implementation class
 */
public class CompareClusterAccuracy extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

        UserContext userContext;
        try {
            userContext = ProcessedLifeStoryCache.getLifeStory(userStoryToShow);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        LifeStory ls = userContext.getLifeStory();

        CompareClusterAccuracyOutput eventPairs = new CompareClusterAccuracyOutput();

        EventMatcher.run(eventPairs, ls.EventsGolden, ls.EventsComputed);

        request.setAttribute("EVENTPAIRS", eventPairs);

        request.setAttribute("LIFESTORY", ls);
        request.setAttribute("LIFESTORYINFO", latestInfo);

        SVMEdgeClassifier svm = new SVMEdgeClassifier(ProcessedLifeStoryCache.SVM_MODEL_PATH);
        request.setAttribute("SVM", svm);

        // Development switch.
        request.setAttribute("SHOW_DUMMY_IMAGES", DevelopmentFlags.ShowDummyImages());

        //request.setAttribute("LIFESTORY", ls);
        RequestDispatcher dispatcher = request
                .getRequestDispatcher("WEB-INF/compareclusteraccuracy.jsp");
        dispatcher.forward(request, response);

    }

}
