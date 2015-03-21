package dataSpout;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class Data1Source extends BaseRichSpout {
    private int emitNum;
    
 //   private StringBuffer data = null;
    private SpoutOutputCollector collector = null;
//    private String[][] data ={{"10","20","30"}                         
//                             ,{"11","21","31"}
//                             ,{"12","22","32"}
//                             ,{"13","23","33"}
//                             ,{"14","24","34"}
//                             ,{"15","25","35"}
//                             ,{"16","26","36"}
//                             ,{"17","27","37"}
//                             ,{"18","28","38"}
//                             ,{"19","29","39"}
//                             ,{"10","21","30"}
//                             };


	@Override
	public void nextTuple() {
		     int lo = RandomUtils.nextInt(50);
		     String lonSt = String.valueOf(lo);
		     int we = RandomUtils.nextInt(1000)+1000;
		     String weSt = String.valueOf(we);
		     int he = RandomUtils.nextInt(1000)+1000;
		     String heSt = String.valueOf(he);
		     
		    

		    String[][] data = {{lonSt,weSt,heSt}};


   
             
//		try {
//			TimeUnit.SECONDS.sleep(1);
//			//Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
    //	if(emitNum<data.length){
			collector.emit("data1",new Values(data[emitNum][0],data[emitNum][1],data[emitNum][2]));
//		}
	//	emitNum++;
	
		
		
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		collector = arg2;
		emitNum = 0;
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declareStream("data1", new Fields("long","width","height"));
	}

}
