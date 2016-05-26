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

public class AnnotationDetect {

	/**
	 * to read properties from external file
	 */
	static ResourceBundle resource = ResourceBundle.getBundle("jira");
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
	 * store row count of ticket which are matched used in excelsheet
	 */
	static int matchticketRowcount = 1;
	/**
	 * store row count of ticket which are unmatched used in excelsheet
	 */
	static int unmatchedTicketRowcount = 1;

	static HSSFWorkbook voodDetectorWorkbook = new HSSFWorkbook();

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
				if (matcherJiraTicket.find() == true
						&& otherJiraTicket.find() == false) {
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

	public void matchedTickets() {

		HSSFSheet matchedTicketSheet = voodDetectorWorkbook
				.createSheet("Matched Tickets");
		HSSFSheetConditionalFormatting conditionalFormatting = matchedTicketSheet
				.getSheetConditionalFormatting();
		HSSFConditionalFormattingRule cfrole = conditionalFormatting
				.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL,
						"-1");

		HSSFPatternFormatting fill_pattern = cfrole.createPatternFormatting();
		fill_pattern
				.setFillBackgroundColor(IndexedColors.GREY_40_PERCENT.index);

		CellRangeAddress[] my_data_range = { (CellRangeAddress) CellRangeAddress
				.valueOf("A2:E2") };
		conditionalFormatting.addConditionalFormatting(my_data_range, cfrole);
		HSSFRow matchedTicketRowhead = matchedTicketSheet
				.createRow(matchticketRowcount);
		matchedTicketSheet.setDefaultRowHeightInPoints((float) 18);
		matchedTicketSheet.setColumnWidth(0, 5000);
		matchedTicketSheet.setColumnWidth(1, 12000);
		matchedTicketSheet.setColumnWidth(2, 3000);
		matchedTicketSheet.setColumnWidth(3, 3000);
		matchedTicketSheet.setColumnWidth(4, 8000);

		HSSFCell cell1 = matchedTicketRowhead.createCell(0);
		cell1.setCellValue("Ticket No");

		HSSFCell cell2 = matchedTicketRowhead.createCell(1);
		cell2.setCellValue("ClassNames");

		HSSFCell cell3 = matchedTicketRowhead.createCell(2);
		cell3.setCellValue("Status");

		HSSFCell cell4 = matchedTicketRowhead.createCell(3);
		cell4.setCellValue("Resolution");

		HSSFCell cell5 = matchedTicketRowhead.createCell(4);
		cell5.setCellValue("FixedVersion");
		matchticketRowcount++;

		Iterator<String> it = ticketList.keySet().iterator();
		ArrayList<String> classNamesList = null;

		while (it.hasNext()) {
			try {
				HSSFRow matchedTicketrow = matchedTicketSheet
						.createRow(matchticketRowcount);
				String key = it.next().toString();
				classNamesList = ticketList.get(key);
				if (classNamesList != null) {
					System.out.print("Ticket no-> " + key);
					matchedTicketrow.createCell(0).setCellValue(key);

					System.out.print("   ClassNames-> ");
					StringBuffer className = new StringBuffer();
					for (String value : classNamesList) {
						className.append(value + ", ");
					}
					String tempClassNames = className.substring(0,
							className.length() - 2);
					matchedTicketrow.createCell(1).setCellValue(tempClassNames);
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
					System.out.print(" Status-> " + status.getName());
					matchedTicketrow.createCell(2).setCellValue(
							status.getName());
					/**
					 * gives Resolution of Ticket
					 */
					if (resolution == null) {
						System.out.print("  Resolution-> null");
						matchedTicketrow.createCell(3).setCellValue("null");

					} else if (resolution.getName().equals("Fixed")) {
						System.out.print("  Resolution-> Fixed");
						matchedTicketrow.createCell(3).setCellValue("Fixed");
						fixedCount++;
					} else {
						System.out.print("  Resolution-> "
								+ resolution.getName());
						matchedTicketrow.createCell(3).setCellValue(
								resolution.getName());
					}
					/**
					 * gives Fix Version of Ticket
					 */
					try {
						if (fixVersion == null) {
							System.out.print("  FixVersion-> null");
							matchedTicketrow.createCell(4).setCellValue(
									status.getName());
						} else {
							String[] fixversionparts = fixVersion.toString()
									.split(",");
							String part1 = fixversionparts[1];

							System.out.print("  FixVersion-> "
									+ part1.substring(6));
							matchedTicketrow.createCell(4).setCellValue(
									part1.substring(6));
						}
					} catch (Exception e) {
						matchedTicketrow.createCell(4).setCellValue("none");
					}
					System.out.print("\n\n");
				}
			} catch (Exception e) {
				System.out.println("  Exception:-> " + e);
				System.out.println();
			}
			matchticketRowcount++;
		}// end of while
		String compareItem = "Fixed";
		HSSFConditionalFormattingRule cfrole1 = conditionalFormatting
				.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\""
						+ compareItem + "\"");

