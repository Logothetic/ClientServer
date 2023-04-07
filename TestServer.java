import java.lang.reflect.Constructor;
import java.net.*;
import java.io.*;
import java.nio.*;


public class TestServer {
    
    private ServerSocket serverSocket;
    String dir = System.getProperty("user.dir");
    final File folder = new File(dir);

    public TestServer(ServerSocket serverSocket){
        this.serverSocket=serverSocket;
    }

    public void startServer(){
        System.out.println("Server is live");
        String s=listFilesForFolder(folder);
        // System.out.println(s);
        try {
            
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("a new client has connected!");
                
                ClientHandler clientHandler = new ClientHandler(socket,s);
                Thread thread=new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            // TODO: handle exception
        }
    }

    public void closeServerSocket(){
        try {
            if(serverSocket!=null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String listFilesForFolder(final File folder) {
        String s="";
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile() && fileEntry.getName().endsWith(".txt")) {
                s=s+(fileEntry.getName())+"\n";
            }
        }
        return s;
    }
    
    


    public static void main(String[] args)throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        TestServer server = new TestServer(serverSocket);
        server.startServer();
    }

}
