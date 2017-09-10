package com.benblamey.evaluation.web.servlets;

import com.benblamey.core.SystemInfo;
import com.benblamey.core.Email;
import com.benblamey.evaluation.web.GroundTruthLifeStoryViewModel;
import com.benblamey.evaluation.web.debug.EvaluationSessionManager;
import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.serialization.LifeStoryInfo;
import com.benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import com.benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import java.io.IOException;
import javax.mail.MessagingException;
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
 * Servlet implementation class SaveUserStory
 */
public class GroundTruth extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String GROUND_TRUTH_STATE = "ground_truth_state";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DateTime start = DateTime.now();

        CrossAppletSession cas = new CrossAppletSession(request);
        if (!cas.isSignedIn()) {
            // Redirect to social world index for ethics + login. (redirect back afterwards)
            response.sendRedirect("/benblamey.evaluation/");
            return;
        }

        // Admins are allowed to view someone else's data for debugging.
        String userStoryToShow;
        if (EvaluationSessionManager.IsBenLoggedOn(cas) && request.getParameter("userid") != null) {
            userStoryToShow = (String) request.getParameter("userid");
        } else {
            userStoryToShow = cas.getFacebookIDOrNull();

        }

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

        System.out.println("Serving Ground Truth for user " + userStoryToShow
                + " - deser LS after " + Seconds.secondsBetween(start, DateTime.now()).getSeconds() + "secs.");

        // Replace the event-set on the deserialized life-story with any existing ground-truth.
        ls.EventsGolden = LifeStoryJsonSerializer.getGroundTruthEvents(ls, user);

        // The view-model creates the pseudo-events.
        GroundTruthLifeStoryViewModel lsvm = new GroundTruthLifeStoryViewModel(ls);

        newState(request); // Each get request updates the state - subsequent POSTs much match.
        // (only one page is allowed to be active at a time.)

        request.setAttribute("LIFESTORYVIEWMODEL", lsvm);

        RequestDispatcher dispatcher = request
                .getRequestDispatcher("WEB-INF/groundtruth.jsp");
        dispatcher.forward(request, response);

        System.out.println("Serving Ground Truth for user " + userStoryToShow
                + " - done after " + Seconds.secondsBetween(start, DateTime.now()).getSeconds() + "secs.");
    }



    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        CrossAppletSession cas = new CrossAppletSession(request);

        SocialWorldUser user = new SocialWorldUser(cas.getFacebookIDOrNull(), false);

        if (request.getParameterMap().containsKey("events")) {

            // This mechanism is to prevent two windows being open, both sending updates.
            String requestState = request.getParameter(GROUND_TRUTH_STATE);
            if (!verifyState(requestState, request)) {
                response.sendError(512, "state out of sync - please refresh the page.");
            }

            String eventsJSON = request.getParameter("events");

            if (eventsJSON == null || eventsJSON.length() == 0) {
                throw new ServletException("'events' parameter is missing.");
            }

            BasicDBObject eventsGroundTruth = (BasicDBObject) JSON.parse(eventsJSON);

            //System.out.println(eventsGroundTruth.toString());
            //System.out.println("Cleaning up payload...");
            eventsGroundTruth = LifeStoryJsonSerializer.cleanUpEvents(eventsGroundTruth);

            //System.out.println(eventsGroundTruth.toString());
            //Object foo = eventsGroundTruth.get("events");
            user.setValue(LifeStoryJsonSerializer.GROUND_TRUTH_EVENTS, eventsGroundTruth);
        } else if (request.getParameterMap().containsKey("finished")) {

            user.setValue(SocialWorldUser.FINISHED_EDITING_GROUND_TRUTH, true);

            if (SystemInfo.doesServerSendEmail()) {
                try {
                    Email.send(Email.BEN_EMAIL_ADDRESS,
                            "User Finished Ground Truth Editing",
                            "Facebook ID: " + cas.getFacebookIDOrNull(), "GroundTruth");

                    // We may not have an email address (e.g. mobile phone used).
                    String userEmail = (String) user.getValue(SocialWorldUser.FACEBOOK_EMAIL);
                    if (userEmail != null && !userEmail.isEmpty()) {
                    Email.send(userEmail,
                            "Thank you.",
                            "Dear " + user.getName() + ",\n"
                            + "\n"
                            + "Thank you for participating in the study!\n"
                            + "If you have any queries or feedback, just reply to this email.\n"
                            + "\n"
                            + "Many Thanks,\n"
                            + "\n"
                            + "Ben Blamey\n"
                            + Email.BEN_EMAIL_ADDRESS, "Ben Blamey");
                    }

                } catch (MessagingException e) {
                    throw new ServletException(e);
                }
            } else {
                System.out.println("Skipping email - server not configured.");
            }

        } else {

            throw new ServletException("Request not understood.");

        }
    }

    private static String newState(HttpServletRequest request) {
        // Generate a new state.
        String state = Long.toString(System.nanoTime() % 1000000);
        request.getSession().setAttribute(GROUND_TRUTH_STATE, state);
        return state;
    }

    private static boolean verifyState(String state, HttpServletRequest request) throws ServletException {

        String sessionState = (String) request.getSession().getAttribute(GROUND_TRUTH_STATE);

        if (sessionState == null) {
            // Server must have been restarted. First one wins!
            request.getSession().setAttribute(GROUND_TRUTH_STATE, state);
            return true;
        }

        if (state == null || state.length() == 0) {
            System.out.println("state is null.");
            throw new ServletException();
        }

        boolean stateTheSame = state.equals(sessionState);
        if (!stateTheSame) {
            System.out.println("Session: " + sessionState + ", client: " + state);
        }

        return stateTheSame;
    }

}
