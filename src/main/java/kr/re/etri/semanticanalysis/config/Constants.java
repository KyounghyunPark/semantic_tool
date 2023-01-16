package kr.re.etri.semanticanalysis.config;

import java.util.Arrays;
import java.util.List;

public class Constants {
	public static final int HTTP_OK_CODE = 200;
	public static final int HTTP_CREATE_CODE = 201;
	public static final int HTTP_NOCONTENT_CODE = 204;
	public static final int HTTP_UNAUTHORIZED_CODE = 401;
	public static final int HTTP_NOT_FOND_CODE = 404;
	public static final List<String> JSON_ORDER = Arrays.asList("dt-id", "hosting-iri", "name", "description", "baseurl", "edit", "nationality", "owner", "manufacturer", "relations", "features");
}
