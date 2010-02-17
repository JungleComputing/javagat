package org.gridlab.gat.resources;

import java.util.HashSet;

/**
 * 
 * 
 * @author Jerome Revillard
 */
public class OrderedCoScheduleJobDescription extends CoScheduleJobDescription {

    private static final long serialVersionUID = -6670271235157948175L;

    private HashSet<JobLink> links = new HashSet<JobLink>();

    public OrderedCoScheduleJobDescription(JobDescription jobDescription) {
		add(jobDescription);
	}
    
    public OrderedCoScheduleJobDescription(JobDescription[] jobDescriptions) {
		add(jobDescriptions);
	}
    
    /**
     * Add a link between 2 {@link Job}s represented by there {@link JobDescription}.
     * The link means that the job represented by the description 1 must be finished before 
     * executing the job represented by the description 2.
     * 
     * @param firstjob
     *                the description of the job that has to be completed before the other one
     * @param nextjob
     *                the description of the job that will be started once the previous job will be completed
     */
    public void addLink(JobDescription firstjob, JobDescription nextjob) throws IllegalArgumentException{
    	if(!jobDescriptions.contains(firstjob)){
    		throw new IllegalArgumentException("The first job description is not part of the actual OrderedCoScheduleJobDescription object");
    	}
    	if(!jobDescriptions.contains(nextjob)){
    		throw new IllegalArgumentException("The second job description is not part of the actual OrderedCoScheduleJobDescription object");
    	}
		this.links.add(new JobLink(firstjob, nextjob));
	}
    
    public HashSet<JobLink> getLinks(){
		return links;
	}
    
    public static class JobLink{
    	private JobDescription jd1;
    	private JobDescription jd2;
    	public JobLink(JobDescription jd1, JobDescription jd2) {
			this.jd1 = jd1;
			this.jd2 = jd2;
		}
    	
    	public JobDescription getFirstJob(){
    		return jd1;
    	}
    	
    	public JobDescription getSecondJob(){
    		return jd2;
    	}
    	
    	@Override
    	public boolean equals(Object obj) {
    		if(obj instanceof JobLink){
    			if(((JobLink)obj).jd1.equals(this.jd1) && ((JobLink)obj).jd2.equals(this.jd2)){
    				return true;
    			}
    		}
    		return false;
    	}
    }
}
