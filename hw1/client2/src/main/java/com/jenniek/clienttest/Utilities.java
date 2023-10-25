package com.jenniek.clienttest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Utilities {
    public static String getFormattedDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        return formattedDate;
    }
    public static String getFormattedDate(long t){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(t));
        return formattedDate;
    }

    public static void writeLog(String startTime, String requestType, long latency, int responseCode, String server_type, String s_numThreadGroups) {
        String record = String.format("%s, %s, %d, %d\n", startTime, requestType, latency, responseCode);
        try {
            // Writing to a file named "client2_logs.csv" in append mode
            FileWriter fw = new FileWriter(Config.CLIENT_LOG_PATH+"/"+server_type+"_"+s_numThreadGroups+".csv", true);
            fw.write(record);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void calculateStats(List<Long> latencies, String requestType) {
        Collections.sort(latencies);
        long sum = 0;
        for (long latency : latencies) {
            sum += latency;
        }
        double mean = (double) sum / latencies.size();
        double median = latencies.get(latencies.size() / 2);
        double p99 = latencies.get((int) Math.ceil(0.99 * latencies.size()));
        long max = Collections.max(latencies);
        long min = Collections.min(latencies);

        System.out.println("----- " + requestType + " request statistics -----");
        System.out.println("Mean: " + mean + " ms");
        System.out.println("Median: " + median + " ms");
        System.out.println("P99: " + p99 + " ms");
        System.out.println("Min: " + min + " ms");
        System.out.println("Max: " + max + " ms");
    }

    public static void writeThroughputToFile(List<Long> throughputs, String server, String n) {
        try (PrintWriter writer = new PrintWriter(new File(Config.CLIENT_LOG_PATH+"/" +server + "_" + n +"_throughputs.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Time(s),Throughput(req/s)\n");
            for (int i = 0; i < throughputs.size(); i++) {
                sb.append(i).append(',').append(throughputs.get(i)).append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

}
