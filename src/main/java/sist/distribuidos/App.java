package sist.distribuidos;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.WatchedEvent;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

//implementando o Watcher 
public class App implements Watcher {

    static String host = "127.0.0.1";
	static ZooKeeper zk = null;
	static Integer mutex;
	static int clienteId = 1;
	static int pedidoId = 1;

	String root;

	App(String address){
		try{
			zk = new ZooKeeper(address, 3000, this);
			mutex = new Integer(-1);
		}catch(IOException e){
			System.out.println(e.toString());
		}
	}

	synchronized public void process(WatchedEvent event){
		synchronized (mutex){
			mutex.notify();
		}
    }
    






    //Criação da Fila de pedidos, criando um znode novo 
    static public class Queue extends App {

		Queue(String address, String name){

			super(address);
            this.root = name;
            			
			if(zk != null){
				try{
					zk.create("/"+root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}catch(KeeperException e){
					System.out.println("Keeper exception when instantiating queue: " + e.toString());
				}catch(InterruptedException e){
					System.out.println("Interrupted exception.");
				}
			}

        }
        
        boolean produce(Pedido pedido) throws KeeperException, InterruptedException {

			byte b[] = null;

			try{

				// Converte o objeto Pedido em array de bytes
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
				objectOutputStream.writeObject(pedido);
				objectOutputStream.close();
				byteArrayOutputStream.close();
				b = byteArrayOutputStream.toByteArray();

			}catch(IOException e){
				e.printStackTrace();
			}

			zk.create("/"+root+"/pedido", b, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

			return true;

        }
        
		// Remove o processo (pedido) da fila, calcular o preço total e exibe os dados do pedido.
		Pedido consume() throws KeeperException, InterruptedException {

			Pedido pedido = null;
			Stat stat = null;

			while(true){

				synchronized(mutex){

					List<String> list = zk.getChildren("/"+root, true);

					if(list.size() == 0){
						System.out.println("Não temos pedidos no momento.");
						mutex.wait();
					}else{

						Integer min = new Integer(list.get(0).substring(7));
						String minString = list.get(0);
						for(String s : list){
							Integer tempValue = new Integer(s.substring(7));
							if(tempValue < min){
								min = tempValue;
								minString = s;
							}
						}

						byte b[] = zk.getData("/"+root+"/"+minString, false, stat);
						zk.delete("/"+root+"/"+minString, 0);

						try{

							
							ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b);
							ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
							pedido = (Pedido) objectInputStream.readObject();
							objectInputStream.close();
							byteArrayInputStream.close();
							return pedido;

						}catch(IOException e){
							e.printStackTrace();
						}catch(ClassNotFoundException e){
							e.printStackTrace();
						}

					}

				}

			}

		}

    }



    public static void main(String args[]) throws Exception {

		Produto cardapio[] = new Produto[5];

		cardapio[0] = new Produto();
		cardapio[0].setId(1);
		cardapio[0].setNome("Bolinho de Chocolate");		
		cardapio[0].setPreco(4.00);

		cardapio[1] = new Produto();
		cardapio[1].setId(2);
		cardapio[1].setNome("Tortinha de Morango");
		cardapio[1].setPreco(5.00);

		cardapio[2] = new Produto();
		cardapio[2].setId(3);
		cardapio[2].setNome("Sonho");
		cardapio[2].setPreco(3.50);

		cardapio[3] = new Produto();
		cardapio[3].setId(4);
		cardapio[3].setNome("Mousse de Maracuja");
		cardapio[3].setPreco(6.50);

		cardapio[4] = new Produto();
		cardapio[4].setId(5);
		cardapio[4].setNome("Torta Holandesa");
		cardapio[4].setPreco(9.00);


		Queue queue = new Queue(host, "doceria");
		Scanner in = new Scanner(System.in);
        int op  = 0;
        
        while(op != 3){

			System.out.printf("\n");
			System.out.println("--------------------------------------------------------------");
			System.out.println("*                     DOCERIA                                *");
			System.out.println("*        1) Faça seu Pedido                                  *");
			System.out.println("*        2) Entregar Pedido                                  *");
			System.out.println("*        3) Sair                                             *");
			System.out.println("*                                                            *");
			System.out.println("--------------------------------------------------------------");
			System.out.print(": ");
			op = Integer.parseInt(in.nextLine());

			if(op == 1){

				System.out.printf("\n");
				System.out.println("Cadastro do Cliente \n");

				Cliente cliente = new Cliente();
				cliente.setId(clienteId);

				System.out.print("Nome: ");
				cliente.setNome(in.nextLine());

				System.out.print("CPF: ");
				cliente.setCpf(in.nextLine());

				System.out.print("Telefone: ");
				cliente.setTelefone(in.nextLine());

				System.out.println("******* Endereço do Cliente \n");

				Endereco endereco = new Endereco();

				System.out.print("Rua: ");
				endereco.setRua(in.nextLine());

				System.out.print("Número: ");
				endereco.setNumero(in.nextLine());

				System.out.print("CEP: ");
				endereco.setCep(in.nextLine());

				cliente.setEndereco(endereco);

				Pedido pedido = new Pedido();
				pedido.setId(pedidoId);
				pedido.setCliente(cliente);
				pedido.setProdutos(new ArrayList<Produto>());
				pedido.setPrecoTotal(0);

				String op2 = "";

				while(!op2.equals("f") && !op2.equals("F")){

					System.out.printf("\n");
					System.out.println("******* Pedido \n");
					System.out.println("Informe o número do produto á ser adicionado no pedido: ");

					for(int i=0;i<5;i++){
						System.out.println("\tID: "+cardapio[i].getId()+" Nome: "+cardapio[i].getNome()
							+" Preço: "+cardapio[i].getPreco());
					}

					System.out.println("Digite (F) para finalizar o pedido.");
					System.out.print("///:> ");
					op2 = in.nextLine();

					if(!op2.equals("f") && !op2.equals("F")){
						pedido.getProdutos().add(cardapio[Integer.parseInt(op2)-1]);
					}

				}

				System.out.printf("\n");
				System.out.print("Deseja efetivar esta operação? (S) ou (N): ");

				String op3 = in.nextLine();

				if(op3.equals("s") || op3.equals("S")){
					clienteId++;
					pedidoId++;
					queue.produce(pedido);
				}

			}else if(op == 2){

				System.out.printf("\n");
				System.out.println("******* Pedido Entregue \n\n");

				Pedido pedido = queue.consume();

				System.out.println("ID do Pedido: "+pedido.getId());
				System.out.println("Nº Cliente: "+pedido.getCliente().getId());
				System.out.println("Nome: "+pedido.getCliente().getNome());
				System.out.println("CPF: "+pedido.getCliente().getCpf());
				System.out.println("Telefone: "+pedido.getCliente().getTelefone());
				System.out.println("Rua: "+pedido.getCliente().getEndereco().getRua());
				System.out.println("Número: "+pedido.getCliente().getEndereco().getNumero());
				System.out.println("CEP: "+pedido.getCliente().getEndereco().getCep());				
				System.out.println("Produtos: ");

				for(Produto p : pedido.getProdutos()){

					System.out.println("\tID: "+p.getId()+" Nome: "+p.getNome()
						+" Preço: "+p.getPreco());

					pedido.setPrecoTotal(pedido.getPrecoTotal() + p.getPreco());

				}

				System.out.println("Preço Total: "+pedido.getPrecoTotal());
				in.nextLine();

			}

		}

		System.out.println("Sair");

	}
    












    
}
