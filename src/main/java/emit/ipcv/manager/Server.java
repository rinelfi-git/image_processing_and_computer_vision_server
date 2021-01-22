/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emit.ipcv.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rinelfi
 */
public class Server implements Runnable {

  // private List<ConnectedClient> clients;
  private ServerSocket socket;
  private int port;

  public Server() {
    this.port = 23456;
  }

  public Server(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    try {
      System.out.println("[INFO] Starting image processing server");
      socket = new ServerSocket(this.port);
      System.out.println("[INFO] Service is running on port : " + this.port);
      while(true) {
        Socket client = socket.accept();
        Thread t = new Thread(new ConnectedClient(client));
        t.start();
      }
    } catch (IOException ex) {
      Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
