package SPRY.Tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import SPRY.Tools.Conceptual.PeriodicTask;
/** This utility assign priorities to a set of real-time tasks, 
 * using the deadline monotonic priority assignment */
public class PriorityUtil {
	public static final int MAX_PRIORITY = 1000;
	
	private static ArrayList<Integer> generatePriorities(int number) {
		ArrayList<Integer> priorities = new ArrayList<>();
		for (int i = 0; i < number; i++)
			priorities.add(MAX_PRIORITY - (i + 1) * 2);
		return priorities;
	}

	public static void deadlineMonotonicPriorityAssignment(List<PeriodicTask> taskSet, int number) {
		ArrayList<Integer> priorities = generatePriorities(number);
		/* deadline monotonic assignment */
		taskSet.sort(Comparator.comparingDouble(t -> t.deadline - t.jitter));
		priorities.sort((p1, p2) -> -Integer.compare(p1, p2));
		for (int i = 0; i < taskSet.size(); i++) {
			taskSet.get(i).priority = priorities.get(i);
		}
	}
}
