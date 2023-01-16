package kr.re.etri.semanticanalysis.config;

import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import kr.re.etri.semanticanalysis.schedular.DittoSchedularCronJob;
import kr.re.etri.semanticanalysis.schedular.MindsphereSchedularCronJob;


@Configuration
public class SchedulingConfig {
	private static Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);
	
	@Autowired
	private Scheduler scheduler;

	public void addJob(Map<String, Object> parameter, String shcedularJobId, String name) {
		try {
			JobDetail jobDetail = null;
			if ("mindsphere".equals(name)) {
				jobDetail = buildJobDetail(MindsphereSchedularCronJob.class, shcedularJobId);
			} else {
				jobDetail = buildJobDetail(DittoSchedularCronJob.class, shcedularJobId);
			}
			
			JobDataMap data = jobDetail.getJobDataMap();
			data.put("parameter", parameter);
			
			// 이미 스케쥴이 DB에 등록되어 있다면 삭제
			if (scheduler.checkExists(jobDetail.getKey())) {
				scheduler.deleteJob(jobDetail.getKey());
			}
			
			int interval = 2;
			if (parameter.get("interval") != null) {
				interval = (int) parameter.get("interval");
			}

			// Job과 트리거를 설정,
			scheduler.scheduleJob(jobDetail, buildSimpleJobTrigger(interval));
			
		} catch (SchedulerException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}	
	}
	
	public void deleteJob(String shcedularJobId) {
		try {
			JobKey jobKey = new JobKey(shcedularJobId);
			if (scheduler.checkExists(jobKey)) {
				scheduler.deleteJob(jobKey);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}	
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JobDetail buildJobDetail(Class job, String shcedularJobId) {
		return JobBuilder.newJob(job).withIdentity(shcedularJobId).build();
	}

	public Trigger buildCronJobTrigger(String scheduleExp) {
		return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(scheduleExp)).build();
	}
	
	public Trigger buildSimpleJobTrigger(int interval) {
		return TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(interval <= 0 ? 1 : interval).repeatForever()).build();
	}
}