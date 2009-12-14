package org.gridlab.gat.io;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains informations about a file (e.g. isDir, isFile,
 * size, name, ...)
 * @author Bastian Boegel, University of Ulm, Germany, 2009
 *
 */
public class FileInfo {

	/**
	 * Enum for the file types (file, directory, soft link)
	 */
	public enum FileType {
		file,
		directory,
		softlink
	} // public enum FileType

	/**
	 * Enum for the file permissions (read, write, execute)
	 */
	public enum FilePermission {
		read,
		write,
		execute
	} // public enum FilePermission
	
	/**
	 * File size.
	 */
	private long size = 0;
	
	/**
	 * File/directory name.
	 */
	private String name = null;
	
	/**
	 * The type of the file.
	 * @see FileType
	 */
	private FileType fileType = null;

	/**
	 * Date of file.
	 */
	private String date = null;
	
	/**
	 * Time of file.
	 */
	private String time = null;
	
	/**
	 * File permissions of the user.
	 */
	private Set<FilePermission> permissionUser = new HashSet<FilePermission>();
	
	/**
	 * File permissions of the group.
	 */
	private Set<FilePermission> permissionGroup = new HashSet<FilePermission>();
	
	/**
	 * File permissions for all.
	 */
	private Set<FilePermission> permissionAll = new HashSet<FilePermission>(); 
	
	/**
	 * The name of the file.
	 * @param name
	 */
	public FileInfo(String name) {
		this.name = name;
	} // public FileInfo(String name)
	
	/**
	 * Setter for the file size.
	 * @param size Size to set.
	 */
	public void setSize(long size) {
		this.size = size;
	} // public void setSize(long size)

	/**
	 * Setter for the file type.
	 * @param fileType The file type to set.
	 * @see FileType
	 */
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	} // public void setFileType(FileType fileType)
	
	/**
	 * Setter for date and time.
	 * @param date
	 * @param time
	 */
	public void setDateTime(String date, String time) {
		this.date = date;
		this.time = time;
	} // public void setDateTime(String date, String time)
	
	/**
	 * Setter for the user permissions.
	 * @param canRead User can read.
	 * @param canWrite User can write.
	 * @param canExecute User can execute.
	 */
	public void setUserPermissions(boolean canRead, boolean canWrite, boolean canExecute) {
		permissionUser.clear();
		if (canRead) {
			permissionUser.add(FilePermission.read);
		}
		if (canWrite) {
			permissionUser.add(FilePermission.write);
		}
		if (canExecute) {
			permissionUser.add(FilePermission.execute);
		}
	} // public void setUserPermissions(boolean canRead, boolean canWrite, boolean canExecute)
	
	/**
	 * Setter for the group permissions.
	 * @param canRead Group can rean.
	 * @param canWrite Group can write.
	 * @param canExecute Group can execute.
	 */
	public void setGroupPermissions(boolean canRead, boolean canWrite, boolean canExecute) {
		permissionGroup.clear();
		if (canRead) {
			permissionGroup.add(FilePermission.read);
		}
		if (canWrite) {
			permissionGroup.add(FilePermission.write);
		}
		if (canExecute) {
			permissionGroup.add(FilePermission.execute);
		}
	} // public void setGroupPermissions(boolean canRead, boolean canWrite, boolean canExecute)
	
	/**
	 * Setter for the permissions for all.
	 * @param canRead All can read.
	 * @param canWrite All can write.
	 * @param canExecute All can execute.
	 */
	public void setAllPermissions(boolean canRead, boolean canWrite, boolean canExecute) {
		permissionAll.clear();
		if (canRead) {
			permissionAll.add(FilePermission.read);
		}
		if (canWrite) {
			permissionAll.add(FilePermission.write);
		}
		if (canExecute) {
			permissionAll.add(FilePermission.execute);
		}
	} // public void setAllPermissions(boolean canRead, boolean canWrite, boolean canExecute)
	
	/**
	 * Getter for the name.
	 * @return {@link #name}.
	 */
	public String getName() {
		return name;
	} // public String getName()

	/**
	 * Returns if current file is a directory.
	 * @return True if it is a directory, otherwise false.
	 */
	public boolean isDir() {
		return (fileType == null?false:fileType.equals(FileType.directory));
	} // public boolean isDir()
	
	/**
	 * Returns if the current file is a real file.
	 * @return True if it is a real file, otherwise false.
	 */
	public boolean isFile() {
		return (fileType == null?false:fileType.equals(FileType.file));
	} // public boolean isFile()
	
	/**
	 * Returns if the current file is a soft link.
	 * @return True if it is a soft link, otherwise false.
	 */
	public boolean isSoftLink() {
		return (fileType == null?false:fileType.equals(FileType.softlink));
	} // public boolean isSoftLink()
	
	/**
	 * Getter for the file size.
	 * @return {@link #size}.
	 */
	public long getSize() {
		return size;
	} // public long getSize()
	
	/**
	 * Getter for date.
	 * @return {@link #date}.
	 */
	public String getDate() {
		return date;
	} // public String getDate()
	
	/**
	 * Getter for time.
	 * @return {@link #time}.
	 */
	public String getTime() {
		return time;
	} // public String getTime()
	
	/**
	 * Returns if a user has read access to that file.
	 * @return True if the user has read access, otherwise false.
	 */
	public boolean userCanRead() {
		return permissionUser.contains(FilePermission.read);
	} // public boolean userCanRead()
	
	/**
	 * Returns if a user has write access to that file.
	 * @return True if the user has write access, otherwise false.
	 */
	public boolean userCanWrite() {
		return permissionUser.contains(FilePermission.write);
	} // public boolean userCanWrite()
	
	/**
	 * Returns if a user has the permission to execute this file.
	 * @return True if the user can execute this file, otherwise false.
	 */
	public boolean userCanExecute() {
		return permissionUser.contains(FilePermission.execute);
	} // public boolean userCanExecute()
	
	/**
	 * Returns if the group has read access to this file.
	 * @return True if the group has read access, otherwise false.
	 */
	public boolean groupCanRead() {
		return permissionGroup.contains(FilePermission.read);
	} // public boolean groupCanRead()
	
	/**
	 * Returns if the group has write access to this file.
	 * @return True if the group has write access, otherwise false.
	 */
	public boolean groupCanWrite() {
		return permissionGroup.contains(FilePermission.write);
	} // public boolean groupCanWrite()
	
	/**
	 * Returns if the group has the permission to execute this file.
	 * @return True if the group can execute this file.
	 */
	public boolean groupCanExecute() {
		return permissionGroup.contains(FilePermission.execute);
	} // public boolean groupCanExecute()
	
	/**
	 * Returns if all can read this file.
	 * @return True if the file is readable for all, otherwise false.
	 */
	public boolean allCanRead() {
		return permissionAll.contains(FilePermission.read);
	} // public boolean allCanRead()
	
	/**
	 * Returns if all have write access to the file.
	 * @return True if all can write to this file, otherwise false.
	 */
	public boolean allCanWrite() {
		return permissionAll.contains(FilePermission.write);
	} // public boolean allCanWrite()
	
	/**
	 * Returns if all have the permission to execute this file.
	 * @return True if all can execute this file.
	 */
	public boolean allCanExecute() {
		return permissionAll.contains(FilePermission.execute);	
	} // public boolean allCanExecute()
	
	
} // public class FileInfo
