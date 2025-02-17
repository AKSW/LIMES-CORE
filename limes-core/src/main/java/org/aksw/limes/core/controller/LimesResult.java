/*
 * LIMES Core Library - LIMES – Link Discovery Framework for Metric Spaces.
 * Copyright © 2011 Data Science Group (DICE) (ngonga@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.limes.core.controller;

import org.aksw.limes.core.datastrutures.GoldStandard;
import org.aksw.limes.core.evaluation.qualititativeMeasures.FMeasure;
import org.aksw.limes.core.evaluation.qualititativeMeasures.PseudoFMeasure;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.aksw.limes.core.measures.mapper.MappingOperations;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author sherif
 */
public class LimesResult {
    protected AMapping verificationMapping;
    protected AMapping acceptanceMapping;
    private ACache sourceCache = null;
    private ACache targetCache = null;
    private long runTime = 0;
    private Map<String, String> lsVerbalization = null; //Language->Verbalization Map

    /**
     * Constructor
     */
    LimesResult() {
        this.verificationMapping = MappingFactory.createDefaultMapping();
        this.acceptanceMapping = MappingFactory.createDefaultMapping();
    }

    /**
     * Constructor
     * @param verificationMapping Mapping where acceptanceThreshold &gt; sim &gt;= verificationThreshold
     * @param acceptanceMapping Mapping where sim &gt;= acceptanceThreshold
     */
    public LimesResult(AMapping verificationMapping, AMapping acceptanceMapping) {
        super();
        this.verificationMapping = verificationMapping;
        this.acceptanceMapping = acceptanceMapping;
    }


    /**
     * Constructor
     * @param verificationMapping Mapping where acceptanceThreshold &gt; sim &gt;= verificationThreshold
     * @param acceptanceMapping Mapping where sim &gt;= acceptanceThreshold
     * @param sourceCache source resources cache
     * @param targetCache target resources cache
     * @param runTime run time
     * @param lsVerbalization A natural language explanation of the metric grouped by language
     */
    public LimesResult(AMapping verificationMapping, AMapping acceptanceMapping, ACache sourceCache, ACache targetCache, long runTime, Map<String, String> lsVerbalization) {
        this(verificationMapping, acceptanceMapping);
        this.sourceCache = sourceCache;
        this.targetCache = targetCache;
        this.runTime = runTime;
        this.lsVerbalization = lsVerbalization;
    }


    /**
     * Getter for verification part
     * @return verification mapping
     */
    public AMapping getVerificationMapping() {
        return verificationMapping;
    }


    /**
     * Getter for acceptance part
     * @return acceptance mapping
     */
    public AMapping getAcceptanceMapping() {
        return acceptanceMapping;
    }

    public String getStatistics() {
        if (sourceCache == null) {
            return "";
        }
        GoldStandard goldStandard = new GoldStandard(null, sourceCache, targetCache);
        double pseudoPrecisionForAcceptance = new PseudoFMeasure().precision(acceptanceMapping, goldStandard);
        double pseudoRecallForAcceptance = new PseudoFMeasure().recall(acceptanceMapping, goldStandard);
        double pseudoFMeasureForAcceptance = new PseudoFMeasure().calculate(acceptanceMapping, goldStandard);
        AMapping wholeMapping = MappingOperations.union(acceptanceMapping, verificationMapping);
        double pseudoPrecisionForAll = new PseudoFMeasure().precision(wholeMapping, goldStandard);
        double pseudoRecallForAll = new PseudoFMeasure().recall(wholeMapping, goldStandard);
        double pseudoFMeasureForAll = new PseudoFMeasure().calculate(wholeMapping, goldStandard);

        StringBuilder lsVerbalizationStringBuilder = new StringBuilder(",\n\t\"lsVerbalization\" : {");
        if (lsVerbalization != null && !lsVerbalization.isEmpty()) {
            for (Map.Entry<String, String> entry : lsVerbalization.entrySet()) {
                lsVerbalizationStringBuilder.append("\n\t\t\"").append(entry.getKey()).append("\" : \"").append(entry.getValue()).append("\"").append(",");
            }
            lsVerbalizationStringBuilder.deleteCharAt(lsVerbalizationStringBuilder.length() - 1);
        }
        lsVerbalizationStringBuilder.append("\n\t}");
        String lsVerbalizationString = lsVerbalizationStringBuilder.toString();

        String formatted = String.format(
                "{" +
                        "\n\t\"mappingTime\" : %d," +
                        "\n\t\"inputSizes\" : {" +
                        "\n\t\t\"source\" : %d," +
                        "\n\t\t\"target\" : %d" +
                        "\n\t}," +
                        "\n\t\"outputSizes\" : {" +
                        "\n\t\t\"verification\" : %d," +
                        "\n\t\t\"acceptance\" : %d" +
                        "\n\t}," +
                        "\n\t\"pseudoPRF\" : {" +
                        "\n\t\t\"acceptance\" : {" +
                        "\n\t\t\t\"precision\" : %s," +
                        "\n\t\t\t\"recall\" : %s," +
                        "\n\t\t\t\"f-measure\" : %s" +
                        "\n\t\t}," +
                        "\n\t\t\"all\" : {" +
                        "\n\t\t\t\"precision\" : %s," +
                        "\n\t\t\t\"recall\" : %s," +
                        "\n\t\t\t\"f-measure\" : %s" +
                        "\n\t\t}" +
                        "\n\t}",
                        this.runTime, this.sourceCache.size(), this.targetCache.size(),
                this.verificationMapping.size(), this.acceptanceMapping.size(),
                Double.toString(pseudoPrecisionForAcceptance), Double.toString(pseudoRecallForAcceptance), Double.toString(pseudoFMeasureForAcceptance),
                Double.toString(pseudoPrecisionForAll), Double.toString(pseudoRecallForAll), Double.toString(pseudoFMeasureForAll));
        return formatted + lsVerbalizationString +
                "\n}";
    }


