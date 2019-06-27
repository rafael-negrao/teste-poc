package br.com.rnegrao.testepoc.host.response;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("errorResponse")
public class ErrorResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer codigo;

    private String mensagem;

    public ErrorResponse(){}
    
    public ErrorResponse(HttpStatus httpStatus, String mensagem) {
		this.codigo = httpStatus.value();
		this.mensagem = mensagem;
	}

	public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}