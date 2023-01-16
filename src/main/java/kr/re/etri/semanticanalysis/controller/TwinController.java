package kr.re.etri.semanticanalysis.controller;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import kr.re.etri.semanticanalysis.config.Constants;
import kr.re.etri.semanticanalysis.config.PropertiesConfig;
import kr.re.etri.semanticanalysis.request.RestfulRequest;
import kr.re.etri.semanticanalysis.service.TDBService;
import kr.re.etri.semanticanalysis.shared.RDFUtils;
import kr.re.etri.semanticanalysis.vo.JsonBindingVO;
import kr.re.etri.semanticanalysis.vo.JsonResultVO;
import kr.re.etri.semanticanalysis.vo.TdbJsonResultVO;
import kr.re.etri.semanticanalysis.websocket.WebSocketHandler;

@RestController
@RequestMapping("/api/twin/")
public class TwinController {
	private static Logger logger = LoggerFactory.getLogger(TwinController.class);
	
	@Autowired
	TDBService tdbService;
	
	@Autowired
	RestfulRequest restfulRequest;
	
	@Autowired
	PropertiesConfig propertiesConfig;
	
	@Autowired
	WebSocketHandler webSocketHandler;
	
	@PostMapping("getList")
	public @ResponseBody String getList(@RequestBody Map<String, Object> parameter) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<Map<String, Object>> resultList = getTwinModel(null);
			
