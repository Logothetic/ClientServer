import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;


class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers=new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    
    
    public ClientHandler(Socket socket){
        try {
            
            this.socket=socket;
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername=bufferedReader.readLine();
            clientHandlers.add(this);
           
            broadcastMessage("SERVER: "+this.clientUsername+" entered the chat");

        } catch (Exception e) {
            closeEverything(socket,bufferedReader,bufferedWriter);
            // TODO: handle exception
        }
    }
    
    @Override
    public void run(){
        String messageFromClient;
        
        while(socket.isConnected()){
            try {
                messageFromClient=bufferedReader.readLine();

                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket,bufferedReader,bufferedWriter);
                break; 
            }
        }
    }
    
    public void messageFromServer(String files){
        for(ClientHandler clientHandler:clientHandlers){
            try {
                clientHandler.bufferedWriter.write(files);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
                
            } catch (Exception e) {
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(ClientHandler clientHandler:clientHandlers){
            try {
                if(!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }
    }


    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: "+clientUsername+" left the chat");

    }

    public void closeEverything(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        removeClientHandler();
        try {
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();    
        }
    }


}



public class Client {
    
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
 

    public Client(Socket socket,String username){
        try {
    
            this.socket=socket;
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username=username;

        } catch (IOException e) {
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    public void sendMessage(){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend=scanner.nextLine();
                bufferedWriter.write(username + ": "+messageToSend);                
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (Exception e) {
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }



    public void listenForMessage(){
        new Thread(new Runnable(){
            public void run(){
                while(socket.isConnected()){
                    try {
                        String msgFromGroupChat=bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        closeEverything(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        try {
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();    
        }
    }


    public static void main(String[] args)throws IOException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username");
        String username=scanner.nextLine();
        Socket socket=new Socket("localhost",7777);
        Client client = new Client(socket,username);
        client.listenForMessage();
        client.sendMessage();
        scanner.close();
    }
}
