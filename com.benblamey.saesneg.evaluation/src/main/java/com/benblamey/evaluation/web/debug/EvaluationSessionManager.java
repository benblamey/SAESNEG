package com.benblamey.evaluation.web.debug;

import com.benblamey.core.SystemInfo;
import com.benblamey.core.SystemInfo.BAGServer;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import socialworld.model.CrossAppletSession;

public class EvaluationSessionManager {

	private EvaluationSessionManager() {};

	public static boolean IsBenLoggedOn(CrossAppletSession cas) {
        String id = cas.getFacebookIDOrNull();

        boolean result = (id != null) && id.equals(BEN_BLAMEY_FACEBOOK_ID);
        return result;
    }

    public static boolean ensureAdminLoggedOn(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        if (SystemInfo.detectServer() != BAGServer.LOCAL_MACHINE) {

            CrossAppletSession cas = new CrossAppletSession(request);
            if (!cas.isSignedIn()) {
                // Redirect to social world index for ethics + login. (redirect back afterwards)
                response.sendRedirect("/benblamey.evaluation/");
                return false;
            }

            if (!EvaluationSessionManager.IsBenLoggedOn(cas)) {
                throw new ServletException("Only admins can access this area.");
            }
        }

        return true;
    }

    public static String BEN_BLAMEY_FACEBOOK_ID = "728995201";
    public static String PARTICIPANT_1_FACEBOOK_ID = "????";

}
