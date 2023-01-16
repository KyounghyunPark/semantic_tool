package kr.re.etri.semanticanalysis;

import java.util.concurrent.Callable;

import kr.re.etri.semanticanalysis.request.DittoRequest;

public class WatchCallable implements Callable<Void> {
	
	DittoRequest dittoRequest;
	
	public WatchCallable(DittoRequest dittoRequest) {
		this.dittoRequest = dittoRequest;
	}
	
    @Override
    public Void call() throws Exception {
    	WatchFolder wf = new WatchFolder();
        wf.watchFolder(dittoRequest);
        
        return null;
    }
}