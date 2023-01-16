package kr.re.etri.semanticanalysis.shared;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class RDFUtils {
	public static String TWIN_BASE_PROPERTY_URL = "https://etri.re.kr#";
	
	@SuppressWarnings("unchecked")
	public static Model getModelByJsonMap(Map<String, Object> twinInfo) {
		String dtId = (String) twinInfo.get("dt-id");
		
		Model model = ModelFactory.createDefaultModel();
		Resource resource = model.createResource(dtId);
		
		for(String key : twinInfo.keySet() ) {
			Object value = twinInfo.get(key);
			
			if (value instanceof String) {
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), (String) value);
			} else if (value instanceof Integer) {
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Integer.toString((int) value));
			} else if (value instanceof Double) {
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Double.toString((double) value));
			} else if (value instanceof List) {
				if (((List<?>)value).size() > 0) {
					if (((List<?>)value).get(0) instanceof Map) {
						getResourceByList(model, resource, key, (List<Map<String, Object>>) value);		
					} else if (((List<?>)value).get(0) instanceof String) {
						getResourceByListString(model, resource, key, (List<String>) value);
					}
				}
			}  else if (value instanceof Map){								
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), getResourceByMap(model, (Map<String, Object>) value));
			}
		}
		
		model.setNsPrefix("dt", TWIN_BASE_PROPERTY_URL );
		
//		if (twinInfo.get("name") != null) {
//			try {
//				String filePath =  System.getProperty("user.dir") + "/rdf/" + (String)twinInfo.get("name") + ".rdf";
//				File file = new File(filePath);
//				
//				FileOutputStream fos = new FileOutputStream(file);
//		        RDFDataMgr.write(fos, model, Lang.RDFXML);
//		        fos.close();	
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//		}
			
		
		return model;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Resource getResourceByMap(Model model, Map<String, Object> map) {
		Resource resource = model.createResource();
		
		for(String key : map.keySet() ) {
			Object value = map.get(key);
			if (value instanceof String) {
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), (String) value);
			} else if (value instanceof Integer) {
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Integer.toString((int) value));
			} else if (value instanceof Double) {
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Double.toString((double) value));
			} else if (value instanceof List) {
				if (((List<?>)value).size() > 0) {
					if (((List<?>)value).get(0) instanceof Map) {
						getResourceByList(model, resource, key, (List<Map<String, Object>>) value);		
					} else if (((List<?>)value).get(0) instanceof String) {
						getResourceByListString(model, resource, key, (List<String>) value);
					}
				}
			} else if (value instanceof Map){
				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), getResourceByMap(model, (Map<String, Object>) value));
			}
		}
		
		return resource;
	}
	
//	private Resource getResourceByList(Model model, Resource resource, String resourceKey, List<Map<String, Object>> valueList) {
//		Resource resource = model.createResource();
//		
//		String convertResourceKey = "notdefined";
//		if ("relations".equals(resourceKey)) {
//			convertResourceKey = "relation";
//		} else if ("features".equals(resourceKey)) {
//			convertResourceKey = "feature";
//		}
//		
//		for (Map<String, Object> map : valueList) {
//			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, convertResourceKey), getResourceByMap(model, map));
//		}
//		
//		return resource;
//	}
	
	public static void getResourceByListString(Model model, Resource resource, String key, List<String> valueList) {
		for (String value : valueList) {			
			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), value);
//			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, convertResourceKey), getResourceByMap(model, map));
		}
	}
	
	public static void getResourceByList(Model model, Resource resource, String key, List<Map<String, Object>> valueList) {
//		Resource resource = model.createResource();
//		
//		String convertResourceKey = "notdefined";
//		if ("relations".equals(resourceKey)) {
//			convertResourceKey = "relation";
//		} else if ("features".equals(resourceKey)) {
//			convertResourceKey = "feature";
//		}
		
		
		for (Map<String, Object> map : valueList) {			
			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), getResourceByMap(model, map));
//			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, convertResourceKey), getResourceByMap(model, map));
		}
	}
}
