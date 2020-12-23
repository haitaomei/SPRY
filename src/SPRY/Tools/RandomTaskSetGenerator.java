package SPRY.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import SPRY.Tools.Conceptual.PeriodicTask;
import SPRY.Tools.Timing.RTA;

/**
 * Generate a set of periodic real-time tasks that have a given total utilisation.
 * The period is randomly generated between the given MIN and MAX values
 */
public class RandomTaskSetGenerator {

    public static enum Deadlines {
        DeadlineEqualPeriod, DeadlineLessThanPeriod, DeadlineBiggerThanPeriod, ArbitraryDeadline
    }

    public static List<PeriodicTask> generateTaskSet(int number, double minT, double maxT, double util) {
        return generateTaskSet(number, minT, maxT, util, Deadlines.DeadlineEqualPeriod);
    }

    public static List<PeriodicTask> generateTaskSet(int number, double minT, double maxT, double util, Deadlines DPolice) {
        List<Double> utils = null;
        uunifastDiscard unifastDiscard = new uunifastDiscard(util, number, 1000);
        while (true) {
            utils = unifastDiscard.getUtils();
            if (utils != null) if (utils.size() == number) break;
        }

        List<PeriodicTask> tasks = new ArrayList<>(number);
        List<Double> periods = new ArrayList<>(number);
        Random random = new Random();
        do {
            tasks.clear();
            periods.clear();
            /* generates random periods */
            do {
                int period = random.nextInt((int) (maxT - minT)) + (int) minT;
                if (!periods.contains((double) period)) periods.add((double) period);
            } while (periods.size() < number);
            periods.sort(Double::compare);
            // utils.forEach(x->System.out.println(x));
            for (int i = 0; i < utils.size(); i++) {
                double T = periods.get(i);
                double C = T * utils.get(i);
                double D = T;
                switch (DPolice) {
                    case DeadlineEqualPeriod:
                        break;
                    case DeadlineLessThanPeriod:
                        D = random.nextInt((int) (T - C - 1)) + (int) C;
                        break;
                    case DeadlineBiggerThanPeriod:
                        D = random.nextInt((int) (Integer.MAX_VALUE - T - 1)) + (int) (T + 1);
                        break;
                    case ArbitraryDeadline:
                        D = random.nextInt((int) (Integer.MAX_VALUE - (int) (C + 1))) + (int) C;
                        break;
                    default:
                        D = T;
                        break;
                }
                PeriodicTask t = new PeriodicTask(-1, T, C, D, "");
                tasks.add(t);
            }
            PriorityUtil.deadlineMonotonicPriorityAssignment(tasks, number);
        } while (!RTA.schedulabilityTest(tasks));
        return tasks;
    }
}
