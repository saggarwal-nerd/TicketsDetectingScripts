package detector;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;

public class VOODTicketMatcher {

	/**
	 * to read properties from external file
	 */
	static ResourceBundle resource = ResourceBundle.getBundle("jira");
	/**
	 * store ClassNames and List of VOOD Tickets
	 */
	static HashMap<String, HashSet<String>> voodTickets = new HashMap<>();

	/**
	 * store Ticket and count
	 */
	static HashMap<String, Integer> ticketCount = new HashMap<>();
	/**
	 * variable to increment rows
	 */
	static int matchTicketRowCount = 1;

	/**
	 * 
	 * @param key
	 *            contains VOODTicket
	 * @param value
	 *            contains Class Name
	 * @throws Exception
	 */

	private static void addTicket(String key, String value) throws Exception {

		HashSet<String> tempVoodTicketList = null;
		if (voodTickets.containsKey(key)) {
			int tempcount = (Integer) ticketCount.get(key);
			tempcount++;
			ticketCount.put(key, tempcount);
			tempVoodTicketList = voodTickets.get(key);
			if (tempVoodTicketList == null)
				tempVoodTicketList = new HashSet<String>();
			tempVoodTicketList.add(value);
		} else {
			ticketCount.put(key, 1);
			tempVoodTicketList = new HashSet<String>();
			tempVoodTicketList.add(value);
		}
		voodTickets.put(key, tempVoodTicketList);
	}

	/**
	 * 
	 * @param file
	 *            contains File from which we have to find VOOD Tickets
	 */
	static void voodTicketFind(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String strLine = "";
			while ((strLine = br.readLine()) != null) {
				String TODO = "TODO[ : ]+";
				Pattern TODOPattern = Pattern.compile(TODO);
				Matcher TODOMatcher = TODOPattern.matcher(strLine);
				if (TODOMatcher.find()) {
					String voodTicket = "VOOD[-][0-9]+";
					Pattern voodTicketPattern = Pattern.compile(voodTicket);
					Matcher voodTicketMatcher = voodTicketPattern
							.matcher(strLine);
					while (voodTicketMatcher.find()) {
						// if
						// (voodTickets.containsKey(voodTicketMatcher.group()))
						// {
						addTicket(voodTicketMatcher.group(), file.getName()
								.split(".java")[0]);
						// } else {
						// JerseyJiraRestClientFactory f = new
						// JerseyJiraRestClientFactory();
						// JiraRestClient jc = f
						// .createWithBasicHttpAuthentication(
						// new URI(resource
						// .getString("jira.url")),
						// resource.getString("jira.username"),
						// resource.getString("jira.password"));
						// try {
						// Issue issue = jc.getIssueClient().getIssue(
						// voodTicketMatcher.group(), null);
						// BasicStatus status = issue.getStatus();
						// if (status.getName().equals("Open"))
						// addTicket(voodTicketMatcher.group(), file
						// .getName().split(".java")[0]);
						// } catch (Exception e) {
						// System.out.println("exception");
						// }
						// }
					}
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Exception while reading file: "
					+ file.getName());
		}
	}

	/**
	 * To print VOOD Ticket, Count and Class Names
	 */
	static void printVoodTickets() {
		Iterator<String> voodTicket = voodTickets.keySet().iterator();
		HashSet<String> tempVoodTickets = null;
		System.out.println("\nVOOD Tickets");
		while (voodTicket.hasNext()) {
			String key = voodTicket.next().toString();
			tempVoodTickets = voodTickets.get(key);
			if (tempVoodTickets != null) {
				System.out.print("\nTicket-> " + key);
				StringBuffer tickets = new StringBuffer();
				for (String value : tempVoodTickets) {
					tickets.append(value + ", ");
				}
				int tempcount = (Integer) ticketCount.get(key);
				System.out.print("  Ticket Count-> " + tempcount);
				String tempTickets = tickets.substring(0, tickets.length() - 2);
				System.out.print("  ClassNames-> " + tempTickets + "\n");
			}
		}
	}

	/**
	 * create Excel Sheet
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
			String filename = "src/main/java/detector/VOODTicketDetector.xls";
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
					.valueOf("A2:C2") };
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
					.createRow(matchTicketRowCount);
			/**
			 * cell Values
			 */
			HSSFCell cell1 = matchedTicketRowhead.createCell(0);
			cell1.setCellValue("  VOOD Ticket      ");

			HSSFCell cell2 = matchedTicketRowhead.createCell(1);
			cell2.setCellValue("   Count       ");

			HSSFCell cell3 = matchedTicketRowhead.createCell(2);
			cell3.setCellValue("   Class Names    ");

			matchTicketRowCount++;// increment rows

			Iterator<String> voodTicket = voodTickets.keySet().iterator();
			HashSet<String> tempClasses = null;
			while (voodTicket.hasNext()) {
				HSSFRow matchedTicketRow = matchedTicketSheet
						.createRow(matchTicketRowCount);
				String key = voodTicket.next().toString();
				tempClasses = voodTickets.get(key);
				if (tempClasses != null) {
					matchedTicketRow.createCell(0).setCellValue(key);
					StringBuffer classNames = new StringBuffer();
					for (String value : tempClasses) {
						classNames.append(value + ", ");
					}
					int tempCount = (Integer) ticketCount.get(key);
					matchedTicketRow.createCell(1).setCellValue(tempCount);
					String tempClassNames = classNames.substring(0,
							classNames.length() - 2);
					matchedTicketRow.createCell(2).setCellValue(tempClassNames);
				}
				for (int columnPosition = 0; columnPosition < 3; columnPosition++) {
					matchedTicketSheet.autoSizeColumn((short) (columnPosition));
				}
				matchTicketRowCount++;
			}
			/**
			 * code for auto filter
			 */
			matchedTicketSheet.setAutoFilter(CellRangeAddress.valueOf("A2:C"
					+ (matchTicketRowCount)));
			/**
			 * for blank row in excel sheet
			 */
			matchTicketRowCount++;
			/**
			 * Print Number of Tickets Fixed
			 */
			HSSFRow ticketfixedrow = matchedTicketSheet
					.createRow(matchTicketRowCount);
			HSSFConditionalFormattingRule matchedCFrole2 = matchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting matched_fill_pattern2 = matchedCFrole2
					.createPatternFormatting();
			matched_fill_pattern2
					.setFillBackgroundColor(IndexedColors.YELLOW.index);

			CellRangeAddress[] matched_data_range2 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (matchTicketRowCount + 1) + ":B"
							+ (matchTicketRowCount + 1)) };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range2, matchedCFrole2);
			ticketfixedrow.createCell(0).setCellValue("Tickets");
			ticketfixedrow.createCell(1).setCellValue(voodTickets.size());
			matchTicketRowCount++;
			/**
			 * creating of Excel workbook, sheet, rows data
			 */
			FileOutputStream fileOut = new FileOutputStream(filename);
			voodDetectorWorkbook.write(fileOut);
			fileOut.close();
			System.out.println("\nYour excel file has been generated!");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		
		long startTime   = System.currentTimeMillis();

		/* Package or Directory */
		String srcDir = "src/main/java/detector";

		File folder = new File(srcDir);
		File[] listOfFiles = folder.listFiles();

		if (listOfFiles.length > 0) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					voodTicketFind(listOfFiles[i]);
				}
			}
			printVoodTickets();
			createExcel();
		}
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total time taken :"+totalTime+" ms");
	}
}

