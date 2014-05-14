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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.StringReader;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.apache.commons.io.FileUtils;

import es.ehu.si.ixa.qwn.ppv.eval.AvgRatioEstimator;
import es.ehu.si.ixa.qwn.ppv.eval.MohammadEstimator;



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
	private final String version = CLI.class.getPackage()
			.getImplementationVersion();
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
	  private Subparser creationParser;
	  /**
	* The parser that manages the evaluation sub-command.
	*/
	  private Subparser evalParser;

	  public CLI() {
	    creationParser = subParsers.addParser("create").help("Lexicon creation CLI");
	    loadCreationParameters();
	    evalParser = subParsers.addParser("eval").help("Lexicon evaluation CLI");
	    loadEvalParameters();
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
	      System.err.println("CLI options: " + parsedArguments);
	      if (args[0].equals("create")) {
	        create(System.in, System.out);
	      } else if (args[0].equals("eval")) {
	        eval();
	      }
	    } catch (ArgumentParserException e) {
	      argParser.handleError(e);
	      System.out.println("Run java -jar target/qwn-ppv-" + version
	          + ".jar (create|eval) -help for details");
	      System.exit(1);
	    }
	  }

	  /**
		* BufferedReader (from standard input) and BufferedWriter are opened. The
		* module takes a plain text containing positive and negative words from standard input, and produces polarity lexicon in tabulated format
		* text by sentences. Resulting lexicons is returned through standard output.
		*
		* @param args
		* @throws IOException
		**/
	  public final void create(final InputStream inputStream,
	      final OutputStream outputStream) throws IOException {
		  
	    String lang = parsedArguments.getString("lang");
	    String graph = parsedArguments.getString("grahp");
	    
	    BufferedReader breader = null;
	    BufferedWriter bwriter = null;

	    try{
	    	breader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	    	bwriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));


	    	System.out.println("qwn-ppv: received arguments are: \n Lang: "+lang+"\n Graph: "+graph+"\n");
	    	System.out.println("qwn-ppv execution finished. Lexicons are ready.\n");
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    
	    bwriter.close();
	    breader.close();
	  }

	  private void loadCreationParameters() {
		// specify language for the lexicons
		creationParser.addArgument("-l", "--lang")
		    	.choices("en", "es", "eu", "cat", "gl")
		    	.required(true).help("It is REQUIRED to choose a language to generate lexicons in a specific language with qwn-ppv.\n");
		    
		// specify the graph which shall be used for propagation
		creationParser.addArgument("-g","--graph")
		    	.choices("synAnt", "mcr", "mcr-ant", "mcr-antGloss")
		    	.required(false).setDefault("synAnt")
		    	.help("A graph is REQUIRED to propagate polarity information over it, if no graph is specified the system defaults to MCR synonymy and antonymy graphs (synAnt).\n");
		    
	  }

	  public final void eval() throws IOException {
	    BufferedReader breader = null;
	    String corpus = parsedArguments.getString("corpus");
	    String normalize = parsedArguments.getString("lexicon");
	    String estimator = parsedArguments.getString("estimator");
	   
	    System.out.println("lexicon evaluator: ");
	    if (estimator.equals("avg")) {
		    System.out.println(new AvgRatioEstimator());
	    } 	
	    else if (estimator.equals("moh")) {		
		    System.out.println(new MohammadEstimator());
	      }	
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
		
	    evalParser.addArgument("-l", "--lexicon")
	        .required(true)
	        .help("Path to the polarity lexicon file.\n");
	        
	    evalParser.addArgument("-s", "--synset")
	        .choices("lemma", "first","rank")
	        .required(false)
	        .setDefault("lemma")
	        .help(
	            "Default polarities are calculated over lemmas. With this option polarity of synsets is taken into account instead of words. Possible values: (lemma|first|rank). 'first' uses the sense with the highest confidence value for the lemma. 'rank' uses complete ranking of synsets.\n");
	    
	    evalParser.addArgument("-w", "--weights")
        .action(Arguments.storeTrue())
        .help(
            "Use polarity weights instead of binary polarities (pos/neg). If the dictionary does not provide polarity scores the program defaults to binary polarities.\n");
    
	    evalParser.addArgument("-o", "--optimize")
        .action(Arguments.storeTrue())
        .help(
        		"Optimize threshold (-o| -optimize): Threshold optimization. Corpus is used as development set for optimizing the positivity threshold. The threshold is computed by trying 1000 thresholds in a range between the minimum and maximum poloarity scores.\n");
	    
	    evalParser.addArgument("-t", "--threshold")
        .required(false)
        .setDefault(0)
        .help(
        		"Threshold which limits positive and negative reviews. Float in the [-1,1] range. Default value is 0.\n");

	    evalParser.addArgument("-e", "--estimator")
        .choices("avg", "moh")
        .required(false)
        .setDefault("avg")
        .help(
        		"evaluation method used for computing the polarity scores [avg | moh]: \n"
        		+ "    - avg: average ratio of the polarity words in the text"
        		+ "    - moh: polarity classifier proposed in (Mohammad et al.,2009 - EMNLP). Originally used on the MPQA corpus\n");
	    
	  }
		
}
