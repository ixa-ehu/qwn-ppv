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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.commons.io.FileUtils;

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
* @version 2014-04-09
*/
public class CLI {

	/**
	* BufferedReader (from standard input) and BufferedWriter are opened. The
	* module takes a plain text containing positive and negative words from standard input, and produces polarity lexicon in tabulated format
	* text by sentences. Resulting lexicons is returned through standard output.
	*
	* @param args
	* @throws IOException
	**/
	
	public static void main(String[] args) throws IOException {

	    // create Argument Parser
	    ArgumentParser parser = ArgumentParsers.newArgumentParser("qwn-ppv-1.0.jar")
	    		.description("qwn-ppv is a propagation algorithm for creating polarity lexicons developed by IXA NLP Group.\n");

	    // specify language for the lexicons
	    parser.addArgument("-l", "--lang")
	    	.choices("en", "es", "eu", "cat", "gl")
	    	.required(true).help("It is REQUIRED to choose a language to generate lexicons in a specific language with qwn-ppv.\n");
	    
	    // specify the graph which shall be used for propagation
	    parser.addArgument("-g","--graph")
	    	.choices("synAnt", "mcr", "mcr-ant", "mcr-antGloss")
	    	.required(false).setDefault("synAnt")
	    	.help("A graph is REQUIRED to propagate polarity information over it, if no graph is specified the system defaults to MCR synonymy and antonymy graphs (synAnt).\n");
	    
	      
	    
	    /*
	     * Parse the command line arguments
	     */
	    Namespace parsedArguments = null;

	    // catch errors and print help
	    try {
	      parsedArguments = parser.parseArgs(args);
	      //System.err.println(parser.parseArgs(args));
	    } catch (ArgumentParserException e) {
	      parser.handleError(e);
	      System.out
	          .println("Run java -jar ixa-qwn-ppv-1.0.jar -help for details.\n");
	      System.exit(1);
	    }
	    
	    // read arguments
	    String lang = parsedArguments.getString("lang");   
	    String graph = parsedArguments.getString("graph"); 
	    
	    BufferedReader breader = null;
	    BufferedWriter bwriter = null;


	    try{
	    	breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
	    	bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));


	    	System.out.println("qwn-ppv: received arguments are: \n Lang: "+lang+"\n Graph: "+graph+"\n");
	    	System.out.println("qwn-ppv execution finished. Lexicons are ready.\n");
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	}

	
}
