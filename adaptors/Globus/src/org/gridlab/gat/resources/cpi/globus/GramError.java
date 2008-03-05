/*
 * Created on Oct 24, 2006
 */
package org.gridlab.gat.resources.cpi.globus;

import java.util.ArrayList;

public class GramError {
    public static final int GRAM_JOBMANAGER_CONNECTION_FAILURE = 79;

    static class Descriptor {
        String shortDescription;
        String longDescription;
        
        public Descriptor(String shortDescription, String longDescription) {
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
        }
    }
    
    private static ArrayList<Descriptor> errors = new ArrayList<Descriptor>();
    
    static {
        init();
    }
    
    private static void addError(int nr, String shortDescription, String longDescription) {
        errors.add(nr, new Descriptor(shortDescription, longDescription));
    }

    private static void init() {
        addError(0, "SUCCESS", "Success");
        addError(1, "PARAMETER_NOT_SUPPORTED",
            "one of the RSL parameters is not supported");
        addError(2, "INVALID_REQUEST",
            "the RSL length is greater than the maximum allowed");
        addError(3, "NO_RESOURCES", "an I/O operation failed");
        addError(4, "BAD_DIRECTORY",
            "jobmanager unable to set default to the directory requested");
        addError(5, "EXECUTABLE_NOT_FOUND", "the executable does not exist");
        addError(6, "INSUFFICIENT_FUNDS", "of an unused INSUFFICIENT_FUNDS");
        addError(7, "AUTHORIZATION",
            "authentication with the remote server failed");
        addError(8, "USER_CANCELLED", "the user cancelled the job");
        addError(9, "SYSTEM_CANCELLED", "the system cancelled the job");
        addError(10, "PROTOCOL_FAILED", "data transfer to the server failed");
        addError(11, "STDIN_NOT_FOUND", "the stdin file does not exist");
        addError(12, "CONNECTION_FAILED",
            "the connection to the server failed (check host and port)");
        addError(13, "INVALID_MAXTIME",
            "the provided RSL 'maxtime' value is not an integer");
        addError(14, "INVALID_COUNT",
            "the provided RSL 'count' value is not an integer");
        addError(15, "NULL_SPECIFICATION_TREE",
            "the job manager received an invalid RSL");
        addError(16, "JM_FAILED_ALLOW_ATTACH",
            "the job manager failed in allowing others to make contact");
        addError(17, "JOB_EXECUTION_FAILED",
            "the job failed when the job manager attempted to run it");
        addError(18, "INVALID_PARADYN", "an invalid paradyn was specified");
        addError(19, "INVALID_JOBTYPE",
            "the provided RSL 'jobtype' value is invalid");
        addError(20, "INVALID_GRAM_MYJOB",
            " the provided RSL 'myjob' value is invalid");
        addError(21, "BAD_SCRIPT_ARG_FILE",
            "the job manager failed to locate an internal script argument file");
        addError(22, "ARG_FILE_CREATION_FAILED",
            "   the job manager failed to create an internal script argument file");
        addError(23, "INVALID_JOBSTATE",
            "the job manager detected an invalid job state");
        addError(24, "INVALID_SCRIPT_REPLY",
            "the job manager detected an invalid script response");
        addError(25, "INVALID_SCRIPT_STATUS",
            "the job manager detected an invalid script status");
        addError(26, "JOBTYPE_NOT_SUPPORTED ",
            "the provided RSL 'jobtype' value is not supported by this job manager");
        addError(27, "UNIMPLEMENTED", "not implemented");
        addError(28, "TEMP_SCRIPT_FILE_FAILED",
            "the job manager failed to create an internal script submission file");
        addError(29, "USER_PROXY_NOT_FOUND ",
            "the job manager cannot find the user proxy");
        addError(30, "OPENING_USER_PROXY",
            "the job manager failed to open the user proxy");
        addError(31, "JOB_CANCEL_FAILED",
            "the job manager failed to cancel the job as requested");
        addError(32, "MALLOC_FAILED", "system memory allocation failed");
        addError(33, "DUCT_INIT_FAILED",
            "the interprocess job communication initialization failed");
        addError(34, "DUCT_LSP_FAILED",
            "the interprocess job communication setup failed");
        addError(35, "INVALID_HOST_COUNT",
            " the provided RSL 'host count' value is invalid");
        addError(36, "UNSUPPORTED_PARAMETER",
            " one of the provided RSL parameters is unsupported");
        addError(37, "INVALID_QUEUE",
            " the provided RSL 'queue' parameter is invalid");
        addError(38, "INVALID_PROJECT",
            "the provided RSL 'project' parameter is invalid");
        addError(39, "RSL_EVALUATION_FAILED ",
            "the provided RSL string includes variables that could not be identified");
        addError(40, "BAD_RSL_ENVIRONMENT ",
            "the provided RSL 'environment' parameter is invalid");
        addError(41, "DRYRUN ",
            "the provided RSL 'dryrun' parameter is invalid");
        addError(42, "ZERO_LENGTH_RSL",
            "the provided RSL is invalid (an empty string)");
        addError(43, "STAGING_EXECUTABLE ",
            "the job manager failed to stage the executable");
        addError(44, "STAGING_STDIN ",
            "the job manager failed to stage the stdin file");
        addError(45, "INVALID_JOB_MANAGER_TYPE ",
            "  the requested job manager type is invalid");
        addError(46, "BAD_ARGUMENTS",
            "the provided RSL 'arguments' parameter is invalid");
        addError(47, "GATEKEEPER_MISCONFIGURED ",
            " the gatekeeper failed to run the job manager");
        addError(48, "BAD_RSL", "the provided RSL could not be properly parsed");
        addError(49, "VERSION_MISMATCH",
            "there is a version mismatch between GRAM components");
        addError(50, "RSL_ARGUMENTS ",
            "the provided RSL 'arguments' parameter is invalid");
        addError(51, "RSL_COUNT ",
            "the provided RSL 'count' parameter is invalid");
        addError(52, "RSL_DIRECTORY",
            "the provided RSL 'directory' parameter is invalid");
        addError(53, "RSL_DRYRUN   ",
            "the provided RSL 'dryrun' parameter is invalid");
        addError(54, "RSL_ENVIRONMENT",
            "the provided RSL 'environment' parameter is invalid");
        addError(55, "RSL_EXECUTABLE ",
            "the provided RSL 'executable' parameter is invalid");
        addError(56, "RSL_HOST_COUNT",
            "the provided RSL 'host_count' parameter is invalid");
        addError(57, "RSL_JOBTYPE ",
            "the provided RSL 'jobtype' parameter is invalid");
        addError(58, "RSL_MAXTIME ",
            "the provided RSL 'maxtime' parameter is invalid");
        addError(59, "RSL_MYJOB  ",
            "the provided RSL 'myjob' parameter is invalid");
        addError(60, "RSL_PARADYN",
            "the provided RSL 'paradyn' parameter is invalid");
        addError(61, "RSL_PROJECT",
            "the provided RSL 'project' parameter is invalid");
        addError(62, "RSL_QUEUE ",
            "the provided RSL 'queue' parameter is invalid");
        addError(63, "RSL_STDERR",
            "the provided RSL 'stderr' parameter is invalid");
        addError(64, "RSL_STDIN",
            "the provided RSL 'stdin' parameter is invalid");
        addError(65, "RSL_STDOUT",
            "the provided RSL 'stdout' parameter is invalid");
        addError(66, "OPENING_JOBMANAGER_SCRIPT",
            "the job manager failed to locate an internal script");
        addError(67, "CREATING_PIPE",
            "the job manager failed on the system call pipe()");
        addError(68, "FCNTL_FAILED",
            "the job manager failed on the system call fcntl()");
        addError(69, "STDOUT_FILENAME_FAILED",
            "the job manager failed to create the temporary stdout filename");
        addError(70, "STDERR_FILENAME_FAILED",
            "the job manager failed to create the temporary stderr filename");
        addError(71, "FORKING_EXECUTABLE",
            "the job manager failed on the system call fork()");
        addError(72, "EXECUTABLE_PERMISSIONS",
            "the executable file permissions do not allow execution");
        addError(73, "OPENING_STDOUT", "the job manager failed to open stdout");
        addError(74, "OPENING_STDERR", "the job manager failed to open stderr");
        addError(75, "OPENING_CACHE_USER_PROXY",
            "the cache file could not be opened in order to relocate the user proxy");
        addError(
            76,
            "OPENING_CACHE",
            "cannot access cache files in ~/.globus/.gass_cache, check permissions, quota, and disk space");
        addError(77, "INSERTING_CLIENT_CONTACT",
            "the job manager failed to insert the contact in the client contact list");
        addError(78, "CLIENT_CONTACT_NOT_FOUND",
            "the contact was not found in the job manager's client contact list");
        addError(
            79,
            "CONTACTING_JOB_MANAGER",
            "connecting to the job manager failed. Possible reasons: job terminated, invalid job contact, network problems, ...");
        addError(80, "INVALID_JOB_CONTACT",
            "the syntax of the job contact is invalid");
        addError(81, "UNDEFINED_EXE",
            "the executable parameter in the RSL is undefined");
        addError(82, "CONDOR_ARCH",
            " the job manager service is misconfigured. condor arch undefined");
        addError(83, "CONDOR_OS  ",
            "the job manager service is misconfigured. condor os undefined");
        addError(84, "RSL_MIN_MEMORY",
            "the provided RSL 'min_memory' parameter is invalid");
        addError(85, "RSL_MAX_MEMORY",
            "the provided RSL 'max_memory' parameter is invalid");
        addError(86, "INVALID_MIN_MEMORY",
            "the RSL 'min_memory' value is not zero or greater");
        addError(87, "INVALID_MAX_MEMORY",
            "the RSL 'max_memory' value is not zero or greater");
        addError(88, "HTTP_FRAME_FAILED",
            "the creation of a HTTP message failed");
        addError(89, "HTTP_UNFRAME_FAILED",
            "parsing incoming HTTP message failed");
        addError(90, "HTTP_PACK_FAILED",
            "the packing of information into a HTTP message failed");
        addError(91, "HTTP_UNPACK_FAILED",
            "an incoming HTTP message did not contain the expected information");
        addError(92, "INVALID_JOB_QUERY",
            "the job manager does not support the service that the client requested");
        addError(93, "SERVICE_NOT_FOUND",
            "the gatekeeper failed to find the requested service");
        addError(94, "JOB_QUERY_DENIAL ",
            "the jobmanager does not accept any new requests (shutting down)");
        addError(95, "CALLBACK_NOT_FOUND ",
            "the client failed to close the listener associated with the callback URL");
        addError(96, "BAD_GATEKEEPER_CONTACT",
            "the gatekeeper contact cannot be parsed");
        addError(97, "POE_NOT_FOUND",
            "the job manager could not find the 'poe' command");
        addError(98, "MPIRUN_NOT_FOUND",
            "the job manager could not find the 'mpirun' command");
        addError(99, "RSL_START_TIME",
            "the provided RSL 'start_time' parameter is invalid");
        addError(100, "RSL_RESERVATION_HANDLE",
            "the provided RSL 'reservation_handle' parameter is invalid");
        addError(101, "RSL_MAX_WALL_TIME",
            "the provided RSL 'max_wall_time' parameter is invalid");
        addError(102, "INVALID_MAX_WALL_TIME",
            "the RSL 'max_wall_time' value is not zero or greater");
        addError(103, "RSL_MAX_CPU_TIME   ",
            "the provided RSL 'max_cpu_time' parameter is invalid");
        addError(104, "INVALID_MAX_CPU_TIME",
            "the RSL 'max_cpu_time' value is not zero or greater");
        addError(105, "JM_SCRIPT_NOT_FOUND ",
            "the job manager is misconfigured, a scheduler script is missing");
        addError(106, "JM_SCRIPT_PERMISSIONS",
            "the job manager is misconfigured, a scheduler script has invalid permissions");
        addError(107, "SIGNALING_JOB",
            "the job manager failed to signal the job");
        addError(108, "UNKNOWN_SIGNAL_TYPE",
            "the job manager did not recognize/support the signal type");
        addError(109, "GETTING_JOBID",
            "the job manager failed to get the job id from the local scheduler");
        addError(110, "WAITING_FOR_COMMIT ",
            "the job manager is waiting for a commit signal");
        addError(111, "COMMIT_TIMED_OUT  ",
            "the job manager timed out while waiting for a commit signal");
        addError(112, "RSL_SAVE_STATE ",
            "the provided RSL 'save_state' parameter is invalid");
        addError(113, "RSL_RESTART    ",
            "the provided RSL 'restart' parameter is invalid");
        addError(114, "RSL_TWO_PHASE_COMMIT ",
            "the provided RSL 'two_phase' parameter is invalid");
        addError(115, "INVALID_TWO_PHASE_COMMIT",
            "the RSL 'two_phase' value is not zero or greater");
        addError(116, "RSL_STDOUT_POSITION ",
            "the provided RSL 'stdout_position' parameter is invalid");
        addError(117, "INVALID_STDOUT_POSITION",
            "the RSL 'stdout_position' value is not zero or greater");
        addError(118, "RSL_STDERR_POSITION",
            "the provided RSL 'stderr_position' parameter is invalid");
        addError(119, "INVALID_STDERR_POSITION",
            "the RSL 'stderr_position' value is not zero or greater");
        addError(120, "RESTART_FAILED ",
            "the job manager restart attempt failed");
        addError(121, "NO_STATE_FILE ", "the job state file doesn't exist");
        addError(122, "READING_STATE_FILE", "could not read the job state file");
        addError(123, "WRITING_STATE_FILE ",
            "could not write the job state file");
        addError(124, "OLD_JM_ALIVE ", "old job manager is still alive");
        addError(125, "TTL_EXPIRED  ", "job manager state file TTL expired");
        addError(126, "SUBMIT_UNKNOWN ",
            "it is unknown if the job was submitted");
        addError(127, "RSL_REMOTE_IO_URL ",
            "the provided RSL 'remote_io_url' parameter is invalid");
        addError(128, "WRITING_REMOTE_IO_URL",
            "could not write the remote io url file");
        addError(129, "STDIO_SIZE  ",
            "the standard output/error size is different");
        addError(130, "JM_STOPPED   ",
            "the job manager was sent a stop signal (job is still running)");
        addError(131, "USER_PROXY_EXPIRED ",
            "the user proxy expired (job is still running)");
        addError(132, "JOB_UNSUBMITTED",
            "the job was not submitted by original jobmanager");
        addError(133, "INVALID_COMMIT ",
            "the job manager is not waiting for that commit signal");
        addError(134, "RSL_SCHEDULER_SPECIFIC ",
            "the provided RSL scheduler specific parameter is invalid");
        addError(135, "STAGE_IN_FAILED",
            "the job manager could not stage in a file");
        addError(136, "INVALID_SCRATCH",
            "the scratch directory could not be created");
        addError(137, "RSL_CACHE   ",
            "the provided 'gass_cache' parameter is invalid");
        addError(138, "INVALID_SUBMIT_ATTRIBUTE",
            "the RSL contains attributes which are not valid for job submission");
        addError(139, "INVALID_STDIO_UPDATE_ATTRIBUTE",
            "the RSL contains attributes which are not valid for stdio update");
        addError(140, "INVALID_RESTART_ATTRIBUTE ",
            "the RSL contains attributes which are not valid for job restart");
        addError(141, "RSL_FILE_STAGE_IN  ",
            "the provided RSL 'file_stage_in' parameter is invalid");
        addError(142, "RSL_FILE_STAGE_IN_SHARED",
            "the provided RSL 'file_stage_in_shared' parameter is invalid");
        addError(143, "RSL_FILE_STAGE_OUT  ",
            "the provided RSL 'file_stage_out' parameter is invalid");
        addError(144, "RSL_GASS_CACHE ",
            "the provided RSL 'gass_cache' parameter is invalid");
        addError(145, "RSL_FILE_CLEANUP  ",
            "the provided RSL 'file_cleanup' parameter is invalid");
        addError(146, "RSL_SCRATCH  ",
            "the provided RSL 'scratch_dir' parameter is invalid");
        addError(147, "INVALID_SCHEDULER_SPECIFIC ",
            "the provided scheduler-specific RSL parameter is invalid");
        addError(148, "UNDEFINED_ATTRIBUTE  ",
            "a required RSL attribute was not defined in the RSL spec");
        addError(149, "INVALID_CACHE ",
            "the gass_cache attribute points to an invalid cache directory");
        addError(150, "INVALID_SAVE_STATE ",
            "the provided RSL 'save_state' parameter has an invalid value");
        addError(151, "OPENING_VALIDATION_FILE",
            "the job manager could not open the RSL attribute validation file");
        addError(152, "READING_VALIDATION_FILE",
            "the job manager could not read the RSL attribute validation file");
        addError(153, "RSL_PROXY_TIMEOUT  ",
            "the provided RSL 'proxy_timeout' is invalid");
        addError(154, "INVALID_PROXY_TIMEOUT",
            "the RSL 'proxy_timeout' value is not greater than zero");
        addError(155, "STAGE_OUT_FAILED   ",
            "the job manager could not stage out a file");
        addError(156, "JOB_CONTACT_NOT_FOUND ",
            "the job contact string does not match any which the job manager is handling");
        addError(157, "DELEGATION_FAILED  ", "proxy delegation failed");
        addError(158, "LOCKING_STATE_LOCK_FILE",
            "the job manager could not lock the state lock file");
        addError(159, "INVALID_ATTR  ",
            "an invalid globus_io_clientattr_t was used.");
        addError(160, "NULL_PARAMETER ",
            "an null parameter was passed to the gram library");
        addError(161, "STILL_STREAMING",
            "the job manager is still streaming output");
    }


    public static String getGramErrorString(int errorCode) {
        Descriptor d = (Descriptor) errors.get(errorCode); 
        
        if (d == null) {
            return "UNKNOWN_ERROR (" + errorCode + ")";
        }

        return d.shortDescription + " (" + errorCode + ")" + ": " + d.longDescription;
    }
}
