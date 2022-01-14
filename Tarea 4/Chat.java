import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.lang.*;


class Chat {

  static byte[] recibe_mensaje(MulticastSocket socket, int longitud_mensaje) throws IOException {
      byte[] buffer = new byte[longitud_mensaje];
      DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
      socket.receive(paquete);
      return paquete.getData();
  }//fin de recibe mensaje

  static void envia_mensaje_multicast(byte[] buffer, String ip, int puerto) throws IOException {
      DatagramSocket socket = new DatagramSocket();
      socket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), puerto));
      socket.close();
  }//fin de envia mensaje

  static class Worker extends Thread {
        public void run() {
            //En un ciclo se recibirán los mensajes enviados al
            //grupo 230.0.0.0 a través del puerto 10000 y se desplegarán en la pantalla.
            for (;;) {
                try {
                    InetAddress grupo = InetAddress.getByName("230.0.0.0");
                    MulticastSocket socket = new MulticastSocket(10000);
                    socket.joinGroup(grupo);
                    byte[] a = recibe_mensaje(socket, 100);
                    System.out.println(new String(a, "UTF-8"));
                    socket.leaveGroup(grupo);
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }//fin de Worker

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println("Se debe pasar como parametro el nombre del usuario");
            System.exit(1);
        }

        new Worker().start();
        String nombre = args[0];
        BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
        //En un ciclo infinito se leerá cada mensaje del teclado y se enviará el mensaje al
        //grupo 230.0.0.0 a través del puerto 10000
        for (;;) {
            String msg = b.readLine();
            String salida = nombre +":"+ msg;
            envia_mensaje_multicast(salida.getBytes(), "230.0.0.0", 10000);
        }
    }//fin de main
}
