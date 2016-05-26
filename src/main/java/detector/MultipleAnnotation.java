package detector;

import org.junit.Ignore;

public class MultipleAnnotation {
	
	// TODO: VOOD-2054
	@Ignore("RS-1522- Wrong error message is displayed while changing the status to 'Published' of a KB record (with past expiration date) on List View/Sub Panel")
	public void Y1() {

	}
	
	//TODO : VOOD-2289, VOOD-444
	@Ignore("Ticket Number is TR-13575 ")
	public void z1() {

	}
	
	//TODO:VOOD-444,"VOOD-444"
	
	@Ignore("VOOD-444 : Ticket no")
	public void Add() {

	}

	@Ignore("Ticket Number is NOMAD-2441 ")
	public void X1() {

	}
}
