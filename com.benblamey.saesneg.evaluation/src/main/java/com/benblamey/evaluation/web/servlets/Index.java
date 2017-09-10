package com.benblamey.evaluation.web.servlets;

import com.benblamey.saesneg.serialization.LifeStoryInfo;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import socialworld.model.CrossAppletSession;
import socialworld.model.NoSuchUserException;
import socialworld.model.SessionConstants;
import socialworld.model.SocialWorldUser;

/**
 * Servlet implementation class Index
 */
public class Index extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        CrossAppletSession cas = new CrossAppletSession(request);
        HttpSession session = request.getSession();

        if (cas.isSignedIn()) {

            System.out.println("User visited /Index :" + cas.getFacebookIDOrNull());

            SocialWorldUser user;
            try {
                    user = new SocialWorldUser(cas.getFacebookIDOrNull());
            } catch (NoSuchUserException e) {
                    // User has been deleted. Log them out.
                    response.sendRedirect("LogOut");
                    return;
            }

            if (session.getAttribute(SessionConstants.HAS_DONE_ETHICS_TEMP) != null) {
                    user.setValue(SocialWorldUser.HAS_DONE_ETHICS, true);
                    session.removeAttribute(SessionConstants.HAS_DONE_ETHICS_TEMP);
            }

            // The user is now logged in. Check to see if we need to redirect to a specific part of the site.
            Object doneEthics = user.getValue(SocialWorldUser.HAS_DONE_ETHICS);
            if (doneEthics != null && (Boolean)doneEthics) {

                    System.out.println("User" + cas.getFacebookIDOrNull() + " loaded /Index ");

                    LifeStoryInfo latestInfo = LifeStoryInfo.getLatestLifeStory(user);
                    request.setAttribute("LATEST_LIFE_STORY_INFO", latestInfo);

                    RequestDispatcher dispatcher = request
                            .getRequestDispatcher("WEB-INF/index.jsp");

                    dispatcher.forward(request, response);


            } else {
                    // They are signed in but haven't done the ethics
                    // This probably means they clicked on "login" when they hadn't registered - show them the consent
                    // form but hide the login link.
                    response.sendRedirect("Ethics");
            }

        } else {
                // User is not signed in.

                if (session.getAttribute(SessionConstants.HAS_DONE_ETHICS_TEMP) != null && (boolean)session.getAttribute(SessionConstants.HAS_DONE_ETHICS_TEMP)) {
                        // They have done the ethics, so now sign in.
                        response.sendRedirect("signInWithFacebook");
                } else {

                        // They have neither signed in nor done ethics, and we don't know which to do.

                        // Send them to Ethics. Existing users have a hyperlink to skip the consent form (they still get asked after
                        // logging in if they haven't done it).
                        response.sendRedirect("Ethics");
                }
        }

    }

}