    private AMapping filterReferenceCompliant(AMapping input, Set<String> referenceS, Set<String> referenceT) {
        AMapping filtered = MappingFactory.createDefaultMapping();
        for (String s : input.getMap().keySet()) {
            if (referenceS.contains(s)) {
                for (String t : input.getMap().get(s).keySet()) {
                    if (referenceT.contains(t)) {
                        filtered.add(s, t, input.getConfidence(s, t));
                    }
                }
            }
        }
        return filtered;
    }

    public String getStatistics(AMapping reference) {
        String stats = getStatistics();
        stats = stats.substring(0, stats.length()-2);
        Set<String> gsS = reference.getMap().keySet();
        Set<String> gsT = new HashSet<>();
        for (String s : reference.getMap().keySet()) {
            gsT.addAll(reference.getMap().get(s).keySet());
        }
        GoldStandard goldStandard = new GoldStandard(reference, sourceCache, targetCache);
        AMapping filteredAcceptance = filterReferenceCompliant(acceptanceMapping, gsS, gsT);
        double precisionForAcceptance = new FMeasure().precision(filteredAcceptance, goldStandard);
        double recallForAcceptance = new FMeasure().recall(filteredAcceptance, goldStandard);
        double fMeasureForAcceptance = new FMeasure().calculate(filteredAcceptance, goldStandard);
        AMapping filteredWhole = MappingOperations.union(filteredAcceptance, filterReferenceCompliant(verificationMapping, gsS, gsT));
        double precisionForAll = new FMeasure().precision(filteredWhole, goldStandard);
        double recallForAll = new FMeasure().recall(filteredWhole, goldStandard);
        double fMeasureForAll = new FMeasure().calculate(filteredWhole, goldStandard);
        return stats + String.format("," +
                        "\n\t\"PRF\" : {" +
                        "\n\t\t\"acceptance\" : {" +
                        "\n\t\t\t\"precision\" : %s," +
                        "\n\t\t\t\"recall\" : %s," +
                        "\n\t\t\t\"f-measure\" : %s" +
                        "\n\t\t}," +
                        "\n\t\t\"all\" : {" +
                        "\n\t\t\t\"precision\" : %s," +
                        "\n\t\t\t\"recall\" : %s," +
                        "\n\t\t\t\"f-measure\" : %s" +
                        "\n\t\t}" +
                        "\n\t}" +
                        "\n}",
                Double.toString(precisionForAcceptance), Double.toString(recallForAcceptance), Double.toString(fMeasureForAcceptance),
                Double.toString(precisionForAll), Double.toString(recallForAll), Double.toString(fMeasureForAll));
    }

    public void forceOneToOneMapping() {
        AMapping map = MappingFactory.createDefaultMapping();
        verificationMapping = map.getBestOneToOneMappings(verificationMapping);
        acceptanceMapping = map.getBestOneToOneMappings(acceptanceMapping);
    }

}
