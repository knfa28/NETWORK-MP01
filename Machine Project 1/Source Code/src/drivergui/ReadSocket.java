package drivergui;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;

public class ReadSocket extends Thread {
    private Socket sock;
    private Server server;
    private DataInputStream dis;	
    private StringBuffer ibuffer;
    private String[] inputParameters, inputParameters2;
    private String tempString;
    private String followerUsername = "", message;
    private byte[] IMGbyteArray = new byte[65500];
    private byte[] inputByteArray;
    private byte[] stringByteArray, tempByteArray;
    private int i, j;
    private FileOutputStream fos;
    private ArrayList<String> approvedUsernames;
    private ArrayList<String> approvedClients;
    private networkGUI network;
    private String textString;
    public ReadSocket(Socket sock, Server server, networkGUI network) {
        this.sock = sock;
        this.server = server;
        this.network = network;
        
        try {		
            dis = new DataInputStream(sock.getInputStream());
            //tempString = dis.readUTF();
        } catch(Exception e) {
            e.printStackTrace();    
        }
    }
        
    /*public synchronized void readSocket() throws Exception {
        int c;
        ibuffer = new StringBuffer();
	while( (c = dis.read()) != 13)
            ibuffer.append((char)c);		
    }*/
        
    public void run() {
        while(true) {		
            try{
                stringByteArray = new byte[65500];//reset everything
                IMGbyteArray = new byte[65500];
                inputByteArray = new byte[120000];    
                int bytesRead = 0;
                int tempRead;
                
                
                do{ 
                tempRead= dis.read(inputByteArray, bytesRead, inputByteArray.length - bytesRead);
                if(tempRead >= 0)
                bytesRead += tempRead;
                }while(tempRead != -1);
                
                int i = 0, j = 0;
                
                while(inputByteArray[i] != ' ' && inputByteArray[i] != '\0'){
                    stringByteArray[i] = inputByteArray[i];
                    bytesRead--;
                    i++; 
                }
                    
                i++;
                tempString = new String(stringByteArray);
                tempString = tempString.trim();
                
                switch(tempString){
                    case "\"FOLLOW\"": stringByteArray = new byte[65500];
                                           
                                       if(inputByteArray[i] != '\0'){
                                           while(inputByteArray[i] != '\0'){
                                               stringByteArray[j] = inputByteArray[i];
                                               bytesRead--;
                                               i++; j++;
                                           }
                                            
                                           i++; j = 0;
                                               
                                           followerUsername = new String(stringByteArray);
                                           followerUsername = followerUsername.trim();                                                

                                           if(inputByteArray[i] != '\0'){
                                               for(int k = 0; k < bytesRead - 1; k++){
                                                   IMGbyteArray[j] = inputByteArray[i];
                                                   i++; j++;
                                               }

                                               fos = new FileOutputStream("src/" + followerUsername + ".jpg");
                                               BufferedOutputStream bos = new BufferedOutputStream(fos);
                                               bos.write(IMGbyteArray, 0, bytesRead - 1);
                                               bos.close();
                                           }
                                                      
                                       }
                                       
                                           if(followerUsername.equals("")){
                                               followerUsername = sock.getInetAddress().getHostAddress();
                                           }
                                           server.addPendingUsernames(followerUsername, sock);
                                           System.out.println(followerUsername + " sent a follow request!");
                                           network.setImage(IMGbyteArray);
                                           textString = followerUsername + " sent a follow request!";
                                           network.addToList(textString);
                                           break;

                                        
                    case "\"UNFOLLOW\"": System.out.println(followerUsername + " unfollowed you.");
                                         server.deleteFollower(followerUsername, sock);
                                         textString = followerUsername + " unfollowed you.";
                                           network.addToList(textString);
                                         sock.close();
                                         this.stop();
                                         break;
                        
                    case "\"APPROVE\"": stringByteArray = new byte[65500];

                                        while(inputByteArray[i] != '\0' && inputByteArray[i] != '\4' && inputByteArray[i] != ' '){
                                            stringByteArray[j] = inputByteArray[i];
                                            bytesRead--;
                                            i++; j++; 
                                        }
                                        
                                        i++; j = 0;
                                        
                                        followerUsername = new String(stringByteArray);
                                        followerUsername = followerUsername.trim();

                                        if(inputByteArray[i] != '\0'){
                                            for(int k = i; k < bytesRead - 1; k++){
                                                IMGbyteArray[j] = inputByteArray[i];
                                                i++; j++;
                                            }

                                            FileOutputStream fos = new FileOutputStream("src/" + followerUsername + ".jpg");
                                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                                            bos.write(IMGbyteArray, 0, bytesRead - 1);
                                            bos.close();
                                        }
                                        this.approvedClients = server.getApprovedClients();
                                        this.approvedUsernames = server.getApprovedUsernames();
                                        
                                        if(approvedUsernames.contains(this.followerUsername))
                                        this.followerUsername = approvedUsernames.get(approvedClients.indexOf(sock.getInetAddress().getHostAddress()));
                                        server.addApprovedUsername(followerUsername, sock);
                                        System.out.println(followerUsername + " approved your follow request!");
                                        network.setImage(IMGbyteArray);
                                        textString = followerUsername + " approved your follow request!";
                                           network.addToList(textString);
                                        sock.close();
                                        this.stop();
                                        break; 
                                                                       
                    case "\"POST\"": stringByteArray = new byte[65500];
                                    
                                     while(inputByteArray[i] != '\0' && inputByteArray[i] != '\4'){
                                         stringByteArray[j] = inputByteArray[i];
                                         bytesRead--;
                                         i++; j++; 
                                     }
                                         
                                     i++; j = 0;

                                     message = new String(stringByteArray);
                                     message = message.trim();
                                         
                                     if(inputByteArray[i] != '\0'){
                                         for(int k = i; k < bytesRead - 1; k++){
                                             IMGbyteArray[j] = inputByteArray[i];
                                             i++; j++;
                                         }

                                         fos = new FileOutputStream("src/" + followerUsername + ".jpg");
                                         BufferedOutputStream bos = new BufferedOutputStream(fos);
                                         bos.write(IMGbyteArray, 0, bytesRead - 1);
                                         bos.close();
                                     }
                                     this.approvedClients = server.getApprovedClients();
                                        this.approvedUsernames = server.getApprovedUsernames();
                                        this.followerUsername = approvedUsernames.get(approvedClients.indexOf(sock.getInetAddress().getHostAddress()));   
                                     System.out.println(followerUsername + " sent a post!");
                                     System.out.println("POST:" + message);
                                     network.setImage(IMGbyteArray);
                                     textString = followerUsername + " sent a post!" + '\n' + "POST: " + message;
                                           network.addToList(textString);
                                     sock.close();
                                     this.stop();
                                     break;                                                                          
                                        
                    case "\"PM\"": stringByteArray = new byte[65500];
                                                                    
                                   while(inputByteArray[i] != '\0' && inputByteArray[i] != '\4'){
                                        stringByteArray[j] = inputByteArray[i];
                                        i++; j++;bytesRead--;
                                   }
                                   
                                   i++;j=0;bytesRead--;
                                   
                                   if(inputByteArray[i] != '\0'){
                                       for(int k = 0; k < bytesRead - 1; k++){
                                           IMGbyteArray[j] = inputByteArray[i];
                                           i++; j++;
                                       }
                                          
                                       fos = new FileOutputStream("src/" + sock.getInetAddress().getHostAddress() + ".jpg");
                                       BufferedOutputStream bos = new BufferedOutputStream(fos);
                                       bos.write(IMGbyteArray, 0, bytesRead - 1);
                                       bos.close();                                          
                                    }
                                   
                                    message = new String(stringByteArray);
                                    message = message.trim();
                                    System.out.println(sock.getInetAddress().getHostAddress() + " sent you a PM!");
                                    System.out.println("PM: " + message);
                                    followerUsername = sock.getInetAddress().getHostAddress();
                                    textString = followerUsername + " sent you a PM!" + '\n' + "PM: " + message;
                                           network.addToList(textString);
                                    network.setImage(IMGbyteArray);
                                    break;
                                        
                    case "\"IMG\"": if(inputByteArray[i] != '\0'){
                                        for(int k = 0; k < bytesRead - 1; k++){
                                            IMGbyteArray[j] = inputByteArray[i];
                                            i++; j++;
                                        }
                                        this.approvedClients = server.getApprovedClients();
                                        this.approvedUsernames = server.getApprovedUsernames();
                                        this.followerUsername = approvedUsernames.get(approvedClients.indexOf(sock.getInetAddress().getHostAddress()));
                                        fos = new FileOutputStream("src/" + followerUsername + ".jpg");
                                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                                        bos.write(IMGbyteArray, 0, bytesRead - 1);
                                        bos.close();
                                        System.out.println(followerUsername + " updated his profile picture!");
                                        
                                        textString = followerUsername + " updated his profile picture!";
                                           network.addToList(textString);
                                        network.setImage(IMGbyteArray);
                                    }
                                    sock.close();
                                    this.stop();
                                    break;
                            
                    case "\"FILE\"": stringByteArray = new byte[65500];                                    
                                     int FileCounter = 0;
                                        
                                     while(inputByteArray[i] != '\0' && inputByteArray[i] != '\4' && inputByteArray[i] != ' '){
                                        stringByteArray[j] = inputByteArray[i];
                                        bytesRead--;
                                        i++; j++;
                                     }
                                         
                                     bytesRead--;
                                     i++; j = 0;
                                        
                                     tempString = new String(stringByteArray);
                                     tempString = tempString.trim();

                                     if(inputByteArray[i] != '\0'){
                                         for(int k = 0; k < bytesRead - 1; k++){
                                             IMGbyteArray[j] = inputByteArray[i];
                                             FileCounter++;
                                             i++; j++; 
                                         }
                                         this.approvedClients = server.getApprovedClients();
                                        this.approvedUsernames = server.getApprovedUsernames();
                                        this.followerUsername = approvedUsernames.get(approvedClients.indexOf(sock.getInetAddress().getHostAddress()));
                                         System.out.println(FileCounter);
                                         fos = new FileOutputStream("src/" + tempString);
                                         BufferedOutputStream bos = new BufferedOutputStream(fos);
                                         bos.write(IMGbyteArray, 0, bytesRead - 1);
                                         bos.close();
                                         System.out.println(followerUsername + " sent you a file.");
                                     }
                                     textString = followerUsername + " sent you a file!";
                                           network.addToList(textString);
                                     break;
                                                                   
                    default: 
                            
                             String s = new String(inputByteArray);
                            
                             
                             System.out.println("ERROR:" + s);
                             System.out.println(sock.getInetAddress().getHostAddress() + " sent an unknown command");                                       
                }  
            } catch(Exception e){
                e.printStackTrace();
                this.stop();
            }finally{
                this.stop();
            }
        }
    }
    public Socket getSocket(){
        return this.sock;
    }
        
    public String getUsername(){
        if(followerUsername.equals(""))
            return sock.getInetAddress().getHostAddress();
        
        return this.followerUsername;
    }
    
    public byte[] concatenateByteArrays(byte[] a, byte[] b){
        byte[] result = new byte[a.length + b.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length); 
        
        return result;
    }  
}
