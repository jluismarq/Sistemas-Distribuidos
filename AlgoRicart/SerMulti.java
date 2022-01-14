import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerMulti {

   static String[] hosts;
   static int[] puertos;
   static int num_nodos,nodo;

   public static class Worker extends Thread {

      Socket conexion;

      Worker(Socket conexion) {
         this.conexion = conexion;
      }

      @Override
      public void run() {
         System.out.println("Inició el thread Worker");
      }
   }

   public static class Servidor extends Thread {

      @Override
      public void run() {
         try {
            ServerSocket servidor = new ServerSocket(puertos[nodo]);
            for (;;) {
               Socket conexion = servidor.accept();
               Worker w = new Worker(conexion);
               w.start();
            }
         } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
         }
      }

   }

   public static void main(String[] args) {
      nodo = Integer.valueOf(args[0]);
      num_nodos = args.length - 1;
      hosts = new String[num_nodos];
      puertos = new int[num_nodos];
      for (int x = 0; x < num_nodos; x++) {
         String[] aux = args[x + 1].split(":");
         hosts[x] = aux[0];
         puertos[x] = Integer.valueOf(aux[1]);
      }
      Servidor s = new Servidor();
      s.start();
   }
}
