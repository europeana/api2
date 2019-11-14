package eu.europeana.api2.v2.web.controller;

import com.amazonaws.services.devicefarm.model.Problem;
import eu.europeana.api2.v2.model.EmailError;
import eu.europeana.corelib.edm.exceptions.BadDataException;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.annotation.Scheduled;

public class GlobalExceptionHandlerTest {

    private static GlobalExceptionHandler globalExceptionHandler;

    @Before
    public void setup() throws Exception {

        globalExceptionHandler = new GlobalExceptionHandler();

        EuropeanaException mongoUnreachableException = new MongoDBException(ProblemType.MONGO_UNREACHABLE);
        EuropeanaException inconsistentDataException = new BadDataException(ProblemType.INCONSISTENT_DATA);
        EuropeanaException invalidArgumentsException = new BadDataException(ProblemType.INVALID_ARGUMENTS);

        addErrorsInGlobalExceptionHandler(mongoUnreachableException , 2);
        addErrorsInGlobalExceptionHandler(inconsistentDataException , 3);
        addErrorsInGlobalExceptionHandler(invalidArgumentsException , 4);

    }

    /* Test case for sending exception email. See EA-1782
     * Assumptions: if the code reaches the sendErrorEmail method in GlobalExceptionHandler is a success scenario
     * It does't actually sends an email.
     */
    @Test
    public void sendEmailForCountLessThanThree() throws Exception{
        globalExceptionHandler.sendEmail();
    }

    private void addErrorsInGlobalExceptionHandler(EuropeanaException e, int count) {
        for(int i=1; i<=count; i++) {
            globalExceptionHandler.addErrorInList(e);
        }
    }

}