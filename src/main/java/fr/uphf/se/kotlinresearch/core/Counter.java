package fr.uphf.se.kotlinresearch.core;

/**
 * 
 * @author Matias Martinez
 *
 */
public class Counter {

	private int counter = 0;

	public void increment() {
		counter++;
	}

	public int getIncremented() {
		counter++;
		return counter;
	}

	public int getCounter() {
		return counter;
	}

	@Override
	public String toString() {
		return Integer.toString(counter);
	}

	public void reset() {
		counter = 0;
	}

}
