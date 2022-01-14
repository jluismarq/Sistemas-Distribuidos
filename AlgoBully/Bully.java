
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bully {

   static String[] hosts;
   static int[] puertos;
   static int numNodos, nodo, coordinadorActual;

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
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            String mensaje = entrada.readUTF();
            if (mensaje.equals("ELECCION")) {
               salida.writeUTF("OK");
               eleccion(nodo);
            } else if (mensaje.equals("COORDINADOR")) {
               coordinadorActual = entrada.readInt();
            }
         } catch (Exception e) {
            System.err.println(e);
         }
      }
   }

   public static void esperaConexion(String host, int puerto) throws Exception {
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
         salida.writeUTF("HOLA");
      } finally {
         conexion.close();
      }
   }

   public static String envia_mensaje_eleccion(String host, int puerto) {
      try {
         Socket conexion = new Socket(host, puerto);
         try {
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            salida.writeUTF("ELECCION");
            String recibido = entrada.readUTF();
            return recibido;
         } finally {
            conexion.close();
         }
      } catch (Exception e) {
         return "";
      }
   }

   public static void envia_mensaje_coordinador(String host, int puerto) {
      try {
         Socket conexion = new Socket(host, puerto);
         try {
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            salida.writeUTF("COORDINADOR");
            salida.writeInt(nodo);
         } finally {
            conexion.close();
         }
      } catch (Exception e) {
      }
   }

   public static void eleccion(int nodo) {
      String respuesta="";
      for(int x=nodo+1,y=0;x<numNodos;x++,y++){
         respuesta = envia_mensaje_eleccion(hosts[x], puertos[x]);
         if(respuesta.equals("OK")){
            return;
         }
      }
      for(int x=0;x<nodo;x++)
         envia_mensaje_coordinador(hosts[x], puertos[x]);
   }

   public static void main(String[] args) throws Exception {
      nodo = Integer.valueOf(args[0]);
      numNodos = args.length - 1;
      hosts = new String[numNodos];
      puertos = new int[numNodos];
      for (int x = 0; x < numNodos; x++) {
         String[] aux = args[x + 1].split(":");
         hosts[x] = aux[0];
         puertos[x] = Integer.valueOf(aux[1]);
      }
      Servidor s = new Servidor();
      s.start();

      for (int x = 0; x < numNodos; x++) {
         if (x != nodo) {
            esperaConexion(hosts[nodo], puertos[nodo]);
         }
      }
      Thread.sleep(3000);
      if (nodo == 7) {
         System.exit(0);
      }
      if (nodo == 4) {
         eleccion(nodo);
      }
      s.join();
   }
}
