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
package nl.overheid.aerius.importer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rwitzel.streamflyer.core.ModifyingReader;
import com.github.rwitzel.streamflyer.regex.RegexModifier;

import nl.overheid.aerius.gml.GMLMetaDataReader;
import nl.overheid.aerius.gml.GMLReader;
import nl.overheid.aerius.gml.GMLReaderFactory;
import nl.overheid.aerius.gml.GMLValidator;
import nl.overheid.aerius.gml.base.AeriusGMLVersion;
import nl.overheid.aerius.gml.base.GMLHelper;
import nl.overheid.aerius.shared.domain.Theme;
import nl.overheid.aerius.shared.domain.scenario.SituationType;
import nl.overheid.aerius.shared.domain.v2.building.BuildingFeature;
import nl.overheid.aerius.shared.domain.v2.geojson.Crs;
import nl.overheid.aerius.shared.domain.v2.geojson.Crs.CrsContent;
import nl.overheid.aerius.shared.domain.v2.geojson.FeatureCollection;
import nl.overheid.aerius.shared.domain.v2.importer.ImportParcel;
import nl.overheid.aerius.shared.domain.v2.nsl.NSLCorrection;
import nl.overheid.aerius.shared.domain.v2.nsl.NSLDispersionLineFeature;
import nl.overheid.aerius.shared.domain.v2.nsl.NSLMeasureFeature;
import nl.overheid.aerius.shared.domain.v2.point.CalculationPointFeature;
import nl.overheid.aerius.shared.domain.v2.point.CustomCalculationPoint;
import nl.overheid.aerius.shared.domain.v2.scenario.Definitions;
import nl.overheid.aerius.shared.domain.v2.scenario.ScenarioSituation;
import nl.overheid.aerius.shared.domain.v2.scenario.ScenarioSituationResults;
import nl.overheid.aerius.shared.domain.v2.source.EmissionSourceFeature;
import nl.overheid.aerius.shared.exception.AeriusException;
import nl.overheid.aerius.shared.exception.ImaerExceptionReason;
import nl.overheid.aerius.shared.geo.EPSG;
import nl.overheid.aerius.validation.BuildingValidator;
import nl.overheid.aerius.validation.EmissionSourceValidator;

/**
 * Importer for IMAER GML files.
 */
