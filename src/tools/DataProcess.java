package tools;

import dataowner.SpatialData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DataProcess {
    public static List<SpatialData> readSpatialData(String path) throws Exception{
        List<SpatialData> data = new LinkedList<>();
        File file = new File(path);
        if(file.isFile()&&file.exists()){
            InputStreamReader fla = new InputStreamReader(new FileInputStream(file));
            BufferedReader scr = new BufferedReader(fla);
            String str = null;
            while((str = scr.readLine()) != null){
                String[] s = str.split(" ");
                int id = Integer.parseInt(s[0]);
                int x = Integer.parseInt(s[1]);
                int y = Integer.parseInt(s[2]);
                List<Integer> keywords = new ArrayList<>();
                for(int i = 3;i < s.length;i++){
                    keywords.add(Integer.parseInt(s[i]));
                }
                data.add(new SpatialData(x, y, id, keywords));
            }
            scr.close();
            fla.close();
        }
        return data;
    }
}
