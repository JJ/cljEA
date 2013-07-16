/**
 * Author José Albert Cruz Almaguer <jalbertcruz@gmail.com>
 * Copyright 2013 by José Albert Cruz Almaguer.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http:www.gnu.org/licenses/agpl-3.0.txt) for more details.
 */

package sheduling;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.concurrent.Callable;

public class ShedulingUtility {

    static Scheduler scheduler;
    static int count = 0;

    public static void start() throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
    }

  public static void shutdown() throws SchedulerException {
        scheduler.shutdown();
    }

    public static synchronized void send_after(long millis, Callable action) {

        JobDetail job = new JobDetail();
        job.setName("jn" + count);
        job.setJobClass(UtilJob.class);

        SimpleTrigger trigger = new SimpleTrigger();
        trigger.setName("dtn" + count);
        trigger.setRepeatCount(0);

        count++;

        JobDataMap jobDataMap = job.getJobDataMap();
        jobDataMap.put("act", action);
        job.setJobDataMap(jobDataMap);

        trigger.setStartTime(new Date(System.currentTimeMillis() + millis));
        try {
            scheduler.scheduleJob(job, trigger);
//            System.out.println("Nuevo job creado");
        } catch (SchedulerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
