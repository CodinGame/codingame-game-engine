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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import com.google.gson.Gson;

/***
 * Mainly based on ContributionArchiveHelper.java
 */
class ConfigHelper {
    private static final Pattern STATEMENT_FILE_PATTERN = Pattern.compile("statement_(?<language>.*?)\\.html?");
    private static final Pattern WELCOME_FILE_PATTERN = Pattern.compile("welcome_(?<language>.*?)\\.html?");
    private static final Pattern WELCOME_IMG_PATTERN = Pattern.compile(".*\\.(png|jpe?g)");
    private static final Pattern BOSS_FILE_PATTERN = Pattern.compile("Boss\\.(?<extension>.*)");
    private static final Pattern LEVEL_DIR_PATTERN = Pattern.compile("level(?<level>\\d+)");
    private static final Pattern TEST_FILE_PATTERN = Pattern.compile("test(?<num>\\d+)\\.json");

    enum GameType {
        SOLO, MULTI, UNDEFINED
    }

    public static class GameConfig {
        private boolean leaguesDetected;
        private GameType gameType;
        private Map<String, QuestionConfig> questionsConfig = new HashMap<>();

        public GameType getGameType() {
            return gameType;
        }

        public void setGameType(GameType gameType) {
            this.gameType = gameType;
        }

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
    }

    class TestCase {
        private Map<Integer, String> title;
        private String testIn;
        private Boolean isTest;
        private Boolean isValidator;

        public Map<Integer, String> getTitle() {
            return title;
        }

        public String getTestIn() {
            return testIn;
        }

        public Boolean getIsTest() {
            return isTest;
        }

        public Boolean getIsValidator() {
            return isValidator;
        }

        public void setTitle(Map<Integer, String> title) {
            this.title = title;
        }

        public void setTestIn(String testIn) {
            this.testIn = testIn;
        }

        public void setIsTest(Boolean isTest) {
            this.isTest = isTest;
        }

        public void setIsValidator(Boolean isValidator) {
            this.isValidator = isValidator;
        }
    }

    public static class QuestionConfig {
        private boolean configDetected;
        private Map<Integer, String> statementsLanguageMap = new HashMap<Integer, String>();
        private Map<Integer, String> welcomeLanguageMap = new HashMap<Integer, String>();
        private List<File> welcomeImagesList = new ArrayList<>();
        private Integer minPlayers;
        private Integer maxPlayers;
        private String aiCode;
        private String aiCodeExtension;
        private String stubGenerator;
        private Integer level;
        private List<TestCase> testCases = new ArrayList<>();
        private String criteria;
        private Map<Integer, String> criteriaLanguageMap = new HashMap<Integer, String>();
        private String sortingOrder;
        private String questionType;
        private Boolean ignoreDraws;

        public Boolean getIgnoreDraws() {
            return ignoreDraws;
        }

        public void setIgnoreDraws(Boolean ignoreDraws) {
            this.ignoreDraws = ignoreDraws;
        }

        //Used to sort test cases by their filename number
        private Map<Integer, TestCase> testCaseDtoMap = new TreeMap<>();

        public Map<Integer, String> getCriteriaLanguageMap() {
            return criteriaLanguageMap;
        }

        public void setCriteriaLanguageMap(Map<Integer, String> criteriaLanguageMap) {
            this.criteriaLanguageMap = criteriaLanguageMap;
        }

        public String getQuestionType() {
            return questionType;
        }

        public void setQuestionType(String questionType) {
            this.questionType = questionType;
        }

        public String getCriteria() {
            return criteria;
        }

        public void setCriteria(String criteria) {
            this.criteria = criteria;
        }

        public String getSortingOrder() {
            return sortingOrder;
        }

        public void setSortingOrder(String sortingOrder) {
            this.sortingOrder = sortingOrder;
        }

        public Map<Integer, TestCase> getTestCaseDtoMap() {
            return testCaseDtoMap;
        }

        public void setTestCaseDtoMap(Map<Integer, TestCase> testCaseDtoMap) {
            this.testCaseDtoMap = testCaseDtoMap;
        }

        public void setTestCases(List<TestCase> testCases) {
            this.testCases = testCases;
        }

        public List<TestCase> getTestCases() {
            return testCases;
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

        public boolean isConfigDetected() {
            return configDetected;
        }

        public void setConfigDetected(boolean configDetected) {
            this.configDetected = configDetected;
        }

        public void merge(QuestionConfig defaultConfig) {
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
            this.testCaseDtoMap = firstNonNullOrEmptyMap(
                testCaseDtoMap,
                defaultConfig.testCaseDtoMap
            );
            this.criteriaLanguageMap = firstNonNullOrEmptyMap(
                criteriaLanguageMap,
                defaultConfig.criteriaLanguageMap
            );
            this.minPlayers = ObjectUtils.firstNonNull(minPlayers, defaultConfig.minPlayers);
            this.maxPlayers = ObjectUtils.firstNonNull(maxPlayers, defaultConfig.maxPlayers);
            this.aiCode = ObjectUtils.firstNonNull(aiCode, defaultConfig.aiCode);
            this.aiCodeExtension = ObjectUtils.firstNonNull(aiCodeExtension, defaultConfig.aiCodeExtension);
            this.stubGenerator = ObjectUtils.firstNonNull(stubGenerator, defaultConfig.stubGenerator);
            this.criteria = ObjectUtils.firstNonNull(criteria, defaultConfig.criteria);
            this.sortingOrder = ObjectUtils.firstNonNull(sortingOrder, defaultConfig.sortingOrder);
            this.questionType = ObjectUtils.firstNonNull(questionType, defaultConfig.questionType);
        }

        private <T, B> Map<T, B> firstNonNullOrEmptyMap(Map<T, B> first, Map<T, B> second) {
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

        public boolean isValidQuestionType() {
            return isSoloQuestion() || isMultiQuestion() || isOptiQuestion();
        }

        public boolean isSoloQuestion() {
            return isTypeTQuestion("solo");
        }

        public boolean isMultiQuestion() {
            return isTypeTQuestion("multi");
        }

        public boolean isOptiQuestion() {
            return isTypeTQuestion("opti");
        }

        private boolean isTypeTQuestion(String type) {
            return type.equalsIgnoreCase(questionType);
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
                Matcher testCaseMatcher = TEST_FILE_PATTERN.matcher(fileName);

                if (p.toFile().isFile() && "config.ini".equals(fileName)) {
                    Properties config = new Properties();
                    try (FileInputStream is = new FileInputStream(p.toFile())) {
                        config.load(is);

                        String minPlayers = config.getProperty("min_players");
                        String maxPlayers = config.getProperty("max_players");
                        String criteria = config.getProperty("criteria");
                        String criteriaEn = config.getProperty("criteria_en");
                        String criteriaFr = config.getProperty("criteria_fr");
                        String sortingOrder = config.getProperty("sorting_order");
                        String questionType = config.getProperty("type");
                        String ignoreDraws = config.getProperty("ignore_draws");

                        questionConfig.setMinPlayers(minPlayers != null ? Integer.valueOf(minPlayers) : null);
                        questionConfig.setMaxPlayers(maxPlayers != null ? Integer.valueOf(maxPlayers) : null);
                        questionConfig.setCriteria(criteria);
                        questionConfig.setSortingOrder(sortingOrder);
                        questionConfig.setQuestionType(questionType);
                        questionConfig.setIgnoreDraws(ignoreDraws != null ? Boolean.valueOf(ignoreDraws) : null);

                        if (sortingOrder != null && criteria != null) {
                            if (criteriaEn != null) {
                                questionConfig.getCriteriaLanguageMap().put(Constants.LANGUAGE_ID_ENGLISH, criteriaEn);
                            } else {
                                questionConfig.getCriteriaLanguageMap().put(Constants.LANGUAGE_ID_ENGLISH, criteria);
                            }

                            if (criteriaFr != null) {
                                questionConfig.getCriteriaLanguageMap().put(Constants.LANGUAGE_ID_FRENCH, criteriaFr);
                            } else {
                                questionConfig.getCriteriaLanguageMap()
                                    .put(Constants.LANGUAGE_ID_FRENCH, questionConfig.getCriteriaLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH));
                            }
                        }
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
                } else if (testCaseMatcher.matches()) {
                    TestCase testCase = new Gson()
                        .fromJson((FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8)), TestCase.class);
                    questionConfig.getTestCaseDtoMap().put(Integer.parseInt(testCaseMatcher.group("num")), testCase);
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot process to parse config directory", e);
            }
        });

        QuestionConfig defaultConfig = questionsConfig.get("");

        // If there is more than 1 element in leagues, then we have a real league system and the empty string key in leagues
        // represents the default value. So if we have a real league system, we will remove the default config and override
        // all the unset leagues values with the default ones
        if (questionsConfig.size() > 1) {
            questionsConfig.remove("");
            for (String leagueKey : questionsConfig.keySet()) {
                QuestionConfig questionConfig = questionsConfig.get(leagueKey);
                questionConfig.merge(defaultConfig);
                questionConfig.configDetected = isPresentConfigIni(gameConfig, questionConfig);
            }
        } else {
            defaultConfig.configDetected = isPresentConfigIni(gameConfig, defaultConfig);
        }

        MutableInt soloQuestions = new MutableInt();
        questionsConfig.values().stream().forEach(c -> {
            // copy english statement, welcome, criteria to french if not translated
            String statementEn = c.getStatementsLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH);
            c.getStatementsLanguageMap().putIfAbsent(Constants.LANGUAGE_ID_FRENCH, statementEn);

            String welcomeEn = c.getWelcomeLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH);
            if (welcomeEn != null) {
                c.getWelcomeLanguageMap().putIfAbsent(Constants.LANGUAGE_ID_FRENCH, welcomeEn);
            }

            String criteriaEn = c.getCriteriaLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH);
            if (criteriaEn != null) {
                c.getCriteriaLanguageMap().putIfAbsent(Constants.LANGUAGE_ID_FRENCH, criteriaEn);
            }

            // set test cases list from the sorted TreeMap
            c.getTestCases().addAll(c.getTestCaseDtoMap().values());
            // copy english test cases' titles to french if not translated
            for (TestCase t : c.getTestCases()) {
                String titleEn = t.getTitle().get(Constants.LANGUAGE_ID_ENGLISH);
                t.getTitle().putIfAbsent(Constants.LANGUAGE_ID_FRENCH, titleEn);
            }

            if (c.maxPlayers != null && c.maxPlayers == 1) {
                soloQuestions.increment();
            }
        });

        if (soloQuestions.intValue() == questionsConfig.size()) {
            gameConfig.setGameType(GameType.SOLO);
        } else if (soloQuestions.intValue() == 0) {
            gameConfig.setGameType(GameType.MULTI);
        } else {
            gameConfig.setGameType(GameType.UNDEFINED);
        }

        //Sort leagues alphabetically
        gameConfig.setQuestionConfigs(new TreeMap<>(questionsConfig));

        return gameConfig;
    }

    private boolean isPresentConfigIni(GameConfig gameConfig, QuestionConfig questionConfig) {
        return questionConfig.minPlayers != null || questionConfig.maxPlayers != null;
    }
}
