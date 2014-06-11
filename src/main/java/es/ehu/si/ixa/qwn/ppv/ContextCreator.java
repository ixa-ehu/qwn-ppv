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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* contextCreator class: this class creates a context file in order to launch the UKB propagations. 
*
* <ol>
* <li>Seed list: choose language to create the lang attribute in KAF header.</li>
* <li>Graph: Graph to use for propagation 4 graphs are available (SynAnt | mcr | mcr-ant | mcr-antGloss).</li>
* <li>Test set (NOT YET IMPLEMENTED - optional): if a test set is provided, the system evaluates the
*     lexicons generated, and returns which of them performs best. </li>
* </ol>
*
*
* @author isanvicente
* @author ragerri
* @version 2014-05-13
*/
public class ContextCreator {

	private boolean weights = false;
	private String KBFile = "";
	/**
	* BufferedReader (from standard input) and BufferedWriter are opened. The
	* module takes a plain text containing positive and negative words from standard input, and produces polarity lexicon in tabulated format
	* text by sentences. Resulting lexicons is returned through standard output.
	*
	* @param args
	* @throws IOException
	**/
	
	public ContextCreator(boolean w) {
		weights = w;
		
		System.err.println("qwn-ppv: context creator initialized\n\tweights= "+weights+"\n");
	}
	
	public void setKBFile (String p) {
		KBFile = this.getClass().getClassLoader().getResourceAsStream("graphs")+File.separator+p+".txt.gz";
		
		System.err.println("qwn-ppv: context creator initialized\n\tweights= "+weights+"\n");
	}
	
	public void createContexts(BufferedReader breader, BufferedWriter bw_pos, BufferedWriter bw_neg){
		
		try {		
			int syn = 0;
			int wrd = 0;
			String w = "";

			// context initialization
			bw_pos.write("ctx_01\n");
			bw_neg.write("ctx_01\n");
			
			String line;
			while ((line = breader.readLine()) != null) {
			    //format of the seed file is: "[word|synset]<tab>[pos|neg]<tab>weight (optional)"
				String[] fields = line.split("\t");
			    //if no weight is provided the program reverses to no weighted seed behavior
				if (fields.length > 2)
			    {
					w = fields[2];
				}
			    
				String toprint = "";
			    
				// control number, by default we'll use '0' value (no disambiguation but use it for computing pagerank), 
			    // if the seed is a synset then the value '2' will be used.
			    // seed is a wordnet synset 
				//IMPORTANT if seeds are synset but no KBFile is provided context creation will fail.
			    if (fields[0].matches("[0-9]{5,}(-[a-z])?")) 
			    {
			    	if (this.KBFile.equals(""))
			    	{
			    		System.err.print("ERROR: Synset seed can not be propagated without a KBfile.\n");
			    		System.exit(1);
			    	}
			    	syn++;	
			    	// if seed concept is not in the KB graph do not include it in the context, else UKB will complain and crash.
			    	int print=0;
			    	try{  
			    		//read large text file  
			    		FileInputStream KBstream = new FileInputStream(new File(this.KBFile));  
			    		FileChannel fc = KBstream.getChannel();  
		    		  
			    		Scanner scan = new Scanner(fc);  
			    		while(scan.hasNext()){  
			    			scan.next();    	    			
			    			if(scan.findWithinHorizon(fields[0],0) != null) {
			    				print++; 
			    				break;
			    			}
			    		}  
			    		scan.close();  
			    		fc.close();  
			    		KBstream.close();
			    	}catch (IOException ioe)
			    	{
			    		System.err.println("ERROR when openning KB file for context creation; "+KBFile);
			    		ioe.printStackTrace();
			    	}
			    		/*String[] command = {"zgrep","-m1","-c",fields[1], KBFile};
			    	ProcessBuilder grepProc = new ProcessBuilder( command );//my $grep=`$grepCommand -m1 -c "$seed" $KBfile`;
			    	Process grep = grepProc.start();*/
			        if (print > 0)
			        {
			        	toprint = fields[0]+"##s"+syn+"#2#"+w;
			        }
				}
			    // seed is a lemma
			    else
			    {
			    	wrd++;
			    	String pos = fields[0].substring(fields[0].lastIndexOf("_")+1);
			    	pos = pos.replaceAll("s", "a");
			    	String lemma = fields[0].substring(0,fields[0].lastIndexOf("_"));			    	
			    	if (pos.length() != 1)
			    	{
			    		System.err.println("Warning: pos value is empty or wrong you should use ukb with the --nopos option");
			    		pos = "";
			    		lemma = fields[0];
			    	}
			    	toprint = lemma+"#"+pos+"#w"+wrd+"#0#"+w;
			    	
			    }
			    // if weight was not present erase the '#' character at the end
			    toprint = toprint.replaceAll("#$", "");
			    if (! toprint.matches("\\s*"))
			    {
			    	if (fields[1].equals("pos"))
			    	{
				    	bw_pos.write (toprint+" ");			    		
			    	}
			    	else
			    	{
				    	bw_neg.write (toprint+" ");
			    	}

			    }			  
			}
			bw_pos.close();
			bw_neg.close();
						
			breader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	} //end createContexts
	
}