package detector;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

public class VOODTicketDetector {

	/**
	 * store VOOD Ticket and List of Classes
	 */
	static HashMap<String, HashSet<String>> voodTicketsWithClasses = new HashMap<>();

	/**
	 * store Ticket and count
	 */
	static HashMap<String, Integer> voodTicketsOccurrence = new HashMap<>();

	/**
	 * store VOOD Tickets with Status
	 */
	static HashMap<String, String> voodTicketsWithStatus = new HashMap<>();

	/**
	 * to store package with number of files
	 */
	static TreeMap<String, Integer> packageWithScriptCount = new TreeMap<>();
	/**
	 * create Excel Workbook
	 */
	static HSSFWorkbook voodDetectorWorkbook = new HSSFWorkbook();
	/**
	 * path of Excel Workbook
	 */
	static String filename = "src/main/resources/VOODTicketDetector.xls";

	/**
	 * method to count number of VOOD tickets with status
	 */
	private static void voodTicketWithStatus() {
		try {
			// Excel file and files to be searched will not be in same
			// directory or folder
			FileInputStream file = new FileInputStream(new File(
					"src/main/resources/JIRA-2.xls"));

			// Create Workbook instance holding reference to .xls file
			HSSFWorkbook workbook = new HSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			HSSFSheet sheet = workbook.getSheetAt(0);
			int counter = 4;
			Row row = null;
			Cell cellVoodTicket = null;
			Cell cellVoodTicketStatus = null;
			boolean isNull = false;
			do {
				try {
					row = sheet.getRow(counter);
					cellVoodTicket = row.getCell(1);
					cellVoodTicketStatus = row.getCell(6);
					voodTicketsWithStatus.put(cellVoodTicket.toString(),
							cellVoodTicketStatus.toString());
					counter++;
				} catch (Exception e) {
					isNull = true;
				}
			} while (isNull != true);
			file.close();
			System.out.println("Total tickets = "
					+ voodTicketsWithStatus.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * method to add tickets and class Names
	 * 
	 * @param key
	 *            contains VOODTicket
	 * @param value
	 *            contains Class Name
	 * @throws Exception
	 */

	private static void addTicket(String key, String value) throws Exception {

		HashSet<String> tempVoodTicketList = null;
		if (voodTicketsWithClasses.containsKey(key)) {
			int tempcount = (Integer) voodTicketsOccurrence.get(key);
			tempcount++;
			voodTicketsOccurrence.put(key, tempcount);
			tempVoodTicketList = voodTicketsWithClasses.get(key);
			if (tempVoodTicketList == null)
				tempVoodTicketList = new HashSet<String>();
			tempVoodTicketList.add(value);
		} else {
			voodTicketsOccurrence.put(key, 1);
			tempVoodTicketList = new HashSet<String>();
			tempVoodTicketList.add(value);
		}
		voodTicketsWithClasses.put(key, tempVoodTicketList);
	}

	/**
	 * Method to detect VOOD tickets from file and compare with VOOD tickets
	 * which are open
	 * 
	 * @param file
	 *            contains File from which we have to find VOOD Tickets
	 */
	static void voodTicketsFinder(String dir) {

		File folder = new File(dir);
		File[] listOfDirectory = folder.listFiles();
		for (int i = 0; i < listOfDirectory.length; i++) {
			if (listOfDirectory[i].isDirectory()) {
				File[] listOfFiles = listOfDirectory[i].listFiles();
				String[] packages = listOfDirectory[i].toString().split("/");
				System.out.println("Package-> " + packages[3]
						+ "  Script Count-> " + listOfFiles.length);
				packageWithScriptCount.put(packages[3], listOfFiles.length);
				for (int j = 0; j < listOfFiles.length; j++) {
					try {
						if (listOfFiles[j].isFile()) {
							BufferedReader br = new BufferedReader(
									new FileReader(listOfFiles[j]));
							String strLine = " ";
							while ((strLine = br.readLine()) != null) {
								String TODO = "//+";
								Pattern TODOPattern = Pattern.compile(TODO);
								Matcher TODOMatcher = TODOPattern
										.matcher(strLine);
								if (TODOMatcher.find()) {
									String voodTicket = "VOOD[-][0-9]+";
									Pattern voodTicketPattern = Pattern
											.compile(voodTicket);
									Matcher voodTicketMatcher = voodTicketPattern
											.matcher(strLine);
									while (voodTicketMatcher.find()) {
										Iterator<HashMap.Entry<String, String>> itr = voodTicketsWithStatus
												.entrySet().iterator();
										while (itr.hasNext()) {
											if (voodTicketMatcher
													.group()
													.equals(itr.next().getKey()))
												addTicket(
														voodTicketMatcher
																.group(),
														listOfFiles[j]
																.getName()
																.split(".java")[0]);
										}
									}
								}
							}
							br.close();
						}

					} catch (Exception e) {
						System.out.println("Exception while reading file: "
								+ listOfFiles[j].getName());
					}
				}
			}
		}
	}

	/**
	 * To print VOOD Tickets, Status, Occurrence and Class Names
	 */
	static void printVoodTickets() {
		Iterator<String> voodTicket = voodTicketsWithClasses.keySet()
				.iterator();
		HashSet<String> tempVoodTickets = null;
		System.out.println("\nVOOD Tickets");
		while (voodTicket.hasNext()) {
			String key = voodTicket.next().toString();
			tempVoodTickets = voodTicketsWithClasses.get(key);
			if (tempVoodTickets != null) {
				System.out.print("\nTicket-> " + key);
				System.out.print("  Status -> "
						+ voodTicketsWithStatus.get(key));
				StringBuffer tickets = new StringBuffer();
				for (String value : tempVoodTickets) {
					tickets.append(value + ", ");
				}
				int tempcount = (Integer) voodTicketsOccurrence.get(key);
				System.out.print("  Occurrence-> " + tempcount);
				String tempTickets = tickets.substring(0, tickets.length() - 2);
				System.out.print("  ClassNames-> " + tempTickets + "\n");
			}
		}
	}

	/**
	 * generate Excel file for Open Tickets Record
	 */
	public static void generateOpenTicketsRecord() {

		/**
		 * variable to increment rows
		 */
		int voodTicketRowCount = 1;

		System.out
				.println("\nExcel Starts for generate Open Tickets Records...");
		try {

			/**
			 * create Sheet "VOOD Tickets" to store VOOD Tickets
			 */
			HSSFSheet matchedTicketSheet = voodDetectorWorkbook
					.createSheet("VOOD OPEN TICKETS");
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
					.valueOf("A2:D2") };
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
					.createRow(voodTicketRowCount);
			/**
			 * cell Values
			 */
			HSSFCell cell1 = matchedTicketRowhead.createCell(0);
			cell1.setCellValue("  VOOD Ticket      ");

			HSSFCell cell2 = matchedTicketRowhead.createCell(1);
			cell2.setCellValue("   Status       ");

			HSSFCell cell3 = matchedTicketRowhead.createCell(2);
			cell3.setCellValue("   Occurrence       ");

			HSSFCell cell4 = matchedTicketRowhead.createCell(3);
			cell4.setCellValue("   Class Names    ");

			voodTicketRowCount++;
			/**
			 * conditional formatting
			 */
			String compare_status1 = "In Progress";
			HSSFConditionalFormattingRule cfrole_statusInProgress = matchedConditionalFormatting
					.createConditionalFormattingRule(ComparisonOperator.EQUAL,
							"\"" + compare_status1 + "\"");
			HSSFPatternFormatting match_fill_pattern_InProgress = cfrole_statusInProgress
					.createPatternFormatting();
			match_fill_pattern_InProgress
					.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.index);

			String compare_status2 = "Code Review";
			HSSFConditionalFormattingRule cfrole_statusCodeReview = matchedConditionalFormatting
					.createConditionalFormattingRule(ComparisonOperator.EQUAL,
							"\"" + compare_status2 + "\"");

			HSSFPatternFormatting match_fill_pattern_CodeReview = cfrole_statusCodeReview
					.createPatternFormatting();
			match_fill_pattern_CodeReview
					.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.index);

			CellRangeAddress[] matched_data_range_status = { (CellRangeAddress) CellRangeAddress
					.valueOf("B1:B1000") };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range_status, cfrole_statusInProgress,
					cfrole_statusCodeReview);

			Iterator<String> voodTicket = voodTicketsWithClasses.keySet()
					.iterator();
			HashSet<String> tempClasses = null;
			CellStyle style = voodDetectorWorkbook.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			while (voodTicket.hasNext()) {
				HSSFRow voodTicketRow = matchedTicketSheet
						.createRow(voodTicketRowCount);
				String key = voodTicket.next().toString();
				tempClasses = voodTicketsWithClasses.get(key);
				if (tempClasses != null) {
					voodTicketRow.createCell(0).setCellValue(key);
					StringBuffer classNames = new StringBuffer();
					for (String value : tempClasses) {
						classNames.append(value + ", ");
					}
					String status = voodTicketsWithStatus.get(key);
					voodTicketRow.createCell(1).setCellValue(status);
					int tempCount = (Integer) voodTicketsOccurrence.get(key);
					HSSFCell countCell = voodTicketRow.createCell(2);
					countCell.setCellValue(tempCount);
					countCell.setCellStyle(style);
					String tempClassNames = classNames.substring(0,
							classNames.length() - 2);
					voodTicketRow.createCell(3).setCellValue(tempClassNames);
				}
				for (int columnPosition = 0; columnPosition < 4; columnPosition++) {
					matchedTicketSheet.autoSizeColumn((short) (columnPosition));
				}
				voodTicketRowCount++;
			}
			/**
			 * code for auto filter
			 */
			matchedTicketSheet.setAutoFilter(CellRangeAddress.valueOf("A2:D"
					+ (voodTicketRowCount)));
			/**
			 * for blank row in excel sheet
			 */
			voodTicketRowCount++;
			/**
			 * Print Number of Tickets
			 */
			HSSFRow ticketfixedrow = matchedTicketSheet
					.createRow(voodTicketRowCount);
			HSSFConditionalFormattingRule matchedCFrole2 = matchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting matched_fill_pattern2 = matchedCFrole2
					.createPatternFormatting();
			matched_fill_pattern2
					.setFillBackgroundColor(IndexedColors.YELLOW.index);

			CellRangeAddress[] matched_data_range2 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (voodTicketRowCount + 1) + ":B"
							+ (voodTicketRowCount + 1)) };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range2, matchedCFrole2);
			ticketfixedrow.createCell(0).setCellValue("Total Tickets");
			ticketfixedrow.createCell(1).setCellValue(
					voodTicketsWithClasses.size());
			voodTicketRowCount++;
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

	/**
	 * generate Excel file for Code Review Tickets Record
	 */

	public static void generateCodeReviewTicketsRecord() {

		int matchTicketRowCount = 1;

		System.out
				.println("\nExcel starts generating Code Review Tickets Records...");
		try {
			HSSFSheet voodTicketSheet = voodDetectorWorkbook
					.createSheet("VOOD Code Review Tickets");
			/**
			 * matched Conditional Formatting
			 */
			HSSFSheetConditionalFormatting matchedConditionalFormatting = voodTicketSheet
					.getSheetConditionalFormatting();
			HSSFConditionalFormattingRule matchedCFrole1 = matchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting match_fill_pattern1 = matchedCFrole1
					.createPatternFormatting();
			match_fill_pattern1
					.setFillBackgroundColor(IndexedColors.GREY_40_PERCENT.index);

			CellRangeAddress[] my_data_range1 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A2:D2") };
			matchedConditionalFormatting.addConditionalFormatting(
					my_data_range1, matchedCFrole1);
			/**
			 * Set Default Row Height of Excel Sheet
			 */
			voodTicketSheet.setDefaultRowHeightInPoints((float) 18);
			/**
			 * create Matched Ticket Header row
			 */
			HSSFRow voodTicketRowhead = voodTicketSheet
					.createRow(matchTicketRowCount);
			/**
			 * cell Values
			 */
			HSSFCell cell1 = voodTicketRowhead.createCell(0);
			cell1.setCellValue("  VOOD Ticket      ");

			HSSFCell cell2 = voodTicketRowhead.createCell(1);
			cell2.setCellValue("   Status       ");

			HSSFCell cell3 = voodTicketRowhead.createCell(2);
			cell3.setCellValue("   Occurrence       ");

			HSSFCell cell4 = voodTicketRowhead.createCell(3);
			cell4.setCellValue("   Class Names    ");

			matchTicketRowCount++;
			CellStyle style = voodDetectorWorkbook.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);

			Iterator<String> voodTicket = voodTicketsWithClasses.keySet()
					.iterator();
			HashSet<String> tempClasses = null;
			int voodTicketCount = 0;
			while (voodTicket.hasNext()) {
				HSSFRow matchedTicketRow = voodTicketSheet
						.createRow(matchTicketRowCount);
				String key = voodTicket.next().toString();
				tempClasses = voodTicketsWithClasses.get(key);
				String status = voodTicketsWithStatus.get(key);
				if (tempClasses != null && status.equals("Code Review")
						|| status.equals("In Progress")) {
					matchedTicketRow.createCell(0).setCellValue(key);
					StringBuffer classNames = new StringBuffer();
					for (String value : tempClasses) {
						classNames.append(value + ", ");
					}

					matchedTicketRow.createCell(1).setCellValue(status);
					int tempCount = (Integer) voodTicketsOccurrence.get(key);
					matchedTicketRow.createCell(2).setCellValue(tempCount);
					HSSFCell countCell = matchedTicketRow.createCell(2);
					countCell.setCellValue(tempCount);
					countCell.setCellStyle(style);
					String tempClassNames = classNames.substring(0,
							classNames.length() - 2);
					matchedTicketRow.createCell(3).setCellValue(tempClassNames);
					matchTicketRowCount++;
					voodTicketCount++;
				}
			}
			/**
			 * code for auto filter
			 */
			voodTicketSheet.setAutoFilter(CellRangeAddress.valueOf("A2:D"
					+ (matchTicketRowCount)));
			/**
			 * for blank row in excel sheet
			 */
			matchTicketRowCount++;
			/**
			 * Print Number of VOOD Tickets Find
			 */
			HSSFRow ticketFixedRow = voodTicketSheet
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
			ticketFixedRow.createCell(0).setCellValue("Total Tickets");
			ticketFixedRow.createCell(1).setCellValue(voodTicketCount);
			matchTicketRowCount++;
			/**
			 * for auto size of columns
			 */
			for (int columnPosition = 0; columnPosition < 4; columnPosition++) {
				voodTicketSheet.autoSizeColumn((short) (columnPosition));
			}
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

	static void generatePackagesScriptsRecord() {

		int matchTicketRowCount = 1;

		try {
			HSSFSheet voodTicketSheet = voodDetectorWorkbook
					.createSheet("Package Script ");
			/**
			 * Set Default Row Height of Excel Sheet
			 */
			voodTicketSheet.setDefaultRowHeightInPoints((float) 18);
			/**
			 * matched Conditional Formatting
			 */
			HSSFSheetConditionalFormatting matchedConditionalFormatting = voodTicketSheet
					.getSheetConditionalFormatting();
			HSSFConditionalFormattingRule matchedCFrole1 = matchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");
			HSSFPatternFormatting match_fill_pattern1 = matchedCFrole1
					.createPatternFormatting();
			match_fill_pattern1
					.setFillBackgroundColor(IndexedColors.GREY_40_PERCENT.index);

			CellRangeAddress[] my_data_range2 = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (matchTicketRowCount + 1) + ":B"
							+ (matchTicketRowCount + 1)) };
			matchedConditionalFormatting.addConditionalFormatting(
					my_data_range2, matchedCFrole1);
			HSSFRow voodTicketRowhead2 = voodTicketSheet
					.createRow(matchTicketRowCount);
			/**
			 * cell Values
			 */
			HSSFCell cell1_1 = voodTicketRowhead2.createCell(0);
			cell1_1.setCellValue("  PACKAGE     ");

			HSSFCell cell1_2 = voodTicketRowhead2.createCell(1);
			cell1_2.setCellValue("  SCRIPT COUNT     ");

			matchTicketRowCount++;
			int start = matchTicketRowCount;

			Iterator<String> packageCountItr = packageWithScriptCount.keySet()
					.iterator();
			while (packageCountItr.hasNext()) {
				HSSFRow voodTicketRow2 = voodTicketSheet
						.createRow(matchTicketRowCount);
				String key = (String) packageCountItr.next();
				voodTicketRow2.createCell(0).setCellValue(key);
				voodTicketRow2.createCell(1).setCellValue(
						packageWithScriptCount.get(key));
				matchTicketRowCount++;
			}
			/**
			 * code for auto filter
			 */
			voodTicketSheet.setAutoFilter(CellRangeAddress.valueOf("A2:B"
					+ (matchTicketRowCount)));
			matchTicketRowCount++;
			/**
			 * conditional formatting
			 */
			HSSFConditionalFormattingRule matchedCFrole2 = matchedConditionalFormatting
					.createConditionalFormattingRule(
							ComparisonOperator.NOT_EQUAL, "-1");

			HSSFPatternFormatting matched_fill_pattern2 = matchedCFrole2
					.createPatternFormatting();
			matched_fill_pattern2
					.setFillBackgroundColor(IndexedColors.YELLOW.index);
			CellRangeAddress[] matched_data_range_script = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (matchTicketRowCount + 1) + ":B"
							+ (matchTicketRowCount + 1)) };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range_script, matchedCFrole2);
			HSSFRow rowTotalScript = voodTicketSheet
					.createRow(matchTicketRowCount);
			/**
			 * print Total Scripts
			 */
			rowTotalScript.createCell(0).setCellValue(" Total Scripts ");
			rowTotalScript.createCell(1).setCellFormula(
					"SUM(B" + start + ":B" + matchTicketRowCount + ")");
			matchTicketRowCount++;
			/**
			 * print Total Packages
			 */
			HSSFRow rowTotalPackage = voodTicketSheet
					.createRow(matchTicketRowCount);
			rowTotalPackage.createCell(0).setCellValue(" Total packages ");
			rowTotalPackage.createCell(1).setCellValue(
					packageWithScriptCount.size());
			CellRangeAddress[] matched_data_range_package = { (CellRangeAddress) CellRangeAddress
					.valueOf("A" + (matchTicketRowCount + 1) + ":B"
							+ (matchTicketRowCount + 1)) };
			matchedConditionalFormatting.addConditionalFormatting(
					matched_data_range_package, matchedCFrole2);
			/**
			 * for auto size of columns
			 */
			for (int columnPosition = 0; columnPosition < 2; columnPosition++) {
				voodTicketSheet.autoSizeColumn((short) (columnPosition));
			}
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

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */

	public static void main(String[] args) throws Exception {

		/*
		 * File file = new File("src/main/java/detector/out.txt"); //Your file
		 * FileOutputStream fos = new FileOutputStream(file); PrintStream ps =
		 * new PrintStream(fos); System.setOut(ps);
		 */
		long startTime = System.currentTimeMillis();
		voodTicketWithStatus();

		/* Package or Directory */
		String srcDir = "src/main/java";

		voodTicketsFinder(srcDir);
		printVoodTickets();
		// generateOpenTicketsRecord();
		generateCodeReviewTicketsRecord();
		generatePackagesScriptsRecord();
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total time taken:" + totalTime + " ms");
	}
}
