package es.ehu.si.ixa.qwn.ppv;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class LexiconEntry {

	private String offset ="";
	private String polarity = "";
	private Float score = (float) 0.0; 
	private Set<String> lemmas = new HashSet<String>();
	
	public LexiconEntry(String offset1, String pol)
	{
		this.offset = offset1;
		if (pol.compareTo("neg") == 0 || pol.compareTo("pos") == 0 || pol.compareTo("neu") == 0)
		{
			this.polarity  = pol;
		}
		else
		{
			System.err.println("LexiconEntry.java: error creating lexiconEntry object, polarity value must be [pos|neg|neu]" );
			System.exit(1);
		}
		
	}
	
	/*
	 *  Constructor: Entry without lemmas (only synset level score 
	 */
	public LexiconEntry(String offset1, String pol, Float s)
	{
		this(offset1,pol);
		this.score = s;
	}
	
	/*
	 *  Constructor: Entry without scores (only labeled polarities [pos|neg|neu] 
	 */
	public LexiconEntry(String offset1, String pol, HashSet<String> lemmaSet)
	{
		this(offset1,pol);
		this.lemmas = lemmaSet;		
	}
	
	/*
	 *  Constructor: Entry without scores (only labeled polarities [pos|neg|neu] 
	 */
	public LexiconEntry(String offset1, String pol, String lemmaStr)
	{
		this(offset1,pol);
		this.lemmas = this.splitLemmas(lemmaStr);		
	}
	
	/*
	 *  Constructor: Entry without scores (only labeled polarities [pos|neg|neu] 
	 */
	public LexiconEntry(String offset1, String pol, Float s, HashSet<String> lemmaSet)
	{
		this(offset1,pol);
		this.lemmas = lemmaSet;		
		this.score = s;
	}

	/*
	 *  Constructor: Entry without scores (only labeled polarities [pos|neg|neu] 
	 */
	public LexiconEntry(String offset1, String pol, Float s, String lemmaStr)
	{
		this(offset1,pol);
		this.lemmas = this.splitLemmas(lemmaStr);		
		this.score = s;
	}

	/*
	 * Return offset (e.g. wordnet synset code)
	 */
	public String getOffset()
	{
		return this.offset;
	}
	
	/*
	 * Return polarity 
	 */
	public String getPolarity()
	{
		return this.polarity;
	}
	
	/*
	 * Return score
	 */
	public Float getScore()
	{
		return this.score;
	}
	
	/*
	 * Add a lemma to the lemma list
	 */
	public void addLemma(String lemma)
	{
		this.lemmas.add(lemma);
	}

	/*
	 * Add a complete lemma list
	 */
	public void addLemmas(HashSet<String> lemmalist)
	{
		this.lemmas = lemmalist;
	}
	
	private HashSet<String> splitLemmas (String lems)
	{
		HashSet<String> hSet = new HashSet<String>();
        StringTokenizer st = new StringTokenizer(lems, ",");
        while (st.hasMoreTokens())
        {
        	hSet.add(st.nextToken());
        }			            
        return hSet;
	}

}
