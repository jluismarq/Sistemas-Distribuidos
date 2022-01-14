import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Matriz {
    static int nodo;
    static String ip;
    static int N = 10;
    static Object lock = new Object();

    long checksum = 0;

    //Declaramos las matrices originales
    static long[][] A = new long[N][N];
    static long[][] B = new long[N][N];
    static long[][] C = new long[N][N];

    static void imprimeMatriz(long[][] M, int col, int fil) {
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < fil; j++) {
                System.out.print(M[i][j] + "\t ");
            }
            System.out.println("");
        }
        System.out.println("");
    }

    static long calcularChecksum(long[][] M, int N) {
        long aux = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                aux += M[i][j];
            }
        }
        return aux;
    }

    static long[][] recibirMatriz(DataInputStream entrada, int fil, int col, int inicioFil, int inicioCol)
    throws Exception {
        long[][] aux = new long[col][fil];

        for (int i = inicioCol; i < col; i++) {
            for (int j = inicioFil; j < fil; j++) {
                aux[i][j] = entrada.readLong();
            }
        }
        return aux;
    }

    static void enviarMatriz(DataOutputStream salida, long[][] M, int fil, int col) throws Exception {
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < fil; j++) {
                salida.writeLong(M[i][j]);
            }
        }

        salida.flush();
    }
    static long[][] dividirMatriz(int inicioCol, int col, int inicioFil, int fil, boolean Matriz) {
        long[][] aux = new long[N / 2][N];

        for (int i = inicioCol; i < col; i++) {
            for (int j = inicioFil; j < fil; j++) {
                if (Matriz) {
                    aux[i - inicioCol][j] = A[i][j];
                } else {
                    aux[i - inicioCol][j] = B[i][j];
                }
            }
        }

        return aux;
    }

    static void agregarFragmento(long[][] M, int inicioFil, int inicioCol, int fil, int col) {
        for (int i = inicioCol; i < col; i++) {
            for (int j = inicioFil; j < fil; j++) {
                C[i][j] = M[i][j];
            }
        }
    }

    static long[][] transponerMatriz(long[][] M, int N) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < i; j++) {
                long aux = M[i][j];
                M[i][j] = M[j][i];
                M[j][i] = aux;
            }
        }
        return M;
    }

    static class Worker extends Thread {
        Socket conexion;

        public Worker(Socket conexion) {
            this.conexion = conexion;
        }

        public void run() {
            try {
                long[][] auxMA = new long[N / 2][N];
                long[][] auxMB = new long[N / 2][N];
                //Creamos la transmisión de entrada para recibir las matrices
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                // Creamos la transmisión de salida para enviar la matrices
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
                //Recibe en la variable x el nodo
                int x = entrada.readInt();
                // Enviar las matrices dependiendo el nodo
                if (x == 1) {
                    System.out.println("\nSe conecto el nodo 1 envio la matriz A1 y B1");
                    auxMA = dividirMatriz(0, N / 2, 0, N, true);
                    auxMB = dividirMatriz(0, N / 2, 0, N, false);
                } else if (x == 2) {
                    System.out.println("\nSe conecto el nodo 2 envio la matriz A1 y B2");
                    auxMA = dividirMatriz(0, N / 2, 0, N, true);
                    auxMB = dividirMatriz(N / 2, N, 0, N, false);
                } else if (x == 3) {
                    System.out.println("\nSe conecto el nodo 3 envio la matriz A2 y B1 ");
                    auxMA = dividirMatriz(N / 2, N, 0, N, true);
                    auxMB = dividirMatriz(0, N / 2, 0, N, false);
                } else if (x == 4) {
                    System.out.println("\nSe conecto el nodo 4 envio la matriz A2 y B2 ");
                    auxMA = dividirMatriz(N / 2, N, 0, N, true);
                    auxMB = dividirMatriz(N / 2, N, 0, N, false);
                }

                enviarMatriz(salida, auxMA, N, N / 2);
                enviarMatriz(salida, auxMB, N, N / 2);

                synchronized(lock) {
                    long[][] matrizAux;
                    if (x == 1) {
                        matrizAux = recibirMatriz(entrada, N / 2, N / 2, 0, 0);
                        agregarFragmento(matrizAux, 0, 0, N / 2, N / 2);
                    } else if (x == 2) {
                        matrizAux = recibirMatriz(entrada, N, N / 2, N / 2, 0);
                        agregarFragmento(matrizAux, N / 2, 0, N, N / 2);
                    } else if (x == 3) {
                        matrizAux = recibirMatriz(entrada, N / 2, N, 0, N / 2);
                        agregarFragmento(matrizAux, 0, N / 2, N / 2, N);
                    } else if (x == 4) {
                        matrizAux = recibirMatriz(entrada, N, N, N / 2, N / 2);
                        agregarFragmento(matrizAux, N / 2, N / 2, N, N);
                    }
                }
                // Mensaje de término de conexión
                System.out.print("Termina conexion con el nodo: " + x);
                // Cerramos la conexion y los flujos de entrada y salida
                entrada.close();
                salida.close();
                conexion.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Se debe pasar como parametros el numero del nodo y la IP");
            System.exit(1);
        }

        nodo = Integer.valueOf(args[0]);
        ip = args[1];

        if (nodo == 0) {
            // Inicializar matrices A, B, C
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    A[i][j] = i + 2 * j;
                    B[i][j] = i - 2 * j;
                    C[i][j] = 0;
                }
            }

            B = transponerMatriz(B, N);

            // Mensaje del nodo 0 de espera
            System.out.println("\nEsperando por conexiones...");
            // creamos el servidor
            ServerSocket servidor = new ServerSocket(5000);
            //Aceptaremos los 4 clientes
            Worker[] w = new Worker[4];

            for (int i = 0; i < 4; ++i) {
                // Una conexión por cada nodo
                Socket conexion = servidor.accept();
                w[i] = new Worker(conexion);
                w[i].start();
            }
            // Esperamos a que se ejecute el hilo
            for (int i = 0; i < 4; ++i) {
                w[i].join();
            }
            //Mensaje del nodo 0
            System.out.println("\nHe dejado de recibir conexiones... ");
            //Cerramos el servidor
            servidor.close();
            //Calculamos el checksum
            System.out.println("Checksum: " + calcularChecksum(C, N));
            //Si N ==10 imprimimos las matrices
            if (N == 10) {
                System.out.println("Desplegando matriz C = A x B");
                System.out.println("\nMatriz A:");
                imprimeMatriz(A, N, N);
                System.out.println("\nMatriz B:");
                imprimeMatriz(B, N, N);
                System.out.println("\nMatriz C");
                imprimeMatriz(C, N, N);
            }

        } else {
            // Creamos la conexión con el socket
            Socket conexion = new Socket(ip, 5000);
            // Creamos las transmisiones de entrada y salida
            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            // Enviamos al servidor (nodo 0) el numero de nodo en el que estamos
            salida.writeInt(nodo);
            // Declaramos las matrices donde se recibirán las originales
            long[][] auxMA = recibirMatriz(entrada, N, N / 2, 0, 0);
            long[][] auxMB = recibirMatriz(entrada, N, N / 2, 0, 0);
            // Declaramos la matriz en donde guardaremos el producto de A1 x B1
            long[][] auxMC = new long[N / 2][N / 2];

            // Multiplicar matriz
            for (int i = 0; i < (N / 2); i++) {
                for (int j = 0; j < (N / 2); j++) {
                    long suma = 0;
                    for (int k = 0; k < N; k++) {
                        suma += auxMA[i][k] * auxMB[j][k];
                        auxMC[i][j] = suma;
                    }
                }
            }
            // Regresar fragmento
            enviarMatriz(salida, auxMC, N / 2, N / 2);
            // cerramos las transmisiones de entrada, salida y la conexión
            entrada.close();
            salida.close();
            conexion.close();
        }
    }
}
