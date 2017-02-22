package drivergui;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;

public class Driver {
    private BufferedReader isr;
    private String input, fileLocation;
    private ArrayList<Client> myClients = new ArrayList();
    private Scanner s = new Scanner(System.in);
    private ArrayList<String> pendingClients = new ArrayList();
    private ArrayList<String> approvedClients = new ArrayList();
    private ArrayList<String> pendingUsernames = new ArrayList();
    private ArrayList<String> approvedUsernames = new ArrayList();
    private String myUsername = "EH";
    private String message = "YOU ARE CONNECTED TO KURT PC";
    private DataOutputStream dos;
    private Socket tempSocket;
    private ArrayList<InetAddress> IP = new ArrayList();
    private char[] postMessage = new char[256];
    private char[] tempChar;
    private ReadSocket rs;
    private int intInput;
    private Client myClient;
    private File newFile, myImage;
    private byte[] fileByte;

    public static void main(String argv[]) throws Exception{
        Driver start = new Driver();
        start.run();
    }
    
    class MyThread extends Thread{
        public void run(){
            try{
                
        myImage = new File("src/bente.jpg");
        byte[] myImg = new byte[(int) myImage.length() + 1];
            Socket socket = new Socket("127.0.0.1", 1234);
            
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            dos.write(("GET / HTTP/1.1\n" +
"Host: 127.0.0.1:1234\n" +
"Connection: keep-alive\n" +
"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n" +
"Upgrade-Insecure-Requests: 1\n" +
"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36\n" +
"Accept-Encoding: gzip, deflate, sdch\n" +
"Accept-Language: en-US,en;q=0.8\n").getBytes());
            
            
            dis.read(myImg);
            dis.close();
            dos.flush();
            dos.close();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    
    public void run(){
        networkGUI network = new networkGUI();
        network.setVisible(true);
        network.setUsername(myUsername);
        myImage = new File("src/bente.jpg");
        byte[] myImg = new byte[(int) myImage.length() + 1];
        int unsuccessful = 0;
        try{
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myImage));
            bis.read(myImg, 0, myImg.length);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Server server= new Server(myUsername, myImg, network);
       
        try{
            server.start();
            
            for(int i = 0; i < 100; i++)
        {
               MyThread mythread = new MyThread();
               mythread.start();
               System.out.println(i);
        }
            myClient = new Client("", myUsername, network);
            myClients.add(myClient);

            myClient.sendFollowRequest(myImg);
            server.setApprovedClients(approvedClients);
            server.setApprovedUsernames(approvedUsernames);
            server.setPendingClients(pendingClients);
            server.setPendingUsernames(pendingUsernames);       
        }catch(Exception e){
            e.printStackTrace();
        }
        
        while(true){
            try{
               
                isr = new BufferedReader(new InputStreamReader(System.in));
                input = isr.readLine();
                
                String[] inputParameters = input.split(" ");
                System.out.println(inputParameters[0]);
                switch(inputParameters[0]){
                    case "\"FOLLOW\"": //List<String> notifList = new ArrayList<String>();
                                       System.out.println("FOLLOW who?");
                                       //notifList.add("FOLLOW who?");
                                       input = s.nextLine();
                                       Client client = new Client(input, myUsername, network); 
                                       myClients.add(client);
                                       client.start();
                                       client.sendFollowRequest(myImg);
                                       break;
                        
                    case "\"POST\"": System.out.println("POST what?");
                                     message = s.nextLine();
                                     server.postToClients(message, myImg);
                                     break;
                    
                    case "\"APPROVE\"": System.out.println("APPROVE WHO?");
                                        if(pendingUsernames.size() != 0){
                                            pendingClients = server.getPendingClients();
                                            pendingUsernames = server.getPendingUsernames();
                                            approvedClients = server.getApprovedClients();
                                            approvedUsernames = server.getApprovedUsernames();
                                            
                                            System.out.println("You have " + pendingUsernames.size() + " pending followers. Let whom connect to you?");
                                            for(int i = 0; i < pendingUsernames.size(); i++)
                                                System.out.println(i + 1 + ". " + pendingUsernames.get(i));                                           
                                            input = s.nextLine();
                                            System.out.println("You have sent an approve request to " + pendingUsernames.get(Integer.parseInt(input) - 1) + "!");
                                            approvedClients.add(pendingClients.get(Integer.parseInt(input) - 1));
                                            approvedUsernames.add(pendingUsernames.get(Integer.parseInt(input) - 1));
                                            server.sendApproveRequest(pendingClients.get(Integer.parseInt(input) - 1));
                                            pendingClients.remove(pendingClients.get(Integer.parseInt(input) - 1));
                                            pendingUsernames.remove(pendingUsernames.get(Integer.parseInt(input) - 1));
                                            
                                            server.setApprovedClients(approvedClients);
                                            server.setPendingClients(pendingClients);
                                            server.setApprovedUsernames(approvedUsernames);
                                            server.setPendingUsernames(pendingUsernames);
                                        }else System.out.println("You have no pending followers.");
                                        break;
                    
                    case "\"PM\"": System.out.println("PM who?");
                                   input = s.nextLine();
                                   Client senderClient = new Client(input, myUsername, network);
                                   
                                   System.out.println("Send what?");
                                   input = s.nextLine();
                                   senderClient.sendPM(input, myImg);
                                   senderClient.closeConnection();
                                   break;
                        
                    case "\"UNFOLLOW\"": System.out.println("UNFOLLOW who?");
                    
                                         if(myClients.size() == 0)
                                             System.out.println("No one is following you.");
                                         
                                         for(int i = 0; i < myClients.size(); i ++)
                                            System.out.println(i+1 + ". " + myClients.get(i).getConnectedAddress()); 

                                         input = s.nextLine();
                                         System.out.println("You have unfollowed " + myClients.get(Integer.parseInt(input) - 1).getConnectedAddress());
                                         myClients.get(Integer.parseInt(input) - 1).sendUnfollowCommand();
                                         myClients.get(Integer.parseInt(input) - 1).closeConnection();
                                         myClients.remove(Integer.parseInt(input) - 1);
                                         break;
                    
                    case "\"IMG\"": System.out.println("Update to what image?");
                                    input = s.nextLine();
                                    myImage = new File(input);
                                    myImg = new byte[(int) myImage.length() + 1];

                                    try{
                                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myImage));
                                        bis.read(myImg, 0, myImg.length);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                    server.sendIMG(myImg);
                                    break;                       
                    
                    case "\"FILE\"": System.out.println("location of the file");
                                     fileLocation = s.nextLine();
                                     
                                     System.out.println("Send what file?");
                                     input = s.nextLine();
                                     newFile = new File(fileLocation+input);
                                     fileByte = new byte[(int) newFile.length()];
                                     
                                     System.out.println(fileByte.length);
                                     if(newFile.length() > 65500)
                                        System.out.println("FILE TOO BIG");
                                    
                                     try{
                                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(newFile));
                                        bis.read(fileByte, 0, fileByte.length);
                                     }catch(Exception e){
                                        e.printStackTrace();
                                     }
                                     
                                     server.sendFile(input, fileByte);
                                     break;
                        
                    default: System.out.println("NO SUCH COMMAND.");
                             break;                                       
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public byte[] concatenateByteArrays(byte[] a, byte[] b){
        byte[] result = new byte[a.length + b.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length); 
        
        return result;
    }  
}

