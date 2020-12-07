package sist.distribuidos;

import java.io.Serializable;

public class Endereco implements Serializable {

	public static final long serialVersionUID = 1L;

	private String rua;
	private String numero;
	private String cep;


	public Endereco(){

	}

	public void setRua(String rua){
		this.rua = rua;
	}

	public String getRua(){
		return this.rua;
	}

	public void setNumero(String numero){
		this.numero = numero;
	}

	public String getNumero(){
		return this.numero;
	}

	public void setCep(String cep){
		this.cep = cep;
	}

	public String getCep(){
		return this.cep;
	}


}