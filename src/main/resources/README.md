This directory contains the graphs and dictionaries used for propagation.

- 'graphs' subdirectory: here are contained the default graphs shipped with qwn-ppv. 

   The "graphs.txt" file in this directory is read when qwn-ppv is initialized to see which are the available graphs. If you want to include new graphs permanently in your qwn-ppv installation, copy them to the 'graphs' directory, and add the new entries to the "graphs.txt" file as follows:
           "graphAlias<tab>graphs/nameOftheGraphFileWithoutExtension" 
   You must include both the ukb compiled binary version (.bin extension) of the graph and the pre-compilation version (.txt.gz extension), needed for synset propagation. Both filename must differ only in their extensions.           
            

- 'dicts' subdirectory: here are contained the default dictionaries shipped with qwn-ppv. These dictionaries contain the mappings from graph edges (MCR synset offets in our case) to lemma in a certain language.

   The "dicts.txt" file in this directory is read when qwn-ppv is initialized to see which are the available dicts. If you want to include new dicts permanently in your qwn-ppv installation, copy them to the 'dicts' directory, and add the new entries to the "dicts.txt" file as follows:
           "langcode<tab>dicts/nameOftheDictFile" 
              
IMPORTANT: Apart from the resources contained here the user can provide a custom graph and/or a dictionary to qwn-ppv. using -l "custom" -d "path/to/custom/dict" "-g "path/to/custom/graph" 
