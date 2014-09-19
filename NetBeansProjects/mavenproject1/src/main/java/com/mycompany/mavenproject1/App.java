/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.mavenproject1;


import java.io.Console;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
 
/**
 *
 * @author borsody
 */
public class App {
    
    public static class WholeFileInputFormat extends FileInputFormat<LongWritable, Text> {

        @Override
        protected boolean isSplitable(FileSystem fs, Path filename) {
            return false;
        }

        // LongWritable k1, Text v1
        @Override
        public RecordReader<LongWritable, Text> getRecordReader(InputSplit split, JobConf job, Reporter reporter) throws IOException {
            WholeFileRecordReader wFRR;
            wFRR= new WholeFileRecordReader((FileSplit) split, job);
           return wFRR;   
       }
    }
    
    
    // LongWritable k1, Text v1 
    public static class WholeFileRecordReader implements RecordReader<LongWritable, Text> {

        private FileSplit fileSplit;
        private Configuration conf;
        private boolean processed = false;

        public  WholeFileRecordReader(FileSplit fileSplit, Configuration conf) throws IOException {
            this.fileSplit = fileSplit;
            this.conf = conf;
        }

        @Override
        public LongWritable createKey() {
            LongWritable lg=new LongWritable();
            return lg;
        }

        @Override
        public Text createValue() {
            return new Text();
        }

        @Override
        public long getPos() throws IOException {
            return processed ? fileSplit.getLength() : 0;
        }

        @Override
        public float getProgress() throws IOException {
            return processed ? 1.0f : 0.0f;
        }

        @Override
        public boolean next(LongWritable key, Text value) throws IOException {
            if (!processed) {
                  byte[] contents = new byte[(int) fileSplit.getLength()];
                  Path file = fileSplit.getPath();
                  FileSystem fs = file.getFileSystem(conf);
                  FSDataInputStream in = null;
                  try {
                    in = fs.open(file);
                    IOUtils.readFully(in, contents, 0, contents.length);
                    value.set(contents, 0, contents.length);
                  } finally {
                    IOUtils.closeStream(in);
                  }
                  processed = true;
                  return true;
            }
            return false;
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    
    
    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
 
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();
 
        @Override
        public void map(LongWritable k1, Text v1, OutputCollector<Text, IntWritable> oc, Reporter rep) throws IOException {
            String line = v1.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                oc.collect(word, one);
            }
        }
    }
 
    public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
 
        @Override
        public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
            int sum = 0;
            while (values.hasNext()) {
                sum += values.next().get();
            }
            output.collect(key, new IntWritable(sum));
        }
    }
 
    
    
    public static void main(String[] args) throws IOException {
        
        // give time to attach debugger
        try {
            Thread.sleep(8000);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JobConf conf = new JobConf(App.class);
        
        // purge existing output file
        FileSystem fs = FileSystem.get (conf);        
        fs.delete(new Path(args[1]), true); // delete file, true for recursive 
        
        
        
        
        conf.setJobName("wordcount");
 
        
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);
 
        conf.setMapperClass(Map.class);
        conf.setCombinerClass(Reduce.class);
        conf.setReducerClass(Reduce.class);
 
        conf.setInputFormat(WholeFileInputFormat.class);
         // conf.setInputFormat(TextInputFormat.class);
         conf.setOutputFormat(TextOutputFormat.class);
 
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 
        JobClient.runJob(conf);
    }
    
    
    
}
