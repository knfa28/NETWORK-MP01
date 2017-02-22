package drivergui;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;

public class Client extends Thread{
    private Socket cSock;
    private DataOutputStream dos; 
    private String myUsername, connectedUsername, ipAddress, tempString;
    private DataInputStream dis;
    private networkGUI network;
    private String toNotif;
    
    public Client(String ipAddress, String myUsername, networkGUI network) throws Exception{
        Socket cSock = new Socket(ipAddress, 1234);
        this.cSock = cSock;
        this.ipAddress = ipAddress;
        this.myUsername= myUsername;
        this.connectedUsername = null;
        this.network = network;
    }
    
    public void run(){
        while(true){
            byte[] inputByteArray = new byte[65500];
            byte[] stringByteArray = new byte[65500];
            byte[] IMGbyteArray = new byte[65500];
            
            try{
                dis = new DataInputStream(cSock.getInputStream());
                int bytesRead = dis.read(inputByteArray, 0, inputByteArray.length);     
                int i = 0, j = 0;
               
                
                while(inputByteArray[i] != '\32' && inputByteArray[i] != '\0'){
                    stringByteArray[i] = inputByteArray[i];
                    bytesRead--;
                    i++; 
                }
                
                i++;
                tempString = new String(stringByteArray);
                tempString = tempString.trim();

                switch(tempString){
                    case "\"FOLLOW\"": System.out.println(cSock.getInetAddress().getHostAddress() + " sent a follow request!");
                                       toNotif = cSock.getInetAddress().getHostAddress() + " sent a follow request!";
                                       network.addToList(toNotif);
                                       break;

                    case "\"UNFOLLOW\"": System.out.println(cSock.getInetAddress().getHostAddress() + " unfollowed you."); 
                                         cSock.close();
                                         this.stop();
                                         break;

                    case "\"POST\"": String message;                       
                                     stringByteArray = new byte[65500];

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

                                        FileOutputStream fos = new FileOutputStream("src/" + connectedUsername + ".jpg");
                                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                                        bos.write(IMGbyteArray, 0, bytesRead - 1);
                                        bos.close();
                                     }

                                     System.out.println(connectedUsername + " sent a post!");
                                     System.out.println("POST:" + message);
                                     break;
                    
                    case "\"APPROVE\"": stringByteArray = new byte[65500];

                                        while(inputByteArray[i] != '\0' && inputByteArray[i] != '\4'){
                                            stringByteArray[j] = inputByteArray[i];
                                            bytesRead--;
                                            i++; j++; 
                                        }
                                        
                                        i++; j = 0;
                                        
                                        connectedUsername = new String(stringByteArray);
                                        connectedUsername = connectedUsername.trim();

                                        if(inputByteArray[i] != '\0'){
                                            for(int k = i; k < bytesRead - 1; k++){
                                                IMGbyteArray[j] = inputByteArray[i];
                                                i++; j++;
                                            }

                                            FileOutputStream fos = new FileOutputStream("src/" + connectedUsername + ".jpg");
                                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                                            bos.write(IMGbyteArray, 0, bytesRead - 1);
                                            bos.close();
                                        }
                                        
                                        System.out.println(connectedUsername + " approved your follow request!");
                                        break; 
                                                   
                    case "\"PM\"": System.out.println(cSock.getInetAddress().getHostAddress() + " sent you a PM!");
                                   break;    
                        
                    case "\"IMG\"": System.out.println(cSock.getInetAddress().getHostAddress() + " sent you an image.");
                                    break; 
                        
                    case "\"File\"": System.out.println(cSock.getInetAddress().getHostAddress() + " sent you a file.");
                                     break;  
                        
                    default: String newString = new String(inputByteArray);
                            newString.trim();
                            System.out.println(newString);
                             break;
                }
            }catch(Exception e){
                e.printStackTrace();
                this.stop();
            }
        }
    }
    
    public Socket getSocket(){
        return cSock;
    }
    
    public String getConnectedAddress(){
        return cSock.getInetAddress().getHostAddress();
    }
    
    public void setConnectedUsername(String username){
        this.connectedUsername = username;
    }
    
    public void sendUnfollowCommand(){
        String message;
        byte[] stringByteArray = new byte[65500];
        
        try{
            this.cSock = new Socket(ipAddress, 1234);
            dos = new DataOutputStream(cSock.getOutputStream());
            message = "\"UNFOLLOW\"";
            stringByteArray = message.getBytes();
            dos.write(stringByteArray);
            dos.flush();
            this.cSock.close();
            this.stop();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void closeConnection(){
        try{
            this.cSock.close();
            this.stop();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length]; 
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        
        return result;
    } 
   
    public void sendFollowRequest(byte[] myImg){
        String message;
        byte[] messageArray, tempArray, resultingArray;
        
        try{
            dos = new DataOutputStream(cSock.getOutputStream());
            message = "\"FOLLOW\" " + myUsername + '\0';
            messageArray = message.getBytes();
            tempArray = concatenateByteArrays(messageArray, myImg);
            resultingArray = tempArray;
            resultingArray[resultingArray.length - 1] = '\4';
            dos.write(resultingArray);
            dos.flush();
            this.cSock.close();
            this.stop();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendPM(String message, byte[] myImg){
        byte[] messageArray = new byte[256];
        byte[] resultingArray, tempArray;
        
        try{
            dos = new DataOutputStream(cSock.getOutputStream());
            message = "\"PM\" " + message + '\4';
            messageArray = message.getBytes();
            
            tempArray = concatenateByteArrays(messageArray, myImg);
            resultingArray = tempArray;
            resultingArray[resultingArray.length - 1] = '\4';
            
            dos.write(resultingArray, 0, resultingArray.length);
            dos.flush();
            this.cSock.close();
            this.stop();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

