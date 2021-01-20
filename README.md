# migA

A Tool for analyzing migrations from Java to Kotlin on Git repositories.

Contact:

Matias Martinez:  matias.sebastian.martinez@gmail.com , [www.martinezmatias.com](www.martinezmatias.com)

if you use this tool, please cite [this report](https://arxiv.org/abs/2003.12730):

```
How and Why did developers migrate Android Applications from Java to Kotlin? 
A study based on code analysis and interviews with developers. 
Matias Martinez, Bruno Gois Mateus. https://arxiv.org/abs/2003.12730. 

```


##  Install

First, execute `mvn clean -Dmaven.test.skip=true`


## Usage:


The main class to run the experiment is `fr.uphf.se.kotlinresearch.core.MigaMain`. 
The method `runExperiment` receives as parameter  the path to the folder that has the GIT repositories to analyze. The second  parameter, optional, is the path to the dir there the results will be written (by default `./coming_results/`).
Moreover, it's also possible to call the `main` method from  `fr.uphf.se.kotlinresearch.core.MigaMain`, which also receives two parameters: a) the output folder b) the path to the repository to analyze.

The class `fr.uphf.se.kotlinresearch.core.MigaMainSeveralRepos` analyzes several repositories, all located in one folder. 
The method `main` has two parameters: a) path of the output folder b) path to folder that has the repositories to analyze.



## 