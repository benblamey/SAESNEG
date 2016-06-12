package benblamey.evaluation.web.servlets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;

import benblamey.evaluation.SocialNetworkConfig;
import socialworld.model.CrossAppletSession;
import socialworld.model.SessionConstants;
import socialworld.model.SocialWorldUser;
import com.benblamey.core.FacebookConfig;
import com.benblamey.core.URLUtils;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;


/**
 * Servlet implementation class Callback
 */
@WebServlet("/callback")
public class FacebookCallback extends HttpServlet {

	public final static String PATH = "/callback";

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		final FacebookConfig fc = SocialNetworkConfig.getFacebook();

		String state = (String) request.getSession().getAttribute(
				SessionConstants.FACEBOOK_ANTI_XSRF);

		if ((request.getParameter("state") == null)
				|| !request.getParameter("state").equals(state)) {
			response.getWriter().print("OAuth error."); // Message for the user.
			response.sendError(500, "State doesn't match");
			return;
		}

		CrossAppletSession cas = new CrossAppletSession(request);
		
		
		// Save specific pre-login session data
		boolean doneEthicsJustNow = request.getSession().getAttribute(SessionConstants.HAS_DONE_ETHICS_TEMP) != null;
		String redirectAfterLogin = cas.getRedirectAfterLoginTemp();

		request.getSession().invalidate();
		cas = null;

		String code = (String) request.getParameter("code");
		if (code == null || code.isEmpty()) {
			response.sendError(500, "No code.");
			return;
		}

		URIBuilder u = null;

		// 
		try {
			u = new URIBuilder("https://graph.facebook.com/oauth/access_token");
		} catch (URISyntaxException e) {
			// Static error.
			throw new ServletException(e);
		}

		// URI-builder handles escaping.
		u.addParameter("client_id", fc.AppID);
		u.addParameter("redirect_uri", fc.RedirectURL);
		u.addParameter("client_secret", fc.AppSecret);
		u.addParameter("code", code);

		String fb_response;
		try {
			fb_response = URLUtils.GetURL(u.build().toURL());
		} catch (URISyntaxException e) {
			// Static error.
			throw new ServletException(e);
		}

		// Example:
		// access_token=USER_ACCESS_TOKEN&expires=NUMBER_OF_SECONDS_UNTIL_TOKEN_EXPIRES
		Map<String, String> facebookTokens = URLUtils
				.getQueryMap(fb_response);


		response.getWriter().print(facebookTokens);

		// (This is a 60-day token.)
		String accessToken = facebookTokens.get("access_token");
		int expiresInSeconds = Integer.parseInt(facebookTokens.get("expires").trim()); // We need to remove trailing whitespace.
		//System.out.println("FacebookCallback - token expires in: " + expiresInSeconds);
		
		// find out the identity of the person who has logged in.
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken
                        //, FacebookConfig.FacebookAPIVersion
                );
		User me = facebookClient.fetchObject("me", User.class);
		
		if (me == null) { 
			response.sendError(500, "Couldn't verify token.");
			return;
		}
		
		String id = me.getId();
                
		
		if (id == null || id.isEmpty()) {
			response.sendError(500, "No ID.");
			return;				
		}
                if (me.getName() == null) {
			response.sendError(500, "No name.");
			return;				                    
                }
		
		// Now the user has logged in, create a new cross-applet-session based on the current request, and store the redirect URI inside it.
		cas = new CrossAppletSession(request);
		cas.setRedirectAfterLoginTemp(redirectAfterLogin);
		cas.SetFacebookID(id);
		
		SocialWorldUser user = new SocialWorldUser(id);
		
		if (doneEthicsJustNow) {
			// If they've just done ethics, update the database.
			user.setValue(SocialWorldUser.HAS_DONE_ETHICS, doneEthicsJustNow);
		}
		
                user.setValue(SocialWorldUser.FACEBOOK_NAME, me.getName());
		user.setValue(SocialWorldUser.FACEBOOK_EMAIL, me.getEmail());
		user.setValue(SocialWorldUser.FACEBOOK_BIRTHDAY, me.getBirthday());
		
		// "In some cases, this newer long-lived token might be identical to the previous one, but we can't guarantee it and your app shouldn't depend upon it."
		// See: https://developers.facebook.com/docs/facebook-login/access-tokens/
		
		user.setValue(SocialWorldUser.FACEBOOK_OAUTH_TOKEN_UPDATED_UNIX, (int) (System.currentTimeMillis() / 1000L));
		user.setValue(SocialWorldUser.FACEBOOK_OAUTH_TOKEN_EXPIRES_UNIX, (int) (System.currentTimeMillis() / 1000L) + expiresInSeconds);
		user.setValue(SocialWorldUser.FACEBOOK_OAUTH_TOKEN, accessToken);
					
		// Done. Go to index. Index handles the redirection (this is because ethics may happen before or after login).
		response.sendRedirect("");


	}

}
