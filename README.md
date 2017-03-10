#Conflerge
*Conflerge* is custom mergetool that strives to automatically resolve merge conflicts. This is an attempt to demonstrate that finer grain merging, unlike the line-by-line merging in Git, can result in fewer false-positive conflicts with a competitive false-negative rate. 

We attempted finer grain merging with two approaches: Token-based and Abstract Syntax Tree based. 

###Tokenization
A token is an individual keyword in source code such as a reserved keyword, variable name or constant. For example, tokenizing the Java statement `System.out.println(hello);` yields the token sequence `[ System .  out .  println (  hello  ) ; ]` We use the [Wagner-Fischer Algorithm](https://en.wikipedia.org/wiki/Wagner%E2%80%93Fischer_algorithm) [1] to find the edit distance between **BASE** and **LOCAL**, and between **BASE** and **REMOTE**.

###Abstract Syntax Trees 
In contrast to tokens, Abstract Syntax Trees retain information about the syntactic structure of the code as hierarchical parent-child relationships. Each node has an ordered set of children. We use the mmdiff algorithm [2] to find the edit distance between **BASE** and **LOCAL**, and between **BASE** and **REMOTE**.


##Architecture

![*Conflerge* architecture](http://i.imgur.com/ds71jB3.png)

Git provides the relevant versions of **BASE**, **LOCAL** and **REMOTE**. We use [javaparser](https://github.com/javaparser/javaparser) to parse and unparse the source code files. We also use it for tokenization and the construction of abstract syntax trees. 

Usage
===================
*Conflerge* is currently scoped to work with Java source code files and Git. 


Instructions to recreate the results in the paper can be found [here](https://github.com/ishansaksena/Conflerge/tree/master/scripts). 

###References
[1] Gonzalo Navarro.  2001.  A guided tour to approximate string matching.  ACM Comput.  Surv.  33,  1(March 2001), 31-88.
[2] Sudarshan S. Chawathe.  1999.  Comparing Hierarchical Data in External Memory.  In Proceedings ofthe 25th International Conference on Very Large Data Bases (VLDB ’99), Malcolm P. Atkinson, Maria E.Orlowska, Patrick Valduriez, Stanley B. Zdonik, and Michael L. Brodie (Eds.). Morgan Kaufmann PublishersInc., San Francisco, CA, USA, 90-101
