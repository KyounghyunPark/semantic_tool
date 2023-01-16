package kr.re.etri.semanticanalysis.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.re.etri.semanticanalysis.config.SchedulingConfig;
import kr.re.etri.semanticanalysis.request.DittoRequest;
import kr.re.etri.semanticanalysis.request.MindsphereRequest;
import kr.re.etri.semanticanalysis.service.TDBService;
import kr.re.etri.semanticanalysis.websocket.WebSocketHandler;

@RestController
@RequestMapping("/api/twin/")
public class DataMonitoringController {
	private static Logger logger = LoggerFactory.getLogger(DataMonitoringController.class);
	
	@Autowired
	MindsphereRequest mindsphereRequest;
	
	@Autowired
	DittoRequest dittoRequest;
	
	@Autowired
	SchedulingConfig schedulingConfig;
	
	@Autowired
	TDBService tdbService;
	
	@Autowired
	WebSocketHandler webSocketHandler;
	
	@SuppressWarnings("unchecked")
	@PostMapping("add-thing")
	public @ResponseBody String addThing(@RequestBody Map<String, Object> parameter) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String twinJson = (String) parameter.get("twinJson");
			String thingJson = (String) parameter.get("thingJson");
			
			Map<String, Object> twinInfo = gson.fromJson(twinJson, new TypeToken<Map<String, Object>>(){}.getType());
			Map<String, Object> thingInfo = gson.fromJson(thingJson, new TypeToken<Map<String, Object>>(){}.getType());
			
			// Registration of Thing			
			String registResult = dittoRequest.registThing(thingInfo);
			if (registResult != null) {
				// Connection of Thing
				String piggybackConnectionResult = dittoRequest.piggybackConnection(thingInfo, twinInfo);
				if (piggybackConnectionResult != null) {
					// Registration and Connection Success 
					// Twin Json Add Features
					List<Map<String, Object>> twinFeatures = (List<Map<String, Object>>) Optional.ofNullable(twinInfo.get("features")).orElse(new ArrayList<String>());
					Map<String, Object> twinFeature = twinFeatures.stream().filter(d -> d.get("name") != null && d.get("name").toString().equals("ditto")).findFirst().orElse(null);
					if (twinFeature == null) {
						twinFeature = new HashMap<>();
						twinFeatures.add(twinFeature);
					}
					
					twinFeature.put("name", "ditto");
					twinFeature.put("thingId", thingInfo.get("thingId"));
					
					twinInfo.put("features", twinFeatures);
					
					tdbService.deleteModel((String) twinInfo.get("dt-id"));
					tdbService.addModel(gson.toJson(twinInfo));
					sendWebSocket((String) parameter.get("uuid"));
				}
			}
			
			map.put("succeeded", true);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return gson.toJson(map);	
	}
	
	@PostMapping("get-thing")
	public @ResponseBody String getThing(@RequestBody Map<String, Object> parameter) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String thingId = (String) parameter.get("thingId");
			String result = dittoRequest.getThing(thingId);
			
			map.put("succeeded", true);
			map.put("data", result);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return gson.toJson(map);	
	}
	
	
	@SuppressWarnings("unchecked")
	@PostMapping("connect")
	public @ResponseBody String connect(@RequestBody Map<String, Object> parameter) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			if (parameter.get("name") == null) {
				throw new Exception("Invalid parameter");
			}
			
			String name = (String) parameter.get("name");
			String shcedularJobId = UUID.randomUUID().toString();
			
			if ("mindsphere".equals(name)) {
				if (parameter.get("authorization") == null || parameter.get("assets-id") == null) {
					throw new Exception("Invalid parameter");
				}
				
				Map<String, Object> authorization = (Map<String, Object>) parameter.get("authorization");
				if (authorization.get("id") == null || authorization.get("secret") == null) {
					throw new Exception("Invalid parameter");
				}
				
				if (!mindsphereRequest.sendAuthReqeust(parameter)) {
					throw new Exception("권한 인증에 실패했습니다.");
				}
			} else if ("ditto".equals(name)) {
				if (parameter.get("topic") == null || parameter.get("thingId") == null) {
					throw new Exception("Invalid parameter");
				}
			} else {
				throw new Exception("The parameter does not have a name.");
			}
			
			schedulingConfig.addJob(parameter, shcedularJobId, name);
			
			map.put("succeeded", true);
			map.put("data", shcedularJobId);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return gson.toJson(map);
	}
	
	
	@PostMapping("disconnect")
	public @ResponseBody String disconnect(@RequestBody Map<String, Object> parameter) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			if (parameter.get("shcedularJobId") == null) {
				throw new Exception("Invalid parameter");
			}
			
			String shcedularJobId = (String) parameter.get("shcedularJobId");
			schedulingConfig.deleteJob(shcedularJobId);
			
			logger.info("####################    disconnect    ###########################");
			logger.info("####################    " + shcedularJobId + "    ###########################");
			
			map.put("succeeded", true);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return gson.toJson(map);
	}
	
	private void sendWebSocket(String uuid) throws Exception {
		Map<String, Object> websocketData = new HashMap<>();
		websocketData.put("command", "twinsChanged");
		websocketData.put("uuid", uuid);
		
		webSocketHandler.sendMessage(new Gson().toJson(websocketData));
	}
}
