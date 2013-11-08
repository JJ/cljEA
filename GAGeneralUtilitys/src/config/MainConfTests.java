package config;

import java.io.IOException;

public class MainConfTests {
    public static void main(String[] args) throws IOException {

        GAConfig obj = GAConfig.loadFromJSON("maxSATConfig.json");

        System.out.println(obj.toErlangRecordInMudule_configuration());

    }
}
