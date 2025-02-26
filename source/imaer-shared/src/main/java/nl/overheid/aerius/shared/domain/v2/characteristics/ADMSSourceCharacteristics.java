/*
 * Crown copyright
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
package nl.overheid.aerius.shared.domain.v2.characteristics;

import nl.overheid.aerius.shared.domain.v2.characteristics.adms.BuoyancyType;
import nl.overheid.aerius.shared.domain.v2.characteristics.adms.EffluxType;
import nl.overheid.aerius.shared.domain.v2.characteristics.adms.ReleaseTemperatureAndPressure;
import nl.overheid.aerius.shared.domain.v2.characteristics.adms.SourceType;

/**
 * ADMS Source Characteristics.
 */
public class ADMSSourceCharacteristics extends SourceCharacteristics {

  private static final long serialVersionUID = 2L;

  private SourceType sourceType;

  private BuoyancyType buoyancyType;

  private EffluxType effluxType;

  private ReleaseTemperatureAndPressure releaseTemperatureAndPressure;

  private double height;

  private double diameter;

  private double width;

  private double temperature;

  private double density;

  private double verticalVelocity;

  private double volumetricFlowRate;

  private double specificHeatCapacity;

  private double percentNOxAsNO2;

  private double verticalDimension;

  private double elevationAngle; // Angle 1

  private double horizontalAngle; // Angle 2

  /**
   * Momentum flux (Fm) of emission (m4/s2).
   */
  private double momentumFlux;

  /**
   * Heat release rate (Fb) of emission (MW).
   */
  private double buoyancyFlux;

  /**
   * Mass flux of the emission (kg/s).
   */
  private double massFlux;

  private String standardDiurnalVariationCode;

  public SourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(final SourceType sourceType) {
    this.sourceType = sourceType;
  }

  public BuoyancyType getBuoyancyType() {
    return buoyancyType;
  }

  public void setBuoyancyType(final BuoyancyType buoyancyType) {
    this.buoyancyType = buoyancyType;
  }

  public EffluxType getEffluxType() {
    return effluxType;
  }

  public void setEffluxType(final EffluxType effluxType) {
    this.effluxType = effluxType;
  }

  public ReleaseTemperatureAndPressure getReleaseTemperatureAndPressure() {
    return releaseTemperatureAndPressure;
  }

  public void setReleaseTemperatureAndPressure(final ReleaseTemperatureAndPressure releaseTemperatureAndPressure) {
    this.releaseTemperatureAndPressure = releaseTemperatureAndPressure;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(final double height) {
    this.height = height;
  }

  public double getDiameter() {
    return diameter;
  }

  public void setDiameter(final double diameter) {
    this.diameter = diameter;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(final double width) {
    this.width = width;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(final double temperature) {
    this.temperature = temperature;
  }

  public double getDensity() {
    return density;
  }

  public void setDensity(final double density) {
    this.density = density;
  }

  public double getVerticalVelocity() {
    return verticalVelocity;
  }

  public void setVerticalVelocity(final double verticalVelocity) {
    this.verticalVelocity = verticalVelocity;
  }

  public double getVolumetricFlowRate() {
    return volumetricFlowRate;
  }

  public void setVolumetricFlowRate(final double volumetricFlowRate) {
    this.volumetricFlowRate = volumetricFlowRate;
  }

  public double getSpecificHeatCapacity() {
    return specificHeatCapacity;
  }

  public void setSpecificHeatCapacity(final double specificHeatCapacity) {
    this.specificHeatCapacity = specificHeatCapacity;
  }

  public double getPercentNOxAsNO2() {
    return percentNOxAsNO2;
  }

  public void setPercentNOxAsNO2(final double percentNOxAsNO2) {
    this.percentNOxAsNO2 = percentNOxAsNO2;
  }

  public double getMomentumFlux() {
    return momentumFlux;
  }

  public void setMomentumFlux(final double momentumFlux) {
    this.momentumFlux = momentumFlux;
  }

  public double getBuoyancyFlux() {
    return buoyancyFlux;
  }

  public void setBuoyancyFlux(final double buoyancyFlux) {
    this.buoyancyFlux = buoyancyFlux;
  }

  @Deprecated
  public double getMassFlux() {
    return massFlux;
  }

  @Deprecated
  public void setMassFlux(final double massFlux) {
    this.massFlux = massFlux;
  }

  public double getVerticalDimension() {
    return verticalDimension;
  }

  public void setVerticalDimension(final double verticalDimension) {
    this.verticalDimension = verticalDimension;
  }

  public double getElevationAngle() {
    return elevationAngle;
  }

  public void setElevationAngle(final double elevationAngle) {
    this.elevationAngle = elevationAngle;
  }

  public double getHorizontalAngle() {
    return horizontalAngle;
  }

  public void setHorizontalAngle(final double horizontalAngle) {
    this.horizontalAngle = horizontalAngle;
  }

  public String getStandardDiurnalVariationCode() {
    return standardDiurnalVariationCode;
  }

  public void setStandardDiurnalVariationCode(final String standardDiurnalVariationCode) {
    this.standardDiurnalVariationCode = standardDiurnalVariationCode;
  }

}
