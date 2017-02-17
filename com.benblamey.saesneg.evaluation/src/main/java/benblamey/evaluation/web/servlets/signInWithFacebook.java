package benblamey.evaluation.web.servlets;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;

import benblamey.evaluation.SocialNetworkConfig;
import socialworld.model.SessionConstants;
import com.benblamey.core.FacebookConfig;


/**
 * Servlet implementation class signInWithFacebook
 */
@WebServlet("/signInWithFacebook")
public class signInWithFacebook extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		FacebookConfig fc = SocialNetworkConfig.getFacebook();

		String state = new BigInteger(130, new SecureRandom()).toString(32);

		request.getSession().setAttribute(SessionConstants.FACEBOOK_ANTI_XSRF,
				state);

		URIBuilder u;
		try {
			u = new URIBuilder("https://www.facebook.com/dialog/oauth");

			// URI-builder handles escaping.
			u.addParameter("client_id", fc.AppID);
			u.addParameter("redirect_uri", fc.RedirectURL);
			u.addParameter("scope", fc.Scope);
			u.addParameter("state", state);

			response.sendRedirect(u.toString());
			
		} catch (URISyntaxException e) {
			// Static/programmer error.
			throw new RuntimeException(e);
		}
	}


}
