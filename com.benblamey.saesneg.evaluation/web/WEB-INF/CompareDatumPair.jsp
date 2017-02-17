<!doctype html>
<%@page import="benblamey.saesneg.experiments.PhaseBOptions"%>
<%@page 
 import="benblamey.evaluation.web.debug.DevelopmentFlags"
 import="bag.core.SystemInfo"
 import="benblamey.evaluation.web.debug.EvaluationSessionManager"
 import="benblamey.saesneg.model.*" 
 import="benblamey.saesneg.evaluation.*" 
 import="benblamey.saesneg.model.datums.*" 
 import="benblamey.saesneg.serialization.*"
 import="benblamey.saesneg.phaseB.*"
 import="java.util.List" %>

<%
UserContext uc = (UserContext)request.getAttribute("USERCONTEXT");
LifeStory ls = (LifeStory)request.getAttribute("LIFESTORY");
LifeStoryInfo lifeStoryInfo = (LifeStoryInfo)request.getAttribute("LIFESTORYINFO");
List<DatumPairSimilarity> pairs = (List<DatumPairSimilarity>)request.getAttribute("PAIRS");
String userid = (String)request.getAttribute("USERID");
SVMEdgeClassifier svm = (SVMEdgeClassifier)request.getAttribute("SVM");

Datum left = (Datum)request.getAttribute("LEFT");
Datum right = (Datum)request.getAttribute("RIGHT");
DatumPairSimilarity pair = (DatumPairSimilarity)request.getAttribute("PAIR");

ClusteringStrategies calc =  new ClusteringStrategies(ls, uc ,new PhaseBOptions() { 
	{
		UseDistributedTemporalSimilarity = false;
	}
	
} );
%>

<html lang="en">
<head>
	<meta charset="utf-8" />
	<title>Compare Datum Pair</title>
	
	 <link rel="shortcut icon" href="book.ico">
	 
	<script src="http://code.jquery.com/jquery-2.0.3.js"></script>
	
	<!-- Built with 'redmond' layout - theme selectively (else causes layout issues when draggging -->
	<link href="css/redmond/jquery-ui-1.10.3.custom.css" rel="stylesheet">
	
	<!-- Include our version of JQuery UI (with customized drag and drop functionality).-->
	<script src="jquery-ui-1.10.3-ben.js"></script>
	
	<script src="data_view.js"></script>
	
	<link rel="stylesheet" type="text/css" href="computeeventclusters.css"/>
	<link rel="stylesheet" type="text/css" href="styles.css">
	

</head>

<body>

<div id="footerdiv">
	<p id="footnotes">
	Loaded <code><%=lifeStoryInfo.filename%></code> - which was fetched at <%=lifeStoryInfo.created%>.
	</p>
</div>

<div id="headerdiv">
	<p id="header">
	</p>
</div>  

<div id="content">

	<div id="pairs">
	
			<div class="pair" >
	
	
	<p>Left: <%=left.getNetworkID()%></p>
	<p>Right: <%=right.getNetworkID()%></p>

	
	<%
			Datum mfo = left;
		%>
	
			     <!-- ******************** Snippet to copy starts here ************************ -->
		                
		                				<li class="socialnetworkdatum <%= mfo.getWebViewClass() %>" 
					data-datumid="<%= mfo.getNetworkID() %>"
					<% String fullImageURL = mfo.getFullImageURL();
					   if (fullImageURL != null) {
					   // Custom attributes are allowed in HTML5.
					%>
					full-size-image="<%= fullImageURL %>"
					<% } %>
					>
				
					<!-- Enable the redmond theme (to get the zoom icon),
						 Hide when shown in the dialog. -->
					<span class="redmond zoomin-container">
						<img class="ui-icon-zoomin" src="zoom.png"/>
					</span>
				
					<!-- We hide the title in the dialog, because the dialog has its own title. -->
				   	<h5 class="hideinzoomdialog"><%= 
						org.apache.commons.lang.StringEscapeUtils.escapeHtml(
							mfo.getWebViewTitle() )%></h5>
				   	
				   	<% if (null != mfo.getImageThumbnailURL()) { %>
				   	<div>
			<img class="datum_photo" src="<%= DevelopmentFlags.ShowDummyImages() ? "dummy.jpg" : mfo.getImageThumbnailURL() %>" alt="<%= mfo.getWebViewTitle() %>" />
					</div>
					<% } %>
		
					
					<%
													for (DatumWebProperty prop : mfo.getWebViewMetadata()) {
												%>
						<p class="datumwebproperty <%=prop.Key%>">
							<span class="datumwebpropertykey"><%=prop.FriendlyName%>:</span>&nbsp;<%=org.apache.commons.lang.StringEscapeUtils.escapeHtml(prop.Value)%>
						</p>
					<%
						}
					%>
				</li>
				
				<!-- ******************** Snippet to copy ends here ************************ -->
				
	<%
						mfo = right;
					%>
				
				<!-- ******************** Snippet to copy starts here ************************ -->
		                
		                				<li class="socialnetworkdatum <%=mfo.getWebViewClass()%>" 
					data-datumid="<%=mfo.getNetworkID()%>"
					<%fullImageURL = mfo.getFullImageURL();
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
				
				<!-- ******************** Snippet to copy ends here ************************ -->
			
							
			</div>
			
			
			<div>
				<table border="1">
				
				<tr>
					<td>FeatureID</td>
					<td>Message</td>
					<td>SVMFeatureValue</td>
					<td>HandCraftedScore</td>
				<tr>
				
				<% for (DatumSimilarityEvidence evidence : pair.getEvidence()) { %>
				<tr>
					<td><%=evidence.getFeatureID()%></td>
					<td><%=evidence.getMessage()%></td>
					<td><%=evidence.getSVMFeatureValue()%></td>
				<tr>
				<% } %>			
				
				</table>
			</div>
	
	</div>
</div>

  
<div id="dialog_container" class="redmond" >
</div>

</body>
</html>
