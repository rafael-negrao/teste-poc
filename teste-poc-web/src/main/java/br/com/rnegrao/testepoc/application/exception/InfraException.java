package br.com.rnegrao.testepoc.application.exception;

import org.springframework.http.HttpStatus;

public class InfraException extends BaseException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
