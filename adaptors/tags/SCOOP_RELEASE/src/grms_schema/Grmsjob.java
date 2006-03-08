/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Grmsjob.java,v 1.6 2005/04/07 13:48:19 rob Exp $
 */

package grms_schema;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Job Description for Gridlab Resource Management System
 * 
 * @version $Revision: 1.6 $ $Date: 2005/04/07 13:48:19 $
 */
public class Grmsjob implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _appid
     */
    private java.lang.String _appid;

    /**
     * Field _persistent
     */
    private boolean _persistent = false;

    /**
     * keeps track of state for field: _persistent
     */
    private boolean _has_persistent;

    /**
     * Field _extension
     */
    private java.lang.String _extension;

    /**
     * Field _project
     */
    private java.lang.String _project;

    /**
     * Description of Simple Job
     */
    private grms_schema.Simplejob _simplejob;

    /**
     * Description of Workflow Job (not implemented yet)
     */
    private grms_schema.Workflowjob _workflowjob;


      //----------------/
     //- Constructors -/
    //----------------/

    public Grmsjob() {
        super();
    } //-- grms_schema.Grmsjob()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deletePersistent
     * 
     */
    public void deletePersistent()
    {
        this._has_persistent= false;
    } //-- void deletePersistent() 

    /**
     * Returns the value of field 'appid'.
     * 
     * @return String
     * @return the value of field 'appid'.
     */
    public java.lang.String getAppid()
    {
        return this._appid;
    } //-- java.lang.String getAppid() 

    /**
     * Returns the value of field 'extension'.
     * 
     * @return String
     * @return the value of field 'extension'.
     */
    public java.lang.String getExtension()
    {
        return this._extension;
    } //-- java.lang.String getExtension() 

    /**
     * Returns the value of field 'persistent'.
     * 
     * @return boolean
     * @return the value of field 'persistent'.
     */
    public boolean getPersistent()
    {
        return this._persistent;
    } //-- boolean getPersistent() 

    /**
     * Returns the value of field 'project'.
     * 
     * @return String
     * @return the value of field 'project'.
     */
    public java.lang.String getProject()
    {
        return this._project;
    } //-- java.lang.String getProject() 

    /**
     * Returns the value of field 'simplejob'. The field
     * 'simplejob' has the following description: Description of
     * Simple Job
     * 
     * @return Simplejob
     * @return the value of field 'simplejob'.
     */
    public grms_schema.Simplejob getSimplejob()
    {
        return this._simplejob;
    } //-- grms_schema.Simplejob getSimplejob() 

    /**
     * Returns the value of field 'workflowjob'. The field
     * 'workflowjob' has the following description: Description of
     * Workflow Job (not implemented yet)
     * 
     * @return Workflowjob
     * @return the value of field 'workflowjob'.
     */
    public grms_schema.Workflowjob getWorkflowjob()
    {
        return this._workflowjob;
    } //-- grms_schema.Workflowjob getWorkflowjob() 

    /**
     * Method hasPersistent
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasPersistent()
    {
        return this._has_persistent;
    } //-- boolean hasPersistent() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'appid'.
     * 
     * @param appid the value of field 'appid'.
     */
    public void setAppid(java.lang.String appid)
    {
        this._appid = appid;
    } //-- void setAppid(java.lang.String) 

    /**
     * Sets the value of field 'extension'.
     * 
     * @param extension the value of field 'extension'.
     */
    public void setExtension(java.lang.String extension)
    {
        this._extension = extension;
    } //-- void setExtension(java.lang.String) 

    /**
     * Sets the value of field 'persistent'.
     * 
     * @param persistent the value of field 'persistent'.
     */
    public void setPersistent(boolean persistent)
    {
        this._persistent = persistent;
        this._has_persistent = true;
    } //-- void setPersistent(boolean) 

    /**
     * Sets the value of field 'project'.
     * 
     * @param project the value of field 'project'.
     */
    public void setProject(java.lang.String project)
    {
        this._project = project;
    } //-- void setProject(java.lang.String) 

    /**
     * Sets the value of field 'simplejob'. The field 'simplejob'
     * has the following description: Description of Simple Job
     * 
     * @param simplejob the value of field 'simplejob'.
     */
    public void setSimplejob(grms_schema.Simplejob simplejob)
    {
        this._simplejob = simplejob;
    } //-- void setSimplejob(grms_schema.Simplejob) 

    /**
     * Sets the value of field 'workflowjob'. The field
     * 'workflowjob' has the following description: Description of
     * Workflow Job (not implemented yet)
     * 
     * @param workflowjob the value of field 'workflowjob'.
     */
    public void setWorkflowjob(grms_schema.Workflowjob workflowjob)
    {
        this._workflowjob = workflowjob;
    } //-- void setWorkflowjob(grms_schema.Workflowjob) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (grms_schema.Grmsjob) Unmarshaller.unmarshal(grms_schema.Grmsjob.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
