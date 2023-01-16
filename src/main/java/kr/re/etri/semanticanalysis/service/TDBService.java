package kr.re.etri.semanticanalysis.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.re.etri.semanticanalysis.shared.RDFUtils;
import kr.re.etri.semanticanalysis.vo.JsonBindingVO;
import kr.re.etri.semanticanalysis.vo.JsonResultVO;
import kr.re.etri.semanticanalysis.vo.TdbJsonResultVO;

@Service
public class TDBService {
	private static Logger logger = LoggerFactory.getLogger(TDBService.class);
	
	private static String TDB_FOLDER_PATH = System.getProperty("user.dir") + "/tdb";
	private Dataset dataset;
	
	public void TDBConnection() {
		File directory = new File(TDB_FOLDER_PATH);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		dataset = TDBFactory.createDataset(directory.getAbsolutePath());
	}
	
	@SuppressWarnings("unchecked")
	public void addModel(String json) {
		Map<String, Object> twinInfo = new Gson().fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
		if (twinInfo.get("dt-id") != null) {
			Model model = RDFUtils.getModelByJsonMap(twinInfo);
			addModel(model);
			
			if (twinInfo.containsKey("relations")) {
				String dtId = (String) twinInfo.get("dt-id");
				
				if (twinInfo.get("relations") instanceof List) {
					List<Map<String, Object>> relations = (List<Map<String, Object>>) twinInfo.get("relations");
					for (Map<String, Object> relation : relations) {
						if (relation.containsKey("dt-id") && relation.containsKey("relationType")) {
							String targetDtId = (String) relation.get("dt-id");
							String targetRelationType = (String) relation.get("relationType");
							
							Resource resource = getResource(targetDtId);
							if (resource != null) {
								StmtIterator stmtIterator = getPropertyIterator(resource, "relations");
								
								List<Statement> statementList = stmtIterator.toList();
								if (statementList.size() == 0) {
									addRelation(resource, dtId, "parent".equals(targetRelationType) ? "child" : "parent");
								} else {
									boolean isExist = false;
									for (int i = 0; i < statementList.size(); i++) {
										Statement statement = statementList.get(i);
										Resource relationResource = statement.getResource();
										
										Statement relationDtIdStatement = getProperty(relationResource, "dt-id");
										if (relationDtIdStatement != null) {
											String relationDtId = relationDtIdStatement.getObject().toString();
											if (relationDtId.equals(dtId)) {
												Statement relationRelationTypeStatement = getProperty(relationResource, "relationType");
												if (relationRelationTypeStatement != null) {
													String relationType = relationRelationTypeStatement.getObject().toString();
													if (relationType.equals(targetRelationType)) {
														updateRelation(relationDtIdStatement, dtId);
														updateRelation(relationRelationTypeStatement, "parent".equals(targetRelationType) ? "child" : "parent");
													}
												}
												isExist = true;
												break;
											}	
										}
									}
									
									if (!isExist) {
										addRelation(resource, dtId, "parent".equals(targetRelationType) ? "child" : "parent");
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void addModel(Model model) {
		dataset.begin(ReadWrite.WRITE);
		
		Model defaultModel = dataset.getDefaultModel();
		defaultModel.add(model);
		
		dataset.commit();
		dataset.end();
	}
	
	public void deleteAllModel() {
		dataset.begin(ReadWrite.WRITE);
		
		Model defaultModel = dataset.getDefaultModel();
		defaultModel.removeAll();
		
		dataset.commit();
		dataset.end();
	}
	
	public void deleteModel(String dtId) {
		if (dtId != null) {
			Resource resource = getResource(dtId);
			if (resource != null) {
				deleteChildResource(resource);
				
				String query = "SELECT *\n"
						+ "WHERE {\n"
						+ "  ?s <https://etri.re.kr#relations> ?o .\n"
						+ "  ?o <https://etri.re.kr#dt-id> \"" + dtId + "\" .\n"
						+ "}";
				
				String queryResult = select(query);
				TdbJsonResultVO tdbJsonResult = new Gson().fromJson(queryResult, TdbJsonResultVO.class);
				if (tdbJsonResult.getResults() != null) {
					JsonResultVO jsonResult = tdbJsonResult.getResults();
					if (jsonResult.getBindings() != null) {
						List<JsonBindingVO> bindings = jsonResult.getBindings();						
						for (JsonBindingVO binding : bindings) {
							String targetDtId = binding.getS().getValue();
							
							Resource targetResource = getResource(targetDtId);
							if (targetResource != null) {
								StmtIterator targetStmtIterator = getPropertyIterator(targetResource, "relations");
								List<Statement> targetStatementList = targetStmtIterator.toList();
								for (Statement targetStatement : targetStatementList) {
									Resource targetRelationResource = targetStatement.getResource();
									Statement targetRelationDtIdStatement = getProperty(targetRelationResource, "dt-id");
									String targetRelationDtId = targetRelationDtIdStatement.getObject().toString();
									
									if (targetRelationDtId.equals(dtId)) {
										deleteReleation(targetStatement);
										deleteModel(targetRelationResource);
									}
								}
							}
						}
					}
				}
				
//				StmtIterator stmtIterator = tdbService.getPropertyIterator(resource, "relations");
//				List<Statement> statementList = stmtIterator.toList();
//				if (statementList.size() > 0) {
//					for (Statement statement : statementList) {
//						Resource relationResource = statement.getResource();
//						
//						Statement relationDtIdStatement = tdbService.getProperty(relationResource, "dt-id");
//						String relationDtId = relationDtIdStatement.getObject().toString();
//						
//						Resource targetResource = tdbService.getResource(relationDtId);
//						if (targetResource != null) {
//							StmtIterator targetStmtIterator = tdbService.getPropertyIterator(targetResource, "relations");
//							List<Statement> targetStatementList = targetStmtIterator.toList();
//							for (Statement targetStatement : targetStatementList) {
//								Resource targetRelationResource = targetStatement.getResource();
//								Statement targetRelationDtIdStatement = tdbService.getProperty(targetRelationResource, "dt-id");
//								String targetRelationDtId = targetRelationDtIdStatement.getObject().toString();
//								
//								if (targetRelationDtId.equals(dtId)) {
//									tdbService.deleteReleation(targetStatement);
//									tdbService.deleteModel(targetRelationResource);
//								}
//							}
//						}
//						
//						tdbService.deleteModel(relationResource);
//					}
//				}
				
				deleteModel(resource);
				
				
			}
		}
	}
	
	private void deleteChildResource(Resource resource) {
		StmtIterator stmtIterator = getPropertyAll(resource);
		List<Statement> statementList = stmtIterator.toList();
		for (int i = 0; i < statementList.size(); i++) {
			Statement statement = statementList.get(i);
			if (statement.getObject() instanceof ResourceImpl) {
				Resource subResource = statement.getResource();
				deleteChildResource(subResource); 
				
				deleteModel(subResource);	
			}
		}
	}
	
	public void deleteModel(Resource resource) {
		dataset.begin(ReadWrite.WRITE);
		
		Model defaultModel = dataset.getDefaultModel();
		defaultModel.removeAll(resource, null, (RDFNode) null);
		
		dataset.commit();
		dataset.end();
	}
	
	public Statement getProperty(Resource resource, String localName) {
		Statement statement = null;
		
		dataset.begin(ReadWrite.READ);
		
		Model defaultModel = dataset.getDefaultModel();
		statement = resource.getProperty(defaultModel.getProperty(RDFUtils.TWIN_BASE_PROPERTY_URL, localName));
		
		dataset.end();
		
		return statement;
	}
	
	public StmtIterator getPropertyAll(Resource resource) {
		StmtIterator stmtIterator = null;
		
		dataset.begin(ReadWrite.READ);
		
		stmtIterator = resource.listProperties();
		
		dataset.end();
		
		return stmtIterator;
	}
	
	public StmtIterator getPropertyIterator(Resource resource, String localName) {
		StmtIterator stmtIterator = null;
		
		dataset.begin(ReadWrite.READ);
		
		Model defaultModel = dataset.getDefaultModel();
		stmtIterator = resource.listProperties(defaultModel.getProperty(RDFUtils.TWIN_BASE_PROPERTY_URL, localName));
		
		dataset.end();
		
		return stmtIterator;
	}
	
	public Resource getResource(String dtId) {
		Resource resource = null;
		
		dataset.begin(ReadWrite.READ);
		
		Model defaultModel = dataset.getDefaultModel();
		Resource toSearch = ResourceFactory.createResource(dtId);
		if (defaultModel.contains( toSearch, null, (RDFNode) null)) {
			resource = defaultModel.getResource(dtId);
		}
		
		dataset.end();
		
		return resource;
	}
	
	public void addRelation(Resource resource, String dtId, String relationType) {
		dataset.begin(ReadWrite.WRITE);
		
		Model defaultModel = dataset.getDefaultModel();
		
		Resource relationResource = defaultModel.createResource();
		relationResource.addProperty(defaultModel.createProperty(RDFUtils.TWIN_BASE_PROPERTY_URL, "dt-id"), dtId);
		relationResource.addProperty(defaultModel.createProperty(RDFUtils.TWIN_BASE_PROPERTY_URL, "relationType"), relationType);
		
		resource.addProperty(defaultModel.createProperty(RDFUtils.TWIN_BASE_PROPERTY_URL, "relations"), relationResource);	
		
		dataset.commit();
		dataset.end();
	}
	
	public void updateRelation(Statement statement, String value) {
		dataset.begin(ReadWrite.WRITE);
		
		statement.changeObject(value);
		
		dataset.commit();
		dataset.end();
	}
	
	public void deleteReleation(Statement statement) {
		dataset.begin(ReadWrite.WRITE);
		
		Model defaultModel = dataset.getDefaultModel();
		defaultModel.remove(statement);
		
		dataset.commit();
		dataset.end();
	}
	
	public String selectAll() {
		String json = null;
		dataset.begin(ReadWrite.READ);
		String sparqlQueryString = "SELECT * WHERE { ?s ?p ?o }";
		try(QueryExecution qExec = QueryExecution.dataset(dataset).query(sparqlQueryString).build() ) {
			ResultSet rs = qExec.execSelect();
			//ResultSetFormatter.out(rs) ;
		     
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsJSON(outputStream, rs);
		    json = new String(outputStream.toByteArray());
		 } finally {
			dataset.end();
		}
		
		return json;
	}
	
	public String select(String query) {
		String json = null;
		dataset.begin(ReadWrite.READ);
		try(QueryExecution qExec = QueryExecution.dataset(dataset).query(query).build() ) {
			ResultSet rs = qExec.execSelect();
//			ResultSetFormatter.out(rs) ;
		     
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsJSON(outputStream, rs);
		    json = new String(outputStream.toByteArray());
		 } finally {
			dataset.end();
		}
		
		return json;
	}
	
	public ResultSet selectResultSet(String query) {
		ResultSet rs = null;
		dataset.begin(ReadWrite.READ);
		try(QueryExecution qExec = QueryExecution.dataset(dataset).query(query).build() ) {
			rs = qExec.execSelect();
		 } finally {
			dataset.end();
		}
		
		return rs;
	}
	
	public String getOtherFormat(String dtId, String format) {
		String data = "";
		ByteArrayOutputStream os = null;
		
		try {
			dataset.begin(ReadWrite.READ) ;
			String sparqlQueryString = 
					"CONSTRUCT { "
				+ 		"?s ?p ?o . GRAPH ?g1 { ?s1 ?p1 ?o1 } "
				+ 	"} "
				+ 	"WHERE {"
				+ 		"SELECT * WHERE"
				+ 		"{ "
				+ 		"  { "
				+ 		"    SELECT * "
				+		 "    WHERE { "
				+		 "      BIND (<%D> as ?s) "
				+ 		"       ?s ?p ?o.\r\n"
				+ 		"    } "
				+ 		"  } "
				+ 		"  UNION "
				+ 		"  { "
				+ 		"     SELECT ?s ?p ?o "
				+ 		"     WHERE { "
				+ 		"        <%D> ?a ?s. "
				+ 		"        ?s ?p ?o . "
				+ 		"     } "
				+ 		"  } "
				+ 		"  UNION "
				+ 		"  { "
				+ 		"     SELECT ?s ?p ?o "
				+ 		"     WHERE { "
				+ 		"        <%D> ?a ?b. "
				+ 		"        ?b ?c ?s . "
				+ 		"        ?s ?p ?o . "
				+ 		"     } "
				+ 		"  } "
				+ 		"}"		
				+	"}";
			
			sparqlQueryString = sparqlQueryString.replaceAll("%D", dtId);
			Query query = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ) ;
		    QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
		    Model model = qexec.execConstruct();
		    //model.write(System.out, "RDF/XML");
		    
		    os = new ByteArrayOutputStream();
		    
		    if ("RDFJSON".equals(format)) {
		    	RDFDataMgr.write(os, model, Lang.RDFJSON) ;	
		    } else if ("RDFXML".equals(format)) {
		    	RDFDataMgr.write(os, model, Lang.RDFXML) ;	
		    } else if ("JSONLD".equals(format)) {
		    	RDFDataMgr.write(os, model, Lang.JSONLD) ;	
		    } else {
		    	RDFDataMgr.write(os, model, Lang.TURTLE) ;
		    }
		    
		    data = new String(os.toByteArray());
		    
		    os.close();
		    qexec.close() ;

		    dataset.end();
		    dataset.close();
		} catch (Exception ex) {
			logger.info(ex.getMessage());
		}

	    return data;
	}
	
	public void getRdfXml() {
		dataset.begin(ReadWrite.READ) ;
//		String sparqlQueryString = "SELECT * WHERE { GRAPH ?graph { ?s ?p ?o } }";
//		String sparqlQueryString = "SELECT * WHERE { ?s ?p ?o .}";
//		String sparqlQueryString = "CONSTRUCT { ?s ?p ?o . GRAPH ?g1 { ?s1 ?p1 ?o1 } } WHERE {?s ?p ?o. filter(?s=<https://dtid.org/e96c7cfa-5bae-4e44-821d-09d1040818c1>)}";
		String sparqlQueryString = "CONSTRUCT { ?s ?p ?o . GRAPH ?g1 { ?s1 ?p1 ?o1 } } WHERE {SELECT * WHERE{   {     SELECT *     WHERE {       BIND (<https://dtid.org/f92f8728-13ff-42c8-8a3e-94c6d1b62fa6> as ?s)        ?s ?p ?o.\r\n"
				+ "    }   }   UNION   {      SELECT ?s ?p ?o      WHERE {         <https://dtid.org/f92f8728-13ff-42c8-8a3e-94c6d1b62fa6> ?a ?s.         ?s ?p ?o .      }   }   UNION   {      SELECT ?s ?p ?o      WHERE {         <https://dtid.org/f92f8728-13ff-42c8-8a3e-94c6d1b62fa6> ?a ?b.         ?b ?c ?s .         ?s ?p ?o .      }   } }}";
		Query query = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ) ;
	    QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
	    Model model = qexec.execConstruct();
	    //model.write(System.out, "RDF/XML");
	    RDFDataMgr.write(System.out, model, Lang.JSONLD) ;
//	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//	    ResultSetFormatter.outputAsJSON(outputStream, results);
//	    String json = new String(outputStream.toByteArray());
	    qexec.close() ;

	    dataset.end();
	    dataset.close();

//	    System.out.println(json);
	}
	
	public void getRdfJSON() {
		dataset.begin(ReadWrite.READ) ;
//		String sparqlQueryString = "CONSTRUCT { ?s ?p ?o . GRAPH ?g1 { ?s1 ?p1 ?o1 } } WHERE {?s ?p ?o}";
		String sparqlQueryString = "CONSTRUCT { ?s ?p ?o . GRAPH ?g1 { ?s1 ?p1 ?o1 } } WHERE {?s <https://etri.re.kr#dt-id> \"https://dtid.org/e96c7cfa-5bae-4e44-821d-09d1040818c1\"}";
		Query query = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ) ;
	    QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
	    Model model = qexec.execConstruct();
	    model.write(System.out, "RDF/JSON");
	    qexec.close() ;

	    dataset.end();
	    dataset.close();
	}
	
	public void getTurtle() {
		dataset.begin(ReadWrite.READ) ;
//		String sparqlQueryString = "CONSTRUCT { ?s ?p ?o . GRAPH ?g1 { ?s1 ?p1 ?o1 } } WHERE {?s ?p ?o}";
		String sparqlQueryString = "CONSTRUCT { ?s ?p ?o . GRAPH ?g1 { ?s1 ?p1 ?o1 } } WHERE {?s <https://etri.re.kr#dt-id> \"https://dtid.org/e96c7cfa-5bae-4e44-821d-09d1040818c1\"}";
		Query query = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ) ;
	    QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
	    Model model = qexec.execConstruct();
	    model.write(System.out, "Turtle");
	    qexec.close() ;

	    dataset.end();
	    dataset.close();
	}
}
