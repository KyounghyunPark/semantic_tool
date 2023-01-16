package kr.re.etri.semanticanalysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.Getter;

@Configuration
@ConfigurationProperties(prefix = "application")
@Primary
@Getter

public class PropertiesConfig {
	@Value("${twinbase.twins.url}")
	private String twinBaseTwinsUrl;
	
	@Value("${login.id}")
	private String loginId;
	
	@Value("${login.password}")
	private String loginPassword;
	
	@Value("${rdf.array.names}")
	private String rdfArrayNames;
	
	
	// Mindsphere
	@Value("${mindsphere.gateway.url}")
	private String mindsphereGatewayUrl;
	
	@Value("${mindsphere.token.url}")
	private String mindsphereTokenUrl;
	
	@Value("${mindsphere.status.url}")
	private String mindsphereStatusUrl;
	
	
	// Ditto
	@Value("${ditto.url}")
	private String dittoUrl;
	
	@Value("${ditto.acs.url}")
	private String dittoACSUrl;
	
	@Value("${ditto.thing.url}")
	private String dittoThingUrl;
	
	@Value("${ditto.thing.authorization.username}")
	private String dittoThingAuthorizationUserName;
	
	@Value("${ditto.thing.authorization.password}")
	private String dittoThingAuthorizationPassword;
	
	@Value("${ditto.connection.url}")
	private String dittoConnectionUrl;
	
	@Value("${ditto.piggyback.url}")
	private String dittoPiggybackUrl;
	
	@Value("${ditto.piggyback.authorization.username}")
	private String dittoPiggybackAuthorizationUserName;
	
	@Value("${ditto.piggyback.authorization.password}")
	private String dittoPiggybackAuthorizationPassword;
	
}
