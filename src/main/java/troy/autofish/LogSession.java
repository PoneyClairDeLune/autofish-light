package troy.autofish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogSession {
	private static final String logFormat = "[{}] {}";
	private static final String loggerName = "Autofish";
	private static final Logger logger = LoggerFactory.getLogger(loggerName);
	public static void error(String message) {
		logger.error(logFormat, loggerName, message);
	}
	public static void warn(String message) {
		logger.warn(logFormat, loggerName, message);
	}
	public static void info(String message) {
		logger.info(logFormat, loggerName, message);
	}
	public static void debug(String message) {
		logger.debug(logFormat, loggerName, message);
	}
}
