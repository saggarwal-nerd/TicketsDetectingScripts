package detector;

import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MyMultipleAnnotation {
	
	@Ignore("TR-13575, VOOD-444")
	@Test
	public void testAdd() {
		String str = "Junit is working fine";
		assertEquals("Junit is working fine", str);
	}
	
	@Ignore("Ticket Number is SI-24433 ")
	public void Y1() {

	}
	// TODO: VOOD-2054
	//TODO : "VOOD-444" is not working due to VOOD-2289
	//TODO: "VOOD-4487"
	//"VOOD-4577"
	//TODO : "VOOD-444"
	//TODO : "VOOD-2289"//TODO : "VOOD-2289"
	//TODO : "VOOD-2289"
	

	@Ignore("TR-1185, MAR-2609, Vood-1446")
	public void Add() {

	}
	// TODO: VOOD-2054

	@Ignore("Ticket Number is TDD- is not working VOOD-MAR1")
	public void Z1() {

	}
	
	@Ignore("Ticket is not working")
	public void X1() {

	}

	public void myAnnotationTestMethod() {

		List<String> types = new ArrayList<>();

		try {
			Class<? extends Object> cls = this.getClass();
			System.out.println(cls);
			Method mth = cls.getMethod("X1");
			// Ignore annotation = mth.getAnnotation(Ignore.class);
			Annotation[] annotation = mth.getAnnotations();
			for (Annotation an : annotation) {
				System.out.println(an);
				types.add(((Ignore) an).value());
				// System.out.println(((Ignore) an).value());
			}
			String pattern = "[A-z]{2,7}-?\\d+";

			// Create a Pattern object
			Pattern r = Pattern.compile(pattern);

			// Now create matcher object.
			for (String type : types) {

				System.out.println(type.length());
				Matcher m = r.matcher(type);

				while (m.find()) {
					System.out.println("match");
					System.out.println("Ticket is:"+m.group());
//					String[] Array = type.split("\\s*,\\s*");
//					int i = 1;
//					for (String name : Array) {
//						System.out.println("Ticket[" + i + "] : " + name);
//						i++;
//					}
					// System.out.println(Array[0]);
//					String[] Array2 = Array[0].split("\\s*:\\s*");
//					for (String name2 : Array2) {
//						System.out.println("\nTicket[" + i + "] : " + name2);
//						i++;
//					}

				} // else {
					// System.out.println("NO MATCH");
				// }
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public static void main(String a[]) throws FileNotFoundException {

		MyMultipleAnnotation mulannotation = new MyMultipleAnnotation();
		mulannotation.myAnnotationTestMethod();
	}
}