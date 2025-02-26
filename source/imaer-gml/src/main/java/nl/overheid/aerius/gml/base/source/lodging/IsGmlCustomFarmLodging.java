/*
 * Copyright the State of the Netherlands
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package nl.overheid.aerius.gml.base.source.lodging;

import java.util.List;

import nl.overheid.aerius.gml.base.IsGmlProperty;
import nl.overheid.aerius.gml.base.source.IsGmlEmission;

public interface IsGmlCustomFarmLodging extends IsGmlFarmLodging {

  default String getAnimalCode() {
    return null;
  }

  String getDescription();

  List<? extends IsGmlProperty<IsGmlEmission>> getEmissionFactors();

}
