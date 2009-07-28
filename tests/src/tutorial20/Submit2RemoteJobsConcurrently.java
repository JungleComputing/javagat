package tutorial20;


public class Submit2RemoteJobsConcurrently {
	 
	public static void main(String[] args)  {
	
		GT42JobThread job1=new GT42JobThread(args[0]);
	    NewWSGT4JobThread job2=new NewWSGT4JobThread(args[1]);
	    
	    
	    job2.start();
	    job1.start(); 
	             
	}	
}
	
	
	
	
	
	