			map.put("succeeded", true);
			map.put("data", resultList);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return gson.toJson(map);
	}
	
	@PostMapping("getSparql")
	public @ResponseBody String getSparql(@RequestBody Map<String, Object> parameter) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String query = (String) parameter.get("sparql");
			String json = tdbService.select(query);
			
			map.put("succeeded", true);
			map.put("data", json);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return new Gson().toJson(map);
	}
	
	@PostMapping("add")
	public @ResponseBody String add(@RequestBody Map<String, Object> parameter) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			if (parameter.get("json") == null) {
				throw new Exception("invalid parameter");
			}
			
			tdbService.addModel((String) parameter.get("json"));
			sendWebSocket((String) parameter.get("uuid"));
			
			map.put("succeeded", true);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return new Gson().toJson(map);
	}
	
	@PostMapping("edit")
	public @ResponseBody String edit(@RequestBody Map<String, Object> parameter) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			if (parameter.get("dt-id") == null || parameter.get("json") == null) {
				throw new Exception("invalid parameter");
			}
			
			tdbService.deleteModel((String) parameter.get("dt-id"));
			tdbService.addModel((String) parameter.get("json"));
			sendWebSocket((String) parameter.get("uuid"));
			
			map.put("succeeded", true);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return new Gson().toJson(map);
	}
	
	@PostMapping("delete")
	public @ResponseBody String delete(@RequestBody Map<String, Object> parameter) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			if (parameter.get("dt-id") == null) {
				throw new Exception("invalid parameter");
			}
			
			tdbService.deleteModel((String) parameter.get("dt-id"));
			sendWebSocket((String) parameter.get("uuid"));
			
			map.put("succeeded", true);
		} catch (Exception e) {
			map.put("succeeded", false);
			map.put("data", e.getMessage());

			logger.error(e.getMessage());
		}
		
		return new Gson().toJson(map);
	}
	
	@PostMapping("getOtherFormat")
	public @ResponseBody String getOtherFormat(@RequestBody Map<String, Object> parameter) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			if (parameter.get("dt-id") == null) {
				throw new Exception("invalid parameter");
			}
			
			String dtId = (String) parameter.get("dt-id");
			String format = Optional.ofNullable(String.valueOf(parameter.get("format"))).orElse("RDFXML");
			String data = tdbService.getOtherFormat(dtId, format);
					
			 map.put("data", data);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map<String, Object>> getTwinModel(String query) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String resultJson = null;
		if (query == null) {
			resultJson = tdbService.selectAll();
		} else {
			resultJson = tdbService.select(query);
		}
		
		if (resultJson != null) {
			Map<String, Object> tmpTwinList = new HashMap<>();
			TdbJsonResultVO tdbJsonResult = new Gson().fromJson(resultJson, TdbJsonResultVO.class);
			if (tdbJsonResult.getResults() != null) {
				JsonResultVO jsonResult = tdbJsonResult.getResults();
				if (jsonResult.getBindings() != null) {
					List<JsonBindingVO> referenceList = new ArrayList<>();
					
					List<JsonBindingVO> bindings = jsonResult.getBindings();						
					for (JsonBindingVO binding : bindings) {
						String sType = binding.getS().getType();
						if ("uri".equals(sType)) {
							String dtId = binding.getS().getValue();
							
							Map<String, Object> twin = null;
							if (tmpTwinList.containsKey(dtId)) {
								twin = (Map<String, Object>) tmpTwinList.get(dtId);
							} else {
								twin = new HashMap<>();
								tmpTwinList.put(dtId, twin);
							}
							
							String pValue = binding.getP().getValue();
							String key = pValue.split("#")[1];
							String oType = binding.getO().getType();
							String oValue = binding.getO().getValue();
							
							if ("bnode".equals(oType)) {
								if (twin.containsKey(key)) {
									String value = (String) twin.get(key);
									twin.put(key, value + "," + oValue);
								} else {
									twin.put(key, "bnode:" + oValue);	
								}
							} else {
								twin.put(key, oValue);	
							}
						} else if ("bnode".equals(sType)) {
//							String sValue = binding.getS().getValue();
//							String oType = binding.getO().getType();
//							String oValue = binding.getO().getValue();
//							if ("bnode".equals(oType)) {
//								if (bnodeMapList.containsKey(sValue)) {
//									Map<String, Object> tmpBnodeMap = new HashMap<>();
//									
//									Map<String, Object> bnodeMap = (Map<String, Object>) bnodeMapList.get(sValue);
//									bnodeMap.put(oValue, tmpBnodeMap);
//									
//									bnodeMapList.put(oValue, tmpBnodeMap);
//								}
//								
//								
//							} 
							
							referenceList.add(binding);	
							
						}
					}
					
					
					for (Entry<String, Object> tmpTwinEntry : tmpTwinList.entrySet()) {
						Map<String, Object> twin = (Map<String, Object>) tmpTwinEntry.getValue(); 
						
						for (Entry<String, Object> twinEntry : twin.entrySet()) {
							if (twinEntry.getValue() instanceof String) {
								String key = (String) twinEntry.getKey();
								String value = (String) twinEntry.getValue();
								setJsonByBnodeData(key, value, twin, referenceList);
							} 
						}
					}
					
					
					
//					
//					for (JsonBindingVO binding : referenceList) {
//						String sType = binding.getS().getType();
//						if ("bnode".equals(sType)) {
//							String sValue = binding.getS().getValue();
//							
//							if (bnodeMapList.containsKey(sValue)) {
//								Map<String, Object> bnodeMap = (Map<String, Object>) bnodeMapList.get(sValue);
//								
//								String key = binding.getP().getValue().split("#")[1];
//								String value = binding.getO().getValue();
//								
//								bnodeMap.put(key, value);
//							}
//						}	
//					}
					
					resultList = new ArrayList(tmpTwinList.values());
				}
				
			}
		}
		
		if (resultList.size() > 0) {
			for (int i = 0; i < resultList.size(); i++ ) {
				Map<String, Object> map = resultList.get(i);
				
				List<Map.Entry<String, Object>> entries = new LinkedList<>(map.entrySet());
			    Collections.sort(entries, (s1, s2) -> {
			    	int s1Index = Constants.JSON_ORDER.indexOf(s1.getKey());
		        	s1Index = s1Index == -1 ? 1000 : s1Index;
		        	
		        	int s2Index = Constants.JSON_ORDER.indexOf(s2.getKey());
		        	s2Index = s2Index == -1 ? 1000 : s2Index;
		        	
		        	if (s1Index < s2Index) {
		        		return -1;
		        	} else {
		        		return 1;
		        	}
			    });

			    Map<String, Object> result = new LinkedHashMap<>();
			    for (Map.Entry<String, Object> entry : entries) {
			        result.put(entry.getKey(), entry.getValue());
			    }
				
			    resultList.set(i, result );
			}
		}
		
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	private void setJsonByBnodeData(String key, String value, Map<String, Object> twin, List<JsonBindingVO> referenceList) {
		if (value.indexOf("bnode:") != -1) {
			String bnodeValues = value.substring(6, value.length());
			String[] bnodeList = bnodeValues.split(",");
			
			if (bnodeList.length > 0) {
				if (propertiesConfig.getRdfArrayNames().indexOf(key) != -1 || bnodeList.length > 1) {
					List<Map<String, Object>> valueList = new ArrayList<>();
					
					for (String bnode : bnodeList) {
						List<JsonBindingVO> findReferenceList = referenceList.stream().filter(d -> d.getS().getValue().equals(bnode)).collect(Collectors.toList());
						
						List<String> excludeBnodeList = new ArrayList<>();
						Map<String, Object> bnodeMap = new HashMap<>();
						for (JsonBindingVO binding : findReferenceList) {
							String oType = binding.getO().getType();
							if ("bnode".equals(oType)) {
								String pValueOriginal = binding.getP().getValue();
								String pValue = binding.getP().getValue().split("#")[1];
								
								if (excludeBnodeList.indexOf(binding.getO().getValue()) == -1) {
									String oValue = "bnode:" + binding.getO().getValue();
									excludeBnodeList = findReferenceList.stream().filter(d -> d.getP().getValue().equals(pValueOriginal)).map(d -> d.getO().getValue()).collect(Collectors.toList());
									if (excludeBnodeList.size() > 1) {
										oValue = "bnode:" + String.join(",", excludeBnodeList);
									}
									bnodeMap.put(pValue, oValue);
									setJsonByBnodeData(pValue, oValue, bnodeMap, referenceList);
								}
							} else {
								String pValue = binding.getP().getValue().split("#")[1];
								String oValue = binding.getO().getValue();
								
								if (propertiesConfig.getRdfArrayNames().indexOf(pValue) != -1) {
									if (bnodeMap.containsKey(pValue)) {
										if (bnodeMap.get(pValue) instanceof List) {
											List<String> list = (List<String>) bnodeMap.get(pValue);
											list.add(oValue);
											
											bnodeMap.put(pValue, list);
										}
									} else {
										List<String> list = new ArrayList<>();
										list.add(oValue);
										
										bnodeMap.put(pValue, list);
									}
								} else {
									bnodeMap.put(pValue, oValue);
								}
							}
						}
						
						valueList.add(bnodeMap);
					}
					
					twin.put(key, valueList);
				} else {
					List<JsonBindingVO> findReferenceList = referenceList.stream().filter(d -> d.getS().getValue().equals(bnodeList[0])).collect(Collectors.toList());
					
					List<String> excludeBnodeList = new ArrayList<>();
					Map<String, Object> bnodeMap = new HashMap<>();
					for (JsonBindingVO binding : findReferenceList) {
						String oType = binding.getO().getType();
						if ("bnode".equals(oType)) {
							String pValueOriginal = binding.getP().getValue();
							String pValue = binding.getP().getValue().split("#")[1];
							
							if (excludeBnodeList.indexOf(binding.getO().getValue()) == -1) {
								String oValue = "bnode:" + binding.getO().getValue();
								excludeBnodeList = findReferenceList.stream().filter(d -> d.getP().getValue().equals(pValueOriginal)).map(d -> d.getO().getValue()).collect(Collectors.toList());
								if (excludeBnodeList.size() > 1) {
									oValue = "bnode:" + String.join(",", excludeBnodeList);
								}
								bnodeMap.put(pValue, oValue);
								setJsonByBnodeData(pValue, oValue, bnodeMap, referenceList);
							}
						} else {
							String pValue = binding.getP().getValue().split("#")[1];
							String oValue = binding.getO().getValue();
							
							bnodeMap.put(pValue, oValue);
						}
					}
					
					twin.put(key, bnodeMap);
				}
			}
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	@PostMapping("collect")
	public @ResponseBody String collect(@RequestBody Map<String, Object> parameter) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			tdbService.deleteAllModel();
			
			String response = restfulRequest.sendRequest(propertiesConfig.getTwinBaseTwinsUrl(), null);
			if (response != null) {
				Map<String, Object> responseMap = gson.fromJson(response, new TypeToken<Map<String, Object>>(){}.getType());
				List<Map<String, Object>> twins = (List<Map<String, Object>>) responseMap.get("twins");
				if (twins != null && twins.size() > 0) {
					for (Map<String, Object> twin : twins) {
						String url = (String) twin.get("url");
						String name = (String) twin.get("name");
						
						System.out.println("url : " + url + ", name : " + name);
						
						String twinResponse = restfulRequest.sendRequest(url + "/index.json", null);
						Map<String, Object> twinInfo = gson.fromJson(twinResponse, new TypeToken<Map<String, Object>>(){}.getType());
						
						if (twinInfo.get("dt-id") != null) {
							String dtId = (String) twinInfo.get("dt-id");
							Resource resource = tdbService.getResource(dtId);
							if (resource == null) {
								Model model = RDFUtils.getModelByJsonMap(twinInfo);
								tdbService.addModel(model);
							}
						}
						
					}
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
	
	
	
	@PostMapping("download")
	public ResponseEntity<?> download(@RequestBody Map<String, Object> parameter) {
		try {
			if (parameter.get("type") == null) {
				return ResponseEntity.badRequest().build();
			}
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			String type = (String) parameter.get("type");
			if ("all".equals(type)) {
				List<Map<String, Object>> resultList = getTwinModel(null);
				if (resultList.size() > 0) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ZipOutputStream zipOut = new ZipOutputStream(baos);
				
					for (int i = 0; i < resultList.size(); i++) {
						Map<String, Object> data = resultList.get(i);
						String name = "no_name_" + i;
						
						if (data.get("name") != null ) {
							name = (String) data.get("name");
						}
		                
		                String json = gson.toJson(data);
						
						ZipEntry zipEntry = new ZipEntry(name + ".json");
						
				        zipOut.putNextEntry(zipEntry);
				        zipOut.write(json.getBytes());
	            	}
				    
					zipOut.closeEntry();
					zipOut.close();
					
					baos.close();

	                return ResponseEntity.ok()
	                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                        .header("Content-Disposition", "attachment; filename=\"all_twin_json_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())  + ".zip\"")
	                        .body(baos.toByteArray());
					
				} else {
				   	return ResponseEntity.noContent().build();
				}
			} else {
				if (parameter.get("dt-id") == null) {
					return ResponseEntity.badRequest().build();
				}
				String dtId = (String) parameter.get("dt-id");
				String format = (String) Optional.ofNullable(parameter.get("format")).orElse("JSON");
				
				if ("JSON".equals(format)) {
					String query = "SELECT * WHERE"
							+ "{ "
							+ "  { "
							+ "    SELECT * "
							+ "    WHERE { "
							+ "      BIND (<%D> as ?s) "
							+ "       ?s ?p ?o.\r\n"
							+ "    } "
							+ "  } "
							+ "  UNION "
							+ "  { "
							+ "     SELECT ?s ?p ?o "
							+ "     WHERE { "
							+ "        <%D> ?a ?s. "
							+ "        ?s ?p ?o . "
							+ "     } "
							+ "  } "
							+ "  UNION "
							+ "  { "
							+ "     SELECT ?s ?p ?o "
							+ "     WHERE { "
							+ "        <%D> ?a ?b. "
							+ "        ?b ?c ?s . "
							+ "        ?s ?p ?o . "
							+ "     } "
							+ "  } "
							+ "}";
					query = query.replaceAll("%D", dtId);
					
					List<Map<String, Object>> resultList = getTwinModel(query);
					if (resultList.size() > 0) {
						Map<String, Object> data = resultList.get(resultList.size() - 1);
						String name = "no_name";
						
						if (data.get("name") != null ) {
							name = (String) data.get("name");
						}
		                
		                String json = gson.toJson(data);

		                ByteArrayResource resource = new ByteArrayResource(json.getBytes());

		                return ResponseEntity.ok()
		                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
		                        .header("Content-Disposition", "attachment; filename=\""+ name + ".json\"")
		                        .body(resource);
						
					} else {
					   	return ResponseEntity.noContent().build();
					}
				} else {
					String data = tdbService.getOtherFormat(dtId, format);
					if (data != null && !data.isEmpty()) {
						String name = "no_name";
						
						Resource rdfResource = tdbService.getResource(dtId);
						if (rdfResource != null) {
							Statement st = tdbService.getProperty(rdfResource, "name");
							name = st.getObject().toString();
						}
						
		                ByteArrayResource resource = new ByteArrayResource(data.getBytes());

		                return ResponseEntity.ok()
		                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
		                        .header("Content-Disposition", "attachment; filename=\""+ name + ".rdf\"")
		                        .body(resource);
						
					} else {
					   	return ResponseEntity.noContent().build();
					}
				}
			}
    	} catch(Exception e) {
    		return ResponseEntity.internalServerError().build();
    	}		
	}
}
