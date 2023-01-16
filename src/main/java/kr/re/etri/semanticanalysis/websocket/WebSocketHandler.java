package kr.re.etri.semanticanalysis.websocket;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static List<WebSocketSession> sessionList = new ArrayList<>();

	@SuppressWarnings("deprecation")
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();

		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(payload);
		String target = element.getAsJsonObject().get("target").getAsString();

		if (target.equals("onRealTimeWebCamImage")) {
//			String machineId = element.getAsJsonObject().get("machineId").getAsString();

//			RealtimeWebCamImageSchedule.addImagePath(session, machineId, realtimeImagePath);
		} else if (target.equals("offRealTimeWebCamImage")) {
//			RealtimeWebCamImageSchedule.removeImagePath(session);
		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessionList.add(session);
		logger.info("Connected websocket.", session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessionList.remove(session);
		logger.info("Disconnected websocket.", session);
	}

	public void sendMessage(String data) throws Exception {
		TextMessage message = new TextMessage(data);
		for (WebSocketSession sess : sessionList) {
			sess.sendMessage(message);
		}
	}
}
