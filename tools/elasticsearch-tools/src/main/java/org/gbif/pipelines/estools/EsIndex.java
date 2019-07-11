package org.gbif.pipelines.estools;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.gbif.pipelines.estools.client.EsClient;
import org.gbif.pipelines.estools.client.EsConfig;
import org.gbif.pipelines.estools.common.SettingsType;
import org.gbif.pipelines.estools.service.EsService;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.gbif.pipelines.estools.service.EsService.getIndexesByAliasAndIndexPattern;
import static org.gbif.pipelines.estools.service.EsService.swapIndexes;
import static org.gbif.pipelines.estools.service.EsService.updateIndexSettings;

/** Exposes a public API to perform operations in a ES instance. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EsIndex {

  public static final String INDEX_SEPARATOR = "_";

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * @param config configuration of the ES instance.
   * @param idxName index name.
   * @return name of the index created.
   */
  public static String create(EsConfig config, String idxName) {
    log.info("Creating index {}", idxName);
    try (EsClient esClient = EsClient.from(config)) {
      return EsService.createIndex(esClient, idxName, SettingsType.INDEXING);
    }
  }

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * <p>Both datasetId and attempt parameters are required. The index created will follow the
   * pattern "{datasetId}_{attempt}".
   *
   * @param config configuration of the ES instance.
   * @param datasetId dataset id.
   * @param attempt attempt of the dataset crawling.
   * @return name of the index created.
   */
  public static String create(EsConfig config, String datasetId, int attempt) {
    final String idxName = createIndexName(datasetId, attempt);
    return create(config, idxName);
  }

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * <p>Both datasetId and attempt parameters are required.
   *
   * @param config configuration of the ES instance.
   * @param idxName index name
   * @param mappings path of the file with the mappings.
   * @return name of the index created.
   */
  public static String create(EsConfig config, String idxName, Path mappings) {
    log.info("Creating index {}", idxName);
    try (EsClient esClient = EsClient.from(config)) {
      return EsService.createIndex(esClient, idxName, SettingsType.INDEXING, mappings);
    }
  }

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * <p>Both datasetId and attempt parameters are required. The index created will follow the
   * pattern "{datasetId}_{attempt}".
   *
   * @param config configuration of the ES instance.
   * @param datasetId dataset id.
   * @param attempt attempt of the dataset crawling.
   * @param mappings path of the file with the mappings.
   * @return name of the index created.
   */
  public static String create(EsConfig config, String datasetId, int attempt, Path mappings) {
    final String idxName = createIndexName(datasetId, attempt);
    return create(config, idxName, mappings);
  }

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * <p>Both datasetId and attempt parameters are required.
   *
   * @param config configuration of the ES instance.
   * @param idxName index name
   * @param mappings mappings as json string.
   * @return name of the index created.
   */
  public static String create(EsConfig config, String idxName, String mappings) {
    log.info("Creating index {}", idxName);
    try (EsClient esClient = EsClient.from(config)) {
      return EsService.createIndex(esClient, idxName, SettingsType.INDEXING, mappings);
    }
  }

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * <p>Both datasetId and attempt parameters are required. The index created will follow the
   * pattern "{datasetId}_{attempt}".
   *
   * @param config configuration of the ES instance.
   * @param datasetId dataset id.
   * @param attempt attempt of the dataset crawling.
   * @param mappings mappings as json string.
   * @return name of the index created.
   */
  public static String create(EsConfig config, String datasetId, int attempt, String mappings) {
    final String idxName = createIndexName(datasetId, attempt);
    return create(config, idxName, mappings);
  }

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * <p>Both datasetId and attempt parameters are required.
   *
   * @param config configuration of the ES instance.
   * @param idxName index name
   * @param mappings mappings as json string.
   * @param settingMap custom settings, number of shards and etc.
   * @return name of the index created.
   */
  public static String create(EsConfig config, String idxName, Path mappings, Map<String, String> settingMap) {
    log.info("Creating index {}", idxName);
    try (EsClient esClient = EsClient.from(config)) {
      return EsService.createIndex(esClient, idxName, SettingsType.INDEXING, mappings, settingMap);
    }
  }

  /**
   * Creates an Index in the ES instance specified in the {@link EsConfig} received.
   *
   * <p>Both datasetId and attempt parameters are required. The index created will follow the
   * pattern "{datasetId}_{attempt}".
   *
   * @param config configuration of the ES instance.
   * @param datasetId dataset id.
   * @param attempt attempt of the dataset crawling.
   * @param mappings mappings as json string.
   * @param settingMap custom settings, number of shards and etc.
   * @return name of the index created.
   */
  public static String create(
      EsConfig config,
      String datasetId,
      int attempt,
      Path mappings,
      Map<String, String> settingMap) {
    final String idxName = createIndexName(datasetId, attempt);
    return create(config, idxName, mappings, settingMap);
  }

  /**
   * Swaps an index in a alias.
   *
   * <p>The index received will be the only index associated to the alias after performing this
   * call. All the indexes that were associated to this alias before will be removed from the ES
   * instance.
   *
   * @param config configuration of the ES instance.
   * @param alias alias that will be modified.
   * @param index index to add to the alias that will become the only index of the alias.
   */
  public static void swapIndexInAlias(EsConfig config, String alias, String index) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(alias), "alias is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(index), "index is required");
    swapIndexInAliases(config, Collections.singleton(alias), index, Collections.emptySet());
  }

  /**
   * Swaps an index in a aliases.
   *
   * <p>The index received will be the only index associated to the alias for the dataset after performing this
   * call. All the indexes that were associated to this alias before will be removed from the ES
   * instance.
   *
   * @param config configuration of the ES instance.
   * @param aliases aliases that will be modified.
   * @param index index to add to the aliases that will become the only index of the aliases for the dataset.
   */
  public static void swapIndexInAliases(EsConfig config, Set<String> aliases, String index) {
    Preconditions.checkArgument(aliases != null && !aliases.isEmpty(), "alias is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(index), "index is required");
    swapIndexInAliases(config, aliases, index, Collections.emptySet());
  }

  /**
   * Swaps indexes in alias. It adds the given index to the alias and removes all the indexes that match the dataset
   * pattern plus some extra indexes that can be passed as parameter.
   *
   * @param config configuration of the ES instance.
   * @param aliases aliases that will be modified.
   * @param index index to add to the aliases that will become the only index of the alias for the dataset.
   * @param extraIdxToRemove extra indexes to be removed from the aliases
   */
  public static void swapIndexInAliases(EsConfig config, Set<String> aliases, String index,
      Set<String> extraIdxToRemove) {
    Preconditions.checkArgument(aliases != null && !aliases.isEmpty(), "alias is required");

    try (EsClient esClient = EsClient.from(config)) {

      Set<String> idxToAdd = new HashSet<>();
      Set<String> idxToRemove = new HashSet<>();

      // the index to add is optional
      Optional.ofNullable(index).ifPresent(idx -> {
        idxToAdd.add(idx);

        // look for old indexes for this datasetId to remove them from the alias
        String datasetId = getDatasetIdFromIndex(idx);
        Optional.ofNullable(
            getIndexesByAliasAndIndexPattern(esClient, getDatasetIndexesPattern(datasetId), aliases))
            .ifPresent(idxToRemove::addAll);
      });

      // add extra indexes to remove
      Optional.ofNullable(extraIdxToRemove).ifPresent(idxToRemove::addAll);

      log.info("Removing indexes {} and adding index {} in alias {}", idxToRemove, index, aliases);

      // swap the indexes
      swapIndexes(esClient, aliases, idxToAdd, idxToRemove);

      // change index settings to search settings
      updateIndexSettings(esClient, index, SettingsType.SEARCH);
    }
  }

  /**
   * Counts the number of documents of an index.
   *
   * @param config configuration of the ES instance.
   * @param index index to count the elements from.
   * @return number of documents of the index.
   */
  public static long countDocuments(EsConfig config, String index) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(index), "index is required");
    log.info("Counting documents from index {}", index);
    try (EsClient esClient = EsClient.from(config)) {
      return EsService.countIndexDocuments(esClient, index);
    }
  }

  /**
   * Refreshes the index received.
   *
   * @param config configuration of the ES instance.
   * @param index name of the index to refresh.
   */
  public static void refresh(EsConfig config, String index) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(index), "index is required");
    log.info("Refreshing index {}", index);
    try (EsClient esClient = EsClient.from(config)) {
      EsService.refreshIndex(esClient, index);
    }
  }

  /**
   * Checks if an index exists in the ES instance.
   *
   * @param config configuration of the ES instance.
   * @param index name of the index to refresh.
   */
  public static boolean indexExists(EsConfig config, String index) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(index), "index is required");
    try (EsClient esClient = EsClient.from(config)) {
      return EsService.existsIndex(esClient, index);
    }
  }

  /**
   * Deletes records in the index by some ES DSL query
   *
   * @param config configuration of the ES instance.
   * @param index name of the index to refresh.
   * @param query ES DSL query.
   **/
  public static void deleteRecordsByQuery(EsConfig config, String index, String query) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(index), "index is required");
    try (EsClient esClient = EsClient.from(config)) {
      EsService.deleteRecordsByQuery(esClient, index, query);
    }
  }

  /**
   * Finds the indexes in an alias where a given dataset is present. This method checks that the aliases exist before
   * querying ES.
   *
   * @param config configuration of the ES instance.
   * @param aliases name of the alias to search in.
   */
  public static Set<String> findDatasetIndexesInAliases(EsConfig config, String[] aliases, String datasetKey) {
    Preconditions.checkArgument(aliases != null && aliases.length > 0, "aliases are required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasetKey), "datasetKey is required");

    try (EsClient esClient = EsClient.from(config)) {
      // we check if the aliases exist, otherwise ES throws an error.
      String existingAlias = Arrays.stream(aliases)
          .filter(alias -> EsService.existsIndex(esClient, alias))
          .collect(Collectors.joining(","));

      return EsService.findDatasetIndexesInAlias(esClient, existingAlias, datasetKey);
    }
  }

  private static String getDatasetIndexesPattern(String datasetId) {
    return datasetId + INDEX_SEPARATOR + "*";
  }

  private static String getDatasetIdFromIndex(String index) {
    List<String> pieces = Arrays.asList(index.split(INDEX_SEPARATOR));

    if (pieces.size() < 2) {
      log.error("Index {} doesn't follow the pattern \"{datasetId}_{attempt}\"", index);
      throw new IllegalArgumentException("index has to follow the pattern \"{datasetId}_{attempt}\"");
    }

    return pieces.get(0);
  }

  private static String createIndexName(String datasetId, int attempt) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasetId), "dataset id is required");
    return datasetId + INDEX_SEPARATOR + attempt;
  }
}
