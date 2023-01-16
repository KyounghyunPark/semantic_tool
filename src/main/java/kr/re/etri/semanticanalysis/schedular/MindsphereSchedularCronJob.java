package kr.re.etri.semanticanalysis.schedular;

import java.util.HashMap;
import java.util.Map;

import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.google.gson.Gson;

import kr.re.etri.semanticanalysis.request.MindsphereRequest;
import kr.re.etri.semanticanalysis.websocket.WebSocketHandler;


public class MindsphereSchedularCronJob  extends QuartzJobBean implements InterruptableJob {
	private static Logger logger = LoggerFactory.getLogger(MindsphereSchedularCronJob.class);
	
    private boolean isInterrupted = false;
    private JobKey jobKey = null;
    
    @Autowired
    MindsphereRequest mindsphereRequest;
    
    @Autowired
	WebSocketHandler webSocketHandler;

    @Override 
    public void interrupt() throws UnableToInterruptJobException {
    	logger.info(jobKey + "  -- INTERRUPTING --");
        isInterrupted = true;
    }

	@SuppressWarnings("unchecked")
	@Override 
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (isInterrupted) {
        	logger.warn("jobKey: " + jobKey + "is Interrupted.");
            return;
        }
        
        JobDetail jobDetail = context.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        Map<String, Object> parameter = (Map<String, Object>) jobDataMap.get("parameter");
        
        try {
        	String jsonResult = mindsphereRequest.sendStatusRequest(parameter);
    		if (jsonResult != null) {
    			Map<String, Object> websocketData = new HashMap<>();
    			websocketData.put("command", "data_monitoring");
    			websocketData.put("schedularJobId", jobKey.getName());
    			websocketData.put("data", jsonResult);
    			
    			webSocketHandler.sendMessage(new Gson().toJson(websocketData));
    		}
        } catch(Exception e) {
        	logger.error(e.getMessage());
        }
		
    }
}