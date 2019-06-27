package br.com.rnegrao.testepoc.application.exception;

import org.springframework.http.HttpStatus;

public class SemResultadoException extends BaseException {

    public SemResultadoException() {
    }

    public SemResultadoException(String message) {
        super(message);
    }

    public SemResultadoException(String message, Throwable cause) {
        super(message, cause);
    }

    public SemResultadoException(Throwable cause) {
        super(cause);
    }

    public SemResultadoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NO_CONTENT;
    }
}
