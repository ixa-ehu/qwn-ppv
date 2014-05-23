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

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/* Main class for handling UKB propagation. For the moment it assumes ukb_ppv binary executable to be in the path, and executes it.
 * 
 * @version: 2014/05/20
 * @author: Iñaki San Vicente
 */

public class PropagationUKB {
		
	private String ukbPath = "/usr/local/bin"; 
	private String graph = "synAnt";
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
	
	//Initializations
	private static final HashMap <String, String> AvailableGraphs = new HashMap<String, String>() {{ 
			put ("syn","wn30-en.txt");
			put ("es","mcr30-es.txt");
			put ("eu","mcr30-eu.txt");
			put ("cat","mcr30-cat.txt");
			put ("gl","mcr30-gl.txt");
		}};*/
	
	public PropagationUKB ()
	{		
		//if no language is passed the system defaults to english.
		this("en");
	}
	
	public PropagationUKB (String langordict)
	{		
		
		try {
			AvailableDicts.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("/dicts.txt"));
			AvailableGraphs.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("/graphs.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("PropagationUKB classs initialization: error when loading lkb_resources properties (dicts/graphs).\n");
			e.printStackTrace();
			
		}
		
		
		if (AvailableDicts.containsKey(langordict))
		{
			this.langDict = this.getClass().getClassLoader().getResource("/dicts/"+AvailableDicts.get(langordict)).toString();
		}
		//if language is not available assume that the argument corresponds to a custom dictionary path.
		else
		{
			System.err.println("PropagationUKB class: provided language is not available, "
					+ "qwn-ppv assumes that the argument provide is the path to a custom language dictionary.\n");
			this.langDict = langordict;
		}
	}
	
	public PropagationUKB (String langordict, String graph1)
	{		
		this (langordict);
		this.setGraph(graph1);		
	}
	
	public PropagationUKB (String langordict, String graph1, String ukbPath1)
	{		
		this (langordict, graph1);
		this.ukbPath = ukbPath1;		
	}
	
	
	public void setGraph (String graph1)
	{

		if (AvailableGraphs.containsKey(graph1))
		{
			this.graph = this.getClass().getClassLoader().getResource("/dicts/"+AvailableGraphs.get(graph1)).toString();
		}
		//if language is not available assume that the argument corresponds to a custom dictionary path.
		else
		{
			System.err.println("PropagationUKB class: provided graph is not available, "
					+ "qwn-ppv assumes that the argument provided is the path to a custom graph.\n");
			this.graph=graph1;
		}
	}
	
	
	public void propagate(String ctxtFile) throws IOException
	{
		try {
			String[] command = {ukbPath+"/ukb_ppv","-K",this.graph+".bin","-D",this.langDict, "--variants", "-O", this.outDir , ctxtFile};    	
			ProcessBuilder ukbBuilder = new ProcessBuilder( command ); //my $grep=`$grepCommand -m1 -c "$seed" $KBfile`;
			Process ukb_ppv = ukbBuilder.start();
			int success = ukb_ppv.waitFor();
		}
		catch (Exception e){
			System.err.println("PropagationUKB class: error when calling ukb_ppv\n.");
			e.printStackTrace();
		}
    	//"$UKB_PATH"/bin/ukb_ppv -K "$PROPAGATION_PATH"/lkb_resources/wnet30_enSyn.bin -D "$UKB_PATH"/lkb_sources/30/wnet30_dict.txt --variants -O "$PPV_OUT_DIR" $TMP_DIR/propag_posSeeds_syn.ctx  
		//mv "$PPV_OUT_DIR"/ctx_01.ppv "$PPV_OUT_DIR"/pos_syn.ppv

	}

	
	
	
}
