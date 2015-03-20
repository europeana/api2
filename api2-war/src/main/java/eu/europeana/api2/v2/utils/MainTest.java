package eu.europeana.api2.v2.utils;

import org.json.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainTest {

    public static void main(String[] args) throws IOException {

        File file = new File("/home/norbert/Projects/BusyMachines/Git/api2/api2-war/src/main/java/eu/europeana/api2/v2/utils/testFile.txt");
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        JSONObject obj = new JSONObject(new String(data, "UTF-8"));


    }
}
