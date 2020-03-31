package fr.uphf.se.kotlinresearch.core;

import com.github.gumtreediff.actions.model.Action;
import com.google.gson.*;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapList;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * 
 * @author Matias Martinez
 *
 */
public class NoOutput implements IOutput {

	@Override
	public void generateFinalOutput(FinalResult finalResult) {
		// Nothing, for the moment..
	}

	@Override
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed) {


	}

}
