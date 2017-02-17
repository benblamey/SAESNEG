package benblamey.evaluation.web.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import socialworld.model.CrossAppletSession;
import socialworld.model.SessionConstants;
import socialworld.model.SocialWorldUser;

/**
 * Servlet implementation class Ethics
 */
@WebServlet("/Ethics")
public class Ethics extends HttpServlet {
	
	
	private static final long serialVersionUID = 1L;
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (request.getSession().getAttribute(SessionConstants.HAS_DONE_ETHICS_TEMP) != null) {
			// User has already completed ethics. Go to index.
			response.sendRedirect("");
			return;
		}
		
		RequestDispatcher dispatcher = 
		        request.getRequestDispatcher("WEB-INF/ethics.jsp");
		    dispatcher.forward( request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append(
				request.getParameterMap().toString());
		String formRead = request.getParameter("read");
		String formVolunt = request.getParameter("volunt");
		String formConsent = request.getParameter("consent");
		
		if (formRead == null || formVolunt == null || formConsent == null) {
			// Redirect them back to the form, show a div with a message, and scroll down to it.
			response.sendRedirect("Ethics?check=1#must_check");
		} else {
			// Ethics done. 
			
			CrossAppletSession cas = new CrossAppletSession(request);
			
			if (cas.isSignedIn()) {
				SocialWorldUser user = SocialWorldUser.fromCrossAppletSession(cas); // Allow add.
				user.setValue(SocialWorldUser.HAS_DONE_ETHICS, true);
			} else {
				request.getSession().setAttribute(SessionConstants.HAS_DONE_ETHICS_TEMP, true);
			}
			
			// Redirect to index.
			response.sendRedirect("");
		}
	}

}
