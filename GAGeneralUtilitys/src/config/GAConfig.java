/**
 * Author José Albert Cruz Almaguer <jalbertcruz@gmail.com>
 * Copyright 2013 by José Albert Cruz Almaguer.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http:www.gnu.org/licenses/agpl-3.0.txt) for more details.
 */

package config;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GAConfig {

    private String terminationCondition, seqOutputFilename, parallelOutputFilename;
    private int evaluatorsCount;
    private int reproducersCount;
    private int evaluatorsCapacity;
    private int reproducersCapacity;
    private int popSize;
    private int evaluations;
    private int chromosomeSize;
    private int repetitions;

    public GAConfig() {
        terminationCondition = "cantEvalsTerminationCondition";

//		seqOutputFilename = "../../results/evostar2014/sclEA/seqResults.csv";
//		parallelOutputFilename = "../../results/evostar2014/sclEA/parResults.csv";
    }

    public static GAConfig loadFromJSON(String jsonFile) throws IOException {
        GAConfig d;
        try (FileReader fr = new FileReader(new File(jsonFile))) {
            Gson g = new Gson();
            d = g.fromJson(fr, GAConfig.class);
        }
        return d;
    }

    public static void saveToJSON(GAConfig obj, String jsonFile) throws IOException {
        Gson g = new Gson();

        try (FileWriter fr = new FileWriter(new File(jsonFile))) {
            fr.write(g.toJson(obj));
        }
    }

    public static void saveToErlangModule(GAConfig obj, String dir) throws IOException {
        try (FileWriter fr = new FileWriter(new File(dir + "configuration.erl"))) {
            fr.write(obj.toErlangRecordInMudule_configuration());
        }
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public String getTerminationCondition() {
        return terminationCondition;
    }

    public void setTerminationCondition(String terminationCondition) {
        this.terminationCondition = terminationCondition;
    }

    public int getEvaluatorsCount() {
        return evaluatorsCount;
    }

    public void setEvaluatorsCount(int evaluatorsCount) {
        this.evaluatorsCount = evaluatorsCount;
    }

    public int getReproducersCount() {
        return reproducersCount;
    }

    public void setReproducersCount(int reproducersCount) {
        this.reproducersCount = reproducersCount;
    }

    public int getEvaluatorsCapacity() {
        return evaluatorsCapacity;
    }

    public void setEvaluatorsCapacity(int evaluatorsCapacity) {
        this.evaluatorsCapacity = evaluatorsCapacity;
    }

    public int getReproducersCapacity() {
        return reproducersCapacity;
    }

    public void setReproducersCapacity(int reproducersCapacity) {
        this.reproducersCapacity = reproducersCapacity;
    }

    public int getPopSize() {
        return popSize;
    }

    public void setPopSize(int popSize) {
        this.popSize = popSize;
    }

    public int getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(int evaluations) {
        this.evaluations = evaluations;
    }

    public int getChromosomeSize() {
        return chromosomeSize;
    }

    public void setChromosomeSize(int chromosomeSize) {
        this.chromosomeSize = chromosomeSize;
    }

    public String getSeqOutputFilename() {
        return seqOutputFilename;
    }

    public void setSeqOutputFilename(String seqOutputFilename) {
        this.seqOutputFilename = seqOutputFilename;
    }

    public String getParallelOutputFilename() {
        return parallelOutputFilename;
    }

    public void setParallelOutputFilename(String parallelOutputFilename) {
        this.parallelOutputFilename = parallelOutputFilename;
    }

    public String toErlangRecordInMudule_configuration() {
        return "-module(configuration).\n\n" +

                "-include(\"./include/mtypes.hrl\").\n\n" +
                "-compile(export_all).\n\n" +

                "gaConfig()-> #gAConfig{" + "\n" +
                "terminationCondition=" + terminationCondition + ",\n" +
                "seqOutputFilename=\"" + seqOutputFilename + '\"' + ",\n" +
                "parallelOutputFilename=\"" + parallelOutputFilename + '\"' + ",\n" +
                "evaluatorsCount=" + evaluatorsCount + ",\n" +
                "reproducersCount=" + reproducersCount + ",\n" +
                "evaluatorsCapacity=" + evaluatorsCapacity + ",\n" +
                "reproducersCapacity=" + reproducersCapacity + ",\n" +
                "popSize=" + popSize + ",\n" +
                "evaluations=" + evaluations + ",\n" +
                "chromosomeSize=" + chromosomeSize + ",\n" +
                "repetitions=" + repetitions + "\n" +
                "}.";
    }

    public String toSufixName() {
        return "_" + evaluatorsCount + "_" + evaluatorsCapacity + "_" + reproducersCount + "_" + reproducersCapacity;
    }

    @Override
    public String toString() {
        return "GAConfig{" +
                "terminationCondition='" + terminationCondition + '\'' +
                ", seqOutputFilename='" + seqOutputFilename + '\'' +
                ", parallelOutputFilename='" + parallelOutputFilename + '\'' +
                ", evaluatorsCount=" + evaluatorsCount +
                ", reproducersCount=" + reproducersCount +
                ", evaluatorsCapacity=" + evaluatorsCapacity +
                ", reproducersCapacity=" + reproducersCapacity +
                ", popSize=" + popSize +
                ", evaluations=" + evaluations +
                ", chromosomeSize=" + chromosomeSize +
                ", repetitions=" + repetitions +
                "}";
    }

}