		HSSFPatternFormatting fill_pattern1 = cfrole1.createPatternFormatting();
		fill_pattern1.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.index);

		CellRangeAddress[] my_data_range1 = { (CellRangeAddress) CellRangeAddress
				.valueOf("D1:D100") };
		conditionalFormatting.addConditionalFormatting(my_data_range1, cfrole1);
		/**
		 * for blank row in excel sheet
		 */
		matchticketRowcount++;
		/**
		 * display Tickets which are Fixed on Excel sheet
		 */

		System.out.println("Number of Tickets with Resolution Fixed: "
				+ fixedCount);
		HSSFRow ticketfixedrow = matchedTicketSheet
				.createRow(matchticketRowcount);
		HSSFConditionalFormattingRule cfrole3 = conditionalFormatting
				.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL,
						"-1");

		HSSFPatternFormatting fill_pattern3 = cfrole3.createPatternFormatting();
		fill_pattern3.setFillBackgroundColor(IndexedColors.YELLOW.index);

		CellRangeAddress[] my_data_range3 = { (CellRangeAddress) CellRangeAddress
				.valueOf("A" + (matchticketRowcount + 1) + ":B"
						+ (matchticketRowcount + 1)) };
		conditionalFormatting.addConditionalFormatting(my_data_range3, cfrole3);
		ticketfixedrow.createCell(0).setCellValue("Tickets Fixed");
		ticketfixedrow.createCell(1).setCellValue(fixedCount);
		matchticketRowcount++;
		/**
		 * print total number of jira tickets found
		 */
		CellRangeAddress[] my_data_range4 = { (CellRangeAddress) CellRangeAddress
				.valueOf("A" + (matchticketRowcount + 1) + ":B"
						+ (matchticketRowcount + 1)) };
		conditionalFormatting.addConditionalFormatting(my_data_range4, cfrole3);
		System.out.println("\nTotal Jira Found" + jiraTicketCount);
		HSSFRow jiraticketrow = matchedTicketSheet
				.createRow(matchticketRowcount);
		jiraticketrow.createCell(0).setCellValue("Total Tickets");
		jiraticketrow.createCell(1).setCellValue(jiraTicketCount);
		matchticketRowcount++;
	}

	public void unmatchedTickets() {
		/**
		 * Excel sheet, row data for tickets no matched
		 */

		HSSFSheet unmatchedTicketsheet = voodDetectorWorkbook
				.createSheet("Unmatched Tickets");
		unmatchedTicketsheet.setDefaultRowHeightInPoints((float) 18);
		HSSFRow unmatchedTicketRowhead = unmatchedTicketsheet
				.createRow(unmatchedTicketRowcount);
		unmatchedTicketsheet.setColumnWidth(0, 5000);
		unmatchedTicketRowhead.createCell(0).setCellValue("Class Name");
		unmatchedTicketsheet.setColumnWidth(1, 15000);
		unmatchedTicketRowhead.createCell(1).setCellValue("Messages");
		unmatchedTicketRowcount++;
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
				HSSFRow unmatchedTicketRow = unmatchedTicketsheet
						.createRow(unmatchedTicketRowcount);
				unmatchedTicketRow.createCell(0).setCellValue(key);
				StringBuffer messages = new StringBuffer();
				for (String value : tempmsg) {
					messages.append(value + ", ");
				}
				String tempMessages = messages.substring(0,
						messages.length() - 2);
				System.out.print("  Messages-> " + tempMessages + "\n");
				unmatchedTicketRow.createCell(1).setCellValue(tempMessages);
			}
			unmatchedTicketRowcount++;
		}
		System.out.println("Total Unmatched: " + ignoreMessage.size());
		unmatchedTicketRowcount++;
		HSSFRow totalUnmatchedTicketRow = unmatchedTicketsheet
				.createRow(unmatchedTicketRowcount);
		totalUnmatchedTicketRow.createCell(0).setCellValue("Total Unmatched");
		totalUnmatchedTicketRow.createCell(1)
				.setCellValue(ignoreMessage.size());
	}

	public static void main(String[] args) {

		try {

			AnnotationDetect annotationDetect = new AnnotationDetect();

			AnnotationDetector cf = new AnnotationDetector(reporter);
			/**
			 * used to detect package
			 */
			cf.detect();
			/**
			 * path and name of Excelfile(.xls) to write data
			 */
			annotationDetect.unmatchedTickets();
			String filename = "ExcelWorkbookPath";
			/**
			 * creating of Excel workbook, sheet, rows data
			 */

			FileOutputStream fileOut = new FileOutputStream(filename);
			voodDetectorWorkbook.write(fileOut);
			fileOut.close();
			System.out.println("\nYour excel file has been generated!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
