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


package es.ehu.si.ixa.qwn.ppv.eval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import es.ehu.si.ixa.qwn.ppv.LexiconEntry;

public class AvgRatioEstimator {

	private HashSet<LexiconEntry> lexicon = new HashSet<LexiconEntry> ();
	private Map<String, Double> stats = new HashMap<String, Double>();
	
	/*
	 * Constructor: Lexicon object given.
	 */
	public AvgRatioEstimator (HashSet<LexiconEntry> Lex)
	{
		this.lexicon = Lex;
	}

	
	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	public AvgRatioEstimator (String LexPath)
	{
		loadLexicon(LexPath);
	}
	
	/*
	 * load a lexicon is 
	 */
	private void loadLexicon(String LexiconPath)
	{
		try 
		{
			BufferedReader lexreader = new BufferedReader(new FileReader(LexiconPath));
    	
			String line;
			while ((line = lexreader.readLine()) != null) 
			{
				String[] fields = line.split("\t");
				LexiconEntry entry = null;
				switch (fields.length)
			    {
				// Only offset and polarity info
			    case 2: 
			    	entry = new LexiconEntry(fields[0], fields[1]); 
			   	// Only offset and polarity info
			    case 3:
			    	//third column contains polarity score
					try {
						float score = Float.parseFloat(fields[2]);
						entry = new LexiconEntry(fields[0], fields[1], score);
					}
					//third column contains lemmas
					catch (NumberFormatException ne)
					{
			            entry = new LexiconEntry(fields[0], fields[1], fields[2]);
					}
				case 4:
					float score = Float.parseFloat(fields[3]);
					entry = new LexiconEntry(fields[0], fields[1], score, fields[2]);
				}
				this.lexicon.add(entry);
			}
			lexreader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("AvgRatioEstimator: error when loading lexicon from file: "+LexiconPath);
			e.printStackTrace();			
		}
	}
	
	/*
	 * This is the core of the class, given the path to a corpus process it 
	 * and return the performance results
	 */
	public Map<String, Double> processCorpus (String corpus)
	{
		
		return this.stats;
	}
	
}
