package com.codingame.gameengine.runner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.SimpleWebServer;

class Renderer {

    private int port = 8080;

    public Renderer(int port) {
        this.port = port;
    }

    private void deleteFolder(File folder) {
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

    private Set<Path> exportViewToWorkingDir(String sourceFolder, Path targetFolder) throws IOException {
        Set<Path> exportedPaths = new HashSet<>();
        exportedPaths.add(targetFolder);
        
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
                    String resourcesClassesFolder = "/src/main/resources/".replace('/', File.separatorChar) + sourceFolder;
                    
                    String targetPath = new File(url.toURI()).getAbsolutePath();
                    targetPath = targetPath.replace(targetClassesFolder, resourcesClassesFolder);
                    
                    exportedPaths.add(new File(targetPath).toPath());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Cannot copy files", e);
                }
            }
        }
        
        return exportedPaths;
    }

    private Set<Path> generateView(int playerCount, String jsonResult) {
        Set<Path> paths;
        
        Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");
        deleteFolder(tmpdir.toFile());
        tmpdir.toFile().mkdirs();

        File game = tmpdir.resolve("game.json").toFile();
        try (PrintWriter out = new PrintWriter(game)) {
            out.println(jsonResult);
        } catch (IOException e) {
            throw new RuntimeException("Cannot generate the game file", e);
        }

        try {
            paths = exportViewToWorkingDir("view", tmpdir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy resources", e);
        }
        
        if(paths.size() == 0) {
            throw new RuntimeException("No resources folder found");
        }

        return paths;
    }

    private void serveHTTP(Set<Path> path) {
        System.out.println("http://localhost:" + port + "/test.html");

        StringBuilder sb = new StringBuilder();
        for(Path p : path) {
            sb.append(" --dir ").append(p.toAbsolutePath());
            System.out.println("Exposed web server dir: " + p.toString());
        }
        
        String commandLine = String.format("--quiet --port %d %s", port, sb.toString());
        
        SimpleWebServer.main(commandLine.split(" "));
    }

    public void render(int playerCount, String jsonResult) {
        Set<Path> paths = generateView(playerCount, jsonResult);
        serveHTTP(paths);
    }
}
