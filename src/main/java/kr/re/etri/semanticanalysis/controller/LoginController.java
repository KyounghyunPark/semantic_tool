package kr.re.etri.semanticanalysis.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import kr.re.etri.semanticanalysis.config.PropertiesConfig;

@Controller
public class LoginController {
	private static Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	PropertiesConfig propertiesConfig;
	
	@PostMapping("/api/login")
	public @ResponseBody String login(@RequestBody Map<String, Object> parameter) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			if (parameter.get("id") == null || parameter.get("password") == null) {
				throw new Exception("invalid parameter");
			}
			
			String id = (String) parameter.get("id");
			String password = (String) parameter.get("password");
			
			if (propertiesConfig.getLoginId().equals(id) && propertiesConfig.getLoginPassword().equals(password)) {
				map.put("succeeded", true);
			} else {
				throw new Exception("ID or Password do not match.");
			}
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return new Gson().toJson(map);
	}
}
