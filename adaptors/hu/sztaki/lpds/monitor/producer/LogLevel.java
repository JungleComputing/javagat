package hu.sztaki.lpds.monitor.producer;

/**
 * The <code>LogLevel</code> class defines the log levels used by
 * the logging service.
 *
 * @author G??bor Gomb??s
 * @version $Id$
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public final class LogLevel {
    /* The codes must match the C interface and the definition of
     * the gridlab.logging metric */
    private static final int PROD_GRIDLOG_DEBUG = 0;

    private static final int PROD_GRIDLOG_INFO = 1;

    private static final int PROD_GRIDLOG_NOTICE = 2;

    private static final int PROD_GRIDLOG_WARNING = 3;

    private static final int PROD_GRIDLOG_ERROR = 4;

    private static final int PROD_GRIDLOG_CRITICAL = 5;

    /** Names of the log levels. The indexes must match the above codes. */
    private static final String[] names = { "DEBUG", "INFO", "NOTICE",
        "WARNING", "ERROR", "CRITICAL" };

    /**
     * Debug message.
     * The message is useful for debugging suspected problems but likely
     * not useful during normal operation.
     */
    public static final LogLevel DEBUG = new LogLevel(PROD_GRIDLOG_DEBUG);

    /**
     * Informational message.
     * The message describes a normal and usually insignificant condition.
     */
    public static final LogLevel INFO = new LogLevel(PROD_GRIDLOG_INFO);

    /**
     * Important message.
     * The message describes an important but normal event.
     */
    public static final LogLevel NOTICE = new LogLevel(PROD_GRIDLOG_NOTICE);

    /**
     * Warning message.
     * The message describes an exceptional condition that was handled
     * without loss of functionality.
     */
    public static final LogLevel WARNING = new LogLevel(PROD_GRIDLOG_WARNING);

    /**
     * Error message.
     * The message desrcibes an error condition that prevents some operation
     * from being performed or completed, but the overall availability of
     * the service is not affected.
     */
    public static final LogLevel ERROR = new LogLevel(PROD_GRIDLOG_ERROR);

    /**
     * Critical error message.
     * The message describes a critical condition that affects the general
     * availability of the service.
     */
    public static final LogLevel CRITICAL = new LogLevel(PROD_GRIDLOG_CRITICAL);

    /** The internal code of the severity object. */
    private int code;

    /**
     * Creates a new log level object.
     *
     * @param code                the internal code of the log level. Must match
     *                        the C code and the definition of the
     *                        <tt>gridlab.logging</tt> metric.
     */
    private LogLevel(int code) {
        this.code = code;
    }

    /**
     * Returns the internal code of the log level.
     *
     * @return                the internal code.
     */
    int getCode() {
        return code;
    }

    /**
     * Converts a log level to a string.
     *
     * @return                the stringified log level.
     */
    public String toString() {
        return names[code];
    }

    /**
     * Converts a numeric log level to a string.
     *
     * @param level                the numeric level.
     * @return                the stringified log level.
     *
     * @throws IllegalArgumentException if the log level is invalid.
     */
    public static String toString(Integer level) {
        String name;

        try {
            /* This might throw an OutOfBoundsException */
            name = names[level.intValue()];
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown log level");
        }

        return name;
    }
}
