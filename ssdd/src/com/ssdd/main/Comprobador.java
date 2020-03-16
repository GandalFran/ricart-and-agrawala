package com.ssdd.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Usage: java Comprobador nombreFichero [delay1] [delay2]
 *         nombreFichero - path al fichero de logs, fusionado y corregido segUn offsets (ver FORMATO debajo)
 *         delay1   - opcional, delay para los procesos de la mAquina 2
 *         delay2   - opcional, delay para los procesos de la mAquina 3
 *         
 * El programa detecta el nUmero de procesos en el log (N) y el nUmero de mAquinas (M=1,2 o 3) dependiendo de si 
 * se ha pasado delay1 y/o delay2. Asigna a cada mAquina Nm procesos=N/M, de modo que los procesos
 * P1-PNm van a la mAquina 1, PNm+1 - P2Nm a la mAquina 2, P2Nm+1-P3N a la mAquina 3.
 *         
 * FORMATO        
 * Varias filas con el siguiente formato:
 * Pi M time
 * 
 * La separaciOn de columnas debe realizarse mediante espacios SIMPLES
 * i va de 1 a N, siendo N el num de procesos
 * M puede ser "E" o "S"
 * 
 * El fichero debe estar ordenado segUn time (y en caso del mismo tiempo, segUn i), 
 * y cada time corregido segUn la estimación de la desviación (offset) media de la
 * mAquina en la que corre su proceso segUn el protocolo NTP.
 * 
 * time puede ser un valor entero o real (se debe usar punto como separador decimal, no coma)
 *
 * El programa decide si ha habido alguna violaciOn de la secciOn crItica, informando del nUmero total
 * de violaciones detectadas, las lIneas donde se producen, y la razOn para la detecciOn
 * 
 * [ Para juntar y ordenar los ficheros, en sistemas Unix podemos usar:
 * cat 0.log 1.log 2.log ... &gt; total.log
 * sort -k 3 total.log &gt; totalSorted.log ]
 * @author rodri
 *
 */
public class Comprobador {

