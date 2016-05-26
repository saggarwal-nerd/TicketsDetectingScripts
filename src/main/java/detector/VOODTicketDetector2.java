package detector;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;

public class VOODTicketDetector2 {

	/**
	 * store VOOD Ticket and List of Classes
	 */
	static HashMap<String, HashSet<String>> voodTickets = new HashMap<>();

	/**
	 * store Ticket and count
	 */
	static HashMap<String, Integer> ticketCount = new HashMap<>();

	/**
	 * store VOOD Tickets
	 */
	static ArrayList<String> voodTicketList = new ArrayList<>();

	/**
	 * variable to increment rows
	 */
	static int matchTicketRowCount = 1;

	/**
	 * method to count number of VOOD tickets in file and store them in a
	 * ArrayList
	 */

	private static void voodTicketCounter() {
		try {
			// csv file path;
			String csvFile = "src/main/java/detector/VoodTickets.csv";
			// URL csvFile = new URL(
			// "https://s3.amazonaws.com/uploads.hipchat.com/106041/985439/D00g8QGfsoWSiv0/testid.csv");
			BufferedReader br = null;
			String line = "";
			br = new BufferedReader(new FileReader(csvFile));
			// br = new BufferedReader(new
			// InputStreamReader(csvFile.openStream()));
			while ((line = br.readLine()) != null) {
				String patternVoodOnly = "VOOD[-][0-9]+";
				Pattern testClassId = Pattern.compile(patternVoodOnly);
				Matcher matcherClassID = testClassId.matcher(line);
				while (matcherClassID.find()) {
					voodTicketList.add(line);
				}
			}
			br.close();
			Collections.sort(voodTicketList);
			System.out.println("Total tickets = " + voodTicketList.size());
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
	 * Method to detect VOOD tickets from file and compare with VOOD tickets
	 * which are open
	 * 
	 * @param file
	 *            contains File from which we have to find VOOD Tickets
	 */
	static void voodTicketFind(String dir) {

		File folder = new File(dir);
		File[] listOfDirectory = folder.listFiles();
		for (int i = 0; i < listOfDirectory.length; i++) {
			
			if (listOfDirectory[i].isDirectory()) {
				File[] listOfFiles = listOfDirectory[i].listFiles();
				for (int j = 0; j < listOfFiles.length; j++) {
					try {
						if (listOfFiles[j].isFile()) {
							BufferedReader br = new BufferedReader(
									new FileReader(listOfFiles[j]));
							String strLine = "";
							while ((strLine = br.readLine()) != null) {
								String TODO = "TODO[ : ]+";
								Pattern TODOPattern = Pattern.compile(TODO);
								Matcher TODOMatcher = TODOPattern
										.matcher(strLine);
								if(TODOMatcher.find()) {
									String voodTicket = "VOOD[-][0-9]+";
									Pattern voodTicketPattern = Pattern
											.compile(voodTicket);
									Matcher voodTicketMatcher = voodTicketPattern
											.matcher(strLine);
									while (voodTicketMatcher.find()) {
										int index = Collections.binarySearch(
												voodTicketList,
												voodTicketMatcher.group());
										if (index > 0)
											addTicket(
													voodTicketMatcher.group(),
													listOfFiles[j].getName()
															.split(".java")[0]);
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

			matchTicketRowCount++;

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
			ticketfixedrow.createCell(0).setCellValue("Total Tickets");
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
		voodTicketCounter();

		/* Package or Directory */
		String srcDir = "src/main/java";

		voodTicketFind(srcDir);
		printVoodTickets();
		createExcel();
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total time taken:" + totalTime + " ms");
	}
}