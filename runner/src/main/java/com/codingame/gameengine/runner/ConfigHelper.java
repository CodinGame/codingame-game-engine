package com.codingame.gameengine.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;

/***
 * Mainly based on ContributionArchiveHelper.java
 */

public class ConfigHelper {
    private static final Pattern STATEMENT_FILE_PATTERN = Pattern.compile("statement_(?<language>.*?)\\.html?");
    private static final Pattern WELCOME_FILE_PATTERN = Pattern.compile("welcome_(?<language>.*?)\\.html?");
    private static final Pattern WELCOME_IMG_PATTERN = Pattern.compile(".*\\.(png|jpe?g)");
    private static final Pattern BOSS_FILE_PATTERN = Pattern.compile("Boss\\.(?<extension>.*)");
    private static final Pattern LEVEL_DIR_PATTERN = Pattern.compile("level(?<level>\\d+)");

    public static class GameConfig {
        private String title;
        private boolean leaguesDetected;
        private Map<String, QuestionConfig> questionsConfig = new HashMap<>();

        public Map<String, QuestionConfig> getQuestionsConfig() {
            return questionsConfig;
        }

        public void setQuestionConfigs(Map<String, QuestionConfig> questionsConfig) {
            this.questionsConfig = questionsConfig;
        }

        public boolean isLeaguesDetected() {
            return leaguesDetected;
        }

        public void setLeaguesDetected(boolean leaguesDetected) {
            this.leaguesDetected = leaguesDetected;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class QuestionConfig {
        private String title;
        private Map<Integer, String> statementsLanguageMap = new HashMap<Integer, String>();
        private Map<Integer, String> welcomeLanguageMap = new HashMap<Integer, String>();
        private List<File> welcomeImagesList = new ArrayList<>();
        private Integer minPlayers;
        private Integer maxPlayers;
        private String aiCode;
        private String aiCodeExtension;
        private String stubGenerator;
        private Integer level;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Map<Integer, String> getStatementsLanguageMap() {
            return statementsLanguageMap;
        }

        public void setStatementsLanguageMap(Map<Integer, String> statementsLanguageMap) {
            this.statementsLanguageMap = statementsLanguageMap;
        }

        public Map<Integer, String> getWelcomeLanguageMap() {
            return welcomeLanguageMap;
        }

        public void setWelcomeLanguageMap(Map<Integer, String> welcomeLanguageMap) {
            this.welcomeLanguageMap = welcomeLanguageMap;
        }

        public List<File> getWelcomeImagesList() {
            return welcomeImagesList;
        }

        public void setWelcomeImagesList(List<File> welcomeImagesList) {
            this.welcomeImagesList = welcomeImagesList;
        }

        public Integer getMinPlayers() {
            return minPlayers;
        }

        public void setMinPlayers(Integer minPlayers) {
            this.minPlayers = minPlayers;
        }

        public Integer getMaxPlayers() {
            return maxPlayers;
        }

        public void setMaxPlayers(Integer maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        public String getAiCode() {
            return aiCode;
        }

        public void setAiCode(String aiCode) {
            this.aiCode = aiCode;
        }

        public String getAiCodeExtension() {
            return aiCodeExtension;
        }

        public void setAiCodeExtension(String aiCodeExtension) {
            this.aiCodeExtension = aiCodeExtension;
        }

        public String getStubGenerator() {
            return stubGenerator;
        }

        public void setStubGenerator(String stubGenerator) {
            this.stubGenerator = stubGenerator;
        }

        public void merge(QuestionConfig defaultConfig) {
            this.title = ObjectUtils.firstNonNull(title, defaultConfig.title);

            this.statementsLanguageMap = firstNonNullOrEmptyMap(
                statementsLanguageMap,
                defaultConfig.statementsLanguageMap
            );
            this.welcomeLanguageMap = firstNonNullOrEmptyMap(
                welcomeLanguageMap,
                defaultConfig.welcomeLanguageMap
            );
            this.welcomeImagesList = firstNonNullOrEmptyList(
                welcomeImagesList,
                defaultConfig.welcomeImagesList
            );
            this.minPlayers = ObjectUtils.firstNonNull(minPlayers, defaultConfig.minPlayers);
            this.maxPlayers = ObjectUtils.firstNonNull(maxPlayers, defaultConfig.maxPlayers);
            this.aiCode = ObjectUtils.firstNonNull(aiCode, defaultConfig.aiCode);
            this.aiCodeExtension = ObjectUtils.firstNonNull(aiCodeExtension, defaultConfig.aiCodeExtension);
            this.stubGenerator = ObjectUtils.firstNonNull(stubGenerator, defaultConfig.stubGenerator);
        }

        private Map<Integer, String> firstNonNullOrEmptyMap(Map<Integer, String> first, Map<Integer, String> second) {
            if (first != null && !first.isEmpty()) {
                return first;
            }
            return second;
        }

        private List<File> firstNonNullOrEmptyList(List<File> first, List<File> second) {
            if (first != null && !first.isEmpty()) {
                return first;
            }
            return second;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    public GameConfig findConfig(Path rootDir) throws IOException {
        GameConfig gameConfig = new GameConfig();
        Map<String, QuestionConfig> questionsConfig = gameConfig.getQuestionsConfig();

        // Do not process more than 1 sub dir. hierarchy depth in the config dir (sub dir. contains config for each league)
        Files.walk(rootDir, 2).filter(Files::isRegularFile).forEach(p -> {
            try {
                String fileName = FilenameUtils.getName(p.toString());

                // if reading file from the config (root) folder, the level will be equals to the empty string ""
                String level = rootDir.relativize(p.getParent()).toString();

                // We only parse folder content with the correct pattern match
                Matcher levelMatcher = LEVEL_DIR_PATTERN.matcher(level);
                if (!levelMatcher.matches() && !level.isEmpty()) {
                    return;
                }

                // Generate the level map (folder name is the league name)
                QuestionConfig questionConfig = questionsConfig.get(level);
                if (questionConfig == null) {
                    questionConfig = new QuestionConfig();
                    questionsConfig.put(level, questionConfig);
                }

                // If we have a level folder different than the empty string it means that a level has been detected
                if (!level.isEmpty()) {
                    gameConfig.setLeaguesDetected(true);
                    questionConfig.setLevel(Integer.parseInt(levelMatcher.group("level")));
                }

                Matcher statementMatcher = STATEMENT_FILE_PATTERN.matcher(fileName);
                Matcher welcomeMatcher = WELCOME_FILE_PATTERN.matcher(fileName);
                Matcher bossMatcher = BOSS_FILE_PATTERN.matcher(fileName);
                Matcher welcomeImagesMatcher = WELCOME_IMG_PATTERN.matcher(fileName);

                if (p.toFile().isFile() && "config.ini".equals(fileName)) {
                    Properties config = new Properties();
                    try (FileInputStream is = new FileInputStream(p.toFile())) {
                        config.load(is);

                        String title = config.getProperty("title");
                        String minPlayers = config.getProperty("min_players");
                        String maxPlayers = config.getProperty("max_players");

                        questionConfig.setTitle(title);
                        questionConfig.setMinPlayers(minPlayers != null ? Integer.valueOf(minPlayers) : null);
                        questionConfig.setMaxPlayers(maxPlayers != null ? Integer.valueOf(maxPlayers) : null);
                    }
                } else if ("stub.txt".equals(fileName)) {
                    questionConfig.setStubGenerator(FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8));
                } else if (statementMatcher.matches()) {
                    String content = FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8);
                    switch (statementMatcher.group("language")) {
                    case "en":
                        questionConfig.getStatementsLanguageMap().put(Constants.LANGUAGE_ID_ENGLISH, content);
                        break;
                    case "fr":
                        questionConfig.getStatementsLanguageMap().put(Constants.LANGUAGE_ID_FRENCH, content);
                        break;
                    }
                } else if (welcomeMatcher.matches()) {
                    String content = FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8);
                    switch (welcomeMatcher.group("language")) {
                    case "en":
                        questionConfig.getWelcomeLanguageMap().put(Constants.LANGUAGE_ID_ENGLISH, content);
                        break;
                    case "fr":
                        questionConfig.getWelcomeLanguageMap().put(Constants.LANGUAGE_ID_FRENCH, content);
                        break;
                    }
                } else if (p.toFile().isFile() && bossMatcher.matches()) {
                    String content = FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8);
                    String extension = bossMatcher.group("extension");
                    questionConfig.setAiCode(content);
                    questionConfig.setAiCodeExtension(extension);
                } else if (welcomeImagesMatcher.matches()) {
                    questionConfig.getWelcomeImagesList().add(p.toFile());
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot process to parse config directory", e);
            }
        });

        QuestionConfig defaultConfig = questionsConfig.get("");
        gameConfig.setTitle(defaultConfig.getTitle());

        // If there is more than 1 element in leagues, then we have a real league system and the empty string key in leagues
        // represents the default value. So if we have a real league system, we will remove the default config and override
        // all the unset leagues values with the default ones
        if (questionsConfig.size() > 1) {
            questionsConfig.remove("");
            for (String leagueKey : questionsConfig.keySet()) {
                QuestionConfig questionConfig = questionsConfig.get(leagueKey);
                // If not set, we force the title of the league with the concatenation of the main title and the league dir. name
                ObjectUtils.firstNonNull(
                    questionConfig.title,
                    String.format("%s - %s", defaultConfig.title, leagueKey)
                );
                questionConfig.merge(defaultConfig);
            }
        }

        // copy english statement & welcome to french if not translated
        questionsConfig.values().stream().forEach(c -> {
            String statementEn = c.getStatementsLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH);
            c.getStatementsLanguageMap().putIfAbsent(Constants.LANGUAGE_ID_FRENCH, statementEn);
            String welcomeEn = c.getWelcomeLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH);
            if (welcomeEn != null)
                c.getWelcomeLanguageMap().putIfAbsent(Constants.LANGUAGE_ID_FRENCH, welcomeEn);
        });

        return gameConfig;
    }
}
