package es.ehu.si.ixa.qwn.ppv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Lexicon {

	private Map<String, Polarity> lexicon = new HashMap<String, Polarity>();
	private int formaterror;
	private float minAbsPolarity;
	
	public class Polarity {
		//private String scalar;
		private int scalar;
		private float numeric;		
		
		/*
		 * Constructor, both numeric and scalar polarities are provided. Scalar polarity value is (-1|0|1).
		 * 2=modifier
		 * 3=shifter
		 * 
		 */
		public Polarity(float pol, int pols){
			numeric = pol;						
			scalar=pols;			
		}	
		
		/*
		 * Constructor, only scalar polarity provided (pos|neg|neu|mod|shi). Numeric polarity is derived from the scalar value.
		 */
		public Polarity(String pol){
			String value = pol.substring(0,3).toLowerCase(); 
			if (value.compareTo("pos") == 0)
			{				
				//scalar = value;
				scalar = 1;
				numeric = scalar;
			}				
			else if (value.compareTo("neg") == 0)
			{				
				scalar = -1;
				numeric = scalar;
			} 
			else if (value.compareTo("neu") == 0) 
			{				
				scalar = 0;
				numeric = scalar;
			}
			else if (value.compareTo("mod") == 0) 
			{				
				scalar = 2;
				numeric = 0;
			}
			else if (value.compareTo("shi") == 0) 
			{				
				scalar = 3;
				numeric = 0;
			}			
			else
			{
				System.err.println("scalar value is not a valid polarity\n");
			}			
		}	


		/*
		 * Constructor, only scalar polarity provided (-1|0|1|2|3). Numeric polarity derived from scalar value.
		 */
		public Polarity(int pols){
			//scalar polarity is normalized to -1|0|1 values
			scalar=pols;
			if (scalar < 2)
			{
				numeric = scalar;				
			}
			else
			{
				numeric = 0;
			}

		}	
		
		/*
		 * Constructor, only numeric polarity provided. Scalar polarity is derived from the numeric value
		 */
		public Polarity(float pol){
			// call the first constructor with and invalid scalar polarity, it will take care of the rest. 
			this(pol,123456789);
		}	
		
		/*
		 * Constructor, both numeric and scalar polarities are provided. Scalar polarity value is (pos|neu|neg)
		 */
		public Polarity(float pol, String pols){
			numeric = pol;
			String value = pols.substring(0,3).toLowerCase();
			switch (value)
			{
			case "neg": scalar = -1; break;
			case "pos": scalar = 1; break;
			case "neu": scalar = 0; break;
			case "mod": scalar = 2; numeric=0; break; //modifiers and shifters must have numeric polarity 0
			case "shi": scalar = 3; numeric=0; break;
			}
		}	
				
		
		public float getNumeric(){
			return numeric;			
		}	
		
		public int getScalar(){
			return scalar;			
		}	
	}
	
	/*
	 * constructor requires a path to the file containing the lexicon and 
	 */
	public Lexicon(File fname, String syn)
	{
		this(fname, syn, 0);
	}
	
	/*
	 * constructor requires a path to the file containing the lexicon and 
	 */
	public Lexicon(File fname, String syn, float minEntryPolarity)
	{
		try {
			this.formaterror=0;
			this.setMinAbsPolarity(minEntryPolarity);
			loadLexicon(fname, syn);
		} catch (IOException e) {
			System.err.println("Lexicon class: error when loading lexicon from file: "+fname);
			e.printStackTrace();
		}
	}
	
	/*
	 * Set minAbsPolarity variable. It determines the minimum absolute polarity score an entry shall have
	 * to accept it in the lexicon. (this is only for evaluation purposes.)
	 */
	private void setMinAbsPolarity (float m)
	{
		this.minAbsPolarity = m;
	}
	
	/**
	 * load a lexicon from a file given the file path. The format of the lexicon must be as follows:
	 * 
	 *  "offset<tab>(pos|neg|neu)<tab>lemma1, lemma2, lemma3, ...<tab>score<tab>..."	
	 * 
	 * 	First two columns are mandatory. Alternatively, firs column can contain lemmas instead of offsets.
	 * 
	 * @param LexiconPath
	 * @param syn
	 * @throws IOException
	 */
	private void loadLexicon(File LexiconPath, String syn) throws IOException
	{
		BufferedReader lexreader = new BufferedReader(new FileReader(LexiconPath));   		
		String line;
		while ((line = lexreader.readLine()) != null) 
		{
			if (formaterror > 10)
			{
				System.err.println("Lexicon class ERROR: too many format errors. Check that the specified lemma/sense is compatile with the format of the lexicon"
						+ "pass a valid lexicon or select the correct lemma/sense option\n");
				System.exit(formaterror);
			}
			
			if (line.matches("#") || line.matches("^\\s*$"))
			{
				continue;
			}
			String[] fields = line.split("\t");
			int ok;
			//LexiconEntry entry = null;
			switch (fields.length)
			{
			// not enough info, too few columns
			case 0: case 1:
				break;
			// Only offset/lemma and polarity info
			case 2: 				
				ok = addEntry(fields[0], fields[1], syn);
				//entry = new LexiconEntry(fields[0], fields[1], score);
				break;
			case 3:
				//third column contains polarity score
				try {
					float score = Float.parseFloat(fields[2]);
					ok = addEntry(fields[0],fields[1],fields[2],syn);
				}
				//third column contains lemmas, no polarity scores
				catch (NumberFormatException ne)
				{
					String[] lemmas = fields[2].split(", ");
					for (String l : lemmas)
					{
						l = l.replaceFirst("#[0-9]+$","");
						ok = addEntry(l,fields[1], syn);			
					}
				}
				break;
			// if the lexicon contains more than three columns is should have a standard format:
			// "offset<tab>(pos|neg|neu)<tab>lemma1, lemma2, lemma3, ...<tab>score<tab>..."	
			default:
				//third column contains lemmas, fourth column has polarity score
				if (syn.matches("(first|rank|mfs)"))
				{
					ok = addEntry(fields[0],fields[1],fields[3], syn);					
				}
				else
				{
					String[] lemmas = fields[2].split(", ");
					for (String l : lemmas)
					{
						l = l.replaceFirst("#[0-9]+$","");
						ok = addEntry(l,fields[1], fields[3], syn);						
					}
				}
				//entry = new LexiconEntry(fields[0], fields[1], score, fields[2]);
				break;
			}
			//this.lexicon.add(entry);
		}
		lexreader.close();			
	}
	
	/**
	 * Add entry to lexicon. If the key already exists it replaces the polarity with the new value.
	 *
	 * @param key : lemma or offset
	 * @param pol : scalar polarity (pos|neg|neu|mod|shi)
	 * @param syn : whether lemmas or senses (offset) should be stored in the lexicon.
	 * @return : int. 0 success, 1 error, 2 ok, but entry not included (no offset or lemma, or neutral polarity).
	 */
	private int addEntry (String key, String pol, String syn)
	{
		String score = "0"; 
		//scalar polarity (pos| neg| neu)
		if (pol.length() < 3)
		{ 
			return 1;
		}
		pol = pol.substring(0,3);			

		switch (pol)
		{
		case "neg": score = "-1"; break;
		case "pos": score = "1"; break;
		case "neu": score = "0"; break;
		case "mod": score = "2"; break; 
		case "shi": score = "3"; break;
		default: this.formaterror++; return 1;
		}

		return addEntry(key, pol, score, syn); 
	}
	/**
	 * Add entry to lexicon. If the key already exists it replaces the polarity with the new value.
	 * 
	 * @param key (String): lemma or synset to add to the lexicon.
	 * @param pol String (pos|neg|neu|mod|shi): polarity of the new entry 
	 * @param scoreValue String: polarity score
	 * @param syn String (lemma|first|mfs|rank): type of the entry (first, mfs and rank) are represented by WN synsets 

	 * @return int:
	 * 				- 0 = success
	 * 				- 1 = format error expected lemma and synset given, or viceversa, or polarity value is not valid.
	 * 				- 2 = entry not added (because synset is unknown or lemma is unavailable)
	 */
	private int addEntry (String key, String pol, String scoreValue, String syn)
	{		
		float currentNumeric = 0;
		int currentScalar = 0;
		if (this.lexicon.containsKey(key))
		{
			currentScalar = lexicon.get(key).getScalar();
			currentNumeric = lexicon.get(key).getNumeric();
			//System.err.println(key+" - "+currentScalar+" - "+currentNumeric);
		}
		
		// control that lemma/sense in the lexicon is coherent with the lemma/sense mode selected by the user
		if ((key.matches("^[0-9]{4,}-[arsvn]$") && syn.compareTo("lemma") == 0) || (!key.matches("^[0-9]{4,}-[arsvn]$") && syn.matches("(first|rank|mfs)")))
		{
			// u-00000 is the code used in a lexicon if no synset is found for a lemma. 
			// If found such an element the entry is ok, just ignore it (we are in synset mode)  
			if (key.matches("^u-[0]{4,}$"))
			{
				return 2;
			}
			// If format unknown there is a format error 
			else
			{
				this.formaterror++;
				return 1;
			}
		}
		
		//If lemma value is "Not available" it means there is no lemma for the current entry.
		if ((syn.compareTo("lemma") == 0)  && (key.matches("Not (Available|in Dictionary)")) )
		{
			//System.err.println(key+"- lemma not available.\n");
			return 2;
		}

		float numericScore=currentNumeric;
		int scalarScore = currentScalar;
		//numeric polarity 
		/*try {
			float score = Float.parseFloat(scoreValue);
			// if the entry does not reach the minimum required value do not include it in the lexicon.
			if (Math.abs(score) < this.minAbsPolarity)
			{
				//System.err.println("added to lexicon: - "+Math.abs(score)+" - "+this.minAbsPolarity);				
				return 2;			
			}
			numericScore =  numericScore+score;
			
			if (score < 0)
			{
				scalarScore = -1+currentScalar;	//"neg"
			}
			else if (score > 0)
			{
				scalarScore = 1+currentScalar; //"pos"
			}
						
			//Polarity polar = new Polarity(currentPol+score);
			//this.lexicon.put(key, polar);		
		}
		catch (NumberFormatException ne)
		{*/
			//scalar polarity (pos| neg| neu)
			if (pol.length() < 3)
			{ 
				return 1;
			}
			pol = pol.substring(0,3);			
			switch (pol)
			{
			case "neg":	scalarScore= -1+currentScalar; break;
			case "pos": scalarScore= 1+currentScalar; break;
			case "neu": break;
			case "mod": scalarScore = 0;break; //modifiers and shifters must have numeric polarity 0
			case "shi": scalarScore = 0; break;
			default: return 1;
			}
			numericScore=scalarScore;
		//}	
				
		// add/update entry in the lexicon.
		Polarity polar = new Polarity(numericScore, scalarScore);
		this.lexicon.put(key, polar);
		
		//System.err.println("added to lexicon: - "+key+" - "+numericScore+" - "+scalarScore);

		return 0;
	}
	
	public int getScalarPolarity (String entrykey)
	{
		if (this.lexicon.containsKey(entrykey))
		{
			int result = lexicon.get(entrykey).getScalar();
			//scalar polarity is normalized to -1|0|1 values
			if (result > 0)
			{
				return result;
			}
			else if (result < 0)
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return 123456789;
		}	
	}

	public float getNumericPolarity (String entrykey)
	{
		if (this.lexicon.containsKey(entrykey))
		{
			return lexicon.get(entrykey).getNumeric();
		}
		else
		{
			return (float) 123456789;
		}	
	}

	public int size()
	{
		return lexicon.size();
	}
	
	public void printLexicon()
	{
		for (String s : lexicon.keySet())
		{
			System.out.println(s+" - "+lexicon.get(s).getScalar());
		}
	}
	
}
