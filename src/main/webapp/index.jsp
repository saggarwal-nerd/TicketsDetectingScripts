<%@page import="java.util.*"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="org.junit.*"%>
<%@page import="java.io.*"%>
<%@page import="org.apache.log4j.*"%>
<%@page import="java.net.URI"%>
<%@page import="javax.servlet.http.*"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.lang.annotation.Annotation"%>
<%@page import="eu.infomas.annotation.AnnotationDetector"%>
<%@page import="eu.infomas.annotation.AnnotationDetector.MethodReporter"%>

<%@page import=" com.atlassian.jira.rest.client.*"%>
<%@page import=" com.atlassian.jira.rest.client.domain.*"%>
<%@page
	import=" com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>VOOD Message Detector</title>
<link rel="stylesheet" type="text/css" href="Vood.css">
</head>

<body>
	<h2>VOOD Message Detector in @Ignore Annotation</h2>

	<%!/** 
	 * store Ticket and ClassNames
	 */
	static Map<String, ArrayList<String>> ticketList = new HashMap<String, ArrayList<String>>();
	/** 
	 * store ClassName and Messages
	 */
	static Map<String, ArrayList<String>> ignoreMessage = new HashMap<String, ArrayList<String>>();
	/** 
	 * store Number of Jira Tickets
	 */
	static int jiraTicketCount = 0;

	//static Logger log = Logger.getLogger("index");
	/** 
	 * This is the addTickets method
	 * @param key contains TicketNumber.
	 * @param value contains ClassName.
	 */

	private static void addTickets(String key, String value) throws Exception {
		ArrayList<String> tempTicketList = null;
		if (ticketList.containsKey(key)) {
			tempTicketList = ticketList.get(key);
			if (tempTicketList == null)
				tempTicketList = new ArrayList<String>();
			tempTicketList.add(value);
		} else {
			tempTicketList = new ArrayList<String>();
			tempTicketList.add(value);
		}
		ticketList.put(key, tempTicketList);
	}%>

	<%!/** 
	 * This is the addMessage method.
	 * @param key contains ClassName.
	 * @param value contains Message.
	 */	
	private static void addMessage(String key, String value) throws Exception {
		ArrayList<String> tempMessageList = null;
		if (ignoreMessage.containsKey(key)) {
			tempMessageList = ignoreMessage.get(key);
			if (tempMessageList == null)
				tempMessageList = new ArrayList<String>();
			tempMessageList.add(value);
		} else {
			tempMessageList = new ArrayList<String>();
			tempMessageList.add(value);
		}
		ignoreMessage.put(key, tempMessageList);
	}%>
	<%
		/**
		 * A {@code MethodReporter} for method annotations.
		 */
		//log.debug("Show DEBUG message");
		final MethodReporter reporter = new MethodReporter() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Annotation>[] annotations() {
				return new Class[] { Ignore.class };
			}

			/** 
			 * This is the Override reportMethodAnnotation method.
			 * @param annotation contains AnnotationValue.
			 * @param className contains ClassName.
			 * @param methodName contains MethodName.
			 */
			@Override
			public void reportMethodAnnotation(
					Class<? extends Annotation> annotation,
					String className, String methodName) {
				try {
					Class<?> classType = Class.forName(className);
					Method method = classType.getMethod(methodName);

					// 	System.out.println("Class Name: "
					//   						+ classType.getSimpleName() + "\nValue of annotation:   "
					//    						+ method.getAnnotation(Ignore.class).value()+"\n");

					/*
					 * regular expression used to match Ticket.
					 */
					String pattern1 = "[A-z]+[-][0-9]+";
					/*
					 * regular expression used to match SI Ticket.
					 */
					String pattern2 = "[SI]+[-][0-9]+";
					Pattern jiraTicket = Pattern.compile(pattern1);
					Pattern otherTicket = Pattern.compile(pattern2);
					Matcher matcherJiraTicket = jiraTicket.matcher(method
							.getAnnotation(Ignore.class).value());
					Matcher otherJiraTicket = otherTicket.matcher(method
							.getAnnotation(Ignore.class).value());
					if (matcherJiraTicket.find() == true
							&& otherJiraTicket.find() == false) {
						addTickets(matcherJiraTicket.group(),
								classType.getSimpleName());
						while (matcherJiraTicket.find() == true) {
							addTickets(matcherJiraTicket.group(),
									classType.getSimpleName());
						}
					} else {
						addMessage(classType.getSimpleName(), method
								.getAnnotation(Ignore.class).value());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		final AnnotationDetector cf = new AnnotationDetector(reporter);
		/**
		 * to get properties 
		 */
		ResourceBundle resource = ResourceBundle.getBundle("jira");
		/**
		 * used to detect package 
		 */
		cf.detect(resource.getString("package"));
		/**
		 * initialization of Values.
		 */
		String ticketValue = "";
		String statusValue = "";
		String fixVersionValue = "";
		String resolutionValue = "";
		int fixedTicketCount = 0;
		int resolutionColor = 0;
	%>
	<h4>Tickets in @Ignore</h4>
	<table style="width: 80%">
		<thead>
			<tr>
				<th>Ticket No</th>
				<th>ClassNames</th>
				<th>Status</th>
				<th>Resolution</th>
				<th>FixVersion</th>
			</tr>
		</thead>
		<%
			Iterator<String> it = ticketList.keySet().iterator();
			ArrayList<String> classNamesList = null;
			while (it.hasNext()) {
				String key = it.next().toString();
				classNamesList = ticketList.get(key);
				if (classNamesList != null) {
					try {
						ticketValue = key;
						/**
						 * code to hit ticketValue to Jira.
						 */
						JerseyJiraRestClientFactory f = new JerseyJiraRestClientFactory();
						JiraRestClient jc = f
								.createWithBasicHttpAuthentication(new URI(
										resource.getString("jira.url")),
										resource.getString("jira.username"),
										resource.getString("jira.password"));

						Issue issue = jc.getIssueClient().getIssue(ticketValue,
								null);
						/**
						 * increment jira ticket which are hit.
						 */
						jiraTicketCount++;
						/** 
						 *to get status, resolution, fixVersion
						 */
						BasicStatus status = issue.getStatus();
						BasicResolution resolution = issue.getResolution();
						Collection<Version> fixVersion = (Collection<Version>) issue
								.getFixVersions();
						try {
							if (status == null) {
								statusValue = "null";
							} else {
								statusValue = status.getName();
							}
						} catch (Exception e) {
							statusValue = e.toString();
							System.out.println("  Exception:-> " + e);
						}

						try {
							if (resolution == null) {
								resolutionValue = "null";
								resolutionColor = 1;
							} else if (resolution.getName().equals("Fixed")) {
								resolutionValue = "Fixed";
								resolutionColor = 2;
								fixedTicketCount++;
							} else {
								resolutionValue = resolution.getName();
								resolutionColor = 3;
							}
						} catch (Exception e) {
							resolutionValue = "";
							System.out.println("  Exception:-> " + e);
						}

						try {
							if (fixVersion == null) {
								fixVersionValue = "null";
							} else {
								String[] fixversionparts = fixVersion
										.toString().split(",");
								String part1 = fixversionparts[1];
								fixVersionValue = part1.substring(6);
							}
						} catch (Exception e) {
							fixVersionValue = "";
							System.out.println("  Exception:-> " + e);
						}
					} catch (Exception e) {
						System.out.println("  Exception:-> " + e);
					}
		%>
		<!-- table body used to display  -->
		<!-- ticketValue, classNames, statusValue, resolutionValue, fixVersionValue   -->
		<tbody>
			<tr class=<%=(resolutionColor == 2 ? "green" : "#ffffff")%>>
				<td><%=ticketValue%></td>
				<td>
					<%
						for (String classNamevalue : classNamesList) {
					%> <%=classNamevalue%>,&nbsp; <%
 	}
 %>
				</td>
				<td><%=statusValue%></td>
				<td><%=resolutionValue%></td>
				<td><%=fixVersionValue%></td>
			</tr>
		</tbody>
		<%
			}
			}
		%>

	</table>
	<!-- display Tickets with Resolution Fixed  -->
	<h4 style="color: green;">
		Number of Tickets with Resolution Fixed:
		<%=fixedTicketCount%></h4>

	<!-- display Jira Ticket Count -->
	<h4>
		Number of Jira Tickets found :
		<%=jiraTicketCount%></h4>

	<!-- table used to display -->
	<!-- className, Message -->
	<h4>Messages in @Ignore</h4>
	<table style="width: 80%">
		<tr>
			<th>Class Name</th>
			<th>Messages</th>
		</tr>
		<%
			Iterator<String> message = ignoreMessage.keySet().iterator();
			ArrayList<String> tempmsg = null;
			String classNameWithMessage = "";
			String messageValue = "";
			while (message.hasNext()) {
				String key = message.next().toString();
				tempmsg = ignoreMessage.get(key);
				if (tempmsg != null) {
					classNameWithMessage = key;
		%>
		<tr>
			<td><%=classNameWithMessage = key%></td>
			<td>
				<%
					for (String value : tempmsg) {
				%> <%=value%>,&nbsp; <%
 	}
 %>
			</td>
			<%
				}
			%>
		</tr>
		<%
			}
		%>
	</table>
</body>
</html>