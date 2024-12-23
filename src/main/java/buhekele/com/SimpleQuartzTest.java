package buhekele.com;

import buhekele.com.SimpleJob;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.concurrent.TimeUnit;

public class SimpleQuartzTest {
    /*
     * 基于时间间隔的定时任务
     */
    @Test
    public void simpleTest() throws SchedulerException, InterruptedException {
        // 1、创建Scheduler（调度器）
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        // 2、创建JobDetail实例，并与SimpleJob类绑定(Job执行内容)
        JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                .withIdentity("job1", "group1")
                .build();
        // 3、构建Trigger（触发器），定义执行频率和时长
        Trigger trigger = TriggerBuilder.newTrigger()
                // 指定group和name，这是唯一身份标识
                .withIdentity("trigger-1", "trigger-group")
                .startNow()  //立即生效
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(2) //每隔2s执行一次
                        .repeatForever())  // 永久执行
                .build();
        //4、将Job和Trigger交给Scheduler调度
        scheduler.scheduleJob(jobDetail, trigger);
        // 5、启动Scheduler
        scheduler.start();
        // 休眠，决定调度器运行时间，这里设置30s
        TimeUnit.SECONDS.sleep(30);
        // 关闭Scheduler
        scheduler.shutdown();
    }



    /*
     * 基于cron表达式的定时任务
     */
    @Test
    public void cronTest() throws SchedulerException, InterruptedException {
        // 1、创建Scheduler（调度器）
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        // 2、创建JobDetail实例，并与SimpleJob类绑定
        JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                .withIdentity("job-1", "job-group").build();
        // 3、构建Trigger060200LJX???060200
        // 和时长
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-1", "trigger-group")
                .startNow()  //立即生效
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * ? * * *"))
                .build();

        //4、执行
        scheduler.scheduleJob(jobDetail, cronTrigger);
        scheduler.start();
        // 休眠，决定调度器运行时间，这里设置30s
        TimeUnit.SECONDS.sleep(30);
        // 关闭Scheduler
        scheduler.shutdown();
    }


    @Test
    public void multiJobTest() throws SchedulerException, InterruptedException {
        // 1、创建Scheduler（调度器）
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        // 2、创建JobDetail实例，与执行内容类SimpleJob绑定，注意要设置 .storeDurably()，否则报错
        JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                .withIdentity("job1", "job-group")
                .storeDurably()
                .build();

        // 3、分别构建Trigger实例
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "trigger-group")
                .startNow()//立即生效
                .forJob(jobDetail)
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInSeconds(2) //每隔3s执行一次
                        .repeatForever()) // 永久循环
                .build();
        Trigger trigger2 = TriggerBuilder.newTrigger().withIdentity("trigger2", "trigger-group")
                .startNow()//立即生效
                .forJob(jobDetail)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(3) //每隔5s执行一次
                        .repeatForever()).build(); // 永久循环
        //4、调度器中添加job
        scheduler.addJob(jobDetail, false);
        scheduler.scheduleJob(trigger);
        scheduler.scheduleJob(trigger2);
        // 启动调度器
        scheduler.start();
        // 休眠任务执行时长
        TimeUnit.SECONDS.sleep(20);
        scheduler.shutdown();
    }


    @Test
    public void dataJobTest() throws SchedulerException, InterruptedException {
        //创建一个scheduler
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.getContext().put("skey", "svalue");

        //创建一个Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .usingJobData("t1", "tv1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3)
                        .repeatForever()).build();
        trigger.getJobDataMap().put("t2", "tv2");

        //创建一个job
        JobDetail job = JobBuilder.newJob(SimpleJob.class)
                .usingJobData("jobSays", "Hello World!")
                .usingJobData("myFloatValue", 3.141f)
                .withIdentity("myjob", "mygroup").build();
        //job.getJobDataMap().put("j2", "jv2");

        //注册trigger并启动scheduler
        scheduler.scheduleJob(job,trigger);
        scheduler.start();
    }
}