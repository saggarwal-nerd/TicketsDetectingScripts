package detector;

import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.*;
import org.junit.*;

import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.MethodReporter;

public class CSVReader {

	/**
	 * store Matched Class ID and Class Names
	 */
	static HashMap<String, String> matchedClassID = new HashMap<String, String>();
	/**
	 * store UnMatched Class ID and Class Names
	 */
	static ArrayList<String> unmatchedClassID = new ArrayList<String>();
	/**
	 * store Class ID
	 */
	static ArrayList<String> idNumberList = new ArrayList<>();
	static int classIDCount = 0;
	static int classIDRowCount = 0;

	private static void readCSV() {
		try {
			// csv file path;
			// String csvFile = "src/test/resources/data/FindScript.csv";
			URL csvFile = new URL(
					"https://s3.amazonaws.com/uploads.hipchat.com/106041/985439/D00g8QGfsoWSiv0/testid.csv");
			BufferedReader br = null;
			String line = "";
			// br = new BufferedReader(new FileReader(csvFile));
			br = new BufferedReader(new InputStreamReader(csvFile.openStream()));
			while ((line = br.readLine()) != null) {
				String patternNumberonly = "[0-9]+";
				Pattern testClassId = Pattern.compile(patternNumberonly);
				Matcher matcherClassID = testClassId.matcher(line);
				if (matcherClassID.find()) {
					idNumberList.add(line);
					classIDCount++;
				}
			}
			System.out.println("Total tickets = " + classIDCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final static MethodReporter reporter = new MethodReporter() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Annotation>[] annotations() {
			return new Class[] { Test.class };
		}

		@Override
		public void reportMethodAnnotation(
				Class<? extends Annotation> annotation, String className,
				String methodName) {
			try {
				Class<?> classType = Class.forName(className);
				for (String strId : idNumberList) {
					String patternNumberonly = "[0-9]+";
					Pattern classes = Pattern.compile(patternNumberonly);
					Matcher matcherClasses = classes.matcher(classType
							.getSimpleName());
					if (matcherClasses.find()) {
						if (matcherClasses.group().equals(strId)) {
							matchedClassID
									.put(strId, classType.getSimpleName());
						} else {
							unmatchedClassID.add(strId);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public static void main(String[] args) {

		readCSV();
		String filename = "src/main/java/detector/TicketIdFinder.xls";
		HSSFWorkbook classIDWorkbook = new HSSFWorkbook();
		HSSFSheet classIDWorkbookSheet = classIDWorkbook
				.createSheet("FirstSheet");
		classIDWorkbookSheet.setDefaultRowHeightInPoints((float) 18);
		AnnotationDetector cf = new AnnotationDetector(reporter);
		try {
			cf.detect();
			HSSFRow classIDrowhead = classIDWorkbookSheet
					.createRow(classIDRowCount);
			classIDWorkbookSheet.setColumnWidth(0, 4000);
			classIDrowhead.createCell(0).setCellValue("Test ClassID");
			classIDWorkbookSheet.setColumnWidth(1, 8000);
			classIDrowhead.createCell(1).setCellValue("Class Names");
			classIDRowCount++;

			System.out.println("Total matched scripts: "
					+ matchedClassID.size());
			for (Map.Entry<String, String> entry : matchedClassID.entrySet()) {
				System.out.println("code: " + entry.getKey() + "  ClassName: "
						+ entry.getValue());
				HSSFRow classIDRow = classIDWorkbookSheet
						.createRow(classIDRowCount);
				classIDRow.createCell(0).setCellValue(entry.getKey());
				classIDRow.createCell(1).setCellValue(entry.getValue());
				classIDRowCount++;
			}

			int unmatchedClassIDcount = 0;
			for (int i = 0; i < unmatchedClassID.size(); i++) {
				System.out.println("Matched not found ["
						+ (++unmatchedClassIDcount) + "] : "
						+ unmatchedClassID.get(i).toString());
				HSSFRow unmatchedClassIDRow = classIDWorkbookSheet
						.createRow((short) classIDRowCount);
				unmatchedClassIDRow.createCell(0).setCellValue(
						unmatchedClassID.get(i).toString());
				unmatchedClassIDRow.createCell(1).setCellValue(
						"Class not found");
				classIDRowCount++;
			}
			HSSFRow whiteSpaceRow = classIDWorkbookSheet
					.createRow((short) classIDRowCount);
			whiteSpaceRow.createCell(0).setCellValue("Total");
			classIDRowCount++;
			HSSFRow totalClassIDRow = classIDWorkbookSheet
					.createRow((short) classIDRowCount);
			totalClassIDRow.createCell(0).setCellValue("ClassID found");
			totalClassIDRow.createCell(1).setCellValue(classIDCount);
			classIDRowCount++;
			HSSFRow totalMatchedClassIDRow = classIDWorkbookSheet
					.createRow((short) classIDRowCount);
			totalMatchedClassIDRow.createCell(0)
					.setCellValue("Matched ClassID");
			totalMatchedClassIDRow.createCell(1).setCellValue(
					matchedClassID.size());
			System.out.println("Total Unmatched TESTClassID :"
					+ matchedClassID.size());
			classIDRowCount++;
			HSSFRow totalUnmatchedClassIDRow = classIDWorkbookSheet
					.createRow((short) classIDRowCount);
			totalUnmatchedClassIDRow.createCell(0).setCellValue(
					"UnMatched ClassID");
			totalUnmatchedClassIDRow.createCell(1).setCellValue(
					unmatchedClassID.size());
			classIDRowCount++;
			System.out.println("Total Unmatched TESTClassID :"
					+ unmatchedClassID.size());
			System.out.println("");

			FileOutputStream fileOut = new FileOutputStream(filename);
			classIDWorkbook.write(fileOut);
			fileOut.close();
			System.out.println("Your excel file has been generated!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
