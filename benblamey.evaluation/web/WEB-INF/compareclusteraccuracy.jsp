<%@page import="benblamey.saesneg.experiments.PhaseBOptions"
	import="benblamey.saesneg.phaseB.*"
	import="benblamey.evaluation.web.CompareClusterDatumAccuracy"
	import="benblamey.evaluation.web.CompareClusterEventPairViewModel"
	import="benblamey.evaluation.web.CompareClusterAccuracyOutput"
	import="benblamey.evaluation.web.debug.DevelopmentFlags"
	import="bag.core.SystemInfo"
	import="benblamey.evaluation.web.debug.EvaluationSessionManager"
	import="benblamey.saesneg.model.*"
	import="benblamey.saesneg.evaluation.*"
	import="benblamey.saesneg.model.datums.*"
	import="benblamey.saesneg.serialization.*"
	import="java.util.ArrayList"
	import="java.util.List"
%><%
	LifeStory ls = (LifeStory) request.getAttribute("LIFESTORY");
	LifeStoryInfo lifeStoryInfo = (LifeStoryInfo) request.getAttribute("LIFESTORYINFO");
	List<DatumPairSimilarity> pairs = (List<DatumPairSimilarity>) request.getAttribute("PAIRS");
	String userid = (String) request.getAttribute("USERID");
	//ClusteringStrategies calc = new ClusteringStrategies(ls, new PhaseBOptions() { });
	CompareClusterAccuracyOutput eventpairs = (CompareClusterAccuracyOutput)request.getAttribute("EVENTPAIRS");
	SVMEdgeClassifier svm = (SVMEdgeClassifier)request.getAttribute("SVM");
%>
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8" />
<title>Compare Event Clusters</title>

<link rel="shortcut icon" href="book.ico">

<script src="http://code.jquery.com/jquery-2.0.3.js"></script>

<!-- Built with 'redmond' layout - theme selectively (else causes layout issues when draggging -->
<link href="css/redmond/jquery-ui-1.10.3.custom.css" rel="stylesheet">

<!-- Include our version of JQuery UI (with customized drag and drop functionality).-->
<script src="jquery-ui-1.10.3-ben.js"></script>
<script src="data_view.js"></script>

<link rel="stylesheet" type="text/css" href="styles.css">
<link rel="stylesheet" type="text/css" href="computeeventclusters.css" />

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
			for (CompareClusterEventPairViewModel eventPair : eventpairs.viewModels) {
				
				
			
				
			%><div class="pair">
				<table class="datum_sim_matrix">
					<%
						for (int rowNum = 0; rowNum < eventPair._datums.size() + 1; rowNum++) {
							
		  Datum rowPhoto = (rowNum == 0) ? null : eventPair._datums.get(rowNum - 1);
CompareClusterDatumAccuracy rowPhotoLabel = (rowNum == 0) ? null : eventPair._labels.get(rowNum - 1);
								
					%><tr>
						<%
							for (int colNum = 0; colNum < eventPair._datums.size() + 1; colNum++) {
										Datum colPhoto = (colNum == 0) ? null : eventPair._datums.get(colNum - 1);
										CompareClusterDatumAccuracy colPhotoLabel = (colNum == 0) ? null : eventPair._labels.get(colNum - 1);
						%>
						<td>
							<%
								Datum mfo = null;
							CompareClusterDatumAccuracy label = null;
							
											DatumPairSimilarity sim = null;

											if ((rowNum == 0) && (colNum == 0)) {

												// Top-left corner.

											} else if (rowNum == 0) {
												mfo = colPhoto;
												label = colPhotoLabel;
											} else if (colNum == 0) {
												mfo = rowPhoto;
												label = rowPhotoLabel;
											} else if (colNum != rowNum) {
												//sim = calc.runFESTIBUS(rowPhoto, colPhoto, new PhaseBOptions());
											}
							%> <%
 	if (mfo != null) {
 %> <!-- ******************** Snippet to copy starts here ************************ -->

							<li class="socialnetworkdatum <%=mfo.getWebViewClass()%> <%=label%>"
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
							class="TODO: run calculation"
							href="CompareDatumPair?userid=<%=userid%>&left=<%=colPhoto.getNetworkID()%>&right=<%=rowPhoto.getNetworkID()%>"
							target="_blank"> </a> <%
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
