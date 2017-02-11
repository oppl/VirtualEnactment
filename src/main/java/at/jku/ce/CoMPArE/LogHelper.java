package at.jku.ce.CoMPArE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oppl on 23/11/2016.
 */
public class LogHelper {

    private static Logger logger = LoggerFactory.getLogger("VirtualEnactment");
    {

    }

    public static void logThrowable(Throwable throwable) {
        logger.error("An exception occured:" + throwable.getMessage(),
                throwable);
    }

    public static void logError(String string) {
        logger.error("There was an error while executing... Error message:\t"
                + string);
    }

    public static void logInfo(String string) {
        logger.info(string);
    }

    public static void logDebug(String string) {
        logger.info("debug :"+string);
    }

}
