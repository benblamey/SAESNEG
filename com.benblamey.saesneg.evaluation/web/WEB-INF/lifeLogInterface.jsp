<!doctype html>
<%@ page 
import="benblamey.evaluation.web.debug.DevelopmentFlags"
import="benblamey.evaluation.web.GroundTruthLifeStoryViewModel"
import="bag.core.SystemInfo"
import="benblamey.evaluation.web.debug.EvaluationSessionManager"
import="benblamey.saesneg.model.*"
import="benblamey.saesneg.evaluation.*"
import="benblamey.saesneg.model.datums.*"
import="benblamey.saesneg.serialization.*"
import="org.joda.time.*"
 %>
<%
EvaluationSessionManager esm = (EvaluationSessionManager)request.getAttribute("ESM");
LifeStory ls = (LifeStory)request.getAttribute("LIFESTORY");
GroundTruthLifeStoryViewModel lsvm =(GroundTruthLifeStoryViewModel)request.getAttribute("LIFESTORYVIEWMODEL");
LifeStoryInfo lifeStoryInfo = (LifeStoryInfo)request.getAttribute("LIFESTORYINFO");
%>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<title>SAESNEG</title>
	
	 <link rel="shortcut icon" href="book.ico">
	 
	<script src="http://code.jquery.com/jquery-2.0.3.js"></script>
	
	<!-- Built with 'redmond' layout - theme selectively (else causes layout issues when draggging -->
	<link href="css/redmond/jquery-ui-1.10.3.custom.css" rel="stylesheet">
	
	<!-- Include our version of JQuery UI (with customized drag and drop functionality).-->
	<script src="jquery-ui-1.10.3-ben.js"></script>
	
	<link rel="stylesheet" type="text/css" href="lifeloginterface.css"/>
	<link rel="stylesheet" type="text/css" href="styles.css">
	<script src="data_view.js"></script>
</head>

<body>

	<div id="content">

		<div id="events">
		
			<%
		    			for (Event e : lsvm.getEvents()) { // These events are sorted.
		    		%>
		    <div class="event" >
		        <ul>
			    <%
			    	for (Datum mfo : e.getDatums()) {
			    %>
			    
			    <!-- ******************** Paste Snippet from above in here ************************ -->
			<li class="socialnetworkdatum <%=mfo.getWebViewClass()%>" 
				data-datumid="<%=mfo.getNetworkID()%>"
				<%String fullImageURL = mfo.getFullImageURL();
				   if (fullImageURL != null) {
				   // Custom attributes are allowed in HTML5.%>
				full-size-image="<%=fullImageURL%>"
				<%}%>
				>
			
				<!-- Enable the redmond theme (to get the zoom icon),
					 Hide when shown in the dialog. -->
				<span class="redmond zoomin-container">
					<img class="ui-icon-zoomin" src="zoom.png"/>
				</span>
			
				<!-- We hide the title in the dialog, because the dialog has its own title. -->
			   	<h5 class="hideinzoomdialog"><%=org.apache.commons.lang.StringEscapeUtils.escapeHtml(
						mfo.getWebViewTitle() )%></h5>
			   	
			   	<%
			   			   		if (null != mfo.getImageThumbnailURL()) {
			   			   	%>
			   	<div>
				<img class="datum_photo" src="<%=DevelopmentFlags.ShowDummyImages() ? "dummy.jpg" : mfo.getImageThumbnailURL()%>" alt="<%=mfo.getWebViewTitle()%>" />
				</div>
				<%
					}
				%>
	
				
				<%
								for (DatumWebProperty prop : mfo.getWebViewMetadata()) {
							%>
					<p class="datumwebproperty <%= prop.Key %>">
						<span class="datumwebpropertykey"><%= prop.FriendlyName %>:</span>&nbsp;<%= org.apache.commons.lang.StringEscapeUtils.escapeHtml(prop.Value) %>
					</p>
				<% } %>
			</li>
					
				<!-- ******************** Paste Snippet from above in here ************************ -->
			
				<% } %>
				</ul>

				
		    </div>
			<% } %>
		
		</div>
	
	</div>

</div>

<div id="footerdiv">
    <p id="footnotes">
        <span id="sync_status"></span>
        <br/>
        Please send details of problems, errors, feedback &amp; suggestions to <a href="ben@benblamey.com">ben@benblamey.com</a>
    </p>
</div>

  
<div id="dialog_container" class="redmond" >
</div>

</body>
</html>
