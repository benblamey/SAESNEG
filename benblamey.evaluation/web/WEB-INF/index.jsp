<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ page import="benblamey.saesneg.serialization.LifeStoryInfo"%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Life-Story - Home</title>
<link rel="stylesheet" type="text/css" href="styles.css">

</head>

<body>

	<h1>Creating a Life-Story from a User's Social Networking
		Footprint</h1>

	<iframe src="//player.vimeo.com/video/74226104" width="500"
		height="375" frameborder="0" webkitallowfullscreen mozallowfullscreen
		allowfullscreen>
	</iframe>

	<p>
		Please also refer to <a href="groundtruthhelp.html" target="_blank">Hints & tips</a>.
	</p>



	<%LifeStoryInfo latestInfo = (LifeStoryInfo) request
			.getAttribute("LATEST_LIFE_STORY_INFO");
	if (latestInfo == null) {%>
	
	<p>		
		Our system is busy processing your data, please watch the video while you are waiting.
		You will receive an email in a few minutes when we're ready.
	</p>
	
	<p>
		If you have not received an email, please check your spam folder, or refresh the page.
	</p>
		
	<%} else if (!latestInfo.success) {%>
	
	<p>
		There was a problem processing your data, Ben has been notified, and is working to fix the problem.
	</p>
	
	<%} else {%>
	
	<p>
		<b><i>Please</i> first watch the instruction video above (full screen recommended)</b>, then <b><a href="GroundTruth">create/edit your life story &gt;&gt;&gt;&gt;&gt;</a></b>
	</p>
	
	<%}%>
	

	<hr />

	<p class="footer">
		Ben Blamey<br> PhD Candidate<br> Cardiff Metropolitan
		University, Llandaff Campus, Western Avenue, Cardiff, CF5 2YB<br>
		<a href="mailto:ben@benblamey.com">ben@benblamey.com</a><br>
	</p>

</body>
</html>
