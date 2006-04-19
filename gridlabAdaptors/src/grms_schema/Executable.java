/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */
package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Application description section
 *
 * @version $Revision: 1.9 $ $Date$
 */
public class Executable implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _type
     */
    private grms_schema.types.ExecutableTypeType _type;

    /**
     * Field _count
     */
    private int _count = 1;

    /**
     * keeps track of state for field: _count
     */
    private boolean _has_count;

    /**
     * Field _checkpointable
     */
    private boolean _checkpointable = false;

    /**
     * keeps track of state for field: _checkpointable
     */
    private boolean _has_checkpointable;

    /**
     * Field _executableChoice
     */
    private grms_schema.ExecutableChoice _executableChoice;

    /**
     * Arguments of execution
     */
    private grms_schema.Arguments _arguments;

    /**
     * Standard input stream location
     */
    private grms_schema.Stdin _stdin;

    /**
     * Standard output stream location
     */
    private grms_schema.Stdout _stdout;

    /**
     * Standard error stream location
     */
    private grms_schema.Stderr _stderr;

    /**
     * Environment variables
     */
    private grms_schema.Environment _environment;

    /**
     * Checkpint files location
     */
    private grms_schema.Checkpoint _checkpoint;

    //----------------/
    //- Constructors -/
    //----------------/
    public Executable() {
        super();
    } //-- grms_schema.Executable()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method deleteCheckpointable
     *
     */
    public void deleteCheckpointable() {
        this._has_checkpointable = false;
    } //-- void deleteCheckpointable() 

    /**
     * Method deleteCount
     *
     */
    public void deleteCount() {
        this._has_count = false;
    } //-- void deleteCount() 

    /**
     * Returns the value of field 'arguments'. The field
     * 'arguments' has the following description: Arguments of
     * execution
     *
     * @return Arguments
     * @return the value of field 'arguments'.
     */
    public grms_schema.Arguments getArguments() {
        return this._arguments;
    } //-- grms_schema.Arguments getArguments() 

    /**
     * Returns the value of field 'checkpoint'. The field
     * 'checkpoint' has the following description: Checkpint files
     * location
     *
     * @return Checkpoint
     * @return the value of field 'checkpoint'.
     */
    public grms_schema.Checkpoint getCheckpoint() {
        return this._checkpoint;
    } //-- grms_schema.Checkpoint getCheckpoint() 

    /**
     * Returns the value of field 'checkpointable'.
     *
     * @return boolean
     * @return the value of field 'checkpointable'.
     */
    public boolean getCheckpointable() {
        return this._checkpointable;
    } //-- boolean getCheckpointable() 

    /**
     * Returns the value of field 'count'.
     *
     * @return int
     * @return the value of field 'count'.
     */
    public int getCount() {
        return this._count;
    } //-- int getCount() 

    /**
     * Returns the value of field 'environment'. The field
     * 'environment' has the following description: Environment
     * variables
     *
     * @return Environment
     * @return the value of field 'environment'.
     */
    public grms_schema.Environment getEnvironment() {
        return this._environment;
    } //-- grms_schema.Environment getEnvironment() 

    /**
     * Returns the value of field 'executableChoice'.
     *
     * @return ExecutableChoice
     * @return the value of field 'executableChoice'.
     */
    public grms_schema.ExecutableChoice getExecutableChoice() {
        return this._executableChoice;
    } //-- grms_schema.ExecutableChoice getExecutableChoice() 

    /**
     * Returns the value of field 'stderr'. The field 'stderr' has
     * the following description: Standard error stream location
     *
     * @return Stderr
     * @return the value of field 'stderr'.
     */
    public grms_schema.Stderr getStderr() {
        return this._stderr;
    } //-- grms_schema.Stderr getStderr() 

    /**
     * Returns the value of field 'stdin'. The field 'stdin' has
     * the following description: Standard input stream location
     *
     * @return Stdin
     * @return the value of field 'stdin'.
     */
    public grms_schema.Stdin getStdin() {
        return this._stdin;
    } //-- grms_schema.Stdin getStdin() 

    /**
     * Returns the value of field 'stdout'. The field 'stdout' has
     * the following description: Standard output stream location
     *
     * @return Stdout
     * @return the value of field 'stdout'.
     */
    public grms_schema.Stdout getStdout() {
        return this._stdout;
    } //-- grms_schema.Stdout getStdout() 

    /**
     * Returns the value of field 'type'.
     *
     * @return ExecutableTypeType
     * @return the value of field 'type'.
     */
    public grms_schema.types.ExecutableTypeType getType() {
        return this._type;
    } //-- grms_schema.types.ExecutableTypeType getType() 

    /**
     * Method hasCheckpointable
     *
     *
     *
     * @return boolean
     */
    public boolean hasCheckpointable() {
        return this._has_checkpointable;
    } //-- boolean hasCheckpointable() 

    /**
     * Method hasCount
     *
     *
     *
     * @return boolean
     */
    public boolean hasCount() {
        return this._has_count;
    } //-- boolean hasCount() 

    /**
     * Method isValid
     *
     *
     *
     * @return boolean
     */
    public boolean isValid() {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
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
            throws org.exolab.castor.xml.MarshalException,
            org.exolab.castor.xml.ValidationException {
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
            throws java.io.IOException, org.exolab.castor.xml.MarshalException,
            org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'arguments'. The field 'arguments'
     * has the following description: Arguments of execution
     *
     * @param arguments the value of field 'arguments'.
     */
    public void setArguments(grms_schema.Arguments arguments) {
        this._arguments = arguments;
    } //-- void setArguments(grms_schema.Arguments) 

    /**
     * Sets the value of field 'checkpoint'. The field 'checkpoint'
     * has the following description: Checkpint files location
     *
     * @param checkpoint the value of field 'checkpoint'.
     */
    public void setCheckpoint(grms_schema.Checkpoint checkpoint) {
        this._checkpoint = checkpoint;
    } //-- void setCheckpoint(grms_schema.Checkpoint) 

    /**
     * Sets the value of field 'checkpointable'.
     *
     * @param checkpointable the value of field 'checkpointable'.
     */
    public void setCheckpointable(boolean checkpointable) {
        this._checkpointable = checkpointable;
        this._has_checkpointable = true;
    } //-- void setCheckpointable(boolean) 

    /**
     * Sets the value of field 'count'.
     *
     * @param count the value of field 'count'.
     */
    public void setCount(int count) {
        this._count = count;
        this._has_count = true;
    } //-- void setCount(int) 

    /**
     * Sets the value of field 'environment'. The field
     * 'environment' has the following description: Environment
     * variables
     *
     * @param environment the value of field 'environment'.
     */
    public void setEnvironment(grms_schema.Environment environment) {
        this._environment = environment;
    } //-- void setEnvironment(grms_schema.Environment) 

    /**
     * Sets the value of field 'executableChoice'.
     *
     * @param executableChoice the value of field 'executableChoice'
     */
    public void setExecutableChoice(
            grms_schema.ExecutableChoice executableChoice) {
        this._executableChoice = executableChoice;
    } //-- void setExecutableChoice(grms_schema.ExecutableChoice) 

    /**
     * Sets the value of field 'stderr'. The field 'stderr' has the
     * following description: Standard error stream location
     *
     * @param stderr the value of field 'stderr'.
     */
    public void setStderr(grms_schema.Stderr stderr) {
        this._stderr = stderr;
    } //-- void setStderr(grms_schema.Stderr) 

    /**
     * Sets the value of field 'stdin'. The field 'stdin' has the
     * following description: Standard input stream location
     *
     * @param stdin the value of field 'stdin'.
     */
    public void setStdin(grms_schema.Stdin stdin) {
        this._stdin = stdin;
    } //-- void setStdin(grms_schema.Stdin) 

    /**
     * Sets the value of field 'stdout'. The field 'stdout' has the
     * following description: Standard output stream location
     *
     * @param stdout the value of field 'stdout'.
     */
    public void setStdout(grms_schema.Stdout stdout) {
        this._stdout = stdout;
    } //-- void setStdout(grms_schema.Stdout) 

    /**
     * Sets the value of field 'type'.
     *
     * @param type the value of field 'type'.
     */
    public void setType(grms_schema.types.ExecutableTypeType type) {
        this._type = type;
    } //-- void setType(grms_schema.types.ExecutableTypeType) 

    /**
     * Method unmarshal
     *
     *
     *
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException,
            org.exolab.castor.xml.ValidationException {
        return (grms_schema.Executable) Unmarshaller.unmarshal(
            grms_schema.Executable.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     *
     */
    public void validate() throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 
}