	public static void main(String args[])
		{
		HashMap<String, Double> desviaciones=new HashMap<String,Double>();
		int desvPorProceso=0;
		double[] desv=new double[]{0,0,0};
		int contViolaciones=0;
		
		try{
		if(args.length<1 || args.length>3)
			{
			System.out.println("Usage: java Comprobador nombreFichero [desviaciOn1] [desviaciOn2]");
			System.out.println("	    - nombreFichero: ruta al fichero de logs, fusionado y ordenado por tiempos.");
			System.out.println("			Los tiempos deben estar corregidos segUn los desfases estimados vIa NTP");
			System.out.println("		- desviaciOn1: desviaciOn del desfase segUn NTP para los procesos P3 y P4");
			System.out.println("		- desviaciOn2: desviaciOn del desfase segUn NTP para los procesos P5 y P6");
			System.out.println("El programa detecta el nUmero de procesos en el log (N) y el nUmero de mAquinas (M=1,2 o 3) dependiendo de si\n"+
			 " se ha pasado desviacion1 y/o desviacion2. Asigna a cada mAquina Nm procesos=N/M, de modo que los procesos\n"+
			 " P1-PNm van a la mAquina 1, PNm+1 - P2Nm a la mAquina 2, etc.\n"+
			 "\n"+
			 " FORMATO  \n"+      
			 " Varias filas con el siguiente formato:\n"+
			 " Pi M time\n"+
			 "\n"+
			 " La separaciOn de columnas debe realizarse mediante espacios SIMPLES\n"+
			 " i va de 1 a N, siendo N el num de procesos\n"+
			 " M puede ser \"E\" o \"S\"\n"+
			 "\n"+
			 " El fichero debe estar ordenado segUn time (y en caso del mismo tiempo, segUn i),\n"+ 
			 " y cada time corregido segUn la deriva media de la mAquina en la que corre su proceso\n"+
			 " segUn el protocolo NTP.\n"+
			 "\n"+
			 " time puede ser un valor entero o real (se debe usar punto como separador decimal, no coma)\n"+
			 "\n"+
			 " El programa decide si ha habido alguna violaciOn de la secciOn crItica, informando del nUmero total\n"+
			 " de violaciones detectadas, las lIneas donde se producen, y la razOn para la detecciOn\n"+
			 "\n"+
			 " [ Para juntar y ordenar los ficheros, en sistemas Unix podemos usar:\n"+
			 " cat 0.log 1.log 2.log ... > total.log\n"+
			 " sort -k 3 total.log > totalSorted.log ]");
			System.exit(1);
			}
		BufferedReader br=new BufferedReader(new FileReader(args[0]));
		String cad=null;
		int maxId=0;
		while((cad=br.readLine())!=null)
			{
			String[] st=cad.split(" ");
			int id=new Integer(st[0].replace("P", "")).intValue();
			if(id>maxId)	maxId=id;
			}
		
		System.out.println("Num de procesos: "+maxId);
		if(maxId % args.length != 0)
			{
			System.err.println("Num de procesos debe ser mUltiplo del nUmero de desviaciones introducidos");
			System.exit(1);
			}
		desvPorProceso=maxId/args.length;
			
		
		if(args.length>=2)
			desv[1]=(new Double(args[1])).doubleValue()*0.5;
		if(args.length==3)
			desv[2]=(new Double(args[2])).doubleValue()*0.5;
		
		for(int i=1;i<=maxId;i++)
			desviaciones.put("P"+i, desv[(i-1)/desvPorProceso]);
		for(String s:desviaciones.keySet())
			{
			System.out.println("Para el proceso "+s+" se usa delay: "+desviaciones.get(s));
			}
		
		String cadAnt=null;
		String cadAnt2=null;
		int cont=1;
		double t1,t2;
		ArrayList<String[]> sospechosos=new ArrayList<String[]>();//contiene las entradas en medio de la estancia en una secciOn crItica
		
		br=new BufferedReader(new FileReader(args[0]));
		cadAnt=br.readLine();
		while((cad=br.readLine())!=null)
			{
			cont++;
			//Volver a rutina de E en cadAnt, S en cad
			while(cadAnt.split(" ")[1].equals("S"))
				{
				cadAnt2=cadAnt;
				cadAnt=cad;
				cad=br.readLine();
				if(cad==null)	//Caso especial de tener dos S al final del documento
					{
					/*cad=cadAn
					cadAnt=cadAnt2;*/
					System.out.println("ComprobaciOn terminada");
					System.out.println("\tViolaciones detectadas: "+contViolaciones);
					return;
					}
				cont++;
				}
			
			//1) ComprobaciOn de que no se viola la secciOn crItica
			//Si estA ordenado por tiempo, cada 2 lIneas tienen que ser del mismo proceso (E y S)
			//Y tienen que ir consecutivamente E S, E S, E S
			String[] st=cad.split(" ");
			String[] stAnt=cadAnt.split(" ");
			
			String p1=stAnt[0];	//Id de proceso (Pn)
			String p2=st[0];
			
			String m1=stAnt[1];//Mensaje escrito (E|S)
			String m2=st[1];
			
			t1=new Double(stAnt[2]).doubleValue();//Los tiempos
			t2=new Double(st[2]).doubleValue();
			
			String cadx=null;
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
			otherSymbols.setDecimalSeparator('.');
			otherSymbols.setGroupingSeparator(','); 

			DecimalFormat dd=new DecimalFormat("#.############", otherSymbols);
			
			//Comprueba que no ocurre nunca que dos elementos entren a la vez en la secciOn crItica
			if(!p1.equals(p2) && m1.equals("E"))	//E1 E|SX (X!=1)
				{
				sospechosos.clear();
				String px=p2;
				String mx=m2;
				String[] stx=st;
				while(!p1.equals(px) && !mx.equals("S"))	//recorremos entradas hasta que encontremos la salida
					{
					System.out.println("LInea: "+cont+" Posible violaciOn de la SC: "+stx[0]+" "+stx[1]+" "+dd.format(Double.parseDouble(stx[2])));
					sospechosos.add(stx);	//Y agnadimos todas las entradas intermedias como sospechosas
					cadx=br.readLine();
					stx=cadx.split(" ");
					px=stx[0];
					mx=stx[1];
					cont++;
					}
				p2=px;
				m2=mx;
				t2=new Double(stx[2]).doubleValue();
				int caso=-1; //0-solo entrada (EES), 1-solo salida(ESS), 2-entrada y salida(EESS)
				String [] sospechoso=null,sospechoso2=null;
				double tx=-1;
			
				//Ahora analizamos todas las entradas sospechosas
				for(int i=0;i<sospechosos.size();i++)	//Probar quE hace esto en el caso de mucho mogollOn entre medias (deberIa funcionar bien con 1 o dos entre medias)
					{
					sospechoso=sospechosos.get(i);
					//Si es entrada de un proceso posterior
					if(sospechoso[1].equals("E"))
						{
						for(int j=i+1;j<sospechosos.size();j++)
							{
							sospechoso2=sospechosos.get(j);
							if(sospechoso[0].equals(sospechoso2[0]) && sospechoso2[1].equals("S"))
								caso=2; //E1 E2S2 S1
							}
						if(caso==-1)	caso=0; //E1 E2 S1
						}
					if(caso==-1)
						caso=1;//E1 S2 S1
					if(caso!=-1)
						{
						px=sospechoso[0];
						tx=new Double(sospechoso[2]).doubleValue();
						break;//de momento sale siempre despuEs del primero
						}
					}
				
				//La casuIstica se podrIa complicar mAs, pero creo que asI es suficiente
				switch(caso)
					{
					//NOTA: en los dos primeros casos podrIa pasar que, por ejemplo en caso 0: 
					//				E1 EX S1 SX --> EX SX E1 S1 pero es una situaciOn tan extrema que no la estamos comprobando
					case 0: //E1 EX S1  --> E1 S1 EX?
						if( (t2-desviaciones.get(p1) > tx + desviaciones.get(px)) )
							{
							//ERROR
							System.out.println("ERROR: "+px+" entra en la SC mientras "+p1+" estA dentro");
							//System.out.println("\tlInea "+cont+" y siguientes");
							System.out.println("\t\t"+p1+" entra en "+dd.format(t1)+" +- "+desviaciones.get(p1));
							System.out.println("\t\t"+px+" entra en "+dd.format(tx)+" +- "+desviaciones.get(px));
							System.out.println("\t\t"+p1+" sale en "+dd.format(t2)+" +- "+desviaciones.get(p1));
							contViolaciones++;
							}
						else
							System.out.println("  ViolaciOn disuelta por desfases de tiempo");
						break;
					case 1:	//E1 SX S1
						if( (t1+desviaciones.get(p1) < tx-desviaciones.get(px)) )
							{
							//ERROR
							System.out.println("ERROR: "+p1+" entra en la SC mientras "+px+" estA dentro");
							//System.out.println("\tlInea "+cont+" y siguientes");
							System.out.println("\t\t"+p1+" entra en "+dd.format(t1)+" +- "+desviaciones.get(p1));
							System.out.println("\t\t"+p1+" sale en "+dd.format(t2)+" +- "+desviaciones.get(p1));
							System.out.println("\t\t"+px+" sale en "+dd.format(tx)+" +- "+desviaciones.get(px));
							contViolaciones++;
							}
						System.out.println("  ViolaciOn disuelta por desfases de tiempo");
						break;
					case 2:  //E1 EX SX E1 (situaciOn bastante extrema, desviaciones mayores que el tiempo que permanecemos en la SC)
						double d=desviaciones.get(p1);
						double dx=desviaciones.get(px);
						double tx1=tx;
						double tx2=new Double(sospechoso2[2]).doubleValue();
						if(tx1-dx > t1+d || tx2+dx < t2-d)
							{
							//ERROR
							System.out.println("ERROR: "+px+" entra y sale en la SC mientras "+p1+" estA dentro");
							System.out.println("\tlInea "+cont+" y siguientes");
							System.out.println("\t\t"+p1+" entra en "+dd.format(t1)+" +- "+dd.format(d));
							System.out.println("\t\t"+px+" entra en "+dd.format(tx1)+" +- "+dd.format(dx));
							System.out.println("\t\t"+px+" sale en "+dd.format(tx2)+" +- "+dd.format(dx));
							System.out.println("\t\t"+p1+" sale en "+dd.format(t2)+" +- "+dd.format(d));
							contViolaciones++;
							}
						break;
					default:
						break;
					}
				}
			if(cadx!=null)	cadAnt=cadx;
			else			cadAnt=cad;
			}
		System.out.println("ComprobaciOn terminada");
		System.out.println("\tViolaciones detectadas: "+contViolaciones);
		}catch(Exception e){e.printStackTrace();}
		}
}
