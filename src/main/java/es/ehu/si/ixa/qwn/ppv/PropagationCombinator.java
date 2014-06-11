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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	public Set<String> sinpleCombinator(Set<String> posPaths, Set<String> negPaths)
	{
		Set<String> Lexicon = new HashSet<String>();

		int posFiles = 0;
		int negFiles = 0;
		
		//iterate through positive score files and sum scores 
		for (String path : posPaths)
    	{
			BufferedReader breader;
			try {
				breader = new BufferedReader(new FileReader(new File(path)));
				System.err.println("PropagationCombinator: "+path+" UKB propagation ranking open");				
				this.sumScores(breader);
				breader.close();
				posFiles++;
			} catch (Exception e) {
				System.err.println("PropagationCombinator: "+path+" UKB propagation ranking file could not be opened, combination continues without the file");
				e.printStackTrace();
			}			
    	}
		
		//iterate through negative score files and substract scores 
		for (String path : negPaths)
    	{
			BufferedReader breader;
			try {
				breader = new BufferedReader(new FileReader(new File(path)));
				System.err.println("PropagationCombinator: "+path+" UKB propagation ranking open");
				this.substractScores(breader);
				breader.close();
				negFiles++;
			} catch (Exception e) {
				System.err.println("PropagationCombinator: "+path+" UKB propagation ranking file could not be opened, combination continues without the file");
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
		System.err.println("PropagationCombinator: "+(posFiles+negFiles)+" rankings merged.\n");		
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
				System.err.println(fields[0]+" <--> "+fields[1]+" <--> "+fields[2]);			
			}
			String offset = fields[0];
			float score = Float.parseFloat(fields[1]);
			
			if(this.scores.containsKey(offset))
			{
				this.scores.put(offset, this.scores.get(offset)+score);
			}
			else
			{
				this.variants.put(offset, fields[2]);
				this.scores.put(offset, score);				
			}
		}
	}
	
	private void substractScores(BufferedReader breader)throws IOException
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
				this.scores.put(offset, this.scores.get(offset)-score);
			}
			else
			{
				this.variants.put(offset, fields[2]);
				this.scores.put(offset, 0-score);				
			}
		}
	}
	
}