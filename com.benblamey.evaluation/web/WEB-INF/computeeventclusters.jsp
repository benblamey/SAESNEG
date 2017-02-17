<!doctype html>
<%@page import="benblamey.evaluation.web.debug.DevelopmentFlags"
	import="bag.core.SystemInfo"
	import="benblamey.evaluation.web.debug.EvaluationSessionManager"
	import="benblamey.saesneg.model.*"
	import="benblamey.saesneg.evaluation.*"
	import="benblamey.saesneg.model.datums.*"
	import="benblamey.saesneg.serialization.*"
	import="benblamey.saesneg.phaseB.*"
	import="java.util.List"%>
<%
	LifeStory ls = (LifeStory) request.getAttribute("LIFESTORY");
	LifeStoryInfo lifeStoryInfo = (LifeStoryInfo) request.getAttribute("LIFESTORYINFO");
	List<DatumPairSimilarity> pairs = (List<DatumPairSimilarity>) request.getAttribute("PAIRS");
	String userid = (String) request.getAttribute("USERID");
	
%>
<html lang="en">
<head>
<meta charset="utf-8" />
<title>Computed Event Clusters</title>

<link rel="shortcut icon" href="book.ico">

<script src="http://code.jquery.com/jquery-2.0.3.js"></script>

<!-- Built with 'redmond' layout - theme selectively (else causes layout issues when draggging -->
<link href="css/redmond/jquery-ui-1.10.3.custom.css" rel="stylesheet">

<!-- Include our version of JQuery UI (with customized drag and drop functionality).-->
<script src="jquery-ui-1.10.3-ben.js"></script>

<script src="data_view.js"></script>

<link rel="stylesheet" type="text/css" href="styles.css">


</head>

<body>

	<div id="footerdiv">
		<p id="footnotes">
			Loaded
			<code><%=lifeStoryInfo.filename%></code>
			- which was fetched at
			<%=lifeStoryInfo.created%>.
		</p>
	</div>

	<div id="headerdiv">
		<p id="header"></p>
	</div>

	<div id="content">

		<div id="pairs">
			<%
				for (Event event : ls.EventsComputed) { // These events are sorted.
			%><div class="pair">
				<table class="datum_sim_matrix">
					<%
						for (int rowNum = 0; rowNum < event.getDatums().size() + 1; rowNum++) {
								Datum rowPhoto = (rowNum == 0) ? null : event.getDatums().get(rowNum - 1);
					%><tr>
						<%
							for (int colNum = 0; colNum < event.getDatums().size() + 1; colNum++) {
										Datum colPhoto = (colNum == 0) ? null : event.getDatums().get(colNum - 1);
						%>
						<td>
							<%
								Datum mfo = null;
											DatumPairSimilarity sim = null;

											if ((rowNum == 0) && (colNum == 0)) {

												// Top-left corner.

											} else if (rowNum == 0) {
												mfo = colPhoto;
											} else if (colNum == 0) {
												mfo = rowPhoto;
											} else if (colNum != rowNum) {
												//sim = calc.runFESTIBUS(rowPhoto, colPhoto, new PhaseBOptions());
											}
							%> <%
 	if (mfo != null) {
 %> <!-- ******************** Snippet to copy starts here ************************ -->

							<li class="socialnetworkdatum <%=mfo.getWebViewClass()%>"
							data-datumid="<%=mfo.getNetworkID()%>"
							<%String fullImageURL = mfo.getFullImageURL();
							if (fullImageURL != null) {
								// Custom attributes are allowed in HTML5.%>
							full-size-image="<%=fullImageURL%>" <%}%>>
								<!-- Enable the redmond theme (to get the zoom icon),
						 Hide when shown in the dialog. --> <span
								class="redmond zoomin-container"> <img
									class="ui-icon-zoomin" src="zoom.png" />
							</span> <!-- We hide the title in the dialog, because the dialog has its own title. -->
								<h5 class="hideinzoomdialog"><%=org.apache.commons.lang.StringEscapeUtils.escapeHtml(mfo.getWebViewTitle())%></h5> <%
 	if (null != mfo.getImageThumbnailURL()) {
 %>
								<div>
									<img class="datum_photo"
										src="<%=DevelopmentFlags.ShowDummyImages() ? "dummy.jpg" : mfo.getImageThumbnailURL()%>"
										alt="<%=mfo.getWebViewTitle()%>" />
								</div> <%
 	}
 %> <%
 	for (DatumWebProperty prop : mfo.getWebViewMetadata()) {
 %>
								<p class="datumwebproperty <%=prop.Key%>">
									<span class="datumwebpropertykey"><%=prop.FriendlyName%>:</span>&nbsp;<%=org.apache.commons.lang.StringEscapeUtils.escapeHtml(prop.Value)%>
								</p> <%
 	}
 %>
						</li> <!-- ******************** Snippet to copy ends here ************************ -->
							<%
								} else if (sim != null) {
							%> <a
							href="CompareDatumPair?userid=<%=userid%>&left=<%=colPhoto.getNetworkID()%>&right=<%=rowPhoto.getNetworkID()%>"
							target="_blank"> 
						</a> <%
 	} else {
 %> - <%
 	}
 %>						</td>
						<%
							}
						%>
					</tr>
					<%}%>
				</table>
			</div>
			<%
				}
			%>
		</div>
	</div>


	<div id="dialog_container" class="redmond"></div>

</body>
</html>
