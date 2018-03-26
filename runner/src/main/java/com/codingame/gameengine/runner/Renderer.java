package com.codingame.gameengine.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;

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

    private static void generateAssetsFile(Path tmpdir, String assetsPath) {
        if (assetsPath != null) {
            tmpdir.resolve("hashed_assets").toFile().mkdirs();
        }
        File assets = tmpdir.resolve("assets.js").toFile();
        try (PrintWriter out = new PrintWriter(assets)) {
            JsonObject jsonAssets = new JsonObject();
            if (assetsPath != null) {
                jsonAssets.addProperty("baseUrl", assetsPath);
            }
            JsonObject images = new JsonObject();
            jsonAssets.add("images", images);

            Path origAssetsPath = tmpdir.resolve("assets");
            try {
                Files.find(origAssetsPath, 100, (p, bfa) -> bfa.isRegularFile()).forEach(
                    f -> {
                        try {
                            if (assetsPath != null) {
                                HashCode hash = com.google.common.io.Files.asByteSource(new File(f.toUri()))
                                    .hash(Hashing.sha256());
                                String newName = hash.toString() + "."
                                    + FilenameUtils.getExtension(f.getFileName().toString());

                                images.addProperty(origAssetsPath.relativize(f).toString(), newName);
                                Files.copy(
                                    f, tmpdir.resolve("hashed_assets").resolve(newName),
                                    StandardCopyOption.REPLACE_EXISTING
                                );
                            } else {
                                images.addProperty(
                                    origAssetsPath.relativize(f).toString(),
                                    tmpdir.relativize(f).toString()
                                );
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

    public static List<Path> generateView(String jsonResult, String assetsPath) {
        List<Path> paths;

        Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");
        
        
        // Windows compatibility hack
        try {
            tmpdir = tmpdir.toRealPath();
        } catch (IOException e) {
            e.printStackTrace();
        }        
        
        deleteFolder(tmpdir.toFile());
        tmpdir.toFile().mkdirs();

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

    private void checkConfig(Path sourceFolderPath) throws IOException {
        if (!sourceFolderPath.resolve("config").toFile().isDirectory()) {
            throw new RuntimeException("Missing config directory.");
        }
        File configFile = sourceFolderPath.resolve("config/config.ini").toFile();
        if (!sourceFolderPath.resolve("config/config.ini").toFile().isFile()) {
            throw new RuntimeException("Missing config.ini file.");
        }
        FileInputStream configInput = new FileInputStream(configFile);
        Properties config = new Properties();
        config.load(configInput);
        if (!config.containsKey("title")) {
            throw new RuntimeException("Missing title property in config.ini.");
        }
        if (!config.containsKey("min_players")) {
            throw new RuntimeException("Missing min_players property in config.ini.");
        }
        if (!config.containsKey("max_players")) {
            throw new RuntimeException("Missing max_players property in config.ini.");
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

    private void serveHTTP(List<Path> path) {
        System.out.println("http://localhost:" + port + "/test.html");

        MultipleResourceSupplier mrs = new MultipleResourceSupplier();
        for (Path p : path) {
            mrs.addDirectory(p.toFile());
            System.out.println("Exposed web server dir: " + p.toString());
        }

        Undertow server = Undertow.builder()
            .addHttpListener(port, "localhost")
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

                                            checkConfig(sourceFolderPath);
                                            byte[] data = Files.readAllBytes(exportSourceCode(sourceFolderPath, zipPath));
                                            exchange.getResponseSender().send(ByteBuffer.wrap(data));

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

                                            exchange.getQueryParameters().forEach(
                                                (k, v) -> {
                                                    config.put(k, v.stream().collect(Collectors.joining(",")));
                                                }
                                            );

                                            config.store(configOutput, null);
                                            exchange.setStatusCode(StatusCodes.FOUND);
                                            exchange.getResponseHeaders().put(Headers.LOCATION, "/export.html");
                                            exchange.endExchange();
                                        } else if (exchange.getRelativePath().equals("/save-replay")) {
                                            Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");
                                            File demoFile = sourceFolderPath.resolve("src/main/resources/view/demo.js").toFile();
                                            File gameFile = tmpdir.resolve("game.json").toFile();

                                            try (PrintWriter out = new PrintWriter(demoFile)) {
                                                out.println("export const demo = ");

                                                List<String> lines = Files.readAllLines(gameFile.toPath());
                                                for (String line : lines) {
                                                    out.println(line);
                                                }
                                                out.println(";");
                                            }

                                            exchange.setStatusCode(StatusCodes.OK);
                                            exchange.endExchange();
                                        }
                                    } catch (Exception e) {
                                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                                        exchange.getResponseSender().send(e.getMessage());
                                    }
                                }
                            }
                        )
                )
            )
            .build();
        server.start();
    }

    public void render(int playerCount, String jsonResult) {
        List<Path> paths = generateView(jsonResult, null);
        serveHTTP(paths);
    }
}
