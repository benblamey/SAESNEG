package benblamey.evaluation;

import benblamey.evaluation.web.servlets.FacebookCallback;
import com.benblamey.core.FacebookConfig;
import com.benblamey.core.StringUtil;
import com.benblamey.core.SystemInfo;

public class SocialNetworkConfig {


	
	private static final String[] FACEBOOK_SCOPES = new String[] { 
		
		// https://developers.facebook.com/docs/facebook-login/permissions/v2.5
		"email",
                "public_profile",

		//"user_about_me",
                "user_posts",
                "user_friends",
		"user_events",	
		"user_location",	
		"user_photos",	
		"user_status",	
		//"user_videos",	
	};
	
	public static String getURLAfterLogout() {
		
		switch (SystemInfo.detectServer()) {
			case LOCAL_MACHINE:
                        case BENBLAMEY_OVH:
                        case BENBLAMEY_OVH_STAGING:
				return ""; // Go back to ethics/login page.
			default:
				throw new IllegalStateException();
		}
	}
	
	public static FacebookConfig getFacebook() {
		
		// Facebook OAuth is so simple - we don't use the library.

		String appID; // (App ID)
		String appSecret;
		String callback;
		String scope = StringUtil.ToCommaList(FACEBOOK_SCOPES);
		
		switch (SystemInfo.detectServer()) {
			case BENBLAMEY_OVH_STAGING:
				// "SAESNEG-staging" (Dev)
				appID = "????";
				appSecret = "????";
				callback = "http://ben-ovh-staging.com:8080/benblamey.evaluation"+FacebookCallback.PATH;
				break;
                        case BENBLAMEY_OVH:
				// App: "SAESNEG"
				appID = "?????";
				appSecret = "????";
				callback = "http://benblamey.com:8080/benblamey.evaluation"+FacebookCallback.PATH;
				break;
			case LOCAL_MACHINE:
				// "SocialWorld-Dev" (Dev)
				appID = "?????";
				appSecret = "????";
				callback = "http://localhost.com:8080/SocialWorld"+FacebookCallback.PATH;
				break;
			default:
				throw new IllegalStateException();
		}
		
		
		FacebookConfig fc = new FacebookConfig();
		fc.AppSecret = appSecret;
		fc.AppID = appID;
		fc.RedirectURL = callback;
		fc.Scope = scope;
		
		return fc;
	}
	
}
