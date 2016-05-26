package detector;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

import java.net.URI;

import org.junit.*;
import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;

import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.MethodReporter;

public class AnnotationFinal {

	/**
	 * to read properties from external file
	 */
	static ResourceBundle resource = ResourceBundle.getBundle("jira");
	/**
	 * store Ticket, Status, Resolution, Fix Version
	 */
	static Map<String, ArrayList<String>> ticketParameters = new HashMap<>();
	/**
	 * store Ticket and Class Names
	 */
	static Map<String, ArrayList<String>> ticketList = new HashMap<>();
	/**
	 * store Class Name and Messages
	 */
	static Map<String, ArrayList<String>> ignoreMessage = new HashMap<>();
	/**
	 * store Number of Jira Tickets
	 */
	static int jiraTicketCount = 0;
	/**
	 * store Number of Jira Tickets which are fixed
	 */
	static int fixedCount = 0;
	/**
	 * store row count of ticket which are matched used in Excelsheet
	 */
	static int matchticketRowCount = 1;
	/**
	 * store row count of ticket which are unmatched used in Excelsheet
	 */
	static int unmatchedTicketRowcount = 1;

	/**
	 * method to generate Excel Workbook
	 */
	public static void createExcel() {

		System.out.println("\nExcel Starts...");
		try {
			/**
			 * create Excel Workbook
			 */
			HSSFWorkbook voodDetectorWorkbook = new HSSFWorkbook();
			/**
			 * path of Excel Workbook
			 */
			String filename = "src/main/java/detector/AnnotationDetector.xls";
			/**
			 * create Sheet "Matched Tickets" to store Matched Tickets
			 */
			HSSFSheet matchedTicketSheet = voodDetectorWorkbook
					.createSheet("Matched Tickets");
			/**
			 * matched Conditional Formatting
			 */
			HSSFSheetConditionalFormatting matchedConditionalFormatting = matchedTicketSheet
					.getSheetConditionalFormatting();
			HSSFConditionalFormattingRule matchedCFrole1 = matchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting match_fill_pattern1 = matchedCFrole1
					.createPatternFormatting();
			match_fill_pattern1
					.setFillBackgroundColor(IndexedColors.GREY_40_PERCENT.index);

			CellRangeAddress[] my_data_range1 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A2:E2") };
			matchedConditionalFormatting.addConditionalFormatting(
					my_data_range1, matchedCFrole1);
			/**
			 * Set Default Row Height of Excel Sheet
			 */
			matchedTicketSheet.setDefaultRowHeightInPoints((float) 18);
			/**
			 * create Matched Ticket Header row
			 */
			HSSFRow matchedTicketRowhead = matchedTicketSheet
					.createRow(matchticketRowCount);
			/**
			 * cell Values
			 */
			HSSFCell cell1 = matchedTicketRowhead.createCell(0);
			cell1.setCellValue(" Ticket No  ");

			HSSFCell cell2 = matchedTicketRowhead.createCell(1);
			cell2.setCellValue(" Class Names  ");

			HSSFCell cell3 = matchedTicketRowhead.createCell(2);
			cell3.setCellValue(" Status  ");

			HSSFCell cell4 = matchedTicketRowhead.createCell(3);
			cell4.setCellValue(" Resolution    ");

			HSSFCell cell5 = matchedTicketRowhead.createCell(4);
			cell5.setCellValue(" Fix Version  ");

			matchticketRowCount++;// increment rows
			/**
			 * generate Matched Ticket Values in Excel Sheet
			 */
			Iterator<String> matchedTicketIterator = ticketList.keySet()
					.iterator();
			ArrayList<String> classNamesList = null;
			ArrayList<String> ticketParametersList = null;
			System.out.println("Matched Tickets Excel sheet generating....");
			while (matchedTicketIterator.hasNext()) {
				try {
					HSSFRow matchedTicketrow = matchedTicketSheet
							.createRow(matchticketRowCount);
					String ticket = matchedTicketIterator.next().toString();
					classNamesList = ticketList.get(ticket);
					if (classNamesList != null) {
						matchedTicketrow.createCell(0).setCellValue(ticket);
						StringBuffer className = new StringBuffer();
						for (String value : classNamesList) {
							className.append(value + ", ");
						}
						String tempClassNames = className.substring(0,
								className.length() - 2);
						matchedTicketrow.createCell(1).setCellValue(
								tempClassNames);
					}
					ticketParametersList = ticketParameters.get(ticket);
					if (ticketParametersList != null) {
						int i = 2;
						for (String value : ticketParametersList) {
							matchedTicketrow.createCell(i).setCellValue(value);
							i++;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				matchticketRowCount++;
			}
			/**
			 * Conditional Formatting for Fixed Resolution
			 */
			String compareItem = "Fixed";
			HSSFConditionalFormattingRule cfrole1 = matchedConditionalFormatting
					.createConditionalFormattingRule(ComparisonOperator.EQUAL,
							"\"" + compareItem + "\"");

			HSSFPatternFormatting match_fill_pattern2 = cfrole1
					.createPatternFormatting();
			match_fill_pattern2
					.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.index);

			CellRangeAddress[] matched_data_range2 = { (CellRangeAddress) CellRangeAddress
					.valueOf("D1:D100") };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range2, cfrole1);
			for (int columnPosition = 0; columnPosition < 5; columnPosition++) {
				matchedTicketSheet.autoSizeColumn((short) (columnPosition));
			}
			/**
			 * for blank row in excel sheet
			 */
			matchticketRowCount++;
			/**
			 * Print Number of Tickets Fixed
			 */
			HSSFRow ticketfixedrow = matchedTicketSheet
					.createRow(matchticketRowCount);
			HSSFConditionalFormattingRule matchedCFrole3 = matchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting matched_fill_pattern3 = matchedCFrole3
					.createPatternFormatting();
			matched_fill_pattern3
					.setFillBackgroundColor(IndexedColors.YELLOW.index);

			CellRangeAddress[] matched_data_range3 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (matchticketRowCount + 1) + ":B"
							+ (matchticketRowCount + 1)) };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range3, matchedCFrole3);
			ticketfixedrow.createCell(0).setCellValue("Tickets Fixed");
			ticketfixedrow.createCell(1).setCellValue(fixedCount);
			matchticketRowCount++;
			/**
			 * print number of jira tickets found
			 */
			CellRangeAddress[] matched_data_range4 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (matchticketRowCount + 1) + ":B"
							+ (matchticketRowCount + 1)) };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range4, matchedCFrole3);
			HSSFRow jiraticketrow = matchedTicketSheet
					.createRow(matchticketRowCount);
			jiraticketrow.createCell(0).setCellValue("Total Tickets");
			jiraticketrow.createCell(1).setCellValue(jiraTicketCount);
			/**
			 * code for auto filter
			 */
			matchedTicketSheet.setAutoFilter(CellRangeAddress.valueOf("A2:E"
					+ (matchticketRowCount - 2)));
			matchticketRowCount++;

			/**
			 * Unmatched Tickets Excel sheet generating...
			 */
			System.out.println("Unmatched Tickets Excel sheet generating...");
			HSSFSheet unmatchedTicketSheet = voodDetectorWorkbook
					.createSheet("Unmatched Tickets");
			HSSFSheetConditionalFormatting unmatchedConditionalFormatting = unmatchedTicketSheet
					.getSheetConditionalFormatting();
			HSSFConditionalFormattingRule unmatchedCFrole1 = unmatchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting unmatched_fill_pattern1 = unmatchedCFrole1
					.createPatternFormatting();
			unmatched_fill_pattern1
					.setFillBackgroundColor(IndexedColors.GREY_40_PERCENT.index);

			CellRangeAddress[] unmatched_data_range1 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A2:B2") };
			unmatchedConditionalFormatting.addConditionalFormatting(
					unmatched_data_range1, unmatchedCFrole1);
			/**
			 * Auto Rows height for UnmatchedTickets Excel Sheet
			 */
			unmatchedTicketSheet.setDefaultRowHeightInPoints((float) 18);
			HSSFRow unmatchedTicketRowhead = unmatchedTicketSheet
					.createRow(unmatchedTicketRowcount);
			unmatchedTicketRowhead.createCell(0).setCellValue("Class Name");
			unmatchedTicketRowhead.createCell(1).setCellValue("Messages");
			unmatchedTicketRowcount++;

			Iterator<String> message = ignoreMessage.keySet().iterator();
			ArrayList<String> tempmsg = null;
			while (message.hasNext()) {
				String key = message.next().toString();
				tempmsg = ignoreMessage.get(key);
				if (tempmsg != null) {
					HSSFRow unmatchedTicketRow = unmatchedTicketSheet
							.createRow(unmatchedTicketRowcount);
					unmatchedTicketRow.createCell(0).setCellValue(key);
					StringBuffer messages = new StringBuffer();
					for (String value : tempmsg) {
						messages.append(value + ", ");
					}
					String tempMessages = messages.substring(0,
							messages.length() - 2);
					unmatchedTicketRow.createCell(1).setCellValue(tempMessages);
				}
				unmatchedTicketRowcount++;
			}
			for (int columnPosition = 0; columnPosition < 5; columnPosition++) {
				unmatchedTicketSheet.autoSizeColumn((short) (columnPosition));
			}
			unmatchedTicketRowcount++;
			HSSFRow totalUnmatchedTicketRow = unmatchedTicketSheet
					.createRow(unmatchedTicketRowcount);
			HSSFConditionalFormattingRule unmatchedCFrole2 = unmatchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting unmatched_fill_pattern2 = unmatchedCFrole2
					.createPatternFormatting();
			unmatched_fill_pattern2
					.setFillBackgroundColor(IndexedColors.YELLOW.index);

			CellRangeAddress[] unmatched_data_range2 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (unmatchedTicketRowcount + 1) + ":B"
							+ (unmatchedTicketRowcount + 1)) };
			unmatchedConditionalFormatting.addConditionalFormatting(
					unmatched_data_range2, unmatchedCFrole2);
			totalUnmatchedTicketRow.createCell(0).setCellValue(
					"Total Unmatched");
			totalUnmatchedTicketRow.createCell(1).setCellValue(
					ignoreMessage.size());
			/**
			 * code for auto filter
			 */
			unmatchedTicketSheet.setAutoFilter(CellRangeAddress.valueOf("A2:B"
					+ (matchticketRowCount - 2)));
			/**
			 * creating of Excel workbook, sheet, rows data
			 */
			FileOutputStream fileOut = new FileOutputStream(filename);
			voodDetectorWorkbook.write(fileOut);
			fileOut.close();
			System.out.println("\nYour excel file has been generated!");
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @param key
	 *            to store Ticket Number
	 * @param value
	 *            to store Ticket Parameters such as Status, Resolution, Fix
	 *            Version
	 * @throws Exception
	 */
	private static void addTicketStatus(String key, String value)
			throws Exception {
		ArrayList<String> tempTicketParameterList = null;
		if (ticketList.containsKey(key)) {
			tempTicketParameterList = ticketParameters.get(key);
			if (tempTicketParameterList == null)
				tempTicketParameterList = new ArrayList<String>();
			tempTicketParameterList.add(value);
		} else {
			tempTicketParameterList = new ArrayList<String>();
			tempTicketParameterList.add(value);
		}
		ticketParameters.put(key, tempTicketParameterList);
	}

	/**
	 * This is the addTickets method
	 * 
	 * @param key
	 *            contains TicketNumber.
	 * @param value
	 *            contains ClassName.
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
	}

	/**
	 * This is the addMessage method.
	 * 
	 * @param key
	 *            contains ClassName.
	 * @param value
	 *            contains Message.
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
	}

	/**
	 * A {@code MethodReporter} for method annotations.
	 */
	final static MethodReporter reporter = new MethodReporter() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Annotation>[] annotations() {
			return new Class[] { Ignore.class };
		}

		/**
		 * This is the Override reportMethodAnnotation method.
		 * 
		 * @param annotation
		 *            contains AnnotationValue.
		 * @param className
		 *            contains ClassName.
		 * @param methodName
		 *            contains MethodName.
		 */
		@Override
		public void reportMethodAnnotation(
				Class<? extends Annotation> annotation, String className,
				String methodName) {
			Class<?> classType;
			try {
				classType = Class.forName(className);
				Method method = classType.getMethod(methodName);
				/**
				 * regular expression used to match Ticket.
				 */
				String pattern1 = "[A-z]+[-][0-9]+";
				/**
				 * regular expression used to match SI Ticket.
				 */
				String pattern2 = "[SI]+[-][0-9]+";
				Pattern jiraTicket = Pattern.compile(pattern1);
				Pattern otherTicket = Pattern.compile(pattern2);
				Matcher matcherJiraTicket = jiraTicket.matcher(method
						.getAnnotation(Ignore.class).value());
				Matcher otherJiraTicket = otherTicket.matcher(method
						.getAnnotation(Ignore.class).value());
				if (otherJiraTicket.find() == false
						&& matcherJiraTicket.find() == true) {
					addTickets(matcherJiraTicket.group(),
							classType.getSimpleName());
					while (matcherJiraTicket.find()) {
						addTickets(matcherJiraTicket.group(),
								classType.getSimpleName());
					}
				} else {
					addMessage(classType.getSimpleName(),
							method.getAnnotation(Ignore.class).value());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * method to get matched Tickets Parameter Status, Resolution, Fix Version
	 */
	public static void matchedTickets() {

		Iterator<String> it = ticketList.keySet().iterator();
		ArrayList<String> classNamesList = null;

		while (it.hasNext()) {
			String statusValue = "";
			String resolutionValue = "";
			String fixVersionValue = "";
			try {
				String key = it.next().toString();
				classNamesList = ticketList.get(key);
				if (classNamesList != null) {
					System.out.print("Ticket no-> " + key);
					System.out.print("   ClassNames-> ");
					StringBuffer className = new StringBuffer();
					for (String value : classNamesList) {
						className.append(value + ", ");
					}
					String tempClassNames = className.substring(0,
							className.length() - 2);
					System.out.print(tempClassNames);
					/**
					 * code to hit ticketValue to Jira.
					 */
					JerseyJiraRestClientFactory f = new JerseyJiraRestClientFactory();
					JiraRestClient jc = f.createWithBasicHttpAuthentication(
							new URI(resource.getString("jira.url")),
							resource.getString("jira.username"),
							resource.getString("jira.password"));

					Issue issue = jc.getIssueClient().getIssue(key, null);
					/**
					 * increment jira ticket which are hit.
					 */
					jiraTicketCount++;
					/**
					 * to get status, resolution, fixVersion
					 */
					BasicStatus status = issue.getStatus();
					BasicResolution resolution = issue.getResolution();
					Collection<Version> fixVersion = (Collection<Version>) issue
							.getFixVersions();
					/**
					 * gives status of Ticket
					 */
					System.out.print("  Status-> " + status.getName());
					statusValue = status.getName();
					addTicketStatus(key, statusValue);
					/**
					 * gives Resolution of Ticket
					 */
					if (resolution == null) {
						System.out.print("  Resolution-> not resolved");
						resolutionValue = "not resolved";
					} else if (resolution.getName().equals("Fixed")) {
						System.out.print("  Resolution-> Fixed");
						resolutionValue = "Fixed";
						fixedCount++;
					} else {
						System.out.print("  Resolution-> "
								+ resolution.getName());
						resolutionValue = resolution.getName();
					}
					addTicketStatus(key, resolutionValue);
					/**
					 * gives Fix Version of Ticket
					 */
					try {
						if (fixVersion == null) {
							System.out.print("  FixVersion-> null");
							fixVersionValue = "null";
						} else {
							String[] fixversionparts = fixVersion.toString()
									.split(",");
							String part1 = fixversionparts[1];
							System.out.print("  FixVersion-> "
									+ part1.substring(6));
							fixVersionValue = part1.substring(6);
						}
						addTicketStatus(key, fixVersionValue);
					} catch (Exception e) {
						fixVersionValue = "none";
						addTicketStatus(key, fixVersionValue);
						System.out.print("  Fix Version:-> " + fixVersionValue);
					}
					System.out.print("\n\n");
				}
			} catch (Exception e) {
				System.out.println("  Exception:-> " + e);
				System.out.println();
			}
		}// end of while
		/**
		 * display Tickets which are Fixed on Excel sheet
		 */
		System.out.println("Number of Tickets with Resolution Fixed: "
				+ fixedCount);
		/**
		 * print total number of jira tickets found
		 */
		System.out.println("\nTotal Jira Found :" + jiraTicketCount);
	}

	/**
	 * method to print Unmatched Tickets Class Name with messages
	 */
	public static void unmatchedTickets() {
		/**
		 * print Messages in @Ignore
		 */
		Iterator<String> message = ignoreMessage.keySet().iterator();
		ArrayList<String> tempmsg = null;
		System.out.println("\nMessages in @Ignore");
		while (message.hasNext()) {
			String key = message.next().toString();
			tempmsg = ignoreMessage.get(key);
			if (tempmsg != null) {
				System.out.print("\nClassName-> " + key);
				StringBuffer messages = new StringBuffer();
				for (String value : tempmsg) {
					messages.append(value + ", ");
				}
				String tempMessages = messages.substring(0,
						messages.length() - 2);
				System.out.print("  Messages-> " + tempMessages + "\n");
			}
		}
		System.out.println("\nTotal Unmatched: " + ignoreMessage.size());
	}

	/**
	 * display Status, Resolution, Fix Version store in HashMap
	 */
	public static void matchedTicketsParameter() {

		Iterator<String> message = ticketParameters.keySet().iterator();
		ArrayList<String> tempStatus = null;
		System.out.println("\nStatus, Resolution, FixVersion");
		while (message.hasNext()) {
			String key = message.next().toString();
			tempStatus = ticketParameters.get(key);
			if (tempStatus != null) {
				System.out.print("\nTicket Number-> " + key);
				StringBuffer statusResolution = new StringBuffer();
				for (String value : tempStatus) {
					statusResolution.append(value + ", ");
				}
				String[] statusResolutionparts = statusResolution.toString()
						.split(",");
				String status = statusResolutionparts[0];
				String resolution = statusResolutionparts[1];
				String fixVersion = statusResolutionparts[2];
				System.out.println(" Status->" + status + "  Resolution-> "
						+ resolution + " Fix Version-> " + fixVersion);
			}
		}
	}

	/**
	 * main method to call methods
	 * 
	 * @param args
	 *            do not used
	 */
	public static void main(String[] args) {

		try {
			AnnotationDetector cf = new AnnotationDetector(reporter);
			/**
			 * call to detect package
			 */
			cf.detect();
			/**
			 * call of methods
			 */
			matchedTickets();
			unmatchedTickets();
			createExcel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
