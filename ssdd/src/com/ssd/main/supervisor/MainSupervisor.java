package com.ssd.main.supervisor;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.main.HashMap;
import com.ssdd.main.List;
import com.ssdd.main.MainNode;
import com.ssdd.main.Map;
import com.ssdd.main.String;
import com.ssdd.main.node.NodeBuilder;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.service.NTPService;

public class MainSupervisor {

	//List of args for init activity:
	//-arg[0] -> True or false, it indicates if is the init or the end of the progress
	//-arg[1] -> name of the ntp time file
	//-arg[2] -> number of nodes
	//-arg[>2] -> servers
	
	//List of args for activity´s end:
	//-arg[0] -> True or false, it indicates if is the init or the end of the progress
	//-arg[1] -> name of the ntp time file
	//-arg[>2] -> logs
	public static void main(String [] args) {
		// take arguments
		boolean isInit = Boolean.parseBoolean(args[0]);
		String fileNameNTPInit = args[1];
		
		if(!isInit) {// In this case is called at the init of the process
			int numberOfNodes = Integer.parseInt(args[2]);
			String [] servers = Arrays.copyOfRange(args, 3, args.length);
			
			// instance builder
			NodeBuilder builder = new NodeBuilder();
			
			// configure builder for ntp use
			builder.servers(servers);
			
			//Restart critical section servers
			for(String server : servers)
				CriticalSectionService.buildProxy(server).restart(numberOfNodes);
			
			//Instance NTP client
			NTPClient ntp = builder.buildNTPClient();
			
			//We need to generate 10 times for each server and write it in a temp file
			Map<NTPService, List<Pair>> initialPairs = ntp.sample();
			
			Gson gson = new Gson();
			String jsonPairs = gson.toJson(initialPairs);
			
			//Write JSON file
	        try (FileWriter file = new FileWriter(fileNameNTPInit)) {
	 
	            file.write(jsonPairs.toJSONString());
	            file.flush();
	 
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
		}else { //In this case is called at the final of the process
			
			//We need to do the following points
			//-Read the init files
			//-Generate 10 times for each server and add it to the previous times
			//-Obtaining the offset for each server
			//-For each logs, read it and correct the times adding the offset to that times
			//-Finally, merge the logs, sort and check it
			
			//Reading file
			//JSON parser object to parse read file
			NTPService [] services = null;
			String [] logsFiles = null;
	        JSONParser jsonParser = new JSONParser();
	         
	        try (FileReader reader = new FileReader(fileNameNTPInit))
	        {
	            //Read JSON file
	            Object simpleObject = jsonParser.parse(reader);
	 
	            JSONObject jsonNTPInit = (JSONObject) simpleObject;
	 
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
	        
	        //Convert json to HashMap
	        Type mapType = new TypeToken<Map<NTPService, List<Pair>>>(){}.getType();
	        Map<NTPService, List<Pair>> ntpInit = gson.fromJson(jsonNTPInit, type); 
			
	        Map<NTPService, List<Pair>> ntpFinal = ntp.sample();
	        
	        //Generate NTP results
	        Map<NTPService, List<Pair>> all = MainSupervisor.joinMaps(ntpInit, ntpFinal);
			Map<NTPService, Pair> results = new HashMap<>();
			
			// calculate results
			for(NTPService service : all.keySet()) {
				results.put(service, ntp.selectBestPair(all.get(service)));
			}
			
			int i = 0;
			List<String> joinedLogs = new ArrayList<>();
			
			//Read Logs, correct times and put together
			for(String logFile : logsFiles) {
				File file=new File(logFile);    //creates a new file instance  
				FileReader fr=new FileReader(file);   //reads the file  
				BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream  
				String line;
				
				Pair servicePair = result.get(services[i]);
				
				while((line=br.readLine())!=null)  
				{ 
					String [] splittedLine = line.split(" ");
					Double currentOffset = Double.parseDouble(line[3]);
					Double correctedOffset = currentOffset + servicePair.getOffset();
					
					StringJoiner newCompossedLine = new StringJoiner(" ");
					for(String s: splittedLine) 
						newCompossedLine.add(s);
					
					 joinedLogs.add(newCompossedLine);
				}  
				fr.close();    //closes the stream and release the resources
				i++;
			}
				
		}

	}
	
	private static Map<NTPService, List<Pair>> joinMaps(Map<NTPService, Pair []> m1, Map<NTPService, Pair []> m2){
		Map<NTPService, List<Pair>> result = new HashMap<>();
		for(NTPService service : m1.keySet()) {
			List<Pair> p = new ArrayList<>();
			p.addAll(Arrays.asList(m1.get(service)));
			p.addAll(Arrays.asList(m2.get(service)));
			result.put(service, p);
		}
		return result;
	}
	
	
}
