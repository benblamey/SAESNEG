<%@page import="java.util.Collection"%>
<%@page import="benblamey.gnuplot.GnuPlotResult"%>
<!doctype html>
<%@page 
    import="bag.core.SystemInfo"
    import="benblamey.evaluation.web.debug.EvaluationSessionManager"
    import="benblamey.saesneg.model.*" 
    import="benblamey.saesneg.model.datums.*"
    import="benblamey.saesneg.model.annotations.*"
    import="benblamey.saesneg.evaluation.DatumWebProperty" %>
<%
    EvaluationSessionManager esm = (EvaluationSessionManager) request.getAttribute("ESM");
    LifeStory ls = (LifeStory) request.getAttribute("LIFESTORY");
    Collection<Event> events = (Collection<Event>) request.getAttribute("EVENTS");
%>

<html>
    <head>
        <title><%= SystemInfo.IsWindowsSystem() ? "(localhost) - " : ""%> Diary of <%= request.getAttribute("OWNER_NAME")%></title>

        <meta http-equiv="Content-type" content="text/html;charset=UTF-8">
        <script src="http://code.jquery.com/jquery-2.0.3.js"></script>

        <!-- Built with 'redmond' layout - theme selectively (else causes layout issues when draggging -->
        <link href="css/redmond/jquery-ui-1.10.3.custom.css" rel="stylesheet">

        <!-- Include our version of JQuery UI (with customized drag and drop functionality).-->
        <script src="jquery-ui-1.10.3-ben.js"></script>

        <style type="text/css">
            body {
                background-color:#D3E6FF;
                font-family:"Arial";
                font-size:75%
            }
            h1 {
                font-size:160%
            }
            h2 {
                font-size:120%
            }
            div.event {
                border-style:solid;
                border-width:medium;
                margin:5px;
                padding:2px;
            }
            div.object {
                border-style:solid;
                border-width:thin;
                margin:2px;
                padding: 2px;
            }
            img.event {
                height: 90px;
                float:right;
            }
            p {
                margin: 2px;
            }
            pre {
                font-size: 10px;
                background: white;

            }
            img.timeconstraints {
                height: 100px;
                width: 1000px;
            }


        </style>
    </head>

    <body>

        <h1><%= request.getAttribute("TITLE")%></h1>

        <% for (Event event : events) {%>

        <div class="event">
            <% for (Datum entry : event.getDatums()) {%>
            <div class="object">

                <img class="event" src="<%= entry.getLocalImageURL()%>" />
                <h2>Name: <%= entry.getWebViewTitle()%></h2>
                <p>Date: <%= entry.getContentAddedDateTime()%> &nbsp;&nbsp;&nbsp;</p>
                <p>ID: <%= entry.getNetworkID()%> </p>

                <% for (DatumWebProperty prop : entry.getWebViewMetadata()) {%>
                <p class="datumwebproperty <%=prop.Key%>">
                    <span class="datumwebpropertykey"><%=prop.FriendlyName%>:</span>&nbsp;<%=org.apache.commons.lang.StringEscapeUtils.escapeHtml(prop.Value)%>
                </p>
                <% } %>


                <table border='1'>
                    <thead>
                        <tr>
                            <td>Class Name</td>
                            <td>Data Kind</td>
                            <td>Text</td>
                            <td>(toString())</td>
                        </tr>    
                    </thead>
                    <tbody>
                        <% for (Annotation anno : entry.getAnnotations().getAllAnnotations()) {%>
                        <tr>
                            <td><%= anno.getClass().getName()%></td>
                            <td><%= anno.SourceDataKind%></td>
                            <td><%= anno.getOriginalText()%></td>
                            <td><%= anno.toString()%></td>
                        </tr>
                        <% }%>
                    </tbody>
                </table>

                <pre><%= entry.getText()%></pre>


                <% for (GnuPlotResult result : entry.getTimeConstraintPlotPaths()) {%>
                <img class="timeconstraints" 
                     data-gnuplot-command="<%= result.command %>"  
                     data-total-mass="<%= result.totalMass %>"  
                     src="gnuplots/<%= result.fileName%>"/><br/>
                <% } %>


            </div>
            <% } %>
        </div>
        <% }%>

    </body>
</html>