import java.io.IOException;
import java.util.Iterator;

import java.util.StringTokenizer;  //added 

import java.util.Hashtable;

import org.apache.hadoop.fs.Path;
// import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.mapred.FileSplit;

import org.apache.hadoop.io.Text; // added 

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class Trivial {

  public static void main(String[] args) throws IOException {
    JobConf conf = new JobConf(Trivial.class);
    conf.setJobName("Inverted_Indexing");

    if (args.length < 2) {
      System.out.println("ERROR: Wrong number of parameters");
      System.out.println("trivial <input_path> <output_path>");
      System.exit(1);
    }

    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(Text.class);

    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

    
    // setMapper
    conf.setMapperClass(Trivial.IdentityMapper.class);

    // setCombinerClass()
    conf.setCombinerClass(Trivial.IdentityCombiner.class);

    // setReducer
    conf.setReducerClass(Trivial.IdentityReducer.class);
    // -- 

    JobClient.runJob(conf);
  }

  public static class IdentityMapper extends MapReduceBase
    implements Mapper<LongWritable, Text, Text , Text > {
    // implements Mapper<WritableComparable, Writable, WritableComparable, Writable> {

    public final static Text word = new Text();
    // the indexed word
    public final static Text loc = new Text();
    // the corresponding location (loc) for word, the idx 
    // word [loc:_]

    public void map(LongWritable key, Text val,
                    OutputCollector<Text, Text> output,
		    Reporter reporter)
      throws IOException {
//    public void map(WritableComparable key, Writable val,
//                   OutputCollector<WritableComparable, Writable> output,
//		    Reporter reporter)

      // begin to add inverted index 
      FileSplit fs = (FileSplit) reporter.getInputSplit(); 
      String fn = fs.getPath().getName();
      loc.set(fn);
      
      String line = val.toString();

//      String mypattern = "";

      line = line.replaceAll("\\-{2,}"," ");

      line = line.replaceAll("_{2,}"," ");
      
      // StringTokenizer itr = new StringTokenizer(line);
      StringTokenizer itr = 
	// new StringTokenizer(line.toLowerCase()," \t\n\r\"',.$-+/~=&*%#`@{}()[]<>|;:?!\\");
	new StringTokenizer(line.toLowerCase()," ,;?!~{}()[]<>|:.\"\t\n\r/");


      Hashtable < String, Integer> file_logs
	    = new Hashtable < String, Integer>();


      while (itr.hasMoreTokens()){
	word.set(itr.nextToken());  
//	 
	String st = word.toString();
	Integer n = file_logs.get(st);
	if(n == null)
            output.collect(word, loc);
	else
	    file_logs.put(st,1);

      }

      // end of add inverted index 
      // output.collect(key, val);
    }
  }

  public static class IdentityCombiner extends MapReduceBase 
  // implements Reducer<WritableComparable, Writable, WritableComparable, Writable> {
    implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<Text, Text> output,
		       Reporter reporter)
     throws IOException {
//    public void reduce(WritableComparable key, Iterator<Writable> values,
//                      OutputCollector<WritableComparable, Writable> output,
//		       Reporter reporter)


	 boolean isfirst = true;
	 // to judge whether the set of word need to initialize or not.
	 
	 StringBuilder toReturn = new StringBuilder();
	 
	 Hashtable < String, Integer> file_logs
	    = new Hashtable < String, Integer>();


	 while(values.hasNext()){
	     String st = values.next().toString();
	     Integer n = file_logs.get(st);

	     if(n == null){ // if the new file name 
		file_logs.put(st,1);
		if(!isfirst== false)
		    toReturn.append(" ");
		isfirst = false;
		toReturn.append(st);
	     }
	     // else{}
	     // has record
	     // skip

	 }
	 /*
	 while(values.hasNext()){
	     if(!isfirst) toReturn.append(", ");
	     // else
	     isfirst = false;
	     toReturn.append(values.next().toString());
	 }
	 */
	 output.collect(key , new Text(toReturn.toString()));
	 /* orginal Trivia.txt
	 while (values.hasNext()) {
	     output.collect(key, (Writable)values.next());
	 }
	 */
     }
  }


  public static class IdentityReducer extends MapReduceBase 
  // implements Reducer<WritableComparable, Writable, WritableComparable, Writable> {
    implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<Text, Text> output,
		       Reporter reporter)
     throws IOException {
//    public void reduce(WritableComparable key, Iterator<Writable> values,
//                      OutputCollector<WritableComparable, Writable> output,
//		       Reporter reporter)


	 boolean isfirst = true;
	 // to judge whether the set of word need to initialize or not.
	 
	 StringBuilder toReturn = new StringBuilder();
	 
	 Hashtable < String, Integer> file_logs
	    = new Hashtable < String, Integer>();

	 while(values.hasNext()){
	     String st = values.next().toString();
	     Integer n = file_logs.get(st);

	     if(n == null){ // if the new file name 
		file_logs.put(st,1);
		if(!isfirst==false)
		    toReturn.append(" ");
		isfirst = false;
		toReturn.append(st);
	     }
	     // else{}
	     // has record
	     // skip
	    }

	 /*
	 while(values.hasNext()){
	     if(!isfirst) toReturn.append(", ");
	     // else
	     isfirst = false;
	     toReturn.append(values.next().toString());
	 }
	 */
	 output.collect(key , new Text(toReturn.toString()));
	 /* orginal Trivia.txt
	 while (values.hasNext()) {
	     output.collect(key, (Writable)values.next());
	 }
	 */
     }
  }
}
