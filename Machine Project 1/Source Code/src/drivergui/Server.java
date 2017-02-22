package drivergui;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;

public class Server extends Thread{
    private ArrayList<String> clientsToConnect = new ArrayList();
    private ArrayList<String> approvedClients = new ArrayList();
    private ArrayList<String> pendingUsernames = new ArrayList();
    private ArrayList<String> approvedUsernames = new ArrayList();
    private ReadSocket rs;
    private DataOutputStream dos;
    private String myUsername;
    private ServerSocket sSock;
    private byte[] myImg;
    private networkGUI network;
    private String toNotif;
    char[] chars = new char[10000];
    
    
    private String OUTPUT = "<html><head><title>Example</title></head><body><p>Worked!!!</p></body></html";
    private static final String OUTPUT_HEADERS = "HTTP/1.1 200 OK \r\n" +
                                                 "Content-Type: text/html\r\n" +
                                                 "Content-Length: ";
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";
    
    private File myImage;
    public Server(String username, byte[] myImg, networkGUI network){
        this.myUsername = username;
        //this.myImg = myImg;
        this.network = network;
        Arrays.fill(chars, 'f');
        String bigString = new String(chars);
        OUTPUT = "<html><head><title>Example</title></head><body><p>Worked!!!"+ bigString + "</p></body></html";
        myImage = new File("src/File1MB.txt");
        this.myImg = new byte[(int) myImage.length() + 1];
        int unsuccessful = 0;
        try{
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myImage));
            bis.read(myImg, 0, myImg.length);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void run(){
        try{
            sSock = new ServerSocket(1234);
            Socket cSock = null;
            ReadSocket rs = null;
            Scanner s = new Scanner(System.in);
            String ans = null;   
            BufferedWriter out = null;
            while(true){
                cSock = sSock.accept();
                
                BufferedReader br = new BufferedReader(new InputStreamReader(cSock.getInputStream()));
                
                String line = null;
                
                while((line = br.readLine()) != null){
                    System.out.println(line);
                    
                    if(line.isEmpty()){
                        break;
                    }
                }
                
                out = new BufferedWriter(
                        new OutputStreamWriter(
                            new BufferedOutputStream(cSock.getOutputStream()), "UTF-8"));
                
                dos = new DataOutputStream(cSock.getOutputStream());
                
                
                dos.write(myImg);
                dos.flush();
                dos.close();
                /*
                cSock = sSock.accept();
                boolean inApproved = false;
                boolean inToConnect = false;
                System.out.println(cSock.getInetAddress().getHostAddress() + " is trying to connect to you!");
                rs = new ReadSocket(cSock, this, network);
                rs.start();
                for(int i = 0; i < clientsToConnect.size(); i++){
                    if(clientsToConnect.get(i).equals(cSock.getInetAddress().getHostAddress())){
                        inToConnect = true;
                        
                        break;}
                }
                
                for(int i = 0; i < approvedClients.size(); i++){
                    if(approvedClients.get(i).equals(cSock.getInetAddress().getHostAddress())){
                        inApproved = true;
                        
                    break;}
                }
                
                System.out.println(inApproved + "" + inToConnect);
                
                if(!inApproved){
                    if(!inToConnect){}
                    else {
                        clientsToConnect.add(cSock.getInetAddress().getHostAddress());}
                }
                        */
            }
                        
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public ArrayList<String> getPendingClients(){
        return clientsToConnect;
    }
    
    public void setApprovedClients(ArrayList<String> approvedClients){
        this.approvedClients = approvedClients;
    }
    
    public ArrayList<String> getApprovedClients(){
        return approvedClients;
    }
    
    public void setPendingClients(ArrayList<String> clientsToConnect){
        this.clientsToConnect = clientsToConnect;
    }
            
    public void sendApproveRequest(String clientSocket) throws Exception{
        String message;
        byte[] tempArray, sentByteArray;
        message = "\"APPROVE\" " + myUsername + ' ';
        tempArray = concatenateByteArrays(message.getBytes(), myImg);
        sentByteArray = tempArray;
        sentByteArray[sentByteArray.length - 1] = '\4';
        Socket s = new Socket(clientSocket, 1234);
        dos = new DataOutputStream(s.getOutputStream());
        dos.write(sentByteArray);
        dos.flush();
        s.close();
    }
    
    public ArrayList<String> getPendingUsernames(){
        return this.pendingUsernames;
    }
    
    public void addPendingUsernames(String username, Socket sock){
        if(username.equals("")){
            pendingUsernames.add(sock.getInetAddress().getHostAddress());
            clientsToConnect.add(sock.getInetAddress().getHostAddress());
        }else if(username.equals(myUsername)){
            approvedUsernames.add(username);
            approvedClients.add(sock.getInetAddress().getHostAddress());
        }else {pendingUsernames.add(username);
                clientsToConnect.add(sock.getInetAddress().getHostAddress());}
    }
    
    public void setPendingUsernames(ArrayList<String> pendingUsernames){
        this.pendingUsernames = pendingUsernames;
    }
    
    public void setApprovedUsernames(ArrayList<String> approvedUsernames){
        this.approvedUsernames = approvedUsernames;
    }
    
    public ArrayList<String> getApprovedUsernames(){
        return this.approvedUsernames;
    }
    
    public ServerSocket getServerSocket(){
        return this.sSock;
    }
    
    public void addApprovedUsername(String username, Socket approvedSocket){
        pendingUsernames.remove(username);
        clientsToConnect.remove(approvedSocket.getInetAddress().getHostAddress());
        approvedUsernames.add(username);
        approvedClients.add(approvedSocket.getInetAddress().getHostAddress());
    }
    
    public void postToClients(String post, byte[] myImg) throws Exception{
        byte[] tempArray, sentByteArray;
        
        if(!approvedClients.isEmpty()){
            post = "\"POST\" " + post + '\4'; 
            tempArray = concatenateByteArrays(post.getBytes(), myImg);
            sentByteArray = tempArray;
            sentByteArray[sentByteArray.length - 1] = '\4';
            
            for(int i = 0; i < approvedClients.size();i++){
                Socket s = new Socket(approvedClients.get(i), 1234);
                dos = new DataOutputStream(s.getOutputStream());
                dos.write(sentByteArray);
                //post = "\"POST\" " + post + '\0';
                //sendbyteArray = post.getBytes();
                dos.flush();
                s.close();
            }
        }else System.out.println("No one has connected to you yet!");    
    }
    
    public void deleteFollower(String username, Socket sock){
        approvedClients.remove(sock.getInetAddress().getHostAddress());
        approvedClients.remove(username);
    }
    
    public void sendIMG(byte[] myImg) throws Exception{
        String message;
        byte[] tempArray, sentByteArray;
        
        if(!approvedClients.isEmpty()){
            message = "\"IMG\" ";
            tempArray = concatenateByteArrays(message.getBytes(), myImg);
            sentByteArray = tempArray;
            sentByteArray[sentByteArray.length - 1] = '\4';
            
            for(int i = 0; i < approvedClients.size();i++){
                Socket s = new Socket(approvedClients.get(i), 1234);
                dos = new DataOutputStream(s.getOutputStream());
                dos.write(sentByteArray);
                //post = "\"POST\" " + post + '\0';
                //sendbyteArray = post.getBytes();
                dos.flush();
                s.close();
            }
        }else System.out.println("No one has connected to you yet!");
    }
    
    public void sendFile(String filename, byte[] file) throws Exception{
        byte[] tempArray, sentByteArray;       
        if(!approvedClients.isEmpty()){
            filename = "\"FILE\" " + filename + " ";
            tempArray = concatenateByteArrays(filename.getBytes(), file);
            sentByteArray = tempArray;
            sentByteArray[sentByteArray.length - 1] = '\4';
            
            for(int i = 0; i < approvedClients.size();i++){
                Socket s = new Socket(approvedClients.get(i), 1234);
                dos = new DataOutputStream(s.getOutputStream());
                dos.write(sentByteArray);
                //post = "\"POST\" " + post + '\0';
                //sendbyteArray = post.getBytes();
                dos.flush();
                s.close();
            }
        }else System.out.println("No one has followed you yet!");
    }
    
    public byte[] concatenateByteArrays(byte[] a, byte[] b){
        byte[] result = new byte[a.length + b.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length); 
        
        return result;
    }
} 

