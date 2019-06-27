package br.com.rnegrao.testepoc.host.handler;

import br.com.rnegrao.testepoc.application.exception.SemResultadoException;
import br.com.rnegrao.testepoc.application.exception.ServiceException;
import br.com.rnegrao.testepoc.host.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler{

	@ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> getInfraException(Exception ex, WebRequest request) {
		this.logger.error("Ocorreu um erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }

    @ExceptionHandler({ServiceException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<?> getServiceException(ServiceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse(ex.getHttpStatus(), ex.getMessage()));
    }

    @ExceptionHandler({SemResultadoException.class})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> getSemResultadoException(SemResultadoException ex) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ErrorResponse(ex.getHttpStatus(), ex.getMessage()));
    }
}