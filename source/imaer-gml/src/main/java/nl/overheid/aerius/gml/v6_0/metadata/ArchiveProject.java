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
package nl.overheid.aerius.gml.v6_0.metadata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import nl.overheid.aerius.gml.base.IsArchiveProject;
import nl.overheid.aerius.gml.v6_0.base.CalculatorSchema;

/**
 *
 */
@XmlType(name = "ArchiveProjectType", namespace = CalculatorSchema.NAMESPACE, propOrder = {"id", "name", "aeriusVersion"})
public class ArchiveProject implements IsArchiveProject {

  private String id;
  private String name;
  private String aeriusVersion;

  @Override
  @XmlElement(namespace = CalculatorSchema.NAMESPACE)
  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  @Override
  @XmlElement(namespace = CalculatorSchema.NAMESPACE)
  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  @XmlElement(namespace = CalculatorSchema.NAMESPACE)
  public String getAeriusVersion() {
    return aeriusVersion;
  }

  public void setAeriusVersion(final String aeriusVersion) {
    this.aeriusVersion = aeriusVersion;
  }

}
