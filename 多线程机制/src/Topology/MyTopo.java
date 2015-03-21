package Topology;

import Bolt.HashJoin;
import Bolt.OutputDemo;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import dataSpout.Data1Source;
import dataSpout.Data2Spout;

public class MyTopo {
    private static TopologyBuilder builder = new TopologyBuilder();
    public static void main(String[] args){
    	Config config = new Config();
    	Data1Source data1 = new Data1Source();
    	Data2Spout data2 = new Data2Spout();
    	builder.setSpout("source1", data1,1);
    	builder.setSpout("source2", data2,1);
    	builder.setBolt("HashJoin", new HashJoin(),1).shuffleGrouping("source1","data1").
    	shuffleGrouping("source2","data2");
//    	builder.setBolt("JedisDemo", new JedisDemo(),1).shuffleGrouping("source1","data1")
//		.shuffleGrouping("source2","data2");
		builder.setBolt("OutputDemo", new OutputDemo(),1).shuffleGrouping("HashJoin");
		config.setMaxTaskParallelism(1);
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("simple", config, builder.createTopology());
    }
}