public class ImaerImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImaerImporter.class);

  private final GMLReaderFactory factory;
  private final EPSG epsg;

  public ImaerImporter(final GMLHelper gmlHelper) throws AeriusException {
    factory = GMLReaderFactory.getFactory(gmlHelper);
    epsg = gmlHelper.getReceptorGridSettings().getEPSG();
  }

  /**
   * Imports the given stream into the {@link ImportParcel} given the import options.
   * The year supplied in the file will be used to obtain emissions.
   *
   * @param inputStream Stream with data to import
   * @param importOptions options for importer
   * @param result result object to store import results in
   * @throws AeriusException error in case of fatal exception
   */
  public void importStream(final InputStream inputStream, final Set<ImportOption> importOptions, final ImportParcel result) throws AeriusException {
    importStream(inputStream, importOptions, result, Optional.empty(), Theme.WNB);
  }

  /**
   * Imports the given stream into the {@link ImportParcel} given the import options.
   * The supplied year will be used to obtain emissions.
   *
   * @param inputStream Stream with data to import
   * @param importOptions options for importer
   * @param result result object to store import results in
   * @throws AeriusException error in case of fatal exception
   */
  public void importStream(final InputStream inputStream, final Set<ImportOption> importOptions, final ImportParcel result,
      final int importYear) throws AeriusException {
    importStream(inputStream, importOptions, result, Optional.of(importYear), Theme.WNB);
  }

  /**
   * Imports the given stream into the {@link ImportParcel} given the import options.
   *
   * @param inputStream Stream with data to import
   * @param importOptions options for importer
   * @param result result object to store import results in
   * @throws AeriusException error in case of fatal exception
   */
  public void importStream(final InputStream inputStream, final Set<ImportOption> importOptions, final ImportParcel result,
      final Optional<Integer> importYear, final Theme theme) throws AeriusException {
    final GMLReader reader;
    try {
      reader = createGMLReader(inputStream, importOptions, result);
    } catch (final AeriusException e) {
      result.getExceptions().add(e);
      return;
    }
    final AeriusGMLVersion version = reader.getVersion();
    setImportResultMetaData(result, reader);
    GMLValidator.validateMetaData(result.getImportedMetaData(), result.getExceptions(), ImportOption.VALIDATE_METADATA.in(importOptions));
    GMLValidator.validateYear(result.getSituation().getYear(), result.getExceptions());
    GMLValidator.validateGMLVersion(version, result.getWarnings());

    final ScenarioSituation situation = addSituationProperties(reader, result);
    addEmissionSources(reader, importOptions, result, importYear);
    addAeriusPoints(reader, importOptions, result);
    addNslMeasures(reader, importOptions, situation);
    addNslDispersionLines(reader, importOptions, situation);
    addNslCorrections(reader, importOptions, situation);
    addBuildings(reader, importOptions, situation);
    addDefinitions(reader, importOptions, situation);
    setCrs(result);

    addCalculationSetOptions(theme, reader, result);
    LOGGER.info("GML imported with GML version: {}", version);
  }

  private static ScenarioSituation addSituationProperties(final GMLReader reader, final ImportParcel result) {
    final ScenarioSituation situation = result.getSituation();
    situation.setName(reader.getName());
    final SituationType type = reader.getSituationType();
    situation.setType(type);
    if (type == SituationType.NETTING) {
      final Double nettingFactor = reader.getNettingFactor();
      if (nettingFactor == null) {
        result.getWarnings().add(new AeriusException(ImaerExceptionReason.GML_MISSING_NETTING_FACTOR));
      } else {
        situation.setNettingFactor(nettingFactor);
      }
    }
    return situation;
  }

  private static void addCalculationSetOptions(final Theme theme, final GMLReader reader, final ImportParcel parcel) {
    parcel.setCalculationSetOptions(reader.readCalculationSetOptions(theme));
  }

  protected GMLReader createGMLReader(final InputStream inputStream, final Set<ImportOption> importOptions, final ImportParcel result)
      throws AeriusException {
    final InputStream filteredInputStream = filterResults(inputStream, importOptions);
    return factory.createReader(filteredInputStream, ImportOption.VALIDATE_AGAINST_SCHEMA.in(importOptions),
        result.getExceptions(), result.getWarnings());
  }

  private void addEmissionSources(final GMLReader reader, final Set<ImportOption> importOptions, final ImportParcel result,
      final Optional<Integer> importYear) throws AeriusException {
    if (ImportOption.INCLUDE_SOURCES.in(importOptions)) {
      final List<EmissionSourceFeature> sources = reader.readEmissionSourceList();
      final List<BuildingFeature> buildings = reader.getBuildings();
      if (ImportOption.VALIDATE_SOURCES.in(importOptions)) {
        EmissionSourceValidator.validateSources(sources, result.getExceptions(), result.getWarnings(),
            factory.createValidationHelper());
        BuildingValidator.validateBuildings(buildings, result.getExceptions(), result.getWarnings());
      }
      reader.enforceEmissions(sources, importYear.orElse(result.getSituation().getYear()));
      if (ImportOption.VALIDATE_SOURCES.in(importOptions)) {
        EmissionSourceValidator.validateSourcesWithEmissions(sources, result.getExceptions(), result.getWarnings());
      }
      result.getSituation().getEmissionSourcesList().addAll(sources);
    }
  }

  /**
   * Set the CRS on all FeatureCollection that are not empty.
   *
   * @param result import parcel to set CRS
   */
  private void setCrs(final ImportParcel result) {
    final Crs crs = new Crs();
    crs.setType("name");
    final CrsContent crsContent = new CrsContent();
    crsContent.setName(epsg.getEpsgCode());
    crs.setProperties(crsContent);

    final ScenarioSituation situation = result.getSituation();
    setCrsIf(situation.getSources(), crs);
    setCrsIf(situation.getNslDispersionLines(), crs);
    setCrsIf(situation.getNslMeasures(), crs);
    setCrsIf(result.getCalculationPoints(), crs);
  }

  private void setCrsIf(final FeatureCollection<?> featureCollection, final Crs crs) {
    if (!featureCollection.getFeatures().isEmpty()) {
      featureCollection.setCrs(crs);
    }
  }

  /**
   * Filter out result block as text based on a regular expression.
   *
   * @param inputStream stream to filter.
   * @param importOptions determine if we want to filer out.
   * @return
   * @throws AeriusException
   */
  private InputStream filterResults(final InputStream inputStream, final Set<ImportOption> importOptions) {
    if (importOptions.contains(ImportOption.INCLUDE_RESULTS)) {
      return inputStream;
    } else {
      final RegexModifier myModifier = new RegexModifier("<imaer:featureMember>[\n].+<imaer:Receptor.*>([\\s\\S]*?)<\\/imaer:featureMember>",
          Pattern.CASE_INSENSITIVE, "");
      final Reader reader = new ModifyingReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), myModifier);
      return new ReaderInputStream(reader, StandardCharsets.UTF_8);
    }
  }

  private static void addAeriusPoints(final GMLReader reader, final Set<ImportOption> importOptions, final ImportParcel result) {
    final boolean includeCalculationPoints = ImportOption.INCLUDE_CALCULATION_POINTS.in(importOptions);
    final boolean includeResults = ImportOption.INCLUDE_RESULTS.in(importOptions);
    final boolean warnOnCalculationPoints = ImportOption.WARNING_ON_CALCULATION_POINTS.in(importOptions);
    final boolean checkCalculationPoints = includeCalculationPoints || warnOnCalculationPoints;

    if (checkCalculationPoints || includeResults) {
      final List<CalculationPointFeature> points = reader.getAeriusPoints(includeResults);

      if (checkCalculationPoints) {
        addCalculationPoints(result, includeCalculationPoints, warnOnCalculationPoints, points);
      }
      if (includeResults) {
        final ScenarioSituationResults situationResults = new ScenarioSituationResults();
        situationResults.getResults().addAll(filterResultPoints(points, includeCalculationPoints));
        result.setSituationResults(situationResults);
      }
    }
  }

  /**
   * Add Calculation points if:
   * 1) If warn is true and Calculation points are present a warning is added, but no points are added even when includeCalculationPoints is true
   * 2) Add Calculation points only if includeCalculationPoints is true and no warning is added
   *
   * @param result add calculation points to the result
   * @param includeCalculationPoints if true calculation points are added unless
   * @param warnOnCalculationPoints if true and calculation points are present a warning is added.
   * @param points All points to get the Calculation points from
   * @throws AeriusException
   */
  private static void addCalculationPoints(final ImportParcel result, final boolean includeCalculationPoints, final boolean warnOnCalculationPoints,
      final List<CalculationPointFeature> points) {
    final List<CalculationPointFeature> calculationPoints = filterCalculationPoints(points);

    if (warnOnCalculationPoints && !calculationPoints.isEmpty()) {
      result.getWarnings().add(new AeriusException(ImaerExceptionReason.IMPORT_CALCULATION_POINTS_PRESENT));
    } else if (includeCalculationPoints) {
      result.getCalculationPointsList().addAll(calculationPoints);
    }
  }

  private static List<CalculationPointFeature> filterCalculationPoints(final List<CalculationPointFeature> points) {
    return points.stream()
        .filter(p -> p.getProperties() instanceof CustomCalculationPoint)
        .collect(Collectors.toList());
  }

  /**
   * Returns list of result points. This can be both point and receptor results.
   *
   * @param points list of points to filter
   * @param includeCalculationPoints if AeriusPointType.POINTs should be included
   * @return
   */
  private static ArrayList<CalculationPointFeature> filterResultPoints(final List<CalculationPointFeature> points,
      final boolean includeCalculationPoints) {
    final ArrayList<CalculationPointFeature> list = new ArrayList<>();

    for (final CalculationPointFeature point : points) {
      if (includeCalculationPoints || !(point.getProperties() instanceof CustomCalculationPoint)) {
        list.add(point);
      }
    }
    return list;
  }

  private void setImportResultMetaData(final ImportParcel result, final GMLReader reader) {
    final GMLMetaDataReader metaDataReader = reader.metaDataReader();
    result.setImportedMetaData(metaDataReader.readMetaData());
    result.getSituation().setYear(metaDataReader.readYear());
    result.setVersion(metaDataReader.readAeriusVersion());
    result.setDatabaseVersion(metaDataReader.readDatabaseVersion());
  }

  private static void addNslMeasures(final GMLReader reader, final Set<ImportOption> importOptions, final ScenarioSituation situation) {
    if (ImportOption.INCLUDE_NSL_MEASURES.in(importOptions)) {
      final List<NSLMeasureFeature> measures = reader.getNSLMeasures();
      situation.getNslMeasuresList().addAll(measures);
    }
  }

  private static void addNslDispersionLines(final GMLReader reader, final Set<ImportOption> importOptions, final ScenarioSituation situation) {
    if (ImportOption.INCLUDE_NSL_DISPERSION_LINES.in(importOptions)) {
      final List<NSLDispersionLineFeature> dispersionLines = reader.getNSLDispersionLines();
      situation.getNslDispersionLinesList().addAll(dispersionLines);
    }
  }

  private static void addNslCorrections(final GMLReader reader, final Set<ImportOption> importOptions, final ScenarioSituation situation) {
    if (ImportOption.INCLUDE_NSL_CORRECTIONS.in(importOptions)) {
      final List<NSLCorrection> corrections = reader.getNSLCorrections();
      situation.getNslCorrections().addAll(corrections);
    }
  }

  private static void addBuildings(final GMLReader reader, final Set<ImportOption> importOptions, final ScenarioSituation situation) {
    if (ImportOption.INCLUDE_SOURCES.in(importOptions)) {
      final List<BuildingFeature> buildings = reader.getBuildings();
      situation.getBuildingsList().addAll(buildings);
    }
  }

  private static void addDefinitions(final GMLReader reader, final Set<ImportOption> importOptions, final ScenarioSituation situation) {
    if (ImportOption.INCLUDE_SOURCES.in(importOptions)) {
      final Definitions definitions = reader.getDefinitions();
      situation.setDefinitions(definitions);
    }
  }

}
