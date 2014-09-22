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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehu.si.ixa.qwn.ppv.Lexicon;

public class AvgRatioEstimator {

	//private HashSet<LexiconEntry> lexicon = new HashSet<LexiconEntry> ();
	private Lexicon lexicon;
	private float threshold;
	private Map<String, Float> stats = new HashMap<String, Float>();
	private Map<String, String> kafResults = new HashMap<String, String>();
	private Map<String, String> ref_pols = new HashMap<String, String>();
	private Map<String, Float> ref_polCounts = new HashMap<String, Float>();
	private Map<String, Float> predicted_pols = new HashMap<String, Float>();
	private int synset = 0;	
	
	
	/*
	 * Constructor: Lexicon object given.
	 */
	public AvgRatioEstimator (Lexicon Lex, String syn)
	{
		this.lexicon = Lex;
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");
		this.setThreshold(0);
		this.setSynset(syn);
	}

	/*
	 * Constructor: Lexicon object given.
	 */
	public AvgRatioEstimator (Lexicon Lex, String syn, float thresh)
	{
		this.lexicon = Lex;
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");
		this.setThreshold(thresh);
		this.setSynset(syn);		
	}

	
	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	public AvgRatioEstimator (String LexPath, String syn)
	{
		this.lexicon = new Lexicon(LexPath, syn);
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");		
		this.setThreshold(0);
		this.setSynset(syn);
	}

	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	public AvgRatioEstimator (String LexPath, String syn, float thresh)
	{
		this.lexicon = new Lexicon(LexPath, syn);
		System.err.println("AvgRatioEstimator: lexicon loaded - "+lexicon.size()+" entries");
		this.setThreshold(thresh);
		this.setSynset(syn);
	}
	
	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	private void setSynset (String syn)
	{
		if (syn.compareTo("lemma") == 0)
			this.synset = 0;
		else if (syn.compareTo("first") == 0 || syn.compareTo("mfs") == 0)
			this.synset = 1;
		else if (syn.compareTo("rank") == 0)
			this.synset = 2;
		else
		{
			System.err.println("AvgRatioEstimator: incorrect sense/lemma option("+syn+"). System defaults to using lemmas\n");
			this.synset = 0;			
		}
	}
	
