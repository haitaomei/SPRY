package SPRY.Tools.Timing;

import java.util.ArrayList;
import java.util.List;

import SPRY.Tools.Conceptual.DServer;
import SPRY.Tools.Conceptual.PeriodicTask;

/**
 * Response time analysis tools for a task, which is executing under a deferrable server,
 * and receiving interference from a set of periodic (sporadic) higher priority real-time tasks
 */
public class RTATaskUnderDServer {

    public static double ResTime(PeriodicTask t, DServer S, List<PeriodicTask> higherPriorities) {
        return ResTime(null, t, S, higherPriorities);
    }

    public static double ResTime(List<PeriodicTask> allTasksInServer, PeriodicTask t, DServer S, List<PeriodicTask> higherPriorities) {
        if (S == null) return Double.MAX_VALUE;
        if (S.WCET <= 0) return Double.MAX_VALUE;
        if (t.WCET == 0) return 0;
        if (allTasksInServer == null) allTasksInServer = new ArrayList<>();
        if (higherPriorities == null) higherPriorities = new ArrayList<>();
        higherPriorities.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

        double w0 = t.WCET + (Math.ceil(t.WCET / S.WCET) - 1) * (S.period - S.WCET);
        // System.out.println("w0=" + w0);
        double w_n = w0;
        double w_nPlus = W(allTasksInServer, w_n, t, S, higherPriorities);
        double jitter = S.period - S.WCET;
        if ((S.period % t.period == 0) || (t.period % S.period == 0)) jitter = 0;
        while (w_n != w_nPlus) {
            if (w_nPlus > (t.deadline - jitter)) return Double.MAX_VALUE;
            w_n = w_nPlus;
            w_nPlus = W(allTasksInServer, w_n, t, S, higherPriorities);
        }
        return w_n;
    }

    private static double load(double wi, List<PeriodicTask> allTasksInServer, PeriodicTask t, DServer S) {
        double l = 0;
        for (PeriodicTask J : allTasksInServer) {
            if (J.priority > t.priority) {
                double jitter = 0;
                if (J instanceof DServer) {
                    if ((S.period % J.period == 0) || (J.period % S.period == 0)) {
                        jitter = 0;
                    } else {
                        jitter = J.period - J.WCET;
                    }
                }
                l += Math.ceil((wi + jitter) / J.period) * J.WCET;
            }
        }
        l += t.WCET;
        return l;
    }

    private static double W(List<PeriodicTask> allTasksInServer, double w_n, PeriodicTask Ti, DServer S, List<PeriodicTask> higherPriorities) {
        double w = 0;
        double l = load(w_n, allTasksInServer, Ti, S);
        //System.out.println("l=" + l);
        w = l + (Math.ceil(l / S.WCET) - 1) * (S.period - S.WCET);
        //System.out.println("w=" + w);
        for (PeriodicTask hp : higherPriorities) {
            if (hp.priority > S.priority) {
                double jitter = 0;
                if (hp instanceof DServer) {
                    if ((S.period % hp.period == 0) || (hp.period % S.period == 0)) {
                        jitter = 0;
                    } else {
                        jitter = hp.period - hp.WCET;
                    }
                }
                w += Math.ceil(Math.max(0, (w_n - (Math.ceil(l / S.WCET) - 1) * S.period) + jitter) / hp.period) * hp.WCET;
            }
        }
        //System.out.println("w=" + w);
        return w;
    }
}
