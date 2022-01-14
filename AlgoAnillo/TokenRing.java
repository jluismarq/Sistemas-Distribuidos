
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenRing {

   static String[] hosts;
   static int[] puertos;
   static int num_nodos, nodo;
   static boolean canBlock = false, isBlock = false;

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

   public static class Worker extends Thread {

      Socket conexion;

      Worker(Socket conexion) {
         this.conexion = conexion;
      }

      @Override
      public void run() {
         try {
            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            long token = entrada.readLong();
            canBlock=true;
            Thread.sleep(500);
            if (isBlock) {
               System.out.println("El nodo: " + nodo + " adquirio el bloqueo");
//               Thread.sleep(3000);
               for(;;){
                  System.out.print("");
                  if (!isBlock) {
                  System.out.println("El nodo: " + nodo + " libero el bloqueo");
                  enviaToken(token, hosts[(nodo + 1) % 3], puertos[(nodo + 1) % 3]);
                  System.out.println(nodo+" envio");
                  break;
               }
               }

            } else {
               enviaToken(token, hosts[(nodo + 1) % 3], puertos[(nodo + 1) % 3]);
            }

            conexion.close();
         } catch (Exception e) {
         } finally {
            canBlock=false;
         }
      }
   }

   public static void enviaToken(long tok, String host, int puerto) throws Exception {
      Socket conexion;
      for (;;) {
         try {
            conexion = new Socket(host, puerto);
            break;
         } catch (Exception e) {
            Thread.sleep(100);
         }
      }
      try {
         DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
         salida.writeLong(tok);
      } finally {
         conexion.close();
      }
   }

   public static void main(String[] args) throws Exception {
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
      if (nodo == 0) {
         enviaToken(1, hosts[1], puertos[1]);
      }
      Thread.sleep(3000);
      //bloqueo
      for (;;) {
         if (canBlock) {
            isBlock = true;
            System.out.println("ya puede bloquear");
            break;
         }
         Thread.sleep(1000);
      }
//      System.out.println("El nodo: " + nodo + " adquirio el bloqueo");
      Thread.sleep(7000);
      //desbloqueo
      isBlock=false;
   }
}
