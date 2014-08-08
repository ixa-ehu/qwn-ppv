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
import java.util.Map;

import es.ehu.si.ixa.qwn.ppv.Lexicon;

public class AvgRatioEstimator {

	//private HashSet<LexiconEntry> lexicon = new HashSet<LexiconEntry> ();
	private Lexicon lexicon;
	private float threshold;
	private Map<String, Double> stats = new HashMap<String, Double>();
	private Map<String, String> kafResults = new HashMap<String, String>();
	/*
	 * Constructor: Lexicon object given.
	 */
	public AvgRatioEstimator (Lexicon Lex, String syn)
	{
		this.lexicon = Lex;
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");
		threshold = 0;
	}

	/*
	 * Constructor: Lexicon object given.
	 */
	public AvgRatioEstimator (Lexicon Lex, String syn, float thresh)
	{
		this.lexicon = Lex;
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");
		threshold = thresh;
	}
	
	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	public AvgRatioEstimator (String LexPath, String syn)
	{
		this.lexicon = new Lexicon(LexPath, syn);
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");
		threshold = 0;
	}

	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	public AvgRatioEstimator (String LexPath, String syn, float thresh)
	{
		this.lexicon = new Lexicon(LexPath, syn);
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");
		threshold = thresh;
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
		float score = 0;
		int sentimentTerms = 0;
		try {
			KAFDocument doc = KAFDocument.createFromFile(new File(fname));
			
			for (Term t : doc.getTerms())
			{				
				String lemma = t.getLemma();			
				
				String pol = lexicon.getScalarPolarity(lemma);
				if (pol.compareTo("unk") != 0)
				{
					Sentiment ts = t.createSentiment();
					ts.setPolarity(pol);
					score+= lexicon.getNumericPolarity(lemma);
					sentimentTerms++;
				}										
			}			
			doc.save(fname+".sent");			
			float avg = score / doc.getTerms().size();
			kafResults.put("taggedFile", fname+".sent");
			kafResults.put("sentTermNum",String.valueOf(sentimentTerms));
			kafResults.put("avg", String.valueOf(avg));
			kafResults.put("thresh", String.valueOf(threshold));
			if (avg > threshold)
			{
				kafResults.put("polarity", "pos");
			}
			else if (avg < threshold)
			{
				kafResults.put("polarity", "neg");
			}
			else
			{
				kafResults.put("polarity", "neu");
			}									
			
		} catch (FileNotFoundException fe) {
			System.err.println("AvgRatioEstimator: error when loading kaf file: "+fname);
			fe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("AvgRatioEstimator: error when loading kaf file: "+fname);
			ioe.printStackTrace();
		}							
		
		return this.kafResults;
	}
	
}
