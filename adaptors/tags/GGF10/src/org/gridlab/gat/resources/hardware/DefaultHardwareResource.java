package org.gridlab.gat.resources.hardware;

import java.util.List;
import java.util.Vector;
import java.util.Hashtable;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * An instance of this interface is an abstract representation of a physical
 * hardware resource which is monitorable.
 * <p>
 * An instance of this interface presents an abstract, system-independent
 * view of a physical hardware resource which is monitorable. Various
 * systems use system-dependent means of representing a physical hardware
 * resource. GAT, however, uses an instance of this interface as an operating
 * system independent description of a physical hardware resource which
 * is monitorable.
 * <p>
 * An instance of this interface allows on to examine the various properties
 * of the physical hardware resource to which this instance
 * corresponds. In addition is allows one to monitor the physical
 * hardware resource to which this instance corresponds.
 */
public class DefaultHardwareResource implements HardwareResource
{
   /**
    * Constructs a DefaultHardwareResource
    */
    public DefaultHardwareResource()
    {
        super();
    }
    
   /**
    * Gets the HardwareResourceDescription which describes this
    * HardwareResource instance.
    *
    * @return A HardwareResourceDescription which describes this 
    * HardwareResource instance.
    */
    public HardwareResourceDescription  getHardwareResourceDescription()
    {
        Hashtable hashtable = new Hashtable();
        
        String cpuType = runCommand("uname -p");
        String machineType = runCommand("uname -m");
        String machineNode = runCommand("uname -n");
        
        hashtable.put("cpu.type", cpuType);
        hashtable.put("machine.type", machineType);
        hashtable.put("machine.node", machineNode);
        
        return new HardwareResourceDescription( hashtable, new Vector(), new Vector() );
    }
    
   /**
    * This runs the passed command and obtains the returned String
    * from standard out. It is a utility method for the method
    * getHardwareResourceDescription
    *
    * @param command The command to run
    * @return The result of the command on standard out
    */
    private String runCommand(String command)
    {
        String result = null;
        
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        
        try
        {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
            
            inputStream = process.getInputStream();
            inputStreamReader = new InputStreamReader( inputStream );
            bufferedReader = new BufferedReader( inputStreamReader );
            
            result = bufferedReader.readLine();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();  // Log !!!
        }
        finally
        {
            if(null != bufferedReader)
            {
                try
                {
                    bufferedReader.close();
                }
                catch(Exception exception)
                {
                    // Ignore exception !!!
                }
            }
            
            if(null != inputStreamReader)
            {
                try
                {
                    inputStreamReader.close();
                }
                catch(Exception exception)
                {
                    // Ignore exception !!!
                }
            }  
            
            if(null != inputStream)
            {
                try
                {
                    inputStream.close();
                }
                catch(Exception exception)
                {
                    // Ignore exception !!!
                }
            }                                  
        }
        
        return result;
    }
    
   /**
    * This method adds the passed instance of a MetricListener to the java.util.List
    * of MetricListeners which are notified of MetricEvents by an
    * instance of this class. The passed MetricListener is only notified of
    * MetricEvents which correspond to Metric instance passed to this
    * method.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    */
    public void addMetricListener(MetricListener metricListener, Metric metric)
    {
    }
    
   /**
    * Removes the passed MetricListener from the java.util.List of MetricListeners
    * which are notified of MetricEvents corresponding to the passed
    * Metric instance.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    */
    public void removeMetricListener(MetricListener metricListener, Metric metric)
    {
    }
    
   /**
    * This method returns a java.util.List of Metric instances. Each Metric 
    * instance in this java.util.List is a Metric which can be monitored on 
    * this instance.
    *
    * @return An java.util.List of Metric instances. Each Metric instance
    * in this java.util.List is a Metric which can be monitored on this 
    * instance.
    */
    public List getMetrics()
    {
        return new Vector();
    }    
}
