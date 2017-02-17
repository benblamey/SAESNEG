package benblamey.evaluation.web.servlets.debug;

import benblamey.core.facebook.FacebookIDs;
import benblamey.evaluation.web.debug.EvaluationSessionManager;
import benblamey.evaluation.web.debug.ProcessedLifeStoryCache;
import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.configs.TomcatExperimentSet;
import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import socialworld.model.SocialWorldUser;

/**
 * Servlet implementation class LifeStory
 */
public class ViewGroundTruthEvents extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!EvaluationSessionManager.ensureAdminLoggedOn(request, response)) {
            return;
        }

        String parameter = request.getParameter("id");
        String userID;
        if (parameter != null) {
            Long.parseLong(parameter); // Throw if injection attack.
            userID = parameter;
        } else {
            userID = FacebookIDs.PARTICIPANT_1_FACEBOOK_ID;
        }

        SocialWorldUser user = new SocialWorldUser(userID);

        UserContext uc;

        String allParam = request.getParameter("all");
        boolean all = (allParam != null);

        String processParam = request.getParameter("processed");
        boolean process = (processParam != null);
        if (process) {
            try {
                uc = ProcessedLifeStoryCache.getLifeStory(userID);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            uc = UserContext.fromSocialWorldUser(user);
            Experiment.selectLifeStory(uc, TomcatExperimentSet.ExperimentOptions.lifeStorySelectStrategy);
        }

        Collection<Event> events;
        if (all) {
            events = new ArrayList<Event>();
            for (Datum d : uc.getLifeStory().datums) {
                Event e = new Event();
                e.getDatums().add(d);
                events.add(e);
            }
        } else {
            events = LifeStoryJsonSerializer.getGroundTruthEvents(uc.getLifeStory(), user);
        }
        request.setAttribute("EVENTS", events);
        request.setAttribute("LIFESTORY", uc.getLifeStory());
        request.setAttribute("OWNER_NAME", uc.getName());
        request.setAttribute("TITLE", uc.getName() + " (Processed)");

        RequestDispatcher dispatcher = request
                .getRequestDispatcher("WEB-INF/ViewGroundTruthEvents.jsp");
        dispatcher.forward(request, response);
    }

}
