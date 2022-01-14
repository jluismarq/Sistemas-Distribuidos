
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

enum Estado {
   NORMAL, ESPERANDO, ADQUIRIDO;
}

public class Ricart extends Thread {

   static String[] hosts;
   static int[] puertos;
   static int numNodos, nodo, numOkRecibidos;
   static long relojLogico, tiempoLogicoEnviado;
   static Object lock = new Object();
   static Estado e = Estado.NORMAL;

   static Queue<Integer> cola = new LinkedList<>();

   public static class Reloj extends Thread {

      @Override
      public void run() {
         try {
            for (;;) {
               synchronized (lock) {
                  System.out.println("reloj logico: " + relojLogico);
                  switch (nodo) {
                     case 0:
                        relojLogico += 4;
                        break;
                     case 1:
                        relojLogico += 5;
                        break;
                     case 2:
                        relojLogico += 6;
                        break;
                  }
               }
               Thread.sleep(1000);
            }
         } catch (InterruptedException e) {

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

   public static class Worker extends Thread {

      Socket conexion;

      Worker(Socket conexion) {
         this.conexion = conexion;
      }

      @Override
      public void run() {
         DataInputStream entrada;
         try {
            entrada = new DataInputStream(conexion.getInputStream());
            String cmd = entrada.readUTF();
            System.out.println("Comando: " + cmd);
            if (!cmd.equals("HOLA")) {
               if (cmd.equals("PETICION")) {
                  int idRecurso = entrada.readInt();
                  int nodoRecibido = entrada.readInt();
                  long tiempoRecibido = entrada.readLong();
                  System.out.println("nodo Recibido: " + nodoRecibido);
                  System.out.println("tiempo Recibido: " + tiempoRecibido);
                  System.out.println("Reloj logico: " + relojLogico);
                  System.out.println("estado: " + e);

                  lamport(tiempoRecibido);
                  switch (e) {
                     case NORMAL:
                        synchronized (lock) {
                           ok(relojLogico, hosts[nodoRecibido], puertos[nodoRecibido]);
                        }
                        break;
                     case ADQUIRIDO:
                        cola.add(nodoRecibido);
                        break;
                     case ESPERANDO:
                        if (tiempoLogicoEnviado < tiempoRecibido) {
                           cola.add(nodoRecibido);
                        } else if (tiempoLogicoEnviado > tiempoRecibido) {
                           synchronized (lock) {
                              ok(relojLogico, hosts[nodoRecibido], puertos[nodoRecibido]);
                           }
                        } else if (nodo < nodoRecibido) {
                           cola.add(nodoRecibido);
                        } else {
                           synchronized (lock) {
                              ok(relojLogico, hosts[nodoRecibido], puertos[nodoRecibido]);
                           }
                        }
                        break;
                  }
               } else if (cmd.equals("OK")) {
                  numOkRecibidos++;
                  long tiempoRecibido = entrada.readLong();
                  lamport(tiempoRecibido);
                  if (numOkRecibidos == numNodos - 1) {
                     System.out.println("Adquirio Recurso");
                     e = Estado.ADQUIRIDO;
                  }
               }
            }
         } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
         }
      }
   }

   public static void lamport(long tiempoRecibido) {
      synchronized (lock) {
         if (tiempoRecibido > relojLogico) {
            relojLogico = tiempoRecibido + 1;
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

   public static void ok(long tiempo, String host, int puerto) throws Exception {
      Socket conexion = new Socket(host, puerto);
      try {
         DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
         salida.writeUTF("OK");
         salida.writeLong(tiempo);
      } finally {
         conexion.close();
      }
   }

   public static void peticion(int id, int nodoAct, long tiempo, String host, int puerto) throws Exception {
      Socket conexion = new Socket(host, puerto);
      try {
         DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
         salida.writeUTF("PETICION");
         salida.writeInt(id);
         salida.writeInt(nodo);
         salida.writeLong(tiempo);
      } finally {
         conexion.close();
      }
   }

   public static void bloquea() throws Exception {
      System.out.println("Bloquea");
      e = Estado.ADQUIRIDO;
      numOkRecibidos = 0;
      synchronized (lock) {
         tiempoLogicoEnviado = relojLogico;
      }
      for (int x = 0; x < numNodos; x++) {
         if (x != nodo) {
            peticion(1, nodo, tiempoLogicoEnviado, hosts[x], puertos[x]);
         }
      }
   }

   public static void desbloquea() throws Exception {
      System.out.println("Desbloquea");
      e = Estado.NORMAL;
      while (!cola.isEmpty()) {
         int nodo = cola.poll();
         synchronized (lock) {
            ok(relojLogico, hosts[nodo], puertos[nodo]);
         }
      }
   }

   public static void main(String[] args) throws InterruptedException, IOException, Exception {
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
      //ver si esta activo
      for (int x = 0; x < numNodos; x++) {
         if (x != nodo) {
            esperaConexion(hosts[nodo], puertos[nodo]);
         }
      }
      new Reloj().start();
      Thread.sleep(1000);
      bloquea();
      while (e != Estado.ADQUIRIDO) {
         Thread.sleep(100);
      }
      Thread.sleep(3000);
      desbloquea();

      s.join();
   }
}
