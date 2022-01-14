import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

public class Cliente
{
	//Direccion a Conectarse
	static String ip="20.115.126.251";
	//
	public static void imprimirMenu()
	{
		System.out.println("\n");
		System.out.println("a. Alta Usuario");
		System.out.println("b. Consulta Usuario");
		System.out.println("c. Borra Usuario");
		System.out.println("d. Salir");
	}
	public static void main(String[] args) throws Exception
	{
		while(true)
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			imprimirMenu();
			System.out.print("\nElige una Opcion: ");
			char opcion=br.readLine().charAt(0);
			switch(opcion)
			{
				case 'a':
					Usuario usuario=new Usuario();
					System.out.println("\tAlta Usuario");
					System.out.print("Email: ");
					usuario.email=br.readLine();
					System.out.print("Nombre: ");
					usuario.nombre=br.readLine();
					System.out.print("Apellido Paterno: ");
					usuario.apellido_paterno=br.readLine();
					System.out.print("Apellido Materno: ");
					usuario.apellido_materno=br.readLine();
					System.out.print("Fecha de Nacimiento: ");
					usuario.fecha_nacimiento=br.readLine();
					System.out.print("Telefono: ");
					usuario.telefono=br.readLine();
					System.out.print("Genero (M/F): ");
					usuario.genero=br.readLine();
					usuario.foto=null;
					alta_usuario(usuario);
					break;
				case 'b':
					System.out.println("\n\tConsulta Usuario");
					System.out.print("Ingrese el ID del Usuario: ");
          consultar_usuario(Integer.parseInt(br.readLine()));
					break;
				case 'c':
					System.out.println("\n\tBorrar Usuario");
					System.out.print("Ingrese el ID del Usuario: ");
          borrar_usuario(Integer.parseInt(br.readLine()));
					break;
				case 'd':
					br.close();
					System.out.println("\nAdios");
					System.exit(0);
					break;
				default:
					System.out.println("\n\tError");
					System.out.println("Ingrese Otra Opcion");
					break;
			}
		}
	}
	public static void alta_usuario(Usuario usuario) throws IOException
	{
		URL url=new URL("http://"+ip+":8080/Servicio/rest/ws/alta_usuario");
        HttpURLConnection conexion= (HttpURLConnection) url.openConnection();
        conexion.setDoOutput(true);
        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        GsonBuilder builder=new GsonBuilder();
        builder.serializeNulls();
        Gson gson=builder.create();
        String body=gson.toJson(usuario);
        String parametros = "usuario=" + URLEncoder.encode(body, "UTF-8");
        OutputStream os=conexion.getOutputStream();
        os.write(parametros.getBytes());
        os.flush();
        if(conexion.getResponseCode()==200)
        {
            BufferedReader br=new BufferedReader(new InputStreamReader((conexion.getInputStream())));
            String respuesta;
            while ((respuesta = br.readLine()) != null)
                System.out.println("Usuario Agregado con ID "+respuesta);
        }else{
            BufferedReader br=new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
            String respuesta;
            while ((respuesta = br.readLine()) != null)
                System.out.println(respuesta);
            throw new RuntimeException("Codigo de Error HTTP: "+conexion.getResponseCode());
        }
        conexion.disconnect();
	}
	public static void consultar_usuario(int id_usuario) throws IOException
	{
		URL url=new URL("http://"+ip+":8080/Servicio/rest/ws/consulta_usuario");
        HttpURLConnection conexion= (HttpURLConnection) url.openConnection();
        conexion.setDoOutput(true);
        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String parametros = "id_usuario=" + URLEncoder.encode(String.valueOf(id_usuario), "UTF-8");
        OutputStream os = conexion.getOutputStream();
        os.write(parametros.getBytes());
        os.flush();
        if(conexion.getResponseCode()==200)
        {
        	BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getInputStream())));
            String respuesta;
            Gson j=new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            while((respuesta = br.readLine()) != null)
            {
                Usuario user=(Usuario) j.fromJson(respuesta, Usuario.class);
                System.out.printf("%s","Nombre: ");
                System.out.println(""+user.nombre);
                System.out.printf("%s","Apellido Paterno: ");
                System.out.println(""+user.apellido_paterno);
                System.out.printf("%s","Apellido Materno: ");
                System.out.println(""+user.apellido_materno);
                System.out.printf("%s","Fecha de Nacimiento: ");
                System.out.println(""+user.fecha_nacimiento);
                System.out.printf("%s","Telefono: ");
                System.out.println(""+user.telefono);
                System.out.printf("%s","Genero: ");
                System.out.println(""+user.genero);
                BufferedReader brr=new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Desea modificar los datos del usuario (s/n)?: ");
                char rsp=brr.readLine().charAt(0);
                if(rsp=='s')
                {
                	Usuario nuser=new Usuario();
                	System.out.println("Opcion Modificar Usuario");
									nuser.id_usuario=user.id_usuario;
									System.out.print("Email: ");
									String email=brr.readLine();
									nuser.email = (email.equals(""))? user.email : email;
									System.out.print("Nombre: ");
									String nombre=brr.readLine();
									nuser.nombre = (nombre.equals(""))? user.nombre : nombre;
									System.out.print("Apellido Paterno: ");
									String apellido_paterno=brr.readLine();
									nuser.apellido_paterno = (apellido_paterno.equals(""))? user.apellido_paterno : apellido_paterno;
									System.out.print("Apellido Materno: ");
									String apellido_materno=brr.readLine();
									nuser.apellido_materno = (apellido_materno.equals(""))? user.apellido_materno : apellido_materno;
									System.out.print("Fecha de Nacimiento: ");
									String fecha_nacimiento=brr.readLine();
									nuser.fecha_nacimiento = (fecha_nacimiento.equals(""))? user.fecha_nacimiento : fecha_nacimiento;
									System.out.print("Telefono: ");
									String telefono=brr.readLine();
									nuser.telefono = (telefono.equals(""))? user.telefono : telefono;
									System.out.print("Genero (M/F): ");
									String genero=brr.readLine();
									nuser.genero = (genero.equals(""))? user.genero : genero;
									nuser.foto=null;
									modifica_usuario(nuser);
                }
            }
            while ((respuesta = br.readLine()) != null)
                System.out.println(respuesta);
        }else{
            BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
            String respuesta;
            while ((respuesta = br.readLine()) != null)
                System.out.println(respuesta);
            throw new RuntimeException("Codigo de Error HTTP: "+conexion.getResponseCode());
        }
        conexion.disconnect();
	}
	public static void modifica_usuario(Usuario usuario) throws IOException
	{
				URL url=new URL("http://"+ip+":8080/Servicio/rest/ws/modifica_usuario");
        HttpURLConnection conexion= (HttpURLConnection) url.openConnection();
        conexion.setDoOutput(true);
        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        GsonBuilder builder=new GsonBuilder();
        builder.serializeNulls();
        Gson gson=builder.create();
        String body=gson.toJson(usuario);
        String parametros = "usuario=" + URLEncoder.encode(body, "UTF-8");
        OutputStream os=conexion.getOutputStream();
        os.write(parametros.getBytes());
        os.flush();
        if(conexion.getResponseCode()==200)
        {
            System.out.println("El usuario ha sido modificado");
        }else{
            BufferedReader br=new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
            String respuesta;
            while ((respuesta = br.readLine()) != null)
                System.out.println(respuesta);
            throw new RuntimeException("Codigo de Error HTTP: "+conexion.getResponseCode());
        }
        conexion.disconnect();
	}
	public static void borrar_usuario(int id_usuario) throws IOException
	{
		URL url = new URL("http://"+ip+":8080/Servicio/rest/ws/borra_usuario");
        HttpURLConnection conexion= (HttpURLConnection) url.openConnection();
        conexion.setDoOutput(true);
        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String parametros = "id_usuario=" + URLEncoder.encode(String.valueOf(id_usuario), "UTF-8");
        OutputStream os = conexion.getOutputStream();
        os.write(parametros.getBytes());
        os.flush();
        if(conexion.getResponseCode()==200)
        {
            System.out.println("El usuario ha sido borrado");
        }else{
        	BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
            String respuesta;
            while ((respuesta = br.readLine()) != null)
                System.out.println(respuesta);
            throw new RuntimeException("Codigo de Error HTTP: "+conexion.getResponseCode());
        }
        conexion.disconnect();
	}
}
class Usuario
{
	int id_usuario;
	String email;
	String nombre;
	String apellido_paterno;
	String apellido_materno;
	String fecha_nacimiento;
	String telefono;
	String genero;
	byte[] foto;
}
