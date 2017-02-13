//package benblamey.evaluation.web.servlets.debug;
//
//import java.io.IOException;
//import java.sql.SQLException;
//
//import javax.servlet.RequestDispatcher;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import socialworld.model.CrossAppletSession;
//import socialworld.model.SocialWorldUser;
//import benblamey.evaluation.web.debug.EvaluationSessionManager;
//import benblamey.evaluation.web.debug.ProcessedLifeStoryCache;
//import benblamey.saesneg.model.LifeStory;
//import benblamey.saesneg.model.UserContext;
//import benblamey.saesneg.model.datums.Datum;
//import benblamey.saesneg.phaseB.DatumSimilarityCalculator;
//import benblamey.saesneg.phaseB.clustering.SVMEdgeClassifier;
//import benblamey.saesneg.phaseB.features.DatumPairSimilarity;
//import benblamey.saesneg.serialization.LifeStoryInfo;
//
///**
// * Servlet implementation class CompareDatumPair
// */
//public class CompareDatumPair extends HttpServlet {
//	private static final long serialVersionUID = 1L;
//       
//
//
//	/**
//	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
//	 */
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//		if (!EvaluationSessionManager.ensureAdminLoggedOn(request, response)) {
//			return;
//		}
//				
//		String userStoryToShow = (String)request.getParameter("userid");
//		if (userStoryToShow == null) {
//			throw new ServletException("userid is missing.");
//		}
//		
//		Long leftGID = Long.parseLong((String)request.getParameter("left"));
//		if (leftGID == null) {
//			throw new ServletException("'left' is missing.");
//		}
//		
//		Long rightGID = Long.parseLong((String)request.getParameter("right"));
//		if (rightGID == null) {
//			throw new ServletException("'right' is missing.");
//		}
//		
//		if (rightGID.equals(leftGID)) {
//			throw new ServletException("'right' and 'left' must be different.");
//		}
//
//		System.out.println("Loading story for user: " + userStoryToShow);
//		
//		SocialWorldUser user = new SocialWorldUser(
//				userStoryToShow,
//				false); // don't allow add.
//		
//        
//		LifeStoryInfo latestInfo = LifeStoryInfo.getLatestLifeStory(user);
//		if (latestInfo == null) {
//        	System.err.append("There is no valid life story according to records in mongo, user: " + userStoryToShow);
//        	response.sendRedirect("./");
//        	return;
//		} else if (!latestInfo.success) {
//        	System.err.append("Latest life story has an error, user: " + userStoryToShow);
//        	response.sendRedirect("./");
//        	return;
//		}
//        
//		UserContext userContext;
//		try {
//			userContext = ProcessedLifeStoryCache.getLifeStory(userStoryToShow);
//		} catch (Exception e) {
//			throw new ServletException(e);
//		}
//        LifeStory ls = userContext.getLifeStory();
//		
//        SVMEdgeClassifier svm = new SVMEdgeClassifier(ProcessedLifeStoryCache.SVM_MODEL_PATH);
//        request.setAttribute("SVM", svm);
//        
//        request.setAttribute("LIFESTORY", ls);
//        request.setAttribute("LIFESTORYINFO", latestInfo);
//		
//        Datum left = null;
//        Datum right = null;
//        
//        for (Datum mfo : ls.datums) {
//        	Long ID = mfo.getNetworkID();
//        	if (left == null && leftGID.equals(ID) ) {
//        		left = mfo;
//            	if (left != null && right != null) {
//            		break;
//            	}
//        	}
//        	if (right == null && rightGID.equals(ID) ) {
//        		right = mfo;
//            	if (left != null && right != null) {
//            		break;
//            	}
//        	}
//        }
//        
//        DatumSimilarityCalculator calc = new DatumSimilarityCalculator(ls, null);
//        DatumPairSimilarity pair = calc.runFESTIBUS(left, right);
//        
//        //DatumPairSimilarity pair = new DatumPairSimilarity(left, right);
//        
//        request.setAttribute("LEFT", left);
//        request.setAttribute("RIGHT", right);
//        
//        request.setAttribute("PAIR", pair);
//        
//		
//		// For now, just use whats in the cache.
//        
//		RequestDispatcher dispatcher = request
//				.getRequestDispatcher("WEB-INF/CompareDatumPair.jsp");
//		dispatcher.forward(request, response);
//		
//		
//		
//	}
//
//
//}
