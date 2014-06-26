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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/* Main class for handling UKB propagation. For the moment it assumes ukb_ppv binary executable to be in the path, and executes it.
 * 
 * @version: 2014/05/20
 * @author: Iñaki San Vicente
 */

public class PropagationCombinator {
	private Map<String, Float> scores = new HashMap<String, Float>();
	private Map<String, String> variants = new HashMap<String, String>();
	
	public PropagationCombinator()
	{		
	}
	
	public SortedSet<String> sinpleCombinator(Set<String> posPaths, Set<String> negPaths)
	{
		SortedSet<String> Lexicon = new TreeSet<String>();

		int posFiles = 0;
		int negFiles = 0;
		
		//iterate through positive score files and sum scores 
		for (String path : posPaths)
    	{
			try {
				BufferedReader breader = new BufferedReader(new FileReader(new File(path)));
				System.err.println("PropagationCombinator: "+path+" UKB propagation positive ranking open");				
				this.sumScores(breader);
				breader.close();
				posFiles++;
			} catch (Exception e) {
				System.err.println("PropagationCombinator: "+path+" UKB propagation positive ranking file could not be opened, combination continues without the file");
				e.printStackTrace();
			}			
    	}
		
		//iterate through negative score files and subtract scores 
		for (String path : negPaths)
    	{
			try {
				BufferedReader breader = new BufferedReader(new FileReader(new File(path)));
				System.err.println("PropagationCombinator: "+path+" UKB propagation negative ranking open");
				this.subtractScores(breader);
				breader.close();
				negFiles++;
			} catch (Exception e) {
				System.err.println("PropagationCombinator: "+path+" UKB propagation negative ranking file could not be opened, combination continues without the file");
				e.printStackTrace();
			}		
    	}
		
		//create lexicon with combined ranking
		for (String key : this.scores.keySet())
		{
			String polarity="neu";
			float score = this.scores.get(key);
			if (score > 0)
			{
				polarity = "pos";				
			}
			else if (score < 0)
			{
				polarity = "neg";			
			}
			
			// neutral words (at the moment those that have 0 polarity score after merging ranks are left out of the lexicon
			if (! polarity.equals("neu"))
			{
				String LexiconEntry = key+"\t"+polarity+"\t"+this.variants.get(key)+"\t"+this.scores.get(key);
				Lexicon.add(LexiconEntry);
			}

		}	
		System.err.println("PropagationCombinator: "+(posFiles+negFiles)+" rankings merged ("+posFiles+"/"+negFiles+").\n");		
		return Lexicon;
	}	
	
	private void sumScores (BufferedReader breader) throws IOException
	{
		String line;
		while ((line = breader.readLine()) != null) 
		{
		    //format of the ranking file is: "[word|synset]<tab>[polarityScore]<tab>variants"
			String[] fields = line.split("\t");
			
			//if no weight is provided the program reverses to no weighted seed behavior
			if (fields.length < 3)
		    {
				System.err.println("propagationCombinator : error: this line in propagation rank file does not contain all the field required.");			
			}
			else
			{
				//System.err.println(fields[0]+" <--> "+fields[1]+" <--> "+fields[2]);			
			}
			String offset = fields[0];
			//scores come in scientific notation. They must be parsed to java 
			float score = Float.parseFloat(fields[1]);
						
			if(this.scores.containsKey(offset))
			{
				float updatedScore = this.scores.get(offset)+score;
				this.scores.put(offset, updatedScore);
			}
			else
			{
				this.variants.put(offset, fields[2]);
				this.scores.put(offset, score);				
			}
		}
	}
	
	private void subtractScores(BufferedReader breader)throws IOException
	{
		String line;
		while ((line = breader.readLine()) != null) 
		{
		    //format of the seed file is: "[word|synset]<tab>[polarityScore]<tab>variants"
			String[] fields = line.split("\t");
			//if no weight is provided the program reverses to no weighted seed behavior
			if (fields.length < 3)
		    {
				System.err.println("propagationCombinator : error: this line in propagation rank file does not contain all the field required.");			
			}
			String offset = fields[0];
			float score = Float.parseFloat(fields[1]);
			
			if(this.scores.containsKey(offset))
			{
				float updatedScore = this.scores.get(offset)-score;
				this.scores.put(offset, updatedScore);
			}
			else
			{
				this.variants.put(offset, fields[2]);
				this.scores.put(offset, -score);				
			}
		}
	}
	
}