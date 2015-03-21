package dataSpout;

import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class Data3Spout extends BaseRichSpout {
    private int emitNum;
    private String[][] data = {};
    private SpoutOutputCollector collector = null;
    
	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		if(emitNum<data.length){
			collector.emit("data", new Values(data[emitNum][0],data[emitNum][1],data[1][2]));
		}
		emitNum++;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		collector = arg2;
		emitNum = 0;
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declareStream("data",new Fields("long","weight","height"));
		
	}

}
