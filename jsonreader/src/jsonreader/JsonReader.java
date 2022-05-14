/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;


public class JsonReader {

    public static String dir = "./";
    public static String ra = "";
    public static String xml = "";
    public static String prefix = "";

    public static void main(String[] args) {

        switch (args.length) {
            case 3:
                dir = args[0];
                ra = args[1];
                xml = args[2];
                break;
            case 2:
                dir = args[0];
                ra = args[1];
                break;
            case 1:
                ra = args[0];
                break;
            default:
                System.out.println("Usage: JsonReader [dir] <ra> [<xml>]");
                System.exit(0);
        }
        final File folder = new File(dir);

        List<String> result = new ArrayList<>();

        if(args.length == 3) {
            //search(ra + "-" + xml + "-.*", folder, result);
            search(".+-" + xml + "-.+-" + ra + "\\.json", folder, result);
        } else {
            search(ra + "_seed_.*", folder, result);
        }
        
        TreeSet<Integer> map = new TreeSet<>();
        HashMap<Integer, TreeSet<Double>> mapFiles = new HashMap();

        for (String s : result) {
            String seed, load;
            if (args.length == 3) {
                prefix = s.split("/")[1].split("-")[0];
                seed = s.split("-")[2];
                load = s.split("-")[3].split(".json")[0];
            } else {
                seed = s.split("seed_")[1].split("_")[0];
                load = s.split("seed_" + seed + "_")[1];
            }
            System.out.println(s);
            if(!mapFiles.containsKey(Integer.parseInt(seed))) {
                TreeSet<Double> loads = new TreeSet<>();
                loads.add(Double.parseDouble(load));
                mapFiles.put(Integer.parseInt(seed), loads);
            } else {
                TreeSet<Double> loads = mapFiles.get(Integer.parseInt(seed));
                loads.add(Double.parseDouble(load));
                mapFiles.put(Integer.parseInt(seed), loads);
            }
        }
        
        Average avg = new Average();
        avg.start();
        for (Map.Entry<Integer, TreeSet<Double>> entry : mapFiles.entrySet()) {
            Integer seed = entry.getKey();
            TreeSet<Double> loads = entry.getValue();
            
            for (Double load : loads) {
                File file;
                if (args.length == 3) {
                    int l = (int) Math.round(load);
                    file = new File(dir + "/" + prefix + "-" + xml + "-" + seed + "-" + l + "-" + ra +".json");
                } else {
                    file = new File(dir + "/" + ra + "_seed_" + seed + "_" + load);
                }
                try {
                    avg.addLoad(file, ra, seed, load);
                } catch (IOException ex) {
                    Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            avg.addSeed();
        }
        try {
            avg.computeRA(ra);
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void search(final String pattern, final File folder, List<String> result) {
        for (final File f : folder.listFiles()) {
            if (f.isDirectory()) {
                search(pattern, f, result);
            }

            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getPath());
                }
            }

        }
    }

}
