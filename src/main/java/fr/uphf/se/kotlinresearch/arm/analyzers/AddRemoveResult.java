package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.util.ArrayList;
import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;

public class AddRemoveResult extends fr.inria.coming.core.entities.AnalysisResult<IRevision> {

	public AddRemoveResult(IRevision analyzed) {
		super(analyzed);

	}

	public List<String> addedJava = new ArrayList<>();
	public List<String> removedJava = new ArrayList<>();
	public List<String> addedKotlin = new ArrayList<>();
	public List<String> removedKotlin = new ArrayList<>();
	public List<String> modifKotlin = new ArrayList<>();
	public List<String> modifJava = new ArrayList<>();
	public List<String> diffName = new ArrayList<>();

	public List<String> migrationJavaToKotlin = new ArrayList<>();
	public List<String> migrationKotlinToJava = new ArrayList<>();

	@Override
	public String toString() {
		return "AddRemoveResult [addedJava=" + addedJava + ", removedJava=" + removedJava + ", modifJava=" + modifJava
				+ ", addedKotlin=" + addedKotlin + ", removedKotlin=" + removedKotlin + ", modifKotlin=" + modifKotlin
				+ ", diffname=" + diffName + "]";
	}

}
