package kr.re.etri.semanticanalysis.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.springframework.stereotype.Service;

import kr.re.etri.semanticanalysis.config.Constants;

@Service
public class RestfulRequest {	
	public String sendRequest(String url, Map<String, Object> parameter) throws IOException {
		try {
			// URL 설정하고 접속하기
			HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection(); // 접속
			// --------------------------
			// 전송 모드 설정 - 기본적인 설정
			// --------------------------
			http.setDefaultUseCaches(false);
			http.setDoInput(true); // 서버에서 읽기 모드 지정
			http.setDoOutput(true); // 서버로 쓰기 모드 지정
			http.setRequestMethod("GET"); // 전송 방식은 GET

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
			} else {
				throw new IOException("API 호출에 실패했습니다.");
			}
		} catch (Exception e) {
			throw e;
		}
	}
}