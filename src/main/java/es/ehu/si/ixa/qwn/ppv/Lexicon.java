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
		private String scalar;
		private float numeric; 
		public Polarity(String pol){
			String value = pol.substring(0,3).toLowerCase(); 
			if (value.compareTo("pos") == 0)
			{
				numeric = 1;
				scalar = value;
			}				
			else if (value.compareTo("neg") == 0)
			{
				numeric = -1;
				scalar = value;
			} 
			else if (value.compareTo("neu") == 0) 
			{
				numeric = 0;
				scalar = value;
			}
			else
			{
				System.err.println("scalar value is not valid a valid polarity\n");
			}			
		}	
		
		public Polarity(float pol){
			numeric = pol;
			if (numeric < 0)
				scalar = "neg";
			else if (numeric > 0)
				scalar = "pos";
			else
				scalar = "neu";			
		}	
		
		public float getNumeric(){
			return numeric;			
		}	
		
		public String getScalar(){
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
				if (syn.matches("(first|rank)"))
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
		float currentPol = 0;
		if (this.lexicon.containsKey(key))
		{
			currentPol = lexicon.get(key).getNumeric();
		}
		
		// control that lemma/sense in the lexicon is coherent with the lemma/sense mode selected by the user
		if ((key.matches("^[0-9]{4,}-[arsvn]$") && syn.compareTo("lemma") == 0) || (!key.matches("^[0-9]{4,}-[arsvn]$") && syn.matches("(first|rank)")))
		{
			this.formaterror++;
			return false;
		}

		
		//numeric polarity
		try {
			float score = Float.parseFloat(value);
			Polarity polar = new Polarity(currentPol+score);
			this.lexicon.put(key, polar);
			return true;
		}
		//scalar polarity (pos| neg| neu)
		catch (NumberFormatException ne)
		{
			value = value.substring(0,3);				
			if (value.compareTo("pos") == 0)
			{
				this.lexicon.put(key, new Polarity(1+currentPol));
			}				
			else if (value.compareTo("neg") == 0)
			{
				this.lexicon.put(key, new Polarity(-1+currentPol));
			} 
			else if (value.compareTo("neu") == 0) 
			{
				this.lexicon.put(key, new Polarity(currentPol));
			}
			else
			{
				return false;
			}			
			return true;
		}	
	}
	
	public String getScalarPolarity (String entrykey)
	{
		if (this.lexicon.containsKey(entrykey))
		{
			return lexicon.get(entrykey).getScalar();
		}
		else
		{
			return "unk";
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
