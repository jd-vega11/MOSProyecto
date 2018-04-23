package algoritmo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Algoritmo para solucionar el problema de asignacion de salones.
 * Se utiliza Hungarian Algorithm como base para abordar el problema.
 * Las referencias correspondientes se encuentran en la clase que lo implementa.
 * @author Andres Felipe Lopez; Juan Manuel Dominguez; Juan David Vega
 *
 */
public class Main 
{
	private static final int CANT_SECCIONES = 18;
	private static final int CANT_CURSOS = 3;
	private static final int CANT_SALONES = 5;
	private static final int CANT_DIAS = 5;
	private static final int CANT_HORAS = 8;
	
	
	
	private static final String RUTA_SECCIONES = "./data/secciones.txt";
	private static final String RUTA_SALONES = "./data/salones.txt";
	private static final String RUTA_RESTRICCIONES_CURSOS = "./data/restriccionesCursos.txt";
	
	
	private static int[][] secciones;
	private static int[][] salones;
	private static int[][] restriccionesCursos;

	
	public static void main(String[] args) 
	{
				
		secciones = cargarDatos(RUTA_SECCIONES, CANT_SECCIONES);
		salones = cargarDatos(RUTA_SALONES, CANT_SALONES);
		restriccionesCursos = cargarDatos(RUTA_RESTRICCIONES_CURSOS, CANT_CURSOS);
		
		double costosDiaHora[][] = new double[CANT_SALONES][CANT_SALONES];
		
		boolean diaHoraTemp[] = new boolean[CANT_DIAS*CANT_HORAS];	
		for(int b = 0; b < CANT_DIAS*CANT_HORAS; b++)
		{
			diaHoraTemp[b] = false;
		}
		
		double asignacionFinal[][] = new double[CANT_SECCIONES][4];
		for(int secc = 0; secc < CANT_SECCIONES; secc++)
		{
			asignacionFinal[secc][3] = -1;
		}
		int[] rtaTemp;
		int[] avanceIteraciones = new int[CANT_SECCIONES];
		avanceIteraciones[0] = 0;
		int asignacionIndice = 0;
		int seccionTemp = 0;
		int desperdicio = 0;	
		int iteraciones = 0;
		int size = 0;
		
		for(int i = 0; i < CANT_SECCIONES; i++)
		{			
			if(!diaHoraTemp[(((secciones[i][1]-1)*CANT_HORAS)+secciones[i][2])-1])
			{				
				diaHoraTemp[(((secciones[i][1]-1)*CANT_HORAS)+secciones[i][2])-1] = true;	
				seccionTemp = 0;
				for(int t = 0; t < CANT_SALONES; t++)
				{
					for(int t1 = 0; t1 < CANT_SALONES; t1++)
					{
						costosDiaHora[t][t1] = 100000;
					}
				}
				
				for(int j = i; j < CANT_SECCIONES; j++)
				{							
					if(secciones[j][1] == secciones[i][1] && secciones[j][2]== secciones[i][2])
					{
						asignacionFinal[asignacionIndice][0] = secciones[j][0];
						asignacionFinal[asignacionIndice][1] = secciones[j][1];
						asignacionFinal[asignacionIndice][2] = secciones[j][2];
						asignacionIndice++;
						
						for(int k = 0; k < CANT_SALONES; k++)
						{
							desperdicio = salones[k][1] - secciones[j][3];
							if(desperdicio >= 0)
							{
								costosDiaHora[k][seccionTemp] = desperdicio;
							}
							else
							{
								costosDiaHora[k][seccionTemp] = 10000;
							}
							
							//Verificacion restriccion movil Xpress
							if(salones[k][2] - restriccionesCursos[secciones[j][0]-1][1] < 0)
							{
								costosDiaHora[k][seccionTemp] = 10000;
							}												
							//Verificacion restriccion tipo auditorio
							if(salones[k][3] - restriccionesCursos[secciones[j][0]-1][2] < 0)
							{
								costosDiaHora[k][seccionTemp] = 10000;
							}
							//Verificacion restriccion oscurecimiento
							if(salones[k][4] - restriccionesCursos[secciones[j][0]-1][3]< 0)
							{
								costosDiaHora[k][seccionTemp] = 10000;
							}
							//Verificacion restriccion implementos especiales
							if(salones[k][5] - restriccionesCursos[secciones[j][0]-1][4] <0)
							{
								costosDiaHora[k][seccionTemp] = 10000;
							}
						
						}					
						seccionTemp++;
						
					}
					
				}	
			
				HungarianAlgorithm algoritmo = new HungarianAlgorithm(costosDiaHora);
				rtaTemp = algoritmo.execute();
				size = rtaTemp.length;
				for(int rta = 0; rta < size; rta++)
				{
					if(rtaTemp[rta] != -1 && rtaTemp[rta] < seccionTemp)
					{
						asignacionFinal[avanceIteraciones[iteraciones]+rtaTemp[rta]][3] = rta+1;
					}
				}					
				avanceIteraciones[iteraciones+1] = seccionTemp+avanceIteraciones[iteraciones];
				iteraciones++;
							
			}
			
		}
		
		for(int a = 0; a < CANT_SECCIONES; a++)
		{
			System.out.println("Para la clase " + asignacionFinal[a][0] +
					" el dia "+ asignacionFinal[a][1] + 
					" a la hora " + asignacionFinal[a][2]+
					" se selecciono el salon " + asignacionFinal[a][3]);
		}
	
	}
	
	public static int[][] cargarDatos(String ruta, int cantFilas)
	{
		int[][] rta = new int[cantFilas][];
		try
		{
			File file = new File(ruta);
			 
			BufferedReader br = new BufferedReader(new FileReader(file));
			 
			String st = br.readLine();
			int cantCols = 0;
			int i = 0;
			while ((st = br.readLine()) != null) {
				String partes[] = st.split(",");
				cantCols = partes.length;
				int[] fila = new int[cantCols];
				
				for(int j = 0; j < cantCols; j++)
				{
					fila[j] = Integer.parseInt(partes[j]);
				}
				rta[i] = fila;
				i++;				
			}
			
		}
		catch(IOException e)
		{
			System.out.println("Problemas para leer la informacion de " + ruta);
			e.printStackTrace();
		}
		return rta;
		
	}
}
