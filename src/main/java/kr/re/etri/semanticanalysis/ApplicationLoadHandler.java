package kr.re.etri.semanticanalysis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.ManagedBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import kr.re.etri.semanticanalysis.request.DittoRequest;
import kr.re.etri.semanticanalysis.service.TDBService;


@ManagedBean
public class ApplicationLoadHandler implements ApplicationRunner {
//	static String TDB_FOLDER_PATH = System.getProperty("user.dir") + "/tdb";
//	static String RDF_FOLDER_PATH = System.getProperty("user.dir") + "/rdf";
//	String inputFileName = "vc-db-1.rdf";
//
//	static String TWIN_BASE_PROPERTY_URL = "https://etri.re.kr#";
//	@Autowired
//	RestfulRequest restfulRequest;
//
//	@Autowired
//    PropertiesConfig propertiesConfig;
//	
	@Autowired 
	TDBService tdbService;
	
	@Autowired
	DittoRequest dittoRequest;
//	
//	@SuppressWarnings("unchecked")
	@Override
	public void run(ApplicationArguments args) throws Exception {
//		
		tdbService.TDBConnection();
		
		dittoRequest.readMappingResult();
		
		ExecutorService executor = Executors.newCachedThreadPool(); 
        executor.submit(new WatchCallable(dittoRequest));
        
		
//		tdbService.getRdfXml();
//		tdbService.getRdfJSON();
//		tdbService.getTurtle();
//
////		// rdf 저장
////		
////	    // some definitions
////        String personURI    = "http://somewhere/JohnSmith";
////        String givenName    = "John";
////        String familyName   = "Smith";
////        String fullName     = givenName + " " + familyName;
////        // create an empty model
////        Model model = ModelFactory.createDefaultModel();
////
////        // create the resource
////        //   and add the properties cascading style
////        Resource johnSmith 
////          = model.createResource(personURI)
////                 .addProperty(VCARD.FN, fullName)
////                 .addProperty(VCARD.N, 
////                              model.createResource()
////                                   .addProperty(VCARD.Given, givenName)
////                                   .addProperty(VCARD.Family, familyName));
////        
////		File directory = new File(RDF_FOLDER_PATH);
////		if (!directory.exists()) {
////			directory.mkdirs();
////		}
////		
////		String filePath = directory + File.separator + inputFileName;
////		File file = new File(filePath);
////		if (!file.exists()) {
////			file.createNewFile();
////		}
////		
////		FileOutputStream fos = new FileOutputStream(file);
////        RDFDataMgr.write(fos, model, Lang.RDFXML);
////        fos.close();
//
////        // rdf 읽기
////		String filePath = RDF_FOLDER_PATH + File.separator + inputFileName;
////        Model model = ModelFactory.createDefaultModel();
////
////        InputStream in = RDFDataMgr.open( filePath );
////
////        if (in == null) {
////            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
////        }
////        
////        // read the RDF/XML file
////        model.read(in, "");
////        model.write(System.out);
//
//		// rdf prefix control
////        Model m = ModelFactory.createDefaultModel();
////        String nsA = "http://somewhere/else#";
////        String nsB = "http://nowhere/else#";
////        Resource root = m.createResource( nsA + "root" );
////        Property P = m.createProperty( nsA + "P" );
////        Property Q = m.createProperty( nsB + "Q" );
////        Resource x = m.createResource( nsA + "x" );
////        Resource y = m.createResource( nsA + "y" );
////        Resource z = m.createResource( nsA + "z" );
////        m.add( root, P, x ).add( root, P, y ).add( y, Q, z );
////        System.out.println( "# -- no special prefixes defined" );
////        m.write( System.out );
////        System.out.println( "" );
////        System.out.println( "# -- nsA defined" );
////        m.setNsPrefix( "nsA", nsA );
////        m.write( System.out );
////        System.out.println( "" );
////        System.out.println( "# -- nsA and cat defined" );
////        m.setNsPrefix( "cat", nsB );
////        m.write( System.out );
//
////		String baseUrl = "https://github.com/AaltoIIC/dt-document/blob/master/descriptions-of-terms.md#";
////		String url = "https://dtw.twinbase.org/lab-room-K3-143/index.json";
////		String filePath = System.getProperty("user.dir") + "/rdf/vc-db-1.rdf";
////		
////		String response = restfulRequest.sendRequest(url, null);
////		if (response != null) {
////		    StreamRDF rdfStream = StreamRDFLib.writer(new OutputStreamWriter(new FileOutputStream(new File(filePath))));
////		    
////		    JsonParser parser = Json.createParser(new StringReader(response));
////            new JsonStreamRDFWriter(parser, rdfStream, baseUrl).convert();
////		}
////		
////		InputStream in = RDFDataMgr.open(filePath);
////		if (in == null) {
////			throw new IllegalArgumentException("File: " + inputFileName + " not found");
////		}
////
////		Model model = ModelFactory.createDefaultModel();
////		
////		model.read(in, null, "N-TRIPLES") ;
////		model.write(System.out);
//
////		Model totalModel = ModelFactory.createDefaultModel();
////		
////		Gson gson = new Gson();
////		String response = restfulRequest.sendRequest(propertiesConfig.getTwinBaseTwinsUrl(), null);
////		if (response != null) {
////			Map<String, Object> responseMap = gson.fromJson(response, new TypeToken<Map<String, Object>>(){}.getType());
////			List<Map<String, Object>> twins = (List<Map<String, Object>>) responseMap.get("twins");
////			if (twins != null && twins.size() > 0) {
////				for (Map<String, Object> twin : twins) {
////					String url = (String) twin.get("url");
////					String name = (String) twin.get("name");
////					
////					System.out.println("url : " + url + ", name : " + name);
////					
////					String twinResponse = restfulRequest.sendRequest(url + "/index.json", null);
////					Map<String, Object> twinResponseMap = gson.fromJson(twinResponse, new TypeToken<Map<String, Object>>(){}.getType());
////					
////					if (twinResponseMap.get("dt-id") != null) {
////						String dtId = (String) twinResponseMap.get("dt-id");
////						
////						Model model = ModelFactory.createDefaultModel();
////						Resource resource = model.createResource(dtId);
////						
////						for(String key : twinResponseMap.keySet() ) {
////							Object value = twinResponseMap.get(key);
////							
////							if (value instanceof String) {
////								resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), (String) value);
////							} else if (value instanceof Integer) {
////								resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Integer.toString((int) value));
////							} else if (value instanceof Double) {
////								resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Double.toString((double) value));
////							} else if (value instanceof List) {
////								//resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), getResourceByList(model, key, (List<Map<String, Object>>) value));
////								getResourceByList(model, resource, key, (List<Map<String, Object>>) value);
////							}  else if (value instanceof Map){								
////								resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), getResourceByMap(model, (Map<String, Object>) value));
////							}
////						}
////						
////						model.setNsPrefix("dt", TWIN_BASE_PROPERTY_URL );
////						
////						String filePath = RDF_FOLDER_PATH + File.separator + name + ".rdf";
////						File file = new File(filePath);
////						if (!file.exists()) {
////							file.createNewFile();
////						}
////						
////						FileOutputStream fos = new FileOutputStream(file);
////				        RDFDataMgr.write(fos, model, Lang.RDFXML);
////				        fos.close();			
////						
////				        totalModel.add(model);
////					}
////				}
////			}
////		}
//		
////		RDFDataMgr.write(System.out, model, Lang.TTL);
////		System.out.println();
////		RDFDataMgr.write(System.out, totalModel, Lang.RDFXML);
//		
////		String directory = TDB_FOLDER_PATH;
////		Dataset dataset = TDBFactory.createDataset(directory);
////		if (true) {
////			dataset.begin(ReadWrite.READ);
//////			String qs1 = "SELECT * {?s ?p ?o}" ;     
////			String qs1 = "SELECT *\r\n"
////					+ "WHERE {\r\n"
////					+ "  ?s <https://etri.re.kr#dt-id> \"http://dtid.org/3091b292-6104-4ef3-a30f-8275b46b5ab4\" .\r\n"
////					+ "  ?s <https://etri.re.kr#relations> ?o .\r\n"
////					+ "  ?o <https://etri.re.kr#dt-id> ?id .\r\n"
////					+ "  ?o <https://etri.re.kr#relationType> ?type .\r\n"
////					+ "}";
////
////			 try(QueryExecution qExec = QueryExecution.dataset(dataset).query(qs1).build() ) {
////			     ResultSet rs = qExec.execSelect() ;
////			     ResultSetFormatter.out(rs) ;
////			 } finally {
////				dataset.end();
////			}
////		} else {
////			dataset.begin(ReadWrite.WRITE);
////			dataset.setDefaultModel(totalModel);
////			dataset.commit();
////			dataset.end();
////		}
	}
//	
//	@SuppressWarnings("unchecked")
//	private Resource getResourceByMap(Model model, Map<String, Object> map) {
//		Resource resource = model.createResource();
//		
//		for(String key : map.keySet() ) {
//			Object value = map.get(key);
//			if (value instanceof String) {
//				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), (String) value);
//			} else if (value instanceof Integer) {
//				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Integer.toString((int) value));
//			} else if (value instanceof Double) {
//				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), Double.toString((double) value));
//			} else if (value instanceof List) {
//				getResourceByList(model, resource, key, (List<Map<String, Object>>) value);
//			} else if (value instanceof Map){
//				resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), getResourceByMap(model, (Map<String, Object>) value));
//			}
//		}
//		
//		return resource;
//	}
//	
////	private Resource getResourceByList(Model model, Resource resource, String resourceKey, List<Map<String, Object>> valueList) {
////		Resource resource = model.createResource();
////		
////		String convertResourceKey = "notdefined";
////		if ("relations".equals(resourceKey)) {
////			convertResourceKey = "relation";
////		} else if ("features".equals(resourceKey)) {
////			convertResourceKey = "feature";
////		}
////		
////		for (Map<String, Object> map : valueList) {
////			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, convertResourceKey), getResourceByMap(model, map));
////		}
////		
////		return resource;
////	}
//	
//	private void getResourceByList(Model model, Resource resource, String key, List<Map<String, Object>> valueList) {
////		Resource resource = model.createResource();
////		
////		String convertResourceKey = "notdefined";
////		if ("relations".equals(resourceKey)) {
////			convertResourceKey = "relation";
////		} else if ("features".equals(resourceKey)) {
////			convertResourceKey = "feature";
////		}
//		
//		for (Map<String, Object> map : valueList) {
//			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, key), getResourceByMap(model, map));	
////			resource.addProperty(model.createProperty(TWIN_BASE_PROPERTY_URL, convertResourceKey), getResourceByMap(model, map));
//		}
//	}
}