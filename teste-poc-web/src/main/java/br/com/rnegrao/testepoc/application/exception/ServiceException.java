package br.com.rnegrao.testepoc.application.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends BaseException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
