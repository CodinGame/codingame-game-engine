package com.codingame.gameengine.runner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;

import fi.iki.elonen.SimpleWebServer;

class Renderer {

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
                Files.find(origAssetsPath, 100, (p, bfa) -> bfa.isRegularFile()).forEach(f -> {
                    try {
                        if (assetsPath != null) {
                            HashCode hash = com.google.common.io.Files.asByteSource(new File(f.toUri()))
                                    .hash(Hashing.sha256());
                            String newName = hash.toString() + "."
                                    + FilenameUtils.getExtension(f.getFileName().toString());
    
                            images.addProperty(origAssetsPath.relativize(f).toString(), newName);
                            Files.copy(f, tmpdir.resolve("hashed_assets").resolve(newName),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            images.addProperty(origAssetsPath.relativize(f).toString(), tmpdir.relativize(f).toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
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

        return paths;
    }

    private void serveHTTP(List<Path> path) {
        System.out.println("http://localhost:" + port + "/test.html");

        StringBuilder sb = new StringBuilder();
        for (Path p : path) {
            sb.append(" --dir ").append(p.toAbsolutePath());
            System.out.println("Exposed web server dir: " + p.toString());
        }

        String commandLine = String.format("--quiet --port %d %s", port, sb.toString());

        SimpleWebServer.main(commandLine.split(" "));
    }

    public void render(int playerCount, String jsonResult) {
        List<Path> paths = generateView(jsonResult, null);
        serveHTTP(paths);
    }
}
