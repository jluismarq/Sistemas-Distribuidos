
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lamport {

   static String[] hosts;
   static int[] puertos;
   static int num_nodos, nodo;
   static long reloj_logico;
   static Object lock = new Object();

   public static class Reloj extends Thread {

      @Override
      public void run() {
         try {
            for (;;) {
               synchronized (lock) {
                  System.out.println("reloj logico: " + reloj_logico);
                  switch (nodo) {
                     case 0:
                        reloj_logico += 4;
                        break;
                     case 1:
                        reloj_logico += 5;
                        break;
                     case 2:
                        reloj_logico += 6;
                        break;
                  }
               }
               Thread.sleep(1000);
            }
         } catch (InterruptedException e) {

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
         System.out.println("Inicio el thread Worker");
         DataInputStream entrada;
         try {
            entrada = new DataInputStream(conexion.getInputStream());
            long tiempo_recibido = entrada.readLong();
            synchronized(lock){
               if(tiempo_recibido>reloj_logico){
                  reloj_logico=tiempo_recibido+1;
               }
            }
            conexion.close();
         } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
         }
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
   
   public static void envia_mensaje(long tiempo_logico, String host, int puerto) throws Exception  {
      Socket conexion;
      for(;;){
         try {
            conexion = new Socket(host, puerto);
            break;
         } catch (Exception e) {
            Thread.sleep(100);
         }
      }
      try {
         DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
         salida.writeLong(tiempo_logico);
      } finally{
         conexion.close();
      }
   }

   public static void main(String[] args) throws InterruptedException, IOException, Exception {
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
      envia_mensaje(0, hosts[0], puertos[0]);
      envia_mensaje(0, hosts[1], puertos[1]);
      envia_mensaje(0, hosts[2], puertos[2]);
      new Reloj().start();
      Thread.sleep(2000);
      envia_mensaje(reloj_logico, hosts[0], puertos[0]);
      
      s.join();
   }
}
