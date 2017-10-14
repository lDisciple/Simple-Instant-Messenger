/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author JBotha
 */
public class ChatClient extends JFrame{
private Socket connection;
private ServerSocket server;
private ObjectOutputStream outStream;
private ObjectInputStream inStream;
private String message = "";
private String serverAddress;

private JTextField txfUserChat;
private JTextArea txaChatWindow;

    public ChatClient(String ip){
         super("Instant Messenger Client");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         serverAddress = ip;
         
         txfUserChat = new JTextField ("Enter Messages Here");
         txfUserChat.setEnabled(false);
         txfUserChat.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent ev){
                 sendMessage(txfUserChat.getText());
                 txfUserChat.setText("");
             }
         });
         add(txfUserChat, BorderLayout.NORTH);
         
         txaChatWindow = new JTextArea("Welcome to Instant Messanger:\n");
         txaChatWindow.setEditable(false);
         add(new JScrollPane(txaChatWindow),BorderLayout.CENTER);
         
         setSize(500, 400);
    }

    public void startRunning(){
        try {
            
            connectToServer();
            setupStreams();
            whileChatting();
            txfUserChat.setEnabled(false);
        } catch (EOFException eof) {
            showMessage("You left.");
        }catch (IOException io) {
            io.printStackTrace();
        }finally{
            cleanUp();
        }
    }
    
    public void connectToServer(){
        showMessage("Attempting to connect to " + serverAddress + ":8488");
        boolean connected = false;
        do{
            try{
                connection = new Socket(InetAddress.getByName(serverAddress),8488);
                System.out.println(connection.getInetAddress().getHostAddress());
                System.out.println(connection.isBound());
                connected = true;
            }catch(IOException io){
                connected = false;
            }
        }while(!connected);
        
    }
    
    public void setupStreams() throws IOException{
         outStream = new ObjectOutputStream(connection.getOutputStream());
         outStream.flush();
         inStream = new ObjectInputStream(connection.getInputStream());
     }
     
     public void whileChatting() throws IOException{
         showMessage("You are now connected.");
         txfUserChat.setEnabled(true);
         do {             
             try {
                message = (String) inStream.readObject();
                showMessage(message);
             } catch (ClassNotFoundException ex) {
                 showMessage("Cannot send that!");
             }
         } while (!message.equals("SERVER - END") && connection.isConnected() == true);
         showMessage("Connection Lost.");
     }
              
     public void sendMessage(String message){
         try {
             outStream.writeObject("CLIENT - " + message);
             outStream.flush();
             showMessage("CLIENT - " + message);
         } catch (IOException e) {
             showMessage("Cannot send that message.");
         }
     }
    
    public void showMessage(String text){
         txaChatWindow.append(text + "\n");
     }
     
     public void cleanUp(){
        try {
            inStream.close();
            outStream.close();
            connection.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
         
     }
     
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChatClient client = new ChatClient(JOptionPane.showInputDialog(null, "Enter an IP address:"));
        client.setVisible(true);
        client.startRunning();
    }
}
