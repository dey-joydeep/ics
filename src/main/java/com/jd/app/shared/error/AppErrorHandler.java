package com.jd.app.shared.error;

import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.jd.app.modules.SharedBean;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@ControllerAdvice
@RestController
public class AppErrorHandler extends ResponseEntityExceptionHandler {

	// API
	// 400
	/**
	 * @param ex
	 * @param request
	 * @return result
	 */
	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> handleBadRequest(final ConstraintViolationException ex, final WebRequest request) {
		SharedBean bean = new SharedBean();
		Map<String, String> errMap = new HashMap<>();
		ex.getConstraintViolations().iterator().forEachRemaining(v -> {
			String porpertName = v.getPropertyPath().toString();
			errMap.put(extractName(porpertName), v.getMessage());
		});

		bean.setErrors(errMap);
		return new ResponseEntity<>(bean, new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		Map<String, String> errMap = new HashMap<>();
		BindingResult result = ex.getBindingResult();
		SharedBean bean = (SharedBean) result.getTarget();
		result.getFieldErrors().forEach(e -> {
			errMap.put(e.getField(), e.getDefaultMessage());
		});
		bean.setErrors(errMap);
		return new ResponseEntity<>(bean, new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	/**
	 * @param ex
	 * @param request
	 * @return result
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> handleInternalError(final Exception ex, final WebRequest request) {
		final String bodyOfResponse = "System error occurred";
		log.error("System error occurred", ex);
		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
				request);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		log.info(ex.getRequestURL() + " not found", ex);
		return handleExceptionInternal(ex, ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}

	protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		final String bodyOfResponse = "Cannot read the request body. Request content might be empty or not parsable";
		// ex.getCause() instanceof JsonMappingException, JsonParseException //
		// for additional information later on
		log.debug("System error occurred.", ex);
		return handleExceptionInternal(ex, bodyOfResponse, headers, HttpStatus.BAD_REQUEST, request);
	}

	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		SharedBean bean = new SharedBean();
		bean.setSuccess(false);
		bean.setMessage(body != null ? body.toString() : ex.getLocalizedMessage());
		return super.handleExceptionInternal(ex, bean, headers, status, request);
	}

	private static String extractName(String key) {
		return key.substring(key.lastIndexOf('.') + 1);
	}
}