	/*
	 * Constructor: Lexicon path given as a string, load lexicon into the lexicon variable.
	 */
	public void setThreshold (float t)
	{
		this.threshold = t;
	}
	/*
	 * This is the core of the class, given the path to a corpus process it 
	 * and return the performance results
	 * 
	 * @ corpus: evaluation corpus 
	 * @ opt: if optimization mode should be entered (find the threshold that maximizes accuracy for the given corpus)
	 * @ weights: whether lexicon weights (if existing) should be used when computing polarity or only binary polarities (pos|neg). Default is to use binary polarities.
	 *  
	 */
	public Map<String, Float> processCorpus (String corpus, boolean opt, boolean weights) 
	{
		
		// clean all previous prediction and references stored in data structures
		this.cleanCorpusData();
		
		float pos = 0;
		float neg = 0;
		float neu = 0;
		int wordCount = 0;
		
		try {
			BufferedReader corpReader = new BufferedReader(new FileReader(corpus));
			String line;
			String docid ="";
			while ((line = corpReader.readLine()) != null) 
			{
				// document start
				Matcher match = Pattern.compile("<doc id=\"([^\"]+)\" pol=\"(neg|pos)\"( score=\"[0-9\\.]*\")?>").matcher(line);
				if (match.find())
				{				
					//store actual polarity.
					docid = match.group(1);
					String pol = match.group(2);
					ref_pols.put(docid, pol);
					//store actual polarity statistics
			        if (! ref_polCounts.containsKey(pol))
			        {
			        	ref_polCounts.put(pol, (float)1); 
			        }
			        else
			        {
			        	float i = ref_polCounts.get(pol)+1;
			        	ref_polCounts.put(pol,i);
			        }

					//initialize polarity word counts
				    pos=0;
				    neg=0;
				    neu=0;
				    wordCount=0;
				}
				// while no document/sentence ending tag comes count polarity words
				else if (! line.matches("^</doc>$"))
				{
					// ignore blank lines 
			        if (line.matches("^\\s*$"))
			        {
			            continue;
			        }
			        //System.err.println("kkkk --- "+line);
			        // Read word form analysis. IMPORTANT: this code is dependent on the output of FreeLing.
			        String[] fields = line.split("\\s+");
			        //fields(string form, String lemma, my $POS, my $POSprob, my $senseInfo) = split /\s+/, $l;
			        // senses come in this format WNsense1:score1/WNsense2:score2/...:...			        
			        String[] senses = fields[4].split("/");
			        //String form = fields[0];
			        String lemma = fields[1];
			        //String POStag = fields[2];
			        //String POSprob = fields [3];
			        
			        //All tokens are counted for the length of the document (including punctuation marks)
			        wordCount++;
			        
			        String wordPol="none";  
			        List<String> lookwords = new ArrayList<String>();
			        switch (this.synset)
			        {
			        //default is polarity is computed over lemmas
			        case 0: 
			        	lookwords.add(lemma+":1");
			        	break;
			       	// take as correct the first sense (the one with the highest score). 
			        // For the moment the score is normalized to 1 (for comparability with 
			        // previous experiments 2013/11/18. Inaki)
			        case 1:
			        	String sens=senses[0]; 
			        	sens.replaceFirst(":[^:]+$",""); 			        	
			        	lookwords.add(sens+":1");
			        	break;
			        // The whole ranking of possible senses returned by FreeLing (UKB) is taken into account.
			        case 2:
			        	lookwords = Arrays.asList(senses);
			        	break;
			        }
			        
			        wordPol= sensePolarity(lookwords);
			        if (wordPol.compareTo("none") != 0)
			        {
			        	float wscore = Float.parseFloat(wordPol);
			        	if (wscore > 0)  // positive
			        	{
			        		if (weights)
			        			pos+=wscore;
			        		else
			        			pos+=1;
			        	}
			        			
			        	else if (wscore < 0) // negative
			        	{	
			        		if (weights)
			        			neg+=wscore;
			        		else
			        			neg-=1;
			        	}
			        	else // neutral
			        	{
			        		if (weights)
			        			neu+=wscore;
			        		else
			        			neu+=1;
			        	}			        		
			        }
			        
				}
				// document/sentence end. Compute final polarity and store statistics regarding the document.
				else
				{
					// neg is a negative value, hence we add it to the positivity value.
					float avg = (pos + neg)*(float)1 / wordCount; 
					predicted_pols.put(docid, avg);
					//System.err.println(docid+" - "+avg+" - pos: "+pos+" -neg: "+neg+" - words: "+wordCount);					
				}			    
			}
			corpReader.close();
		} catch (FileNotFoundException e) {
			System.err.println("AvgRatioEstimator: error when loading corpus from file: "+corpus);
			e.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("AvgRatioEstimator: error when reading corpus from file: "+corpus);
			ioe.printStackTrace();
		}	
		
		int docCount=predicted_pols.size();
		System.err.println("AvgRatioEstimator: corpus processed ("+docCount+" elements). Polarity scores ready. \n");

		// compute statistics.
		if (! opt)
		{
			computeStatistics(this.threshold);
		}
		/*
		 *  Optimization mode: find the threshold that maximizes accuracy over the given corpus
		 *  with the current lexicon. 
		 *  Example: OpFinder_Strong and PangLee_movie_docs = 0.00585, BUT same corpus for Qwnet = 0.0655 
		 */		 
		else
		{
		    		    
		    //min and max values are of the threshold are the min and max scores. If threshold would result on one of those all elements would be classified under the same class.    
			float minValue = Collections.min(predicted_pols.values());
		    float topValue= Collections.max(predicted_pols.values());
		    // interval is 1/1000 portion between the minimum and maximum posible values.
		    float interval=(topValue-minValue)/1000;
		    		    
		    float current = minValue;
		    float optimum = minValue;
		    float maxAcc = 0;
			
		    System.err.println("polarityDetector_Base.pl: optimization mode entered : "
		    		+minValue+" - "+topValue+" in "+interval+" intervals\n");
			
		    while (current < topValue)
		    {
		        computeStatistics(current);

		        //print STDERR "\r$unekoa - $topValue";
		        if (stats.get("Acc") > maxAcc)
		        {
		            optimum=current;
		            maxAcc=stats.get("Acc");
		        }
		        current+=interval;
		    }
		    System.out.println(optimum+"- max Accuracy: "+maxAcc+" ----\n");
		    this.stats.clear();
		    this.stats.put("thresh", optimum);
		    this.stats.put("Acc", maxAcc);
		}
		return this.stats;
	}
	
	private void cleanCorpusData() {
		// TODO Auto-generated method stub
		this.stats.clear();
		this.kafResults.clear();
		this.ref_pols.clear();
		this.ref_polCounts.clear();
		this.predicted_pols.clear();
	}

