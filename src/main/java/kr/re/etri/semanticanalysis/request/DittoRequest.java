package kr.re.etri.semanticanalysis.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import kr.re.etri.semanticanalysis.config.Constants;
import kr.re.etri.semanticanalysis.config.PropertiesConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Service
public class DittoRequest {
	private static Logger logger = LoggerFactory.getLogger(DittoRequest.class);
	
	@Autowired
	PropertiesConfig propertiesConfig;
	
	private Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	private List<Map<String, Object>> mapping_result_list = new ArrayList<>();
	
	public void readMappingResult() {
		try {
			mapping_result_list.clear();
			
			File mappingResultFolder = new File(System.getProperty("user.dir") + "/mapping_result");
			for (final File fileEntry : mappingResultFolder.listFiles()) {
		        if (!fileEntry.isDirectory()) {
		        	Reader reader = Files.newBufferedReader(Paths.get(fileEntry.getPath()));
		        	Map<String, Object> fileContent = gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
		        	mapping_result_list.add(fileContent);
		        }
		    }
		} catch(Exception e) {
			logger.info(e.getMessage());
		}	
	}
	
	
	public DittoResonse sendRequest(String url, String method, Map<String, String> headers, String jsonParameter) throws Exception {
		HttpURLConnection http = null;
				
		try {
			http = (HttpURLConnection) new URL(url).openConnection(); // 접속
			http.setDefaultUseCaches(false);
			http.setDoInput(true);
			http.setDoOutput(true); 
			http.setRequestMethod(method); 

			if (headers != null && headers.size() > 0) {
				for (Entry<String, String> entry : headers.entrySet()) {
					http.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			
			if (jsonParameter != null) {
				try (OutputStream os = http.getOutputStream()){
					byte request_data[] = jsonParameter.getBytes("utf-8");
					os.write(request_data);
					os.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			int statusCode = http.getResponseCode();
			String responseBody = null;
			
			if (statusCode == Constants.HTTP_OK_CODE || statusCode == Constants.HTTP_CREATE_CODE) {
				BufferedReader bufferReader = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
				StringBuilder builder = new StringBuilder();
				String str;
				while ((str = bufferReader.readLine()) != null) {
					builder.append(str + "\n");
				}
				
				responseBody = builder.toString();
			} else if (statusCode == Constants.HTTP_NOCONTENT_CODE) {
				responseBody = "";
			} else {
				if (statusCode == Constants.HTTP_UNAUTHORIZED_CODE) {
					throw new Exception("Authentication failed.");
				} else {
					BufferedReader bufferReader = new BufferedReader(new InputStreamReader(http.getErrorStream(), "UTF-8"));
					StringBuilder builder = new StringBuilder();
					String str;
					while ((str = bufferReader.readLine()) != null) {
						builder.append(str + "\n");
					}
					
					Map<String, Object> errorInfo = gson.fromJson(builder.toString(), new TypeToken<Map<String, Object>>(){}.getType());
					responseBody = gson.toJson(errorInfo);
				}
			}
			
			return new DittoResonse(statusCode, responseBody);
		} catch (Exception e) {
			throw e;
		} finally {
			if (http != null) http.disconnect();
		}
	}
	
	//http://localhost:8080/api/2/things/kr.re.etri.kmac:env_sensor
	
	private Map<String, String> getHeaders(boolean isDitto) {
		Map<String, String> headers = new HashMap<>();
		
		String username = propertiesConfig.getDittoThingAuthorizationUserName();
		String password = propertiesConfig.getDittoThingAuthorizationPassword();
		
		if (!isDitto) {
			username = propertiesConfig.getDittoPiggybackAuthorizationUserName();
			password = propertiesConfig.getDittoPiggybackAuthorizationPassword();
		}
		
		String token = username + ':' + password;
		
		headers.put("Authorization", "Basic " + new String(Base64.getEncoder().encode(token.getBytes())));
		headers.put("Content-Type", "application/json");
		
		return headers;
	}
	
	public String getThing(String thingId) throws Exception {
		String url = propertiesConfig.getDittoThingUrl() + "/" + thingId;
		Map<String, String> headers = getHeaders(true);
		
		DittoResonse dittoResonse = sendRequest(url, "GET", headers, null);
		
		String responseBody = dittoResonse.getResponseBody();
		int statusCode = dittoResonse.getStatusCode();
		if (statusCode == Constants.HTTP_OK_CODE) {
			return responseBody;
		} else {
			if (statusCode != Constants.HTTP_NOT_FOND_CODE) {
				throw new Exception(responseBody);
			}
		}
		
		return null;
	}
	
	public String registThing(Map<String, Object> thingInfo) throws Exception {
		String thingId = (String) thingInfo.get("thingId");
		
		String url = propertiesConfig.getDittoThingUrl() + "/" + thingId;
		Map<String, String> headers = getHeaders(true);
		String jsonParameter = new Gson().toJson(thingInfo);
		
		DittoResonse dittoResonse = sendRequest(url, "PUT", headers, jsonParameter);
		
		String responseBody = dittoResonse.getResponseBody();
		int statusCode = dittoResonse.getStatusCode();
		if (statusCode == Constants.HTTP_CREATE_CODE || statusCode == Constants.HTTP_NOCONTENT_CODE) {
			return responseBody;
		} else {
			throw new Exception(responseBody);
		}
	}	
	
	public String getConnection(String connectionId) throws Exception {
		String url = propertiesConfig.getDittoConnectionUrl() + "/" + connectionId;
		Map<String, String> headers = getHeaders(false);
		
		DittoResonse dittoResonse = sendRequest(url, "GET", headers, null);
		
		String responseBody = dittoResonse.getResponseBody();
		int statusCode = dittoResonse.getStatusCode();
		if (statusCode == Constants.HTTP_OK_CODE) {
			return responseBody;
		} else {
			return null;
		}
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String piggybackConnection(Map<String, Object> thingInfo, Map<String, Object> twinInfo) throws Exception {
		String thingId = (String) thingInfo.get("thingId");
		String[] thingSplit = thingId.split(":");
		
		Map<String, Object> features = (Map<String, Object>) thingInfo.get("features");	
		Map<String, Object> mappingResultFeatures = mapping_result_list.stream().filter(d -> d.get("thingId") != null && d.get("thingId").toString().equals(thingId)).findFirst().orElse(null);
		
		List<String> keys = new ArrayList(features.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			
			String topic = key;
			if (mappingResultFeatures != null) {
				Map<String, Object> findMappingResult = (Map<String, Object>) getMappingResultByKey(mappingResultFeatures, key);
				if (findMappingResult != null && findMappingResult.containsKey("topic") && findMappingResult.get("topic") != null) {
					String findTopic = (String) findMappingResult.get("topic");
					if (!findTopic.isEmpty()) {
						topic = findTopic;	
					}
				} 
			}
			
			String connectionId = "mqtt-connection-" + thingSplit[1] + "-" + topic;
			String oldConnection = getConnection(connectionId);			
			
			String url = propertiesConfig.getDittoPiggybackUrl();
			Map<String, String> headers = getHeaders(false);
			String jsonParameter = getPiggybackConnectionData(oldConnection == null, thingInfo, mappingResultFeatures, topic, key, (Map<String, Object>) features.get(key));
			
			DittoResonse dittoResonse = sendRequest(url, "POST", headers, jsonParameter);
			
			String responseBody = dittoResonse.getResponseBody();
			int statusCode = dittoResonse.getStatusCode();
			if (statusCode != Constants.HTTP_OK_CODE) {
				throw new Exception(responseBody);
			}
		}
		
		return "success";
	}
	
	public String getPiggybackConnectionData(boolean isCreate, Map<String, Object> thingInfo, Map<String, Object> mappingResultFeatures, String topic, String key, Map<String, Object> value) {
		String type = isCreate ? "connectivity.commands:createConnection" : "connectivity.commands:modifyConnection";
		String acsUrl = propertiesConfig.getDittoACSUrl();
		String thingId = (String) thingInfo.get("thingId");
		String[] thingSplit = thingId.split(":");
		
		String connectionData = 
				"{\n"
				+ "    \"targetActorSelection\": \"/system/sharding/connection\",\n"
				+ "    \"headers\": {\n"
				+ "    	\"aggregate\": false\n"
				+ "    },\n"
				+ "    \"piggybackCommand\": {\n"
				+ "        \"type\": \"" + type + "\",\n"
				+ "        \"connection\": {\n"
				+ "            \"id\": \"mqtt-connection-" + thingSplit[1] + "-" + topic + "\",\n"
				+ "            \"connectionType\": \"mqtt\",\n"
				+ "            \"connectionStatus\": \"open\",\n"
				+ "            \"failoverEnabled\": true,\n"
				+ "            \"uri\": \"" + acsUrl + "\",\n"
				+ "            \"sources\": [{\n"
				+ "                \"addresses\": [\"" + thingId +"/" + topic + "\"],\n"
				+ "                \"authorizationContext\": [\"nginx:ditto\"],\n"
				+ "                \"qos\": 0,\n"
				+ "                \"filters\": []\n"
				+ "            }],\n"
				+ "            \"mappingContext\": {\n"
				+ "                \"mappingEngine\": \"JavaScript\",\n"
				+ "                \"options\": {\n"
				+ "                    \"incomingScript\": \""+ getPayloadMapping(thingInfo, mappingResultFeatures, key, value) + "\",\n"
				+ "                    \"outgoingScript\": \"function mapFromDittoProtocolMsg(namespace, id, group, channel, criterion, action, path, dittoHeaders, value, status, extra) {return null;}\",\n"
				+ "                    \"loadBytebufferJS\": \"false\",\n"
				+ "                    \"loadLongJS\": \"false\"\n"
				+ "                }\n"
				+ "            }\n"
				+ "        }\n"
				+ "    }\n"
				+ "}";
		
		System.out.println(connectionData);
		return connectionData;
	}
	
	@SuppressWarnings({ "unchecked" })
	private String getPayloadMapping(Map<String, Object> thingInfo, Map<String, Object> mappingResultFeatures, String key, Map<String, Object> value) {
		String thingId = (String) thingInfo.get("thingId");
		String[] thingSplit = thingId.split(":");
		
		StringBuilder payloadValues = new StringBuilder();
		
		payloadValues.append("const value={");
//		payloadValues.append(key + ":{");
		
		if (value.containsKey("properties")) {
			payloadValues.append("properties:{");
			
			setPayloadValues(payloadValues, "jsonData", (Map<String, Object>) value.get("properties"), mappingResultFeatures, 1);
			
			payloadValues.append("}");
		}
		
//		payloadValues.append("}");
		payloadValues.append("};");
		
		String payload = 
				"function mapToDittoProtocolMsg(headers,textPayload,bytePayload,contentType){"
				+ 	"const jsonString=String.fromCharCode.apply(null,new Uint8Array(bytePayload));"
				+ 	"const jsonData=JSON.parse(jsonString);"
				+ 	payloadValues.toString()
				+ 	"return Ditto.buildDittoProtocolMsg("
				+ 		"'" + thingSplit[0] + "',"
				+ 		"'" + thingSplit[1] + "',"
				+ 		"'things',"
				+ 		"'twin',"
				+ 		"'commands',"
				+ 		"'modify',"
				+ 		"'/features/" + key  + "',"
				+ 		"headers,"
				+ 		"value"
				+ 	")"
				+ "}";
		
		return payload;
	}
	
	@SuppressWarnings("unchecked")
	public Object getMappingResultByKey(Map<String, Object> mappingResultFeatures, String findKey) {
		if (mappingResultFeatures.containsKey(findKey)) {
			return mappingResultFeatures.get(findKey);
		} else {
			for (String key : mappingResultFeatures.keySet()) {
				if (mappingResultFeatures.get(key) instanceof Map) {
					Object findItem = getMappingResultByKey((Map<String, Object>) mappingResultFeatures.get(key), findKey);
					if (findItem != null) {
						return findItem;	
					}
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setPayloadValues(StringBuilder payloadValues, String hierarchy, Map<String, Object> data, Map<String, Object> mappingResultFeatures, int depth) {
		List<String> keys = new ArrayList(data.keySet());
		
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			
			String topic = key;
			if (mappingResultFeatures != null) {
				Map<String, Object> findMappingResult = (Map<String, Object>) getMappingResultByKey(mappingResultFeatures, key);
				if (findMappingResult != null && findMappingResult.containsKey("topic") && findMappingResult.get("topic") != null) {
					String findTopic = (String) findMappingResult.get("topic");
					if (!findTopic.isEmpty()) {
						topic = findTopic;	
					}
				} 
			}
			
			if (data.get(key) instanceof Map) {
				payloadValues.append(key + ":{");
			
				setPayloadValues(payloadValues, hierarchy + "." + topic, (Map<String, Object>)data.get(key), mappingResultFeatures, depth + 1);
				
				payloadValues.append(i == keys.size() - 1 ? "}" : "},");
			} else {
				if (i == keys.size() - 1) {
					// ex) temp_c:devices[0].temp_c
					payloadValues.append(String.format("%s:%s.%s", key, hierarchy.toString(), topic));
				} else {
					// ex) temp_c:devices[0].temp_c,
					payloadValues.append(String.format("%s:%s.%s,", key, hierarchy.toString(), topic));	
				}
			}
		}
	}
	
	public String getFeaturesTopic(Map<String, Object> parameter) throws Exception {
		String thingId = (String) parameter.get("thingId");
		String topic = (String) parameter.get("topic");
		
		String url = propertiesConfig.getDittoThingUrl() + "/" + thingId + "/features/" + topic;
		Map<String, String> headers = getHeaders(true);
		
		DittoResonse dittoResonse = sendRequest(url, "GET", headers, null);
		
		String responseBody = dittoResonse.getResponseBody();
		int statusCode = dittoResonse.getStatusCode();
		if (statusCode == Constants.HTTP_OK_CODE) {
			return responseBody;
		} else {
			throw new Exception(responseBody);
		}
	}
	
	@Getter
	@Setter
	@ToString
	class DittoResonse {
		private int statusCode;
		private String responseBody;
		
		public DittoResonse(int statusCode, String responseBody) {
			this.statusCode = statusCode;
			this.responseBody = responseBody;
		}
	}
}
