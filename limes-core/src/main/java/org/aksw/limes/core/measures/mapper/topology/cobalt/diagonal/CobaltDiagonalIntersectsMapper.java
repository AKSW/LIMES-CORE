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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.limes.core.measures.mapper.topology.cobalt.diagonal;

import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.measures.mapper.AMapper;
import org.aksw.limes.core.measures.mapper.pointsets.Polygon;
import org.aksw.limes.core.measures.mapper.topology.ITopologicRelationMapper;
import org.aksw.limes.core.measures.mapper.topology.cobalt.CobaltMeasures;

import java.util.Set;

/**
 * Mapper that checks for the topological relation intersects using cobalts diagonal function.
 */
public class CobaltDiagonalIntersectsMapper extends AMapper implements ITopologicRelationMapper {

    @Override
    public AMapping getMapping(Set<Polygon> sourceData, Set<Polygon> targetData) {
        return CobaltDiagonal.getMapping(sourceData, targetData, CobaltMeasures.INTERSECTS);
    }

    @Override
    public AMapping getMapping(ACache source, ACache target, String sourceVar, String targetVar, String expression, double threshold) {
        return CobaltDiagonal.getMapping(source, target, sourceVar, targetVar, expression, threshold, CobaltMeasures.INTERSECTS);
    }

    @Override
    public double getRuntimeApproximation(int sourceSize, int targetSize, double theta, Language language) {
        return 1d;
    }

    @Override
    public double getMappingSizeApproximation(int sourceSize, int targetSize, double theta, Language language) {
        return 1d;
    }

    @Override
    public String getName() {
        return "top_cobalt_diagonal_intersects";
    }
}
