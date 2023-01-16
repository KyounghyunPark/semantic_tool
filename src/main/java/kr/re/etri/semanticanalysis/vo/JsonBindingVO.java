package kr.re.etri.semanticanalysis.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JsonBindingVO {
	private JsonValuesVO s;
	private JsonValuesVO p;
	private JsonValuesVO o;
}