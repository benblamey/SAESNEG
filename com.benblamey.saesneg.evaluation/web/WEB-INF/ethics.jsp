<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="socialworld.model.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>PhD Research Project</title>
	<link rel="stylesheet" type="text/css" href="styles-socialworld.css">
</head>

<body>
<div id="header">
	<img src="swt33smaller.jpg">
</div>

<h1>PhD Research Project, SAESNEG: "Creating a Life-Story from a User's Social Networking Footprint"</h1>

<%
CrossAppletSession cas = new CrossAppletSession(request);
if (!cas.isSignedIn()) {
%>
<p id="existing_user_login">
<a href="signInWithFacebook">Existing User Login (via Facebook)</a>
</p>
<%
}
%>



<h1>Creation of Life-Story - Privacy Policy</h1>
<p>	
Ben's PhD research project is investigating ways to improve the user-experience of browsing our social media footprint, by organizing the data so that it reflects the structure of everyday life. 
The research will hopefully lead to novel user interfaces for online social networking data. In order to build something that actually works effectively it would be really useful to have some real data to use to help with development, testing, and evaluation 
- I would be extremely grateful if your data could be used for this purpose. The work should contribute to knowledge about how to design user interfaces, an new data mining techniques to represent one's social networking footprint.
</p>

<h2>Has Cardiff Metropolitan approved this study?</h2>
Yes, this study has been granted approval by the research ethics degree board.

<h2>Why me?</h2>
<p>We would be grateful for the involvement of anyone who uses social networking services.</p>

<h2>Do I have to participate?</h2>
<p>Of course not! There is no compulsion to participate.</p>

<h2>What will happen if I choose to participate?</h2>
<p>
You will be asked to authorize our Facebook App. Some of the data from the social networks that you choose to link to the study may be used to develop, test, and/or evaluate new algorithms for the organization of social networking data. 
From time to time, the output of the software may be delivered to you, and you may be asked to fill in a simple on-line questionnaire about the results, or if it is practical, one or more short interviews/discussions about the results - your participation in either will be entirely optional.</p>

<h2>Exactly what data will you be using?</h2>
<p>We won't use data from social networks which you have not 'linked' to the study. The only data which we will use in the research is that which you have granted permission for the study 'apps' on the respective social networks. In the case of Facebook, 
    this is things you might share with your friends like <b>photos</b> and <b>status messages</b>, but <u><b>not</b> things like private messages</u>.</p><p>

</p><h2>What happens if I change my mind?</h2>
<p>You are free to decide to end your participation in the study at any time, and we will stop using your data immediately. You can opt-out at any time by emailing either Ben <a href="mailto:ben@benblamey.com">ben@benblamey.com</a> or Dr Oatley 
<a href="mailto:goatley@cardiffmet.ac.uk">goatley@cardiffmet.ac.uk</a>. You do not need to give a reason.</p>

<h2>Who will have access to my data?</h2>
<p>
Ben Blamey and Dr Giles Oatley are the researchers on these projects. Your data will not be combined with anyone else's data - everything will be kept separate. You will remain anonymous and nobody will know you have taken part. 
If something in your data is particularly interesting - we may contact you separately for permission to include an (anonymized) example in the final PhD thesis or research paper.
</p>

<h2>We have another question?</h2>
If you have a question about this study, please send me an <a href="mailto:ben@benblamey.com">email</a>.

<h2>Contact Details</h2>
<p>Ben Blamey
<br>
PhD Candidate
<br>
<a href="http://www.benblamey.com">benblamey.com</a>
<br>
Cardiff Metropolitan University, Llandaff Campus, Western Avenue, Cardiff, CF5 2YB, United Kingdom<br>
<a href="mailto:ben@benblamey.com">ben@benblamey.com</a><br>
</p>

<p>Dr. Giles Oatley
<br>
Bio <a href="http://www3.cardiffmet.ac.uk/English/Cardiff-School-of-Management/Our-Staff/Pages/Giles-Oatley.aspx">here.</a>
<br>
Cardiff Metropolitan University, Llandaff Campus, Western Avenue, Cardiff, CF5 2YB, United Kingdom<br>
<a href="mailto:goatley@cardiffmet.ac.uk">goatley@cardiffmet.ac.uk</a><br>
Telephone: 02920 416419
</p>

<%
	boolean showForm;
String facebookID = (String)request.getSession().getAttribute(SessionConstants.FACEBOOK_USER_ID);
if (facebookID != null) {
	// Logged in, see if we already have consent:
	SocialWorldUser swu = new SocialWorldUser(facebookID);
	showForm = (swu.getValue(SocialWorldUser.HAS_DONE_ETHICS) == null);
} else {
	// Not logged in. Show form.
	showForm = true;
}
%>

<% if (showForm) { %>
	<div id="consent_form_div">
	<h2>Consent Form</h2>
	
	<form action="Ethics" method="POST">
	
	<% if (request.getParameter("check") != null) { %>
		
		<p id="must_check">
		YOU MUST TICK ALL THE BOXES TO PARTICIPATE IN THE STUDY!
		</p>
		
	<% } %>
	
	<input type="checkbox" name="read" value="YES_READ"> I confirm that I have read and understand the above information about the study. I have had the opportunity to consider the information, ask questions and have had these answered satisfactorily.<br><br>
	
	<input type="checkbox" name="volunt" value="YES_VOLUNT">I understand that my participation is voluntary and that I am free to withdraw 
	at any time, without giving any reason.<br><br>
	
	<input type="checkbox" name="consent" value="YES_CONSENT">I agree to take part in the above study.<br><br>
	
	<p>
	After you click submit, you will be directed to Facebook to authorize the app. <b>Your authorization of the app indicates your consent to participate in the study</b>.
	</p>
	
	<input type="submit" name="SUBMIT" value="Submit">
	
	</form> 
	</div>

<% } %>

</body>
</html>
