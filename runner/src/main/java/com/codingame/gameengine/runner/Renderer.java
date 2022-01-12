package com.codingame.gameengine.runner;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.*;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.DisableCacheHandler;
import io.undertow.server.handlers.SetHeaderHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceSupplier;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.html.*;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class Renderer {

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
                List<String> linkedImages = new ArrayList<>();

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
                                    linkedImages.add(hashedImageName);
                                } else {
                                    String relativeImagePath = tmpdir.relativize(imagePath).toString();
                                    jsonObject.getAsJsonObject("meta").add("image", new JsonPrimitive(relativeImagePath));
                                    jsonToWriteTo = f.toString();
                                    sprites.add(tmpdir.relativize(f).toString().replace("\\", "/"));
                                    linkedImages.add(relativeImagePath);
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
                                    if (!fonts.contains(new JsonPrimitive(newName))) {
                                        fonts.add(newName);
                                    }

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
                        } catch (JsonSyntaxException e) {
                            log.error("Invalid JSON in file " + f.getFileName() + " at " + f.toString().replaceAll("^" + tmpdir.toString(), ""));
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                );

                // Don't load images that are also getting loaded by spritesheets
                for (String imageName : linkedImages) {
                    images.entrySet().removeIf(
                        entry -> entry.getValue().getAsString().equals(imageName)
                    );
                }

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

        return paths;
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
                    Handlers.path(new SetHeaderHandler(new ResourceHandler(mrs).addWelcomeFiles("test.html"), "Access-Control-Allow-Origin", "*"))
                        .addPrefixPath(
                            "/services/", new HttpHandler() {
                                @Override
                                public void handleRequest(HttpServerExchange exchange) throws Exception {
                                    Path sourceFolderPath = new File(System.getProperty("user.dir")).toPath();
                                    try {
                                        if (exchange.getRelativePath().equals("/stub")) {
                                            File stubFile = sourceFolderPath.resolve("config/stub.txt").toFile();
                                            if (exchange.getRequestMethod().equalToString("GET")) {
                                                String stub = FileUtils.readFileToString(stubFile, StandardCharsets.UTF_8);
                                                exchange.getResponseSender().send(stub);
                                            } else if (exchange.getRequestMethod().equalToString("PUT")) {
                                                exchange.getRequestReceiver().receiveFullString((e, data) -> {
                                                    try {
                                                        FileUtils.writeStringToFile(stubFile, data);
                                                        exchange.setStatusCode(StatusCodes.CREATED);
                                                    } catch (IOException ex) {
                                                        sendException(e, ex, StatusCodes.BAD_REQUEST);
                                                    }
                                                });
                                            } else {
                                                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                                            }
                                        } else if (exchange.getRelativePath().equals("/preview-levels")) {
                                            JsonParser parser = new JsonParser();
                                            ExportReport exportReport = new ExportReport();
                                            exchange.getRequestReceiver().receiveFullString((e, data) -> {
                                                JsonObject result = parser.parse(data).getAsJsonObject();
                                                String statement = result.get("statement").getAsString();
                                                List<String> lines = Arrays.asList(statement.split("\\\n"));

                                                try {
                                                    exchange.setStatusCode(StatusCodes.OK);
                                                    JsonObject statements = StatementSplitter.generateSplitStatementInMemory(lines, exportReport);
                                                    Iterator<String> keys = statements.keySet().iterator();
                                                    if (keys.hasNext()) {
                                                        while (keys.hasNext()) {
                                                            String key = keys.next();
                                                            statement = sanitizeHTML(statements.get(key).getAsString());
                                                            statements.addProperty(key, statement);
                                                        }
                                                    } else {
                                                        statements.add("level1", new JsonPrimitive(sanitizeHTML(statement)));
                                                    }
                                                    exchange.getResponseSender().send(statements.toString());
                                                } catch (IOException ex) {
                                                    sendException(e, ex, StatusCodes.BAD_REQUEST);
                                                }
                                            }, StandardCharsets.UTF_8);
                                        } else if (exchange.getRelativePath().equals("/statement")) {
                                            File statementFileEN = getStatementFile(sourceFolderPath, "en");
                                            File statementFileFR = getStatementFile(sourceFolderPath, "fr");

                                            if (exchange.getRequestMethod().equalToString("GET")) {
                                                JsonObject statements = new JsonObject();
                                                String statementEN = FileUtils.readFileToString(statementFileEN, StandardCharsets.UTF_8);
                                                String statementFR;
                                                if (!statementFileFR.exists()) {
                                                    statementFR = null;
                                                } else {
                                                    statementFR = FileUtils.readFileToString(statementFileFR, StandardCharsets.UTF_8);
                                                }
                                                JsonElement statementElement = toJsonElement(statementFR);
                                                statements.add("EN", new JsonPrimitive(statementEN));
                                                statements.add("FR", statementElement);
                                                exchange.getResponseSender().send(statements.toString());
                                            } else if (exchange.getRequestMethod().equalToString("PUT")) {
                                                JsonParser parser = new JsonParser();
                                                exchange.getRequestReceiver().receiveFullString((e, data) -> {
                                                    try {
                                                        JsonObject result = parser.parse(data).getAsJsonObject();
                                                        String language = result.get("language").getAsString();
                                                        String statement = result.get("statement").getAsString();
                                                        if (language.equals("FR")) {
                                                            if (!createFileIfNotExists(statementFileFR, e)) {
                                                                // terminate if error in createFileIfNotExists
                                                                return;
                                                            }
                                                            FileUtils.write(statementFileFR, statement, StandardCharsets.UTF_8);
                                                        } else if (language.equals("EN")) {
                                                            FileUtils.write(statementFileEN, statement, StandardCharsets.UTF_8);
                                                        }
                                                        exchange.setStatusCode(StatusCodes.CREATED);
                                                    } catch (IOException ex) {
                                                        sendException(e, ex, StatusCodes.BAD_REQUEST);
                                                    }
                                                }, StandardCharsets.UTF_8);
                                            } else {
                                                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                                            }
                                        }
                                    } catch (Exception e) {
                                        sendException(exchange, e, StatusCodes.BAD_REQUEST);
                                    } finally {
                                        exchange.endExchange();
                                    }
                                }

                                private File getStatementFile(Path sourceFolderPath, String langId) {
                                    File file = sourceFolderPath.resolve("config/statement_" + langId + ".html").toFile();
                                    if (!file.exists()) {
                                        file = sourceFolderPath.resolve("config/statement_" + langId + ".html.tpl").toFile();
                                    }
                                    return file;

                                }

                                private JsonElement toJsonElement(String statementFR) {
                                    return Optional.ofNullable(statementFR)
                                        .map(s -> (JsonElement) new JsonPrimitive(s))
                                        .orElse(JsonNull.INSTANCE);
                                }

                                private boolean createFileIfNotExists(File statementFileFR, HttpServerExchange e) {
                                    if (!statementFileFR.exists()) {
                                        try {
                                            statementFileFR.createNewFile();
                                            return true;
                                        } catch (IOException ex) {
                                            sendException(e, ex, StatusCodes.INTERNAL_SERVER_ERROR);
                                            // only return false in case of error
                                            return false;
                                        }
                                    } else {
                                        return true;
                                    }
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

    public static String sanitizeHTML(String html) {
        PolicyFactory htmlSanitizer = Sanitizers.FORMATTING
            .and(Sanitizers.LINKS)
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.TABLES)
            .and(new HtmlPolicyBuilder().allowElements(new ElementPolicy() {
                public String apply(String elementName, List<String> attrs) {
                    // force a target="_blank" on all links
                    if ("a".equals(elementName)) {
                        attrs.add("target");
                        attrs.add("_blank");
                    }
                    return elementName;
                }
            }, "const", "var", "action", "keyword",
                "a",
                "track",
                "article",
                "aside",
                "header",
                "hgroup",
                "hr",
                "footer",
                "nav",
                "section",
                "summary",
                "details",
                "base",
                "basefont",
                "span",
                "title",
                "button",
                "datalist",
                "form",
                "keygen",
                "label",
                "input",
                "legend",
                "fieldset",
                "meter",
                "optgroup",
                "option",
                "select",
                "textarea",
                "abbr",
                "acronym",
                "address",
                "bdi",
                "bdo",
                "center",
                "cite",
                "del",
                "dfn",
                "kbd",
                "mark",
                "output",
                "progress",
                "q",
                "rp",
                "rt",
                "ruby",
                "samp",
                "wbr",
                "dd",
                "dir",
                "dl",
                "dt",
                "menu",
                "area",
                "figcaption",
                "figure",
                "map",
                "param",
                "source",
                "audio",
                "time",
                "video"
            )
                .allowWithoutAttributes("span")
                .allowAttributes("class", "colspan").globally()
                .allowStandardUrlProtocols()
                .allowStyling()
                .allowUrlsInStyles(AttributePolicy.IDENTITY_ATTRIBUTE_POLICY)
                .requireRelNofollowOnLinks()
                .toFactory()
            );
        return htmlSanitizer.sanitize(html);
    }
}
