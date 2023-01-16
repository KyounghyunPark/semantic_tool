package kr.re.etri.semanticanalysis.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TdbJsonResultVO {
	private JsonHeadVO head;
	private JsonResultVO results;

}