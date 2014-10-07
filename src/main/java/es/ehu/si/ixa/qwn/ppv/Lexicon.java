package es.ehu.si.ixa.qwn.ppv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Lexicon {

	private Map<String, Polarity> lexicon = new HashMap<String, Polarity>();
	private int formaterror;
	
	public class Polarity {
		//private String scalar;
		private int scalar;
		private float numeric;
		
		/*
		 * Constructor, both numeric and scalar polarities are provided. Scalar polarity value is (-1|0|1)
		 */
		public Polarity(float pol, int pols){
			numeric = pol;						
			scalar=pols;	
		}	
		
		/*
		 * Constructor, only scalar polarity provided (pos|neg|neu). Numeric polarity is derived from the scalar value.
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
			else
			{
				System.err.println("scalar value is not a valid polarity\n");
			}			
		}	


		/*
		 * Constructor, only scalar polarity provided (-1|0|1). Numeric polarity derived from scalar value.
		 */
		public Polarity(int pols){
			//scalar polarity is normalized to -1|0|1 values
			scalar=pols;
			numeric = scalar;
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
			if (value.compareTo("neg") == 0)
			{
				scalar = -1;
			}
			else if (value.compareTo("pos") == 0)
			{
				//scalar = "pos";		
				scalar = 1;
			}
			else	
			{
				//scalar = "neu";
				scalar = 0;
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
	public Lexicon(String fname, String syn)
	{
		try {
			this.formaterror =0;
			loadLexicon(fname, syn);
		} catch (IOException e) {
			System.err.println("Lexicon class: error when loading lexicon from file: "+fname);
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
			//LexiconEntry entry = null;
			switch (fields.length)
			{
			// Only offset/lemma and polarity info
			case 2: 				
				boolean ok = addEntry(fields[0], fields[1], syn);
				//entry = new LexiconEntry(fields[0], fields[1], score);
				break;
			case 3:
				//third column contains polarity score
				ok = addEntry(fields[0],fields[2],syn); 
				if (!ok)
				{
					ok = addEntry(fields[0],fields[1],syn);
				}		
				break;
			case 4:
				if (syn.matches("(first|rank|mfs)"))
				{
					ok = addEntry(fields[0],fields[3], syn);
					if (!ok)
					{
						ok = addEntry(fields[0],fields[1],syn);
					}
				}
				else
				{
					String[] lemmas = fields[2].split(", ");
					for (String l : lemmas)
					{
						l = l.replaceFirst("#[0-9]+$","");
						ok = addEntry(l,fields[3], syn);
					}
				}
				//entry = new LexiconEntry(fields[0], fields[1], score, fields[2]);
				break;
			}
			//this.lexicon.add(entry);
		}
		lexreader.close();		
	}
	
	/*
	 * Add entry to lexicon. If the key already exists it replaces the polarity with the new value.
	 */
	private boolean addEntry (String key, String value, String syn)
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
			this.formaterror++;
			return false;
		}

		float numericScore=currentNumeric;
		int scalarScore = currentScalar;
		//numeric polarity 
		try {
			float score = Float.parseFloat(value);
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
		{
			//scalar polarity (pos| neg| neu)		
			value = value.substring(0,3);			
			if (value.compareTo("pos") == 0)
			{
				scalarScore= 1+currentScalar;
			}				
			else if (value.compareTo("neg") == 0)
			{
				scalarScore= -1+currentScalar;
			} 		
			else
			{				
				return false;
			}
			numericScore=scalarScore;
		}	
				
		// add/update entry in the lexicon.
		Polarity polar = new Polarity(numericScore, scalarScore);
		this.lexicon.put(key, polar);
		
		//System.err.println("added to lexicon: - "+key+" - "+numericScore+" - "+scalarScore);

		return true;
	}
	
	public int getScalarPolarity (String entrykey)
	{
		if (this.lexicon.containsKey(entrykey))
		{
			int result = lexicon.get(entrykey).getScalar();
			//scalar polarity is normalized to -1|0|1 values
			if (result > 0)
			{
				return 1;
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
