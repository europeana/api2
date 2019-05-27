package eu.europeana.api2.web.controller;

import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.model.json.ApiError;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class ExceptionController {

	@ExceptionHandler({TypeMismatchException.class, MissingServletRequestParameterException.class})
	@ResponseBody
	public ApiError handleMismatchException(Exception ex) {
		return new ApiError(null, "Invalid argument(s): " + ex.toString());
	}

}
