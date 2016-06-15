package com.sac.scratch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScratchMainTest {
	protected static final Logger logger = LoggerFactory.getLogger(ScratchMain.class);

	public static void main(String[] args) throws Exception {
		
		
		ScratchObj scratchObj = new ScratchObj();
		scratchObj.setScratch("gjj");
		scratchObj.setScratchGroup("sb");
		ScratchMain.runScratch(scratchObj);
	}
	
}
