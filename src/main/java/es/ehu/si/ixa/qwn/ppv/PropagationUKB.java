/*
* Copyright 2014 Iñaki San Vicente and Rodrigo Agerri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package es.ehu.si.ixa.qwn.ppv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;

/* Main class for handling UKB propagation. For the moment it assumes ukb_ppv binary executable to be in the path, and executes it.
 * 
 * @version: 2014/05/20
 * @author: Iñaki San Vicente
 */

public class PropagationUKB {
		
	private String ukbPath = "/usr/local/bin"; 
	private String graph = "mcr";
	private String langDict = "";
	private String outDir = "tmp-qwn/";
	//Initializations
	private static final Properties AvailableDicts = new Properties(); 
	private static final Properties AvailableGraphs = new Properties();
	
	/*private static final HashMap <String, String> AvailableDicts = new HashMap<String, String>() {{ 
		put ("en","wn30-en.txt");
		put ("es","mcr30-es.txt");
		put ("eu","mcr30-eu.txt");
		put ("cat","mcr30-cat.txt");
		put ("gl","mcr30-gl.txt");
	}};
	*/
	
	public PropagationUKB (String outFolder)
	{		
		//if no language is passed the system defaults to english.
		this("en", outFolder);
		
	}
	
	public PropagationUKB (String langordict, String outFolder)
	{	
		// temporal files directory
		this.outDir = outFolder;
		
		try {
			AvailableDicts.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("dicts.txt"));
			AvailableGraphs.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("graphs.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("PropagationUKB class initialization: error when loading lkb_resources properties (dicts/graphs).\n");
			e.printStackTrace();
			
		}
		
		
		if (AvailableDicts.containsKey(langordict))
		{
			String destPath = this.outDir+File.separator+(String) AvailableDicts.get(langordict);

			try {						
				InputStream dictToExtract =  this.getClass().getClassLoader().getResourceAsStream("dicts/"+(String) AvailableDicts.get(langordict));			
				OutputStream dictDestination = new FileOutputStream(destPath);
				IOUtils.copy(dictToExtract, dictDestination);
				dictToExtract.close();
			    dictDestination.close();
			} catch (FileNotFoundException e) {
				System.err.println("PropagationUKB class initialization: could not extract (create) needed dictionary file\n");
				e.printStackTrace();
			} catch (IOException ioe){
				System.err.println("PropagationUKB class initialization: could not extract needed dictionary file\n");
				ioe.printStackTrace();
			} 
			
			this.langDict = destPath;			
		}
		//if language is not available assume that the argument corresponds to a custom dictionary path.
		else
		{
			System.err.println("PropagationUKB class: provided language is not available ("+langordict+"), "
					+ "qwn-ppv assumes that the argument provide is the path to a custom language dictionary.\n");
			this.langDict = langordict;
		}
				
	}
	
	public PropagationUKB (String langordict, String outFolder, String graph1)
	{		
		this (langordict, outFolder);
		this.setGraph(graph1);		
	}
	
	public PropagationUKB (String langordict, String outFolder, String graph1, String ukbPath1)
	{		
		this (langordict, outFolder, graph1);
		this.ukbPath = ukbPath1;		
	}
	
	
	public void setGraph (String graph1)
	{

		//AvailableGraphs.list(System.err);
		//System.err.println("set graph to: "+graph1);
		if (AvailableGraphs.containsKey(graph1))
		{
			String destPath = this.outDir+File.separator+(String) AvailableGraphs.get(graph1)+".bin";

			try {						
				InputStream dictToExtract =  this.getClass().getClassLoader().getResourceAsStream("graphs/"+(String) AvailableGraphs.get(graph1)+".bin");			
				OutputStream dictDestination = new FileOutputStream(destPath);
				IOUtils.copy(dictToExtract, dictDestination);
				dictToExtract.close();
			    dictDestination.close();
			} catch (FileNotFoundException e) {
				System.err.println("PropagationUKB class initialization: could not extract (create) needed graph file\n");
				e.printStackTrace();
			} catch (IOException ioe){
				System.err.println("PropagationUKB class initialization: could not extract needed graph file\n");
				ioe.printStackTrace();
			} 

			this.graph = destPath;
		}
		//if language is not available assume that the argument corresponds to a custom dictionary path.
		else
		{
			System.err.println("PropagationUKB class: provided graph is not available ("+graph1+"), "
					+ "qwn-ppv assumes that the argument provided is the path to a custom graph.\n");
			this.graph=graph1;
		}
	}
	
	
	public void propagate(String ctxtFile) throws IOException
	{
		try {
			String[] command = {ukbPath+"/ukb_ppv","-K",this.graph,"-D",this.langDict, "--variants", "-O", this.outDir , ctxtFile};
			//System.err.println("UKB komandoa: "+Arrays.toString(command));
			
			ProcessBuilder ukbBuilder = new ProcessBuilder()
				.command(command);
				//.redirectErrorStream(true);
			Process ukb_ppv = ukbBuilder.start();
			int success = ukb_ppv.waitFor();
			System.err.println("ukb_ppv succesful? "+success);
			if (success != 0)
			{
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ukb_ppv.getErrorStream()), 1);
					String line;
					while ((line = bufferedReader.readLine()) != null) {
		                 System.err.println(line);
		             }
			}
		}
		catch (Exception e){
			System.err.println("PropagationUKB class: error when calling ukb_ppv\n.");
			e.printStackTrace();
		}
    	//"$UKB_PATH"/bin/ukb_ppv -K "$PROPAGATION_PATH"/lkb_resources/wnet30_enSyn.bin -D "$UKB_PATH"/lkb_sources/30/wnet30_dict.txt --variants -O "$PPV_OUT_DIR" $TMP_DIR/propag_posSeeds_syn.ctx  
		//mv "$PPV_OUT_DIR"/ctx_01.ppv "$PPV_OUT_DIR"/pos_syn.ppv

	}
	
	
}
