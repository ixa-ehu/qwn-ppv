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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.zip.GZIPInputStream;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;



import org.jdom2.JDOMException;

/*
 * lexicon creation classes
 */
import es.ehu.si.ixa.qwn.ppv.ContextCreator;
import es.ehu.si.ixa.qwn.ppv.UKBwrapper;

/*
 * lexicon evaluation classes
 */
import es.ehu.si.ixa.qwn.ppv.Evaluator;
/*
 * Kaf document processing libraries
 */
import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.WF;


/**
 * Main class of qwn-ppv provides several configuration parameters:
 *
 * <ol>
 * <li>Seed list: choose language to create the lang attribute in KAF header.</li>
 * <li>Graph: Graph to use for propagation 4 graphs are available (SynAnt | mcr | mcr-ant | mcr-antGloss).</li>
 * <li>Test set (NOT YET IMPLEMENTED - optional): if a test set is provided, the system evaluates the
 *     lexicons generated, and returns which of them performs best. </li>
 * </ol>
 *
 *
 * @author isanvicente
 * @author ragerri
 * @version 2014-05-14
 */
public class CLI {

	/**
	 * Get dynamically the version of qwn-ppv by looking at the MANIFEST
	 * file.
	 */
	//private final String version = CLI.class.getPackage().getImplementationVersion();
	private final String version = "1.0";
	Namespace parsedArguments = null;

	// create Argument Parser
	ArgumentParser argParser = ArgumentParsers.newArgumentParser(
			"qwn-ppv-" + version + ".jar").description(
					"qwn-ppv-" + version
					+ " is a propagation algorithm for creating multilingual polarity lexicons developed by IXA NLP Group.\n");

	/**
	 * Sub parser instance.
	 */
	private Subparsers subParsers = argParser.addSubparsers().help(
			"sub-command help");

	/**
	 * The parser that manages the create sub-command.
	 */
	private Subparser compileParser;
	/**
	 * The parser that manages the create sub-command.
	 */
	private Subparser creationParser;
	/**
	 * The parser that manages the evaluation sub-command.
	 */
	private Subparser evalParser;

	/**
	 * Parser that manages the polarity tagging and estimation of a text (kaf format for the moment).
	 */
	private Subparser predictParser;

	private Subparser kafParser;

	public CLI() {
		compileParser = subParsers.addParser("compile").help("Graph compilation CLI");
		loadCompilationParameters();
		creationParser = subParsers.addParser("create").help("Lexicon creation CLI");
		loadCreationParameters();
		evalParser = subParsers.addParser("eval").help("Lexicon evaluation CLI");
		loadEvalParameters();
		predictParser = subParsers.addParser("predict").help("Predict polarity of a text");
		loadPredictionParameters();
		kafParser = subParsers.addParser("kaf").help("Kaf document library test");
	}


	public static void main(String[] args) throws IOException {

		CLI cmdLine = new CLI();
		cmdLine.parseCLI(args);
	}

	/**
	 * Parse the command interface parameters with the argParser.
	 *
	 * @param args
	 * the arguments passed through the CLI
	 * @throws IOException
	 * exception if problems with the incoming data
	 */
	public final void parseCLI(final String[] args) throws IOException {
		try {
			parsedArguments = argParser.parseArgs(args);
			//System.err.println("CLI options: " + parsedArguments);

			if (args[0].equals("create")){
				create(System.out);
			}
			else if (args[0].equals("eval")){
				eval();
			}
			else if (args[0].equals("compile")){
				compileGraph();
			}
			else if (args[0].equals("predict")){
				predictPolarity();
			}
			else if (args[0].equals("kaf")){
				processKaf(System.in, System.out);
			}


		} catch (ArgumentParserException e) {
			argParser.handleError(e);
			System.err.println("Run java -jar target/ixa-qwn-ppv-" + version
					+ ".jar (compile|create|eval) -help for details");
			//e.printStackTrace();
			System.exit(1);
		} catch (JDOMException e) {
			System.err.println("Error when processing kaf:\n Run java -jar target/ixa-qwn-ppv-" + version
					+ ".jar (compile|create|eval) -help for details");
			e.printStackTrace();
		}
	}


