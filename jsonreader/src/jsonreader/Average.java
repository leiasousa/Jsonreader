/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonreader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Average {
    
    protected static ArrayList<String> header; 
    private int numberSeeds;
    protected static TreeSet<Double> loads;
    private Map<Double,Map<String,Double>> results;
    private ArrayList<Map<Double,Map<String,Double>>> resultsArray;

    void start() {
        header = new ArrayList<>();
        numberSeeds = 0;
        loads = new TreeSet<>();
        results = new HashMap<>();
        resultsArray = new ArrayList<>();
    }
    
    void addLoad(File file, String raClass, int seed, double load) throws IOException, ParseException {
        jsonParse(new FileReader(file));
        loads.add(load);
        Map<String,Double> mapload = new HashMap<>();
        JSONObject jsonObject;
        JSONParser parser = new JSONParser();
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(file));
            
            String[] metrics = new String[jsonObject.size()];
            int i = 0;
            Iterator<String> it = jsonObject.keySet().iterator();
            while (it.hasNext()) {
                metrics[i++] = it.next();
            }
            for (i = 0; i < metrics.length; i++) {
                if(!metrics[i].equals("Load")) {
                    if(jsonObject.get(metrics[i]) instanceof Double) {
                        mapload.put(metrics[i], (Double) jsonObject.get(metrics[i]));
                    } else {
                        if(jsonObject.get(metrics[i]) instanceof Long) {
                            mapload.put(metrics[i], Double.parseDouble(jsonObject.get(metrics[i]).toString()));
                        } else {
                            throw (new IllegalArgumentException("One of json's results is not a Double  "));
                        }
                    }
                    
                    
                }
            }
            results.put(load, mapload);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void computeRA(String raClass) throws IOException {
        File file = new File(JsonReader.dir +"/" + raClass + ".txt");
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("#load\t");
        for (int i = 1; i < header.size(); i++) {
            bw.write(header.get(i) + "\t" + header.get(i) + "_ic\t");
        }
        bw.newLine();
        
        for (Double load : loads) {
            for (String metric : header) {
                if(!metric.equals("Load")) {
                    double avg = 0.0;
                    for (int i = 0; i < numberSeeds; i++) {
                        avg += resultsArray.get(i).get(load).get(metric);
                    }
                    avg = avg/numberSeeds;
                    bw.write(avg + "\t");
                    double sum = 0.0;
                    for (int i = 0; i < numberSeeds; i++) {
                        double v = resultsArray.get(i).get(load).get(metric) - avg;
                        v = v*v;
                        sum += v;
                    }
                    double st = Math.sqrt(sum/numberSeeds);
                    double ic = 1.96 * (st/Math.sqrt(numberSeeds));
                    bw.write(ic + "\t");
                } else {
                    bw.write(load + "\t");
                }
            }
            bw.newLine();
        }
        bw.close();
        fw.close();
    }

    void addSeed() {
        numberSeeds++;
        Map<Double,Map<String,Double>> aux = new HashMap(results);
        resultsArray.add(aux);
    }

    private void jsonParse(FileReader fr) throws IOException {
        BufferedReader br = new BufferedReader(fr);
        br.readLine();
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] div = line.split("\"");
            if(div.length > 1 && !header.contains(div[1])) {
                header.add(div[1]);
            }
        }
    }
}
