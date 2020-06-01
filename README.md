# migA

##  Install

First, execute `mvn clean -Dmaven.test.skip=true`



## Usage:


```
java -cp coming.jar -location <Path to project to anayze>  -input fr.uphf.se.kotlinresearch.core.MigACore  -mode nullmode -parameters 
				"projectname:<project_name>:save_result_revision_analysis:false:optimize_navigation:true

```

The class `MigAGitRunnerTest` provides different cases that executes MigA.