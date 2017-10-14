/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author JBotha
 */
public class ChatServer extends JFrame{
private Socket[] connections;
private ServerSocket server;
private ObjectOutputStream[] outStream;
private ObjectInputStream[] inStream;
private int numConnected = 0;

private JTextField txfUserChat;
private JTextArea txaChatWindow;
    /**
     * @param args the command line arguments
     */

     
     public ChatServer(){
         super("IM Server");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         txfUserChat = new JTextField ("Enter Messages Here");
         txfUserChat.setEnabled(false);
         txfUserChat.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent ev){
                 sendMessage(txfUserChat.getText());
                 txfUserChat.setText("");
             }
         });
         add(txfUserChat, BorderLayout.NORTH);
         
         txaChatWindow = new JTextArea("IM server started:\n");
         txaChatWindow.setEditable(false);
         add(new JScrollPane(txaChatWindow),BorderLayout.CENTER);
         
         setSize(500, 400);
     }
     
     public void startRunning(){
         connections = new Socket[1];
         inStream = new ObjectInputStream[1];
         outStream = new ObjectOutputStream[1];
         
    try {
        server = new ServerSocket(8488, 100);
        showMessage("Server running on " + getExtIP() + ":8488");
        while(true){
            try {
                if (numConnected == 0) {
                    txfUserChat.setEnabled(false);
                }
                checkForConnections();
                setupStreams();
                whileChatting();
            } catch (EOFException eof) {
                showMessage(connections[0].getInetAddress().getHostAddress() + " left.");
            } finally {
            cleanUp();
            }
        }
    } catch (IOException ex) {
        ex.printStackTrace();
        showMessage("Server already running!");
    }
     }
     
     public void checkForConnections() throws IOException{
         showMessage("Waiting for connection...");
         connections[0] = server.accept();
         showMessage("Connecting: " + connections[0].getInetAddress().getHostAddress());
     }
     
     public void setupStreams() throws IOException{
         outStream[0] = new ObjectOutputStream(connections[0].getOutputStream());
         outStream[0].flush();
         inStream[0] = new ObjectInputStream(connections[0].getInputStream());
     }
     
     public void whileChatting(){
         String message = "Connected: " + connections[0].getInetAddress().getHostAddress();
         boolean connected = true;
         
         sendMessage(message);
         txfUserChat.setEnabled(true);
         do {             
             try {
                message = (String) inStream[0].readObject();
                showMessage(message);
             } catch (ClassNotFoundException ex) {
                 showMessage("Cannot send that!");
             }catch (IOException io){
                 showMessage ("Connection lost.");
                 connected = false;
             }
         } while (!message.equals("CLIENT - END") && connected == true);
     }
              
     public void sendMessage(String message){
         try {
             outStream[0].writeObject("SERVER - " + message);
             outStream[0].flush();
             showMessage("SERVER - " + message);
         } catch (IOException e) {
             showMessage("Cannot send that message.");
         }
     }
     
     public void showMessage(String text){
         txaChatWindow.append(text + "\n");
     }
     
     public void cleanUp(){
    try {
        inStream[0].close();
        outStream[0].close();
        connections[0].close();
    } catch (IOException ex) {
        Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
    }
         
     }
     
     public static String getExtIP () throws IOException{
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

        String ip = in.readLine(); //you get the IP as a String
        return ip;
        }
     
     public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.setVisible(true);
        chatServer.startRunning();
    }
}
