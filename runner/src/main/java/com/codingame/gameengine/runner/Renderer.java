package com.codingame.gameengine.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.codingame.gameengine.runner.ConfigHelper.GameConfig;
import com.codingame.gameengine.runner.ConfigHelper.GameType;
import com.codingame.gameengine.runner.ConfigHelper.QuestionConfig;
import com.codingame.gameengine.runner.ConfigHelper.TestCase;
import com.codingame.gameengine.runner.dto.ConfigResponseDto;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.DisableCacheHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceSupplier;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

class Renderer {

    private static final int MIN_PLAYERS = 1;
    private static final int MULTI_MAX_PLAYERS = 8;
    private static final int SOLO_MAX_PLAYERS = 8;
    private static final Pattern HTML_IMG_MARKER = Pattern.compile("<\\s*img [^\\>]*src\\s*=\\s*([\"\\'])(?<source>.*?)\\1");
    private static final Pattern GEN_STATEMENT_MARKER = Pattern.compile("statement_[a-zA-Z]{2}\\.html\\.tpl");

    protected static Log log = LogFactory.getLog(Renderer.class);

    public class MultipleResourceSupplier implements ResourceSupplier {

        private List<FileResourceManager> directories = new ArrayList<>();

        public void addDirectory(File directory) {
            FileResourceManager p = new FileResourceManager(directory);
            directories.add(p);
        }

        @Override
        public Resource getResource(HttpServerExchange exchange, String path) throws IOException {
            for (FileResourceManager dir : directories) {
                Resource resource = dir.getResource(path);
                if (resource != null) {
                    return resource;
                }
            }
            return null;
        }
    }

    private int port = 8080;