	/*
	 * Compute system performance statistics (Acc, P, R, F) for the given corpus.
	 */
	private void computeStatistics(float threshold) {
		             
		float okPos=0;
	    float okNeg=0;
	    float predPos=0;
	    float predNeg=0;
	    float undefined=0;
	    
	    //initialize results matrix
	    this.stats.put("Ppos", (float) 0);
	    this.stats.put("Pneg", (float) 0);
	    this.stats.put("Rpos", (float) 0);
	    this.stats.put("Rneg", (float) 0);
	    this.stats.put("Fpos", (float) 0);
	    this.stats.put("Fneg", (float) 0);
	    
	    // compare predictions with the reference
	    for (String id : predicted_pols.keySet())
	    {
	    	// prediction = 0 => undefined / neutral?
	    	if (predicted_pols.get(id) == threshold )
	    	{
	    		undefined++;
	    	}
	    	// predicted as positive
	    	else if (predicted_pols.get(id) > threshold )
	    	{
	    		predPos++;
	    		//correct?
	    		if ( ref_pols.get(id).compareTo("pos") == 0 )
	    		{
	    			okPos++;
	    		}
	    	}
	    	// predicted as negative
	    	else if  (predicted_pols.get(id) < threshold ) 
	    	{
	    		predNeg++;
	    		if ( ref_pols.get(id).compareTo("neg") == 0 )
	    		{
	    			okNeg++;
	    		}
	    	} 
	    }
	    
	    // compute Acc , P, R, F
	    this.stats.put("predPos", (float) predPos);
	    this.stats.put("predNeg", (float) predNeg);
	    this.stats.put("undefined", (float) undefined);

	    //System.err.println(predPos+" pos, "+predNeg+" neg, "+undefined+" undef");
	    // calculate statistics: Accuracy | precision | recall |  f-score
	    int docCount=predicted_pols.size();
	    // ## Accuracy
	    this.stats.put("Acc", (float)((okPos + okNeg) * 1.0 / docCount)) ;   
	    // ## Positive docs' Precision
	    if (predPos > 0)
	    {
	    	this.stats.put("Ppos", (okPos / predPos));
	    }
	    // ## Negative docs' Precision
	    if (predNeg > 0)
	    {
	    	this.stats.put("Pneg", (okNeg / predNeg));
	    }
	    float div = ref_polCounts.get("pos"); 
	    // ## Positive docs' Recall    
	    if (div > 0)
	    {
	    	this.stats.put("Rpos", (okPos / div)); //#polCount_hash->{"pos"};  
	    }
	    div  = ref_polCounts.get("neg"); 
	    // ## Negative docs' Recall
	    if (div > 0)
	    {
	    	this.stats.put("Rneg", (okNeg / div)); //#polCount_hash->{"pos"};	          
	    }
	    // ## Positive docs' F-score
	    if ((this.stats.get("Ppos") + this.stats.get("Rpos")) > 0)
	    {
	    	this.stats.put("Fpos", (2 * this.stats.get("Ppos") * this.stats.get("Rpos") / (this.stats.get("Ppos") + this.stats.get("Rpos")))); 
	    }
	    // ## Negative docs' F-score
	    if ((this.stats.get("Pneg") + this.stats.get("Rneg")) > 0)
	    {
	    	this.stats.put("Fneg", (2 * this.stats.get("Pneg") * this.stats.get("Rneg") / (this.stats.get("Pneg") + this.stats.get("Rneg")))); 
	    }

	}

	/*
	 *  Compute the polarity of a lemma/sense based on its ranking of senses
	 */
	private String sensePolarity (List<String> senses)
	{
	    float polarity=0;
	    boolean found= false;
	    for (String s : senses)
	    {
	    	
	        String[] fields = s.split(":");
	        String sense = fields[0];
	        String scoreStr = "";
	        if (s.startsWith("::"))
	    	{
	        	scoreStr = fields[2];
	    		sense = ":";	
	    	}
	        else
	        {
	        	scoreStr = fields[1];
	        }
	    	//System.err.println("word to look for: "+sense+" : "+scoreStr+" - \n");

	        float senseScore = Float.parseFloat(scoreStr);

	        //System.err.println("word to look for: "+sense+" - "+senseScore+"\n");

	        //look up in the lexicon for the polarity of the words.
	        if (this.lexicon.getScalarPolarity(sense).compareTo("unk") != 0)
	        {	        	
	        	//System.err.println("word found! -"+sense);
	        	polarity+=lexicon.getNumericPolarity(sense)*senseScore;
	            found = true;
	        }
	    }
	    
	    // return polarity score, main program will interpret the score
	    if (found)
	    {
	        return String.valueOf(polarity);
	    }
	    else
	    {
	        return "none";
	    }


	}

	
	
	/*
	 * This function predicts the polarity of a text given in the KAF format. argument is the path to the KAF file 
	 * It saves tagged file to the given path + ".sent" extension.
	 * 
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
