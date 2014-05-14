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

import org.apache.commons.io.FileUtils;

/**
* contextCreator class: this class creates a context file in order to launch the UKB propagations. 
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
* @version 2014-05-13
*/
public class contextCreator {

	/**
	* BufferedReader (from standard input) and BufferedWriter are opened. The
	* module takes a plain text containing positive and negative words from standard input, and produces polarity lexicon in tabulated format
	* text by sentences. Resulting lexicons is returned through standard output.
	*
	* @param args
	* @throws IOException
	**/
	
	public contextCreator(BufferedReader breader, String weights) {
		System.out.println("qwn-ppv: average ratio polarity estimator created\n");
	}

	
}