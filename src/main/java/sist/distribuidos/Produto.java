package sist.distribuidos;
import java.io.Serializable;

public class Produto implements Serializable {

	public static final long serialVersionUID = 1L;

	private int id;
	private String nome;
	private double preco;

	public Produto(){

	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return this.id;
	}

	public void setNome(String nome){
		this.nome = nome;
	}

	public String getNome(){
		return this.nome;
	}

	public void setPreco(double preco){
		this.preco = preco;
	}

	public double getPreco(){
		return this.preco;
	}

}
