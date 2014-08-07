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

import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.Term.Sentiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import es.ehu.si.ixa.qwn.ppv.LexiconEntry;

public class AvgRatioEstimator {

	//private HashSet<LexiconEntry> lexicon = new HashSet<LexiconEntry> ();
	private Map<String, Float> lexicon = new HashMap<String, Float>();
	private Map<String, Double> stats = new HashMap<String, Double>();
	private Map<String, String> kafResults = new HashMap<String, String>();
	/*
	 * Constructor: Lexicon object given.
	 */
	public AvgRatioEstimator (Map<String, Float> Lex, String syn)
	{
		this.lexicon = Lex;
	}

	
	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	public AvgRatioEstimator (String LexPath, String syn)
	{
		try {
			loadLexicon(LexPath, syn);
			System.err.println("AvgRatioEstimator: lexicon loaded - "+this.lexicon.size()+" entries");
		} catch (IOException e) {
			System.err.println("AvgRatioEstimator: error when loading lexicon from file: "+LexPath);
			e.printStackTrace();
		}		
	}
	
	/*
	 * load a lexicon  
	 */
	private void loadLexicon(String LexiconPath, String syn) throws IOException
	{
		BufferedReader lexreader = new BufferedReader(new FileReader(LexiconPath));   		
		String line;
		while ((line = lexreader.readLine()) != null) 
		{
			if (line.matches("#") || line.matches("^\\s*$"))
			{
				continue;
			}
			String[] fields = line.split("\t");
			LexiconEntry entry = null;
			switch (fields.length)
			{
			// Only offset/lemma and polarity info
			case 2: 				
				boolean ok = addEntry2Lex(fields[0], fields[1]);					
				//entry = new LexiconEntry(fields[0], fields[1], score);
				break;
			case 3:
				//third column contains polarity score
				ok = addEntry2Lex(fields[0],fields[2]);
				if (!ok)
				{
					ok = addEntry2Lex(fields[0],fields[1]);
				}		
				break;
			case 4:
				ok = addEntry2Lex(fields[0],fields[3]);
				//entry = new LexiconEntry(fields[0], fields[1], score, fields[2]);
				break;
			}
			//this.lexicon.add(entry);
		}
		lexreader.close();		
	}
	
	/*
	 * This is the core of the class, given the path to a corpus process it 
	 * and return the performance results
	 */
	public Map<String, Double> processCorpus (String corpus) 
	{
		try {
			BufferedReader corpReader = new BufferedReader(new FileReader(corpus));
		} catch (FileNotFoundException e) {
			System.err.println("AvgRatioEstimator: error when loading corpus from file: "+corpus);
			e.printStackTrace();
		}	
				
		return this.stats;
	}
	
	/*
	 * This is the core of the class, given the path to a corpus process it 
	 * and return the performance results
	 */
	public Map<String, String> processKaf (String fname) 
	{
		try {
			KAFDocument doc = KAFDocument.createFromFile(new File(fname));
			
			for (Term t : doc.getTerms())
			{				
				String lemma = t.getLemma();			
				
				String pol = getLexiconPolarity(lemma);
				if (pol.compareTo("unk") != 0)
				{
					Sentiment ts = t.createSentiment();
					ts.setPolarity(pol);
				}										
			}
			
			doc.save(fname+".sent");
			
			
		} catch (FileNotFoundException fe) {
			System.err.println("AvgRatioEstimator: error when loading kaf file: "+fname);
			fe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("AvgRatioEstimator: error when loading kaf file: "+fname);
			ioe.printStackTrace();
		}							
		
		return this.kafResults;
	}
	
	private boolean addEntry2Lex (String key, String value)
	{
		float currentPol = 0;
		if (this.lexicon.containsKey(key))
		{
			currentPol = lexicon.get(key);
		}
		
		//numeric polarity
		try {
			float score = Float.parseFloat(value);
			this.lexicon.put(key, currentPol+score);
			return true;
		}
		//scalar polarity (pos| neg| neu)
		catch (NumberFormatException ne)
		{
			value = value.substring(0,3);			
			if (value.compareTo("pos") == 0)
			{
				this.lexicon.put(key, (1+currentPol));
			}				
			else if (value.compareTo("neg") == 0)
			{
				this.lexicon.put(key, (-1+currentPol));
			} 
			else if (value.compareTo("neu") == 0) 
			{
				this.lexicon.put(key, currentPol);
			}
			else
			{
				return false;
			}			
			return true;
		}	
	}
	
	private String getLexiconPolarity (String entrykey)
	{
		if (this.lexicon.containsKey(entrykey))
		{
			float score = lexicon.get(entrykey);
			if (score > 0)
			{
				return "pos";			
			}
			else if (score < 0)
			{
				return "neg";				
			}
			else
			{
				return "neu";
			}				
		}
		else
		{
			return "unk";
		}	
	}
}
