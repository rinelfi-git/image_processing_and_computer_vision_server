/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emit.ipcv;

import emit.ipcv.manager.Server;

/**
 *
 * @author rinelfi
 */
public class Launcher {
  public static void main(String[] args) {
    Thread t = new Thread(new Server(2046));
    t.start();
  }
}
