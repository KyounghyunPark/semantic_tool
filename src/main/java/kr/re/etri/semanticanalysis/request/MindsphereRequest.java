package kr.re.etri.semanticanalysis.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.re.etri.semanticanalysis.config.Constants;
import kr.re.etri.semanticanalysis.config.PropertiesConfig;

@Service
public class MindsphereRequest {
	@Autowired
	PropertiesConfig propertiesConfig;
	
	private Map<String, String> accessTokens = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public boolean sendAuthReqeust(Map<String, Object> parameter) throws IOException {
		try {
			String url = propertiesConfig.getMindsphereGatewayUrl() + "/" + propertiesConfig.getMindsphereTokenUrl();
			
			// URL 설정하고 접속하기
			HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection(); // 접속
			// --------------------------
			// 전송 모드 설정 - 기본적인 설정
			// --------------------------
			http.setDefaultUseCaches(false);
			http.setDoInput(true);
			http.setDoOutput(true); 
			http.setRequestMethod("POST"); 

			Map<String, Object> authorization = (Map<String, Object>) parameter.get("authorization");
			
			String id = (String) authorization.get("id");
			String[] idSplit = id.split("-");
			String secret = (String) authorization.get("secret");
			
			String token = id + ':' + secret;
			
			http.setRequestProperty("X-SPACE-AUTH-KEY", "Bearer " + new String(Base64.getEncoder().encode(token.getBytes())));
			http.setRequestProperty("Content-Type", "application/json");
			
			Map<String, Object> body = new HashMap<>();
			body.put("grant_type", "client_credentials");
			body.put("appName", idSplit[1]);
			body.put("appVersion", idSplit[2]);
			body.put("hostTenant", idSplit[0]);
			body.put("userTenant", idSplit[0]);
			
			try (OutputStream os = http.getOutputStream()){
				byte request_data[] = new Gson().toJson(body).getBytes("utf-8");
				os.write(request_data);
				os.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			int statusCode = http.getResponseCode();
			if (statusCode == Constants.HTTP_OK_CODE) {
				InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
				BufferedReader reader = new BufferedReader(tmp);
				StringBuilder builder = new StringBuilder();
				String str;
				while ((str = reader.readLine()) != null) {
					builder.append(str + "\n");
				}
				
				Map<String, Object> responseMap = new Gson().fromJson(builder.toString(), new TypeToken<Map<String, Object>>(){}.getType());
				if (responseMap.get("access_token") != null) {
					accessTokens.put(token, (String) responseMap.get("access_token"));
					return true;
				} 
			}
		} catch (Exception e) {
			throw e;
		}
		
		return false;
	}
	
	
	@SuppressWarnings("unchecked")
	public String sendStatusRequest(Map<String, Object> parameter) throws IOException {
		try {
			Map<String, Object> authorization = (Map<String, Object>) parameter.get("authorization");
			
			String id = (String) authorization.get("id");
			String secret = (String) authorization.get("secret");
			
			String token = id + ':' + secret;
			if (!this.accessTokens.containsKey(token)) {
				sendAuthReqeust(parameter);
			}
			
			String assetsId = (String) parameter.get("assets-id");
			String statusUrl = String.format(propertiesConfig.getMindsphereStatusUrl(), assetsId);
			
			String url = propertiesConfig.getMindsphereGatewayUrl() + "/" + statusUrl;
			
			HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection(); // 접속
			// --------------------------
			// 전송 모드 설정 - 기본적인 설정
			// --------------------------
			http.setDefaultUseCaches(false);
			http.setDoInput(true); // 서버에서 읽기 모드 지정
			http.setDoOutput(true); // 서버로 쓰기 모드 지정
			http.setRequestMethod("GET"); // 전송 방식은 GET
			
			
			http.setRequestProperty("Authorization", "Bearer " + this.accessTokens.get(token));
			
			System.out.println("Test sendStatusRequest");
			
			// --------------------------
			// Response Code
			// --------------------------
			int statusCode = http.getResponseCode();
			if (statusCode == Constants.HTTP_OK_CODE) {
				InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
				BufferedReader reader = new BufferedReader(tmp);
				StringBuilder builder = new StringBuilder();
				String str;
				while ((str = reader.readLine()) != null) {
					builder.append(str + "\n");
				}
				return builder.toString();
			} else if (statusCode == Constants.HTTP_UNAUTHORIZED_CODE){
				if (sendAuthReqeust(parameter)) {
					return sendStatusRequest(parameter);	
				} else {
					throw new IOException("권한 인증에 실패하였습니다.");
				}
			} else {
				throw new IOException("API 호출에 실패했습니다.");
			}
		} catch (Exception e) {
			throw e;
		}
	}


}
