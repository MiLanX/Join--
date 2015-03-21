package Bolt;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;


public class OutputDemo extends BaseBasicBolt {
    public OutputDemo(){}
	@Override
	public void execute(Tuple arg0, BasicOutputCollector arg1) {
	
		if(arg0.size()!=0){
		 System.err.println("join result:"+arg0.getValues());
	    }
	}
	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
	
		
	}

}
