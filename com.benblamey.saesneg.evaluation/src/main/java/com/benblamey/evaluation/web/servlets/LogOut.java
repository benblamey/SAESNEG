package com.benblamey.evaluation.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.benblamey.evaluation.SocialNetworkConfig;
import socialworld.model.CrossAppletSession;

/**
 * Servlet implementation class LogOut
 */
@WebServlet("/LogOut")
public class LogOut extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogOut() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// Invalidate the cross-applet-session data.
		CrossAppletSession cas = new CrossAppletSession(request);
		cas.RemoveFacebookID();

		// Invalidate the actual session.
		request.getSession().invalidate();

		// Go to index.
		response.sendRedirect(SocialNetworkConfig.getURLAfterLogout());
	}

}