	public final void compileGraph() throws IOException {
		String kb = parsedArguments.getString("kb");
		String ukb = parsedArguments.getString("ukbPath");


		// normal case: single graph is created
		if (!kb.equals("all")) 
		{
			ukb_compile(ukb,kb);
			System.out.println("new graph generated: "+kb+".bin");
		}
		// compile all graphs in the distribution
		else
		{
			System.out.println("Default behavior: all the graphs in the qwn-ppv distribution will be compiled");
			Properties AvailableGraphs = new Properties();
			try {
				AvailableGraphs.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("graphs.txt"));
			}catch (IOException ioe){
				ioe.printStackTrace(); 
			}

			for(String key : AvailableGraphs.stringPropertyNames()) 
			{			  
				String jarlocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
				String location = jarlocation.substring(0, jarlocation.lastIndexOf("/"));
				String destKBPath = location+File.separator+"graphs"+File.separator+(String) AvailableGraphs.get(key);

				try {						
					GZIPInputStream kbExtract =  new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("graphs/"+(String) AvailableGraphs.get(key)+".txt.gz"));
					File KBPath = new File(location+File.separator+"graphs");
					KBPath.mkdirs();
					OutputStream kbDestination = new FileOutputStream(destKBPath+".txt");				  
					IOUtils.copy(kbExtract, kbDestination);
					kbExtract.close();
					kbDestination.close();
				}catch (IOException ioe){					  
					ioe.printStackTrace();
				}
				ukb_compile(ukb,destKBPath+".txt");
			}	
		}
	}// end compileGraph



	private void loadCompilationParameters() {
		/*
		 *  Parameters:
		 *     - Knowledge base (-k | --kb=): path to the file containing the knowledge base information to construct the graph, UKB format. If no kb info is provided
		 *     - UKB path (-p | --execPath=): path to the executables of ukb software. 
		 */

		// specify the graph which shall be used for propagation
		compileParser.addArgument("-k","--kb")		    	
		.required(false).setDefault("all")
		.help("a KB information file in UKB format is needed to create a graph. If no KB argument is given, the system will compile the default graphs provided by QWN-PPV.\n"
				+ "NOTE THAT this should be done the first time you run qwn-ppv, as the graphs shall be compiled for your platform. \n"
				+ "QWN-PPV assumes UKB software has been previously installed, and it is in the paths. If not it will rise an error.\n"
				+ "Possible values are:\n"
				+ "\t - all: all graphs will compiled. 	Same behaviour as giving no -k argument.\n"
				+ "\t - synAnt: synonymy and antonymy graphs are used for propagation\n"
				+ "\t - mcr: graph built using all relations in MCR3.0\n"
				+ "\t - mcr-ant: graph built using all relations in MCR3.0, except for the antonymy relations\n"
				+ "\t - mcr-antGloss: graph built using all relations in MCR3.0, except antonymy and Gloss information\n"
				+ "\nFOR ADVANCED USERS ONLY!!\n"
				+ "The abovementioned graphs are those provided by the package. Additionally custom graphs may be compiled for using them with QWN-PPV."
				+ " To use custom graphs, you have to pass the path to the custom graph as the value of the -k option:\n"				
				+ "\t - /path/to/custom/graph.txt : path to the custom graph you want to compile."
				+ " NOTE that the graph must be in UKB compatible format.\n");


		// specify the path to the ukb executables, if no path is given default location is /usr/local/bin
		compileParser.addArgument("-u","--ukbPath")		    	
		.required(false).setDefault("/usr/local/bin")
		.help("UKB software path. If no path is specified WN-PPV assumes that UKB software"
				+ " has been previously installed in /usr/local/bin\n");

	}


	/**
	 * BufferedReader (from standard input) and BufferedWriter are opened. The
	 * module takes a plain text containing positive and negative words from standard input, and 
	 * produces a polarity lexicon in tabulated format. 
	 * Resulting lexicon is returned through standard output.
	 *
	 * @param args
	 * @throws IOException
	 **/
	public final void create(	final OutputStream outputStream) throws IOException {

		String seeds = parsedArguments.getString("seeds");
		String lang = parsedArguments.getString("lang");
		String graph = parsedArguments.getString("graph");
		boolean w = parsedArguments.getBoolean("weights");	    
		String ukbPath = parsedArguments.getString("ukbPath");

		System.err.println("qwn-ppv: received arguments are: \n Lang: "+lang+"\n Graph: "+graph+"\n");

		BufferedReader breader = null;
		BufferedWriter bwriter = null;

		try{
			//input seed list from standard input
			breader = new BufferedReader(new FileReader(seeds));
			//resulting lexicon to standard output 
			bwriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

			//temporal directory used for UKB related files.
			File UKBTempDir = createTempDirectory();


			// arrays to store propagation filenames needed for merging positive and negative propagations in order to obtain the lexicon.
			Set<String> posPropagPaths = new HashSet<String>();
			Set<String> negPropagPaths = new HashSet<String>();

			//create temporal files to store contexts
			String ctxtPosPath = UKBTempDir.getAbsolutePath()+File.separator+"pos_ctx.qwnppv";
			String ctxtNegPath = UKBTempDir.getAbsolutePath()+File.separator+"neg_ctx.qwnppv";


			//1. STEP: CONTEXT CREATION USING SEEDS 
			//create context to initialize UKB propagation algorithm. Depending on the graph 2 or for context files will be returned
			ContextCreator ctxt = new ContextCreator(w);		
			//ctxt.createContexts(breader, bw_pos, bw_neg);

			//2. STEP: PROPAGATIONS 
			UKBwrapper Propagation = new UKBwrapper(lang, UKBTempDir.getAbsolutePath());
			Propagation.setUKBPath(ukbPath);
			//"synAnt" graph requires 4 propagations (posSyn, negSyn, posAnt, negAnt)
			if (graph.equals("synAnt"))
			{
				ctxt.setKBFile("mcr30_Syn");
				ctxt.createContexts(breader, ctxtPosPath, ctxtNegPath);

				Propagation.setGraph("mcr_syn");
				Propagation.propagate(ctxtPosPath);
				renameFile(UKBTempDir.getAbsolutePath()+File.separator+"ctx_01.ppv", UKBTempDir.getAbsolutePath()+File.separator+"syn_pos.ppv");
				posPropagPaths.add(UKBTempDir.getAbsolutePath()+File.separator+"syn_pos.ppv");

				Propagation.propagate(ctxtNegPath);
				renameFile(UKBTempDir.getAbsolutePath()+File.separator+"ctx_01.ppv", UKBTempDir.getAbsolutePath()+File.separator+"syn_neg.ppv");
				negPropagPaths.add(UKBTempDir.getAbsolutePath()+File.separator+"syn_neg.ppv");

				ctxt.setKBFile("mcr30_Ant");
				ctxt.createContexts(breader, ctxtPosPath, ctxtNegPath);

				Propagation.setGraph("mcr_ant");	    	
				Propagation.propagate(ctxtPosPath);
				renameFile(UKBTempDir.getAbsolutePath()+File.separator+"ctx_01.ppv", UKBTempDir.getAbsolutePath()+File.separator+"ant_pos.ppv");
				negPropagPaths.add(UKBTempDir.getAbsolutePath()+File.separator+"ant_pos.ppv");

				Propagation.propagate(ctxtNegPath);
				renameFile(UKBTempDir.getAbsolutePath()+File.separator+"ctx_01.ppv", UKBTempDir.getAbsolutePath()+File.separator+"ant_neg.ppv");
				posPropagPaths.add(UKBTempDir.getAbsolutePath()+File.separator+"ant_neg.ppv");

			}
			//The rest of the graphs require only 2 propagations (positive seeds & negative seeds)	    	
			else
			{
				ctxt.setKBFile(graph);
				ctxt.createContexts(breader, ctxtPosPath, ctxtNegPath);

				Propagation.setGraph(graph);	    	

				Propagation.propagate(ctxtPosPath);
				renameFile(UKBTempDir.getAbsolutePath()+File.separator+"ctx_01.ppv", UKBTempDir.getAbsolutePath()+File.separator+"prop_pos.ppv");
				posPropagPaths.add(UKBTempDir.getAbsolutePath()+File.separator+"prop_pos.ppv");

				Propagation.propagate(ctxtNegPath);
				renameFile(UKBTempDir.getAbsolutePath()+File.separator+"ctx_01.ppv", UKBTempDir.getAbsolutePath()+File.separator+"prop_neg.ppv");
				negPropagPaths.add(UKBTempDir.getAbsolutePath()+File.separator+"prop_neg.ppv");

			}

			//3. STEP: MERGE UKB PROPAGATIONS 
			PropagationCombinator combinator = new PropagationCombinator();
			SortedSet<String> Lexicon = combinator.sinpleCombinator(posPropagPaths, negPropagPaths);

			//print lexicon to bwriter
			for (String s : Lexicon)
			{
				bwriter.write(s+"\n");
			}

			// delete all temporal files used in the process.
			FileUtils.deleteDirectory(UKBTempDir);   	

		} catch (IOException e) {
			e.printStackTrace();
		} 

		bwriter.close();
		breader.close();
		System.out.println("qwn-ppv execution finished. Lexicons are ready.\n");
	}

	private void loadCreationParameters() {
		/*
		 *  Parameters:
		 *     - Input File (standard input): File containing the seed words used for lexicon creations and propagation. Format is tabulated, one word/synset per line: word/synset<tab>[pos|neg]<tab>weight.      
		 *     - Synset polarities (-s | --synset=): default propagation of seed polarities are done with lemmas. With this option polarity of synsets are used instead of lemmas.
		 *     - Graph (-g | --graph=): graph to use in for propagation of seed polarities. possible values are: [synAnt|mcr|mcr-ant|mcr-antGloss]. If the parameter doesn't much one of these values, it is assumed that the value provided contains the path to a precompiled ukb compatible graph, which will be used for propagation. This way custom graph may be used. 
		 *     - Dictionary weights (-w | --weights): if the seeds have polarity strength info use it when initializing the pageRank propagation. Default behavior is to not use weights  
		 *     - Language (-l | --lang): language of the seed words and the resulting polarity lexicon.       
		 * 
		 */

		// specify language for the lexicons
		creationParser.addArgument("-s", "--seeds")
		.required(true).help("path to the file containing the seeds to start qwn-ppv propagations process.\n"
				+ "\tSeed file must contain one seed-term per line with the following format:\n"
				+ "\tterm1<tab>[pos|neg]\n"
				+ "\tAdditionally seed can contain weight values >=0 :\n"
				+ "\tterm1<tab>[pos|neg]<tab>weight\n");

		// specify language for the lexicons
		creationParser.addArgument("-l", "--lang")
		.choices("en", "es", "eu", "cat", "gl","custom")
		.required(true).help("It is REQUIRED to choose a language to generate lexicons in a specific language with qwn-ppv.\n");

		// specify the graph which shall be used for propagation
		creationParser.addArgument("-g","--graph")		    	
		.required(false).setDefault("synAnt")
		.help("A graph is REQUIRED to propagate polarity information over it, if no graph is specified the system defaults to MCR synonymy and antonymy graphs (synAnt).\n"
				+ "possible values are:\n"
				+ "\t - synAnt: synonymy and antonymy graphs are used for propagation\n"
				+ "\t - mcr: graph built using all relations in MCR3.0\n"
				+ "\t - mcr-ant: graph built using all relations in MCR3.0, except for the antonymy relations\n"
				+ "\t - mcr-antGloss: graph built using all relations in MCR3.0, except antonymy and Gloss information\n"
				+ "\nFOR ADVANCED USERS ONLY!!\n"
				+ "The abovementioned graphs are those provided by the package. Additionally custom graphs may be used for propagation."
				+ " To use custom graphs, you have to pass the path to the custom graph as the value of the -g option\n"				
				+ "\t - /path/to/custom/graph.bin : path to the custom graph you want to use for propagation."
				+ " NOTE that the graph must be in UKB compatible binary format.\n");

		creationParser.addArgument("-w", "--weights")
		.action(Arguments.storeTrue())
		.help(
				"Use weights when initializing the propagation algorithm.\n"
						+ "If the seed list does not provide polarity weights the program assigns the default value 1 to all the seeds.\n"
						+ "If weights are used all seeds must contain a weight value >= 0 (do not leave lines without weights).\n");


		// specify the path to the ukb executables, if no path is given default location is /usr/local/bin
		creationParser.addArgument("-u","--ukbPath")		    		
		.required(false).setDefault("/usr/local/bin")
		.help("UKB software path. If no paht is specified WN-PPV assumes that UKB software"
				+ " has been previously installed in /usr/local/bin\n");
		
		//String usageMessage = creationParser.formatUsage().trim()+" < seeds.txt\n\noutput: Standard output.";
		//creationParser.usage(usageMessage);
	}

	public final void eval() throws IOException {

		String corpus = parsedArguments.getString("corpus");
		String test = parsedArguments.getString("testfile");
		String lexicon = parsedArguments.getString("lexicon");
		String estimator = parsedArguments.getString("estimator");
		String synset = parsedArguments.getString("synset");	    
		boolean shouldOptimize = parsedArguments.getBoolean("optimize");
		boolean weights = parsedArguments.getBoolean("weights");
		float thres = Float.parseFloat(parsedArguments.getString("threshold"));
		float lexThres = Float.parseFloat(parsedArguments.getString("lexScoreThreshold"));
		
		// parameter control. 
		if (synset.equals("mfs"))
		{
			System.err.println("\nWARNING: You have selected to check polarity based on the most frequent sense (MFS). "
					+ "Take into account that MFS works as if it were the first sense. It is left up to the "
					+ "user to provide a corpus which has the MFS as the first sense in the annotated corpus");
		}
		// parameter combination control. Mohammad's evaluation is always done without optimization.
		if (shouldOptimize && estimator.equals("moh"))
		{
			System.err.println("\nWARNING: Mohammad's (2009) polarity score is directly applied over the given corpus, "
					+ "it does not require optimization. Thus, optimization WILL NOT BE PERFORMED,"
					+ "and testfile WILL NOT BE USED.");
			shouldOptimize = false;
		}

		// parameter combination control. Mohammad's evaluation threshold is always 0.
		if (thres !=0 && estimator.equals("moh"))
		{
			System.err.println("\nWARNING: Mohammad's (2009) polarity score needs no threshold."
					+ " It WILL NOT BE taken into account.");
			thres = 0;
		}
				
		
		System.out.println("QWN-PPV: Lexicon evaluator: ");

		File lexFile = new File(lexicon);
		
		// Given lexicon path does not exist, print error and exit;
		if (! lexFile.exists())
		{
			System.out.println("QWN-PPV: Given lexicon does not exist. Evaluation can not continue.\n");
			System.exit(1);
		}
		// Given lexicon path is a single file;
		else if (lexFile.isFile())
		{			
			System.out.println("\tcorpus: "+corpus+"\n\tlexicon: "+lexFile.getAbsolutePath()+"\n\tSense information: "+synset+"\n");
			System.err.println("\tcorpus: "+corpus+"\n\tlexicon: "+lexFile.getAbsolutePath()+"\n\tSense information: "+synset+"\n");
			
			//load Lexicon
			Lexicon lex = new Lexicon(lexFile, synset, lexThres);		
			//create evaluator object
			Evaluator evalCorpus = new Evaluator(lex, synset, estimator);
			evalCorpus.setThreshold(thres);		
			Map<String, Float> results = evalCorpus.processCorpus(corpus, shouldOptimize, weights);
			if (shouldOptimize && test != "")
			{
				System.out.println("optimization finished, starting test");
				evalCorpus.setThreshold(results.get("thresh"));
				results = evalCorpus.processCorpus(test, false, weights);
			}	 
			//System.out.println("eval avg done"+results.toString());

			System.out.println("\tThreshold = "+results.get("thresh")+"\n\t"+
					results.get("predPos")+" labeled as positive\t"+results.get("predNeg")+" labeled as negative\t "+results.get("undefined")+" undefined. \n"
					+ "\tAccuracy => "+results.get("Acc")+"\n\tPositive docs: P => "+results.get("Ppos")+"\tR => "+results.get("Rpos")+"\tF => "+results.get("Fpos")+"\n"
					+ "\tNegative docs: P => "+results.get("Pneg")+"\tR => "+results.get("Rneg")+"\tF => "+results.get("Fneg")+" \n");
		}
		// Given lexicon path is a directory;
		else if (lexFile.isDirectory())
		{
			//get all files with the *.dict extension and evaluate them)
			String[] allowedDictExtensions = {"dict"};
			Iterator<File> it = FileUtils.iterateFiles(lexFile, allowedDictExtensions, false);
			
			while (it.hasNext())
			{
				File itLex = it.next();
				System.out.println("\tcorpus: "+corpus+"\n\tlexicon: "+itLex.getAbsolutePath()+"\n\tSense information: "+synset+"\n");
				System.err.println("\tcorpus: "+corpus+"\n\tlexicon: "+itLex.getAbsolutePath()+"\n\tSense information: "+synset+"\n");

				
				//load Lexicon
				Lexicon lex = new Lexicon(itLex, synset, lexThres);		
				
				//create evaluator object
				Evaluator evalCorpus = new Evaluator(lex, synset, estimator);
				evalCorpus.setThreshold(thres);		
				Map<String, Float> results = evalCorpus.processCorpus(corpus, shouldOptimize, weights);
				if (shouldOptimize && test != "")
				{
					System.out.println("optimization finished, starting test");
					evalCorpus.setThreshold(results.get("thresh"));
					results = evalCorpus.processCorpus(test, false, weights);
				}	 
				//System.out.println("eval avg done"+results.toString());

				System.out.println("\tThreshold = "+results.get("thresh")+"\n\t"+
						results.get("predPos")+" labeled as positive\t"+results.get("predNeg")+" labeled as negative\t "+results.get("undefined")+" undefined. \n"
						+ "\tAccuracy => "+results.get("Acc")+"\n\tPositive docs: P => "+results.get("Ppos")+"\tR => "+results.get("Rpos")+"\tF => "+results.get("Fpos")+"\n"
						+ "\tNegative docs: P => "+results.get("Pneg")+"\tR => "+results.get("Rneg")+"\tF => "+results.get("Fneg")+" \n");

			}

		}
				
		System.out.println("QWN-PPV: Lexicon evaluator: End.");	
	}

	private void loadEvalParameters() {
		/*
		 *  Parameters:
        - Input File (-c | --corpus= ): File containing the texts whose polarity we want to estimate. documents must be separated by "<doc (id=.*)? (pol=.*)? (scrore=.*)?>text</doc>" tags.    
        - dict file  (-l | --lexicon= ): path to the polarity lexicon.
        - Synset polarities (-s | --synset=): default polarities are calculated over lemmas. With this option polarity of synsets is taken into account instead of words. It has two posible values: (first|rank). 'first' uses the sense with the highest confidence value for the lemma. 'rank' uses complete ranking of synsets.
        - Dictionary weights (-w | --weights): use polarity weights instead of binary polarities (pos/neg). If the dictionary does not provide polarity scores the program defaults to binary polarities.
        - Threshold (-t | --threshold=) [-1,1]: Threshold which limits positive and negative reviews. Default value is 0.
        - Optimize threshold (-o| -optimize): Threshold optimization. Corpus is used as development set for optimizing the positivity threshold. The threshold is computed by trying 1000 thresholds in a range between the minimum and maximum poloarity scores.
        - MPQA evaluation method (-m| -mpqa): Instead of the average polarity ratio, the polarity classifier in the one proposed in (Mohammad et al.,2009 - EMNLP) and used on mpqa 
        - -v | --verbose : print extra info, i.e., scores for each document.

		 * 
		 */

		evalParser.addArgument("-c", "--corpus")
		.required(true)
		.help("Input corpus file to evaluate the polarity lexicon.\n");

		evalParser.addArgument("-f", "--testfile")
		.required(false)
		.help("Input test file to evaluate the polarity lexicon. This option is only taken into account if"
				+ " threshold optimization is activated. Be aware, that if optimization is activated and no"
				+ "test-set is provided optimization and evaluation will be done over the same corpus.\n");


		evalParser.addArgument("-l", "--lexicon")
		.required(true)
		.help("Path to the polarity lexicon file.\n");

		evalParser.addArgument("-s", "--synset")
		.choices("lemma", "first","rank", "mfs")
		.required(false)
		.setDefault("lemma")
		.help(
				"Default polarities are calculated over lemmas. With this option polarity of synsets is taken into account instead of words. "
						+ "Possible values: (lemma|first|rank|mfs). "
						+ "'first' uses the sense with the highest confidence value for the lemma. "
						+ "'rank' uses complete ranking of synsets.\n");

		evalParser.addArgument("-w", "--weights")
		.action(Arguments.storeTrue())
		.help("Use polarity weights instead of binary polarities (pos/neg). "
				+ "If the dictionary does not provide polarity scores the program defaults"
				+ " to binary polarities.\n");

		evalParser.addArgument("-o", "--optimize")
		.action(Arguments.storeTrue())
		.help("Optimize threshold (-o| -optimize): Threshold optimization. Corpus is used as development set"
				+ " for optimizing the positivity threshold. The threshold is computed by trying "
				+ "1000 thresholds in a range between the minimum and maximum poloarity scores.\n");

		evalParser.addArgument("-t", "--threshold")
		.required(false)
		.setDefault("0")
		.help("Threshold which limits positive and negative reviews. Float in the [-1,1] range. "
				+ "Default value is 0.\n");

		evalParser.addArgument("-e", "--estimator")
		.choices("avg", "moh")
		.required(false)
		.setDefault("avg")
		.help("evaluation method used for computing the polarity scores [avg | moh]: \n"
				+ "    - avg: average ratio of the polarity words in the text"
				+ "    - moh: polarity classifier proposed in (Mohammad et al.,2009 - EMNLP). Originally used on the MPQA corpus\n");

		evalParser.addArgument("-lt", "--lexScoreThreshold")
		.required(false)
		.setDefault("0")
		.help("Threshold which limits the minimum absolute polarity score an entry in the lexicon must "
				+ "have to be used as polarity calculation. This option discard a number of entries in the given lexicon."
				+ " Float in the [-1,1] range. Default value is 0. Warning: this is an evaluation only option, "
				+ "if you are not sure what this means do not use it\n ");
	}

	public final void predictPolarity() throws IOException {

		String files = parsedArguments.getString("file");
		String lexicon = parsedArguments.getString("lexicon");
		String estimator = parsedArguments.getString("estimator");
		String synset = parsedArguments.getString("synset");
		float threshold = parsedArguments.getFloat("threshold");

		System.out.println("Polarity Predictor: ");
		BufferedReader freader = new BufferedReader(new FileReader(files));   		
		String line;
		while ((line = freader.readLine()) != null) 
		{
			try {
				File lexFile = new File(lexicon);
				Evaluator evalDoc = new Evaluator(lexFile, synset, threshold, estimator);
				Map<String, String> results = evalDoc.processKaf(line, lexFile.getName());
				//Map<String, Double> results = avg.processCorpus(corpus);
				//System.out.println("eval avg done"+results.toString());
				/*System.out.println("Prediction with avg done: \n"
		    				+ "\tTagged file: "+results.get("taggedFile")+"\n"
		    				+ "\tNumber of words containing sentiment found: "+results.get("sentTermNum")+"\n"
		    				+ "\tPolarity score: "+results.get("avg")
		    				+ "\tPolarity (threshold -> "+results.get("thresh")+"): "+results.get("polarity"));*/
				prettyPrintSentKaf(results);					
			} catch (Exception e) {
				System.err.println("predictPolarity: error when processing "+line+" file");
				//e.printStackTrace();
			}
		}
		freader.close();
	}

	private void prettyPrintSentKaf(Map<String,String> kaf) throws Exception, JDOMException
	{
		String fname = kaf.get("taggedFile");
		BufferedReader breader = new BufferedReader(new FileReader(fname));
		KAFDocument doc = KAFDocument.createFromStream(breader);
		String toprint = "";
		Map <String, String> wfspans = new HashMap <String,String>();

		for (List<WF> sentence : doc.getSentences())
		{			
			// store spans of polarity terms in wfspans variable.
			for (Term term : doc.getTermsByWFs(sentence))
			{ 
				if (term != null && term.hasSentiment() && (term.getSentiment().hasPolarity() || term.getSentiment().hasSentimentModifier()))
				{
					String tPol="";
					if (term.getSentiment().hasPolarity())
					{
						tPol = term.getSentiment().getPolarity();
					}
					else if (term.getSentiment().hasSentimentModifier())
					{
						tPol = term.getSentiment().getSentimentModifier();
					}
					else //this should never be fulfilled. If so forget this term.
					{
						continue;
					}
					// single form term
					if (term.getWFs().size() < 2)
					{
						wfspans.put(term.getWFs().get(0).getId(),tPol+"single");						 						 							 
					}
					// multiword term
					else
					{
						wfspans.put(term.getWFs().get(0).getId(),tPol);
						wfspans.put(term.getWFs().get(term.getWFs().size()-1).getId() ,"end");
					}
				}
			}

			// from now on write the html code to print the document.
			for (WF form : sentence)
			{

				String wId = form.getId();
				if (wfspans.containsKey(wId))
				{
					if (wfspans.get(wId).compareTo("end") == 0)
					{
						toprint+=form.getForm()+"</span> ";							 
					}
					else
					{
						String color = wfspans.get(wId).substring(0, 3);
						boolean single = wfspans.get(wId).endsWith("single");

						color = color.replaceAll("pos", "Green");
						color = color.replaceAll("neg", "Red");
						color = color.replaceAll("neu", "Orange");
						color = color.replaceAll("int", "Indigo");
						color = color.replaceAll("weak", "MediumPurple");
						color = color.replaceAll("shi", "Blue");
						if (single)
						{
							toprint+="<span style=\"font-weight:bold; color:"+color+"\">"+form.getForm()+"</span> ";								 
						}
						else
						{
							toprint+="<span style=\"font-weight:bold; color:"+color+"\">"+form.getForm()+" ";
						}

					}
				}
				else 
				{
					toprint+=form.getForm()+" ";					 
				}					 
				//System.out.print(form.getForm()+" ");
			}
			toprint+="\n"; 
		};
		BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname+"_tagged.html")));			  
		toprint = toprint.replaceAll("([a-z>]) ([.,;:\\)\\?\\!])","$1$2");
		toprint = toprint.replaceAll("\\.\n",".\n<br/>");
		bwriter.write("<html>\n<body>\n<div id=\"details\">"		    			
				+ "\n<br/><strong>Number of words containing sentiment found:</strong> "+kaf.get("sentTermNum")
				+ "\n<br/><strong>Polarity score:</strong> "+kaf.get("avg")
				+ "\n<br/><strong>Polarity (threshold -> "+kaf.get("thresh")+"):</strong> "+kaf.get("polarity")
				+ "</div>\n<div id=text\">"+toprint+"\n</div>\n</body>\n</html>");
		bwriter.close();
		//System.out.println("\n\n"+toprint+"\n\nlisto!");
		//System.out.println("\n\nlisto!");
	}


	private void loadPredictionParameters() {
		/*
		 *  Parameters:
	        - Input File (-f | --file= ): File containing the a list of text files in KAF format whose polarity we want to estimate.    
	        - dict file  (-l | --lexicon= ): path to the polarity lexicon.
	        - Synset polarities (-s | --synset=): default polarities are calculated over lemmas. With this option polarity of synsets is taken into account instead of words. It has two posible values: (first|rank). 'first' uses the sense with the highest confidence value for the lemma. 'rank' uses complete ranking of synsets.
	        - Dictionary weights (-w | --weights): use polarity weights instead of binary polarities (pos/neg). If the dictionary does not provide polarity scores the program defaults to binary polarities.
	        - Threshold (-t | --threshold=) [-1,1]: Threshold which limits positive and negative reviews. Default value is 0.
	        - Polarity score estimator (-e| --estimator) [avg|moh]: average polarity ratio or estimator proposed in (Mohammad et al.,2009 - EMNLP) 

		 * 
		 */		  

		predictParser.addArgument("-f", "--file")
		.required(true)
		.help("Input file to predict the polarity lexicon.\n");

		predictParser.addArgument("-l", "--lexicon")
		.required(true)
		.help("Path to the polarity lexicon file.\n");

		predictParser.addArgument("-s", "--synset")
		.choices("lemma", "first","rank")
		.required(false)
		.setDefault("lemma")
		.help(
				"Default polarities are calculated over lemmas. With this option polarity of synsets is taken into account instead of words. Possible values: (lemma|first|rank). 'first' uses the sense with the highest confidence value for the lemma. 'rank' uses complete ranking of synsets.\n");

		predictParser.addArgument("-w", "--weights")
		.action(Arguments.storeTrue())
		.help(
				"Use polarity weights instead of binary polarities (pos/neg). If the dictionary does not provide polarity scores the program defaults to binary polarities.\n");

		predictParser.addArgument("-t", "--threshold")
		.required(false)
		.setDefault((float)0)
		.help(
				"Threshold which limits positive and negative reviews. Float in the [-1,1] range. Default value is 0.\n");

		predictParser.addArgument("-e", "--estimator")
		.choices("avg", "moh")
		.required(false)
		.setDefault("avg")
		.help(
				"scoring function used for computing the polarity [avg | moh]: \n"
						+ "    - avg: average ratio of the polarity words in the text"
						+ "    - moh: polarity classifier proposed in (Mohammad et al.,2009 - EMNLP). Originally used on the MPQA corpus\n");

	}



	/*
	 * kaf library test, for the moment only print the document contents.
	 */
	public final void processKaf(final InputStream inputStream,
			final OutputStream outputStream) throws IOException, JDOMException {
		BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		KAFDocument kafdoc = ixa.kaflib.KAFDocument.createFromStream(breader);		  
		String lang = kafdoc.getLang();
		List<WF> wfs = kafdoc.getWFs();
		int wfamount = wfs.size();
		System.out.println("Word form number in the document: "+wfamount+"\nlang: "+lang+"\n content:\n");
		for (List<WF> sentence : kafdoc.getSentences())
		{
			for (WF form : sentence)
			{
				System.out.print(form.getForm()+" ");
			}
			System.out.println("\n"); 
		};
		System.out.println("\n\nlisto!");
	}


	/*private void loadKafParameters() 
	  {
		  kaflibParser.addArgument("-p", "--print")	        
	        .action(Arguments.storeTrue())
	        .required(false)
	        .help(
	        		"test for kaf document processing library. a single argument is needed:"
	        		+ " - a kaf document pass through the standard input\n");		    		  
	  }*/


	/*
	 * Function creates a temporal directory with a random name.
	 */
	public static File createTempDirectory()
			throws IOException
	{
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if(!(temp.delete()))
		{
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if(!(temp.mkdir()))
		{
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}

	/*
	 * Function renames a file to a new name. if the new name already exists throws an exception. 
	 */
	private void renameFile (String oldFile, String newName) throws IOException 
	{
		File file1 = new File(oldFile);
		File file2 = new File(newName);
		if(file2.exists()) throw new java.io.IOException("file exists");

		// Rename file (or directory)
		file1.renameTo(file2);		 
	}

	/*
	 * Function calls compile_kb from ukb and compiles a graph. 
	 */
	private void ukb_compile (String execpath, String kbfile)
	{
		String graph = FilenameUtils.removeExtension(kbfile);
		graph = graph+".bin";

		try {
			String[] command = {execpath+File.separator+"compile_kb","-o",graph,kbfile};
			//System.err.println("UKB komandoa: "+Arrays.toString(command));

			ProcessBuilder ukbBuilder = new ProcessBuilder()
			.command(command);
			//.redirectErrorStream(true);
			Process compile_kb = ukbBuilder.start();
			int success = compile_kb.waitFor();
			//System.err.println("compile_kb succesful? "+success);
			if (success != 0)
			{
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(compile_kb.getErrorStream()), 1);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					System.err.println(line);
				}
			}
		}
		catch (Exception e){
			System.err.println("Graph compilation: error when calling compile_kb.\n");
			e.printStackTrace();
			System.exit(1);
		}
	}

}