    public Renderer(int port) {
        this.port = port;
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty directories
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private static List<Path> exportViewToWorkingDir(String sourceFolder, Path targetFolder)
        throws IOException {
        List<Path> exportedPaths = new ArrayList<>();

        Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources(sourceFolder);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (url == null) {
                continue;
            }

            if ("jar".equals(url.getProtocol())) {
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                ZipFile jar = jarConnection.getJarFile();
                Enumeration<? extends ZipEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.startsWith(sourceFolder)) {
                        continue;
                    }
                    String entryTail = name.substring(sourceFolder.length());

                    File f = new File(targetFolder + File.separator + entryTail);
                    if (!f.toPath().normalize().startsWith(targetFolder)) {
                        throw new IOException("Zip entry contained path traversal");
                    }
                    if (entry.isDirectory()) {
                        f.mkdir();
                    } else {
                        Files.copy(jar.getInputStream(entry), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } else if ("file".equals(url.getProtocol())) {
                try {
                    String targetClassesFolder = "/target/classes/".replace('/', File.separatorChar) + sourceFolder;
                    String resourcesClassesFolder = "/src/main/resources/".replace('/', File.separatorChar)
                        + sourceFolder;

                    String targetPath = new File(url.toURI()).getAbsolutePath();
                    targetPath = targetPath.replace(targetClassesFolder, resourcesClassesFolder);

                    exportedPaths.add(new File(targetPath).toPath());
                    FileUtils.copyDirectory(new File(url.toURI()), targetFolder.toFile());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Cannot copy files", e);
                }
            }
        }

        // copied version has a lower priority
        exportedPaths.add(targetFolder);

        return exportedPaths;
    }

    private static String hashAsset(Path asset) throws IOException {
        HashCode hash = com.google.common.io.Files.asByteSource(new File(asset.toUri()))
            .hash(Hashing.sha256());
        String newName = hash.toString() + "."
            + FilenameUtils.getExtension(asset.getFileName().toString());
        return newName;

    }

    private static void generateAssetsFile(Path tmpdir, String assetsPath) {
        boolean assetsNeedHashing = assetsPath != null;

        if (assetsNeedHashing) {
            tmpdir.resolve("hashed_assets").toFile().mkdirs();
        }
        File assets = tmpdir.resolve("assets.js").toFile();
        try (PrintWriter out = new PrintWriter(assets)) {
            JsonObject jsonAssets = new JsonObject();
            if (assetsNeedHashing) {
                jsonAssets.addProperty("baseUrl", assetsPath);
            }
            JsonObject images = new JsonObject();
            JsonArray fonts = new JsonArray();
            JsonArray sprites = new JsonArray();
            jsonAssets.add("images", images);
            jsonAssets.add("sprites", sprites);
            jsonAssets.add("fonts", fonts);

            Path origAssetsPath = tmpdir.resolve("assets");
            try {
                Files.find(origAssetsPath, 100, (p, bfa) -> bfa.isRegularFile()).forEach(
                    f -> {
                        try {
                            if (isSpriteJson(f)) {
                                JsonParser parser = new JsonParser();
                                JsonElement jsonElement = parser.parse(new FileReader(f.toString()));
                                JsonObject jsonObject = jsonElement.getAsJsonObject();
                                String image = jsonObject.getAsJsonObject("meta").get("image").getAsString();
                                Path imagePath = origAssetsPath.resolve(image);

                                String jsonToWriteTo = null;
                                if (assetsNeedHashing) {
                                    String hashedImageName = hashAsset(imagePath);
                                    jsonObject.getAsJsonObject("meta").add("image", new JsonPrimitive(hashedImageName));
                                    String newName = hashAsset(f);
                                    jsonToWriteTo = tmpdir.resolve("hashed_assets").resolve(newName).toString();
                                    Files.createDirectories(tmpdir.resolve("hashed_assets"));
                                    sprites.add(newName);
                                } else {
                                    String relativeImagePath = tmpdir.relativize(imagePath).toString();
                                    jsonObject.getAsJsonObject("meta").add("image", new JsonPrimitive(relativeImagePath));
                                    jsonToWriteTo = f.toString();
                                    sprites.add(tmpdir.relativize(f).toString().replace("\\", "/"));
                                }
                                try (FileWriter writer = new FileWriter(jsonToWriteTo)) {
                                    Gson gson = new GsonBuilder().create();
                                    gson.toJson(jsonObject, writer);
                                }
                            } else if (isFont(f)) {
                                if (assetsNeedHashing) {
                                    List<String> content = Files.readAllLines(f);
                                    String newContent = "";
                                    Pattern regex = Pattern.compile("<page.*file=\\\"([^\\\"]+)\\\".*\\/>"); // looking for the font resources
                                    for (String line : content) {
                                        Matcher ressourcesMatcher = regex.matcher(line);
                                        String newLine = "";
                                        int startCpy = 0;
                                        while (ressourcesMatcher.find()) {
                                            int startMatch = ressourcesMatcher.start(1);
                                            int endMatch = ressourcesMatcher.end(1);
                                            newLine = newLine.concat(line.substring(startCpy, startMatch));
                                            Path assetPath = f.getParent().resolve(ressourcesMatcher.group(1));
                                            newLine = newLine.concat(hashAsset(assetPath));
                                            startCpy = endMatch;
                                        }
                                        newLine = newLine.concat(line.substring(startCpy));
                                        newContent = newContent.concat("\n".concat(newLine));
                                    }
                                    String newName = hashAsset(f);
                                    fonts.add(newName);
                                    Files.write(
                                        tmpdir.resolve("hashed_assets").resolve(newName),
                                        newContent.getBytes(),
                                        StandardOpenOption.CREATE
                                    );
                                } else {
                                    fonts.add(
                                        tmpdir.relativize(f).toString().replace("\\", "/")
                                    );
                                }
                            } else {
                                if (assetsNeedHashing) {
                                    String newName = hashAsset(f);
                                    images.addProperty(origAssetsPath.relativize(f).toString().replace("\\", "/"), newName);
                                    Files.copy(
                                        f, tmpdir.resolve("hashed_assets").resolve(newName),
                                        StandardCopyOption.REPLACE_EXISTING
                                    );
                                } else {
                                    images.addProperty(
                                        origAssetsPath.relativize(f).toString().replace("\\", "/"),
                                        tmpdir.relativize(f).toString().replace("\\", "/")
                                    );
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                );
            } catch (NoSuchFileException e) {
                System.out.println("Directory src/main/resources/view/assets not found.");
            }

            out.print("export const assets = ");
            out.println(jsonAssets.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isSpriteJson(Path f) {
        return "json".equals(FilenameUtils.getExtension(f.toString()));
    }

    private static boolean isFont(Path f) {
        return "fnt".equals(FilenameUtils.getExtension(f.toString()));
    }

    public static List<Path> generateView(String jsonResult, String assetsPath) {
        List<Path> paths;

        Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");
        deleteFolder(tmpdir.toFile());
        tmpdir.toFile().mkdirs();

        // Windows compatibility hack
        try {
            tmpdir = tmpdir.toRealPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonResult != null) {
            File game = tmpdir.resolve("game.json").toFile();
            try (PrintWriter out = new PrintWriter(game)) {
                out.println(jsonResult);
            } catch (IOException e) {
                throw new RuntimeException("Cannot generate the game file", e);
            }
        }

        try {
            paths = exportViewToWorkingDir("view", tmpdir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy resources", e);
        }

        // Depends on exportViewToWorkingDir
        generateAssetsFile(tmpdir, assetsPath);

        if (paths.size() == 0) {
            throw new RuntimeException("No resources folder found");
        }

        // Create empty demo.js if needed
        Path sourceFolderPath = new File(System.getProperty("user.dir")).toPath();
        File demoFile = sourceFolderPath.resolve("src/main/resources/view/demo.js").toFile();
        if (!demoFile.exists()) {
            try (PrintWriter out = new PrintWriter(demoFile)) {
                out.println("export const demo = null;");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return paths;
    }

    private void checkConfig(Path sourceFolderPath, ExportReport exportReport) throws IOException, MissingConfigException {
        ConfigHelper configHelper = new ConfigHelper();
        GameConfig gameConfig = configHelper.findConfig(sourceFolderPath.resolve("config"));

        //Check unique opti question
        checkUniqueOpti(gameConfig, exportReport);

        for (String league : gameConfig.getQuestionsConfig().keySet()) {
            QuestionConfig questionConfig = gameConfig.getQuestionsConfig().get(league);
            String tag = league + (league.isEmpty() ? "" : ": ");

            //Check config.ini
            checkConfigIni(gameConfig, questionConfig, tag, exportReport);

            //Check stub
            checkStub(questionConfig, tag, exportReport);

            //Check statement
            checkStatement(questionConfig, tag, exportReport);

            if (questionConfig.isMultiQuestion()) {
                //Check Boss
                checkBoss(questionConfig, tag, exportReport);

                //Check League popups
                if (gameConfig.isLeaguesDetected() && !"level1".equals(league)) {
                    checkLeaguePopups(questionConfig, tag, exportReport);
                }
            } else if (questionConfig.isSoloQuestion() || questionConfig.isOptiQuestion()) {
                //Check test cases
                checkTestCases(questionConfig, tag, exportReport);
            }
        }

    }

    private void checkTestCases(QuestionConfig questionConfig, String tag, ExportReport exportReport) {
        if (questionConfig.getTestCases().isEmpty()) {
            exportReport.addItem(ReportItemType.ERROR, "A solo game must have at least one test case.");
        }

        for (TestCase testCase : questionConfig.getTestCases()) {
            if (testCase.getTitle().get(Constants.LANGUAGE_ID_ENGLISH) == null) {
                exportReport.addItem(ReportItemType.ERROR, tag + "A test case must have at least an English title.");
            }
            if (testCase.getTestIn() == null || testCase.getTestIn().isEmpty()) {
                exportReport.addItem(ReportItemType.ERROR, tag + "A test case must have a testIn property.");
            }
            if (testCase.getIsTest() == null) {
                exportReport.addItem(ReportItemType.ERROR, tag + "A test case must have an isTest property.");
            }
            if (testCase.getIsValidator() == null) {
                exportReport.addItem(ReportItemType.ERROR, tag + "A test case must have an isValidator property.");
            }
        }
    }

    private void checkUniqueOpti(GameConfig gameConfig, ExportReport exportReport) {
        boolean hasAnOptiQuestion = false;
        for (QuestionConfig questionConfig : gameConfig.getQuestionsConfig().values()) {
            if (questionConfig.isOptiQuestion() && !hasAnOptiQuestion) {
                hasAnOptiQuestion = true;
            } else if (hasAnOptiQuestion) {
                exportReport.addItem(ReportItemType.ERROR, "An optimization game must have only one question.");
                break;
            }
        }
    }

    private void checkLeaguePopups(QuestionConfig questionConfig, String tag, ExportReport exportReport) {
        if (!questionConfig.getWelcomeLanguageMap().containsKey(Constants.LANGUAGE_ID_ENGLISH)
            || questionConfig.getWelcomeLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH).isEmpty()) {
            exportReport.addItem(
                ReportItemType.WARNING, tag + "Missing welcome_"
                    + Constants.LANGUAGE_CODE[Constants.LANGUAGE_ID_ENGLISH - 1] + ".html file."
            );
        } else {
            for (int languageId : questionConfig.getWelcomeLanguageMap().keySet()) {
                //Avoid checking the same popup twice if duplicated
                if (languageId != Constants.LANGUAGE_ID_ENGLISH
                    && questionConfig.getWelcomeLanguageMap().get(languageId)
                        .equals(questionConfig.getWelcomeLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH))) {
                    continue;
                }

                //List of all images used in the welcome popup
                Matcher imageMatcher = HTML_IMG_MARKER.matcher(questionConfig.getWelcomeLanguageMap().get(languageId));
                Set<String> imagesName = new HashSet<>();

                while (imageMatcher.find()) {
                    imagesName.add(imageMatcher.group("source"));
                }

                //Substract all found images to present ones: elements left in the list do not exist in config
                imagesName.removeAll(
                    questionConfig.getWelcomeImagesList().stream().map(f -> f.getName()).collect(Collectors.toSet())
                );

                for (String imageName : imagesName) {
                    exportReport.addItem(
                        ReportItemType.WARNING, tag + "File " + imageName + " is used in welcome_"
                            + Constants.LANGUAGE_CODE[languageId - 1] + ".html but is missing."
                    );
                }
            }
        }
    }

    private void checkBoss(QuestionConfig questionConfig, String tag, ExportReport exportReport) {
        if (questionConfig.getAiCode() == null || questionConfig.getAiCode().isEmpty()) {
            exportReport.addItem(ReportItemType.ERROR, tag + "Missing Boss.* file.");
        }
    }

    private void checkStatement(QuestionConfig questionConfig, String tag, ExportReport exportReport) {
        if (!questionConfig.getStatementsLanguageMap().containsKey(Constants.LANGUAGE_ID_ENGLISH)
            || questionConfig.getStatementsLanguageMap().get(Constants.LANGUAGE_ID_ENGLISH).isEmpty()) {
            exportReport.addItem(ReportItemType.ERROR, tag + "Missing statement_en.html file. An English statement is mandatory.");
        }
    }

    private void checkStub(QuestionConfig questionConfig, String tag, ExportReport exportReport) {
        if (questionConfig.getStubGenerator() == null || questionConfig.getStubGenerator().isEmpty()) {
            exportReport.addItem(
                ReportItemType.WARNING, tag + "Missing stub.txt file.",
                "https://github.com/CodinGame/codingame-game-engine/blob/master/stubGeneratorSyntax.md"
            );
        } else {
            exportReport.getStubs().put(tag, questionConfig.getStubGenerator());
        }
    }

    private void checkConfigIni(GameConfig gameConfig, QuestionConfig questionConfig, String tag, ExportReport exportReport)
        throws MissingConfigException {
        if (!questionConfig.isConfigDetected()) {
            throw new MissingConfigException(tag + "Missing config.ini file");
        } else if (questionConfig.getMinPlayers() == null) {
            throw new MissingConfigException(tag + "Missing min_players property in config.ini.");
        } else if (questionConfig.getMaxPlayers() == null) {
            throw new MissingConfigException(tag + "Missing max_players property in config.ini.");
        } else {
            checkQuestionsTypeValidity(gameConfig, questionConfig, tag, exportReport);

            checkPlayerNumber(questionConfig, tag, exportReport, gameConfig.getGameType() == GameType.MULTI ? MULTI_MAX_PLAYERS : SOLO_MAX_PLAYERS);
        }
    }

    private void checkQuestionsTypeValidity(GameConfig gameConfig, QuestionConfig questionConfig, String tag, ExportReport exportReport)
        throws MissingConfigException {
        if (!questionConfig.isValidQuestionType()) {
            throw new MissingConfigException(tag + "Your question type is not valid. Please choose one among MULTI, SOLO and OPTI.");
        }

        if (gameConfig.getGameType() == GameType.UNDEFINED) {
            exportReport.addItem(ReportItemType.ERROR, "The game has both multiplayer and solo player questions. Please, choose either one.");
        }

        if (questionConfig.isOptiQuestion()) {
            if (gameConfig.getGameType() != GameType.SOLO) {
                exportReport.addItem(ReportItemType.ERROR, "An optimization game must be solo player.");
            }
            if (questionConfig.getCriteria() == null) {
                throw new MissingConfigException("An optimization game must have a criteria property in config.ini.");
            }
            if (questionConfig.getSortingOrder() == null) {
                throw new MissingConfigException("An optimization game must have a sorting_order property in config.ini.");
            } else if (!"ASC".equalsIgnoreCase(questionConfig.getSortingOrder())
                && !"DESC".equalsIgnoreCase(questionConfig.getSortingOrder())) {
                throw new MissingConfigException("The sorting order for an optimization game must be ASC (ascendant) or DESC (descendant)");
            }
        }

        switch (gameConfig.getGameType()) {
        case MULTI:
            if (!questionConfig.isMultiQuestion()) {
                exportReport.addItem(ReportItemType.ERROR, "The game has several players but the type is not MULTI.");
            }
            break;
        case SOLO:
            if (!questionConfig.isSoloQuestion() && !questionConfig.isOptiQuestion()) {
                exportReport.addItem(ReportItemType.ERROR, "The game has one player but the type is not SOLO or OPTI.");
            }
            break;
        case UNDEFINED:
        default:
            break;
        }
    }

    private void checkPlayerNumber(QuestionConfig questionConfig, String tag, ExportReport exportReport, int maxPlayers) {
        if (questionConfig.getMinPlayers() < MIN_PLAYERS) {
            exportReport.addItem(
                ReportItemType.ERROR, tag + "Min players ("
                    + questionConfig.getMinPlayers()
                    + ") should be greater or equal to " + MIN_PLAYERS + "."
            );
        }
        if (questionConfig.getMaxPlayers() < questionConfig.getMinPlayers()) {
            exportReport.addItem(
                ReportItemType.ERROR, tag + "Max players ("
                    + questionConfig.getMaxPlayers()
                    + ") should be greater or equal to min players ("
                    + questionConfig.getMinPlayers()
                    + ")."
            );
        }
        if (questionConfig.getMaxPlayers() > maxPlayers) {
            exportReport.addItem(
                ReportItemType.ERROR, tag + "Max players ("
                    + questionConfig.getMaxPlayers()
                    + ") should be lower or equal to " + maxPlayers + "."
            );
        }
    }

    private Path exportSourceCode(Path sourceFolderPath, Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Files.walkFileTree(
                sourceFolderPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String relativePath = sourceFolderPath.relativize(file).toString();
                        if (relativePath.startsWith("config") || relativePath.startsWith("src") || relativePath.equals("pom.xml")) {
                            zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString().replace('\\', '/')));
                            Files.copy(file, zos);
                            zos.closeEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                }
            );
        }

        return zipPath;
    }

    private void generateSplittedStatements(Path sourceFolderPath, ExportReport exportReport) throws IOException {
        Files.list(sourceFolderPath.resolve("config/"))
            .filter(p -> GEN_STATEMENT_MARKER.matcher(FilenameUtils.getName(p.toString())).matches() && p.toFile().isFile())
            .forEach(p -> StatementSplitter.generateSplittedStatement(sourceFolderPath, p.toFile(), exportReport));
    }

    private void serveHTTP(List<Path> path) {
        System.out.println("http://localhost:" + port + "/test.html");

        MultipleResourceSupplier mrs = new MultipleResourceSupplier();
        for (Path p : path) {
            mrs.addDirectory(p.toFile());
            System.out.println("Exposed web server dir: " + p.toString());
        }

        Undertow server = Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler(
                new DisableCacheHandler(
                    Handlers.path(new ResourceHandler(mrs).addWelcomeFiles("test.html"))
                        .addPrefixPath(
                            "/services/", new HttpHandler() {
                                @Override
                                public void handleRequest(HttpServerExchange exchange) throws Exception {
                                    Path sourceFolderPath = new File(System.getProperty("user.dir")).toPath();
                                    try {
                                        if (exchange.getRelativePath().equals("/export")) {
                                            Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");

                                            Path zipPath = tmpdir.resolve("source.zip");

                                            ExportReport exportReport = new ExportReport();
                                            generateSplittedStatements(sourceFolderPath, exportReport);
                                            checkConfig(sourceFolderPath, exportReport);
                                            if (exportReport.getExportStatus() == ExportStatus.SUCCESS) {
                                                exportSourceCode(sourceFolderPath, zipPath);
                                                exportReport.setDataUrl(zipPath.getFileName().toString());
                                            }

                                            String jsonExportReport = new Gson().toJson(exportReport);
                                            exchange.getResponseSender().send(jsonExportReport);
                                        } else if (exchange.getRelativePath().equals("/init-config")) {
                                            if (!sourceFolderPath.resolve("config").toFile().isDirectory()) {
                                                sourceFolderPath.resolve("config").toFile().mkdir();
                                            }
                                            File configFile = sourceFolderPath.resolve("config/config.ini").toFile();
                                            if (!configFile.exists()) {
                                                configFile.createNewFile();
                                            }
                                            FileOutputStream configOutput = new FileOutputStream(configFile);
                                            Properties config = new Properties();

                                            exchange.getRequestReceiver().receiveFullString((e, data) -> {
                                                ConfigResponseDto configResponseDto = new Gson().fromJson(data, ConfigResponseDto.class);
                                                config.put("min_players", String.valueOf(configResponseDto.minPlayers));
                                                config.put("max_players", String.valueOf(configResponseDto.maxPlayers));
                                                config.put("type", configResponseDto.type);
                                                if (configResponseDto.criteria != null) {
                                                    config.put("criteria", configResponseDto.criteria);
                                                }
                                                if (configResponseDto.sortingOrder != null) {
                                                    config.put("sorting_order", configResponseDto.sortingOrder);
                                                }
                                                if (configResponseDto.criteriaFr != null) {
                                                    config.put("criteria_fr", configResponseDto.criteriaFr);
                                                }
                                                if (configResponseDto.criteriaEn != null) {
                                                    config.put("criteria_en", configResponseDto.criteriaEn);
                                                }
                                            });

                                            config.store(configOutput, null);
                                            exchange.setStatusCode(StatusCodes.FOUND);
                                        } else if (exchange.getRelativePath().equals("/save-replay")) {
                                            Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");
                                            File demoFile = sourceFolderPath.resolve("src/main/resources/view/demo.js").toFile();
                                            File gameFile = tmpdir.resolve("game.json").toFile();

                                            try (PrintWriter out = new PrintWriter(demoFile)) {
                                                out.println("export const demo = ");
                                                out.print(extractDemoFromGameJson(gameFile).toString());
                                                out.println(";");
                                            }

                                            exchange.setStatusCode(StatusCodes.OK);
                                        }
                                    } catch (MissingConfigException e) {
                                        sendException(exchange, e, StatusCodes.UNPROCESSABLE_ENTITY);
                                    } catch (Exception e) {
                                        sendException(exchange, e, StatusCodes.BAD_REQUEST);
                                    } finally {
                                        exchange.endExchange();
                                    }
                                }

                                private JsonObject extractDemoFromGameJson(File gameFile) {
                                    JsonObject result = new JsonObject();
                                    JsonParser parser = new JsonParser();
                                    try {
                                        JsonElement obj = parser.parse(new FileReader(gameFile));
                                        JsonElement views = obj.getAsJsonObject().get("views");
                                        JsonElement agents = obj.getAsJsonObject().get("agents");
                                        result.add("views", views);
                                        result.add("agents", agents);

                                    } catch (IllegalStateException | JsonIOException | JsonSyntaxException | FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    return result;
                                }

                                private void sendException(HttpServerExchange exchange, Exception e, int statusCode) {
                                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                    exchange.setStatusCode(statusCode);
                                    exchange.getResponseSender().send(e.getMessage());
                                }
                            }
                        )
                )
            )
            .build();
        try {
            server.start();
        } catch (RuntimeException e) {
            checkAddressAlreadyBound(e);
        }
    }

    private void checkAddressAlreadyBound(RuntimeException e) {
        if (e.getMessage() != null) {
            Matcher bindExceptionMatcher = Pattern.compile("java.net.BindException.*").matcher(e.getMessage());
            if (bindExceptionMatcher.matches()) {
                log.warn(
                    "Run successful but port already in use. If you are running a different game, please restart the server."
                );
                return;
            }
        }
        throw e;
    }

    public void render(int playerCount, String jsonResult) {
        List<Path> paths = generateView(jsonResult, null);
        serveHTTP(paths);
    }
}
