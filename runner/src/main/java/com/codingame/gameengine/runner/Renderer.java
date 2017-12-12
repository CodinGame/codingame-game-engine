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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import fi.iki.elonen.SimpleWebServer;

public class Renderer {

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

    private void exportViewToWorkingDir(String sourceFolder, Path targetFolder) throws IOException {
        Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources(sourceFolder);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            if ((url != null) && url.getProtocol().equals("jar")) {
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                ZipFile jar = jarConnection.getJarFile();
                Enumeration<? extends ZipEntry> entries = jar.entries(); // gives ALL entries in jar

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
            } else if ((url != null) && url.getProtocol().equals("file")) {
                try {
                    FileUtils.copyDirectory(new File(url.toURI()), targetFolder.toFile());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Cannot copy files", e);
                }
            }
        }
    }

    private Path generateView(int playerCount, String jsonResult) {
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
            exportViewToWorkingDir("view", tmpdir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy resources", e);
        }

        return tmpdir;
    }

    private void serveHTTP(Path path) {
        System.out.println("http://localhost:" + port + "/test.html");
        System.out.println("Web server dir 1 : " + path.toAbsolutePath().toString());
        SimpleWebServer.main(("--quiet --port " + port + " --dir " + path.toAbsolutePath()).split(" "));
    }

    public void render(int playerCount, String jsonResult) {
        Path p = generateView(playerCount, jsonResult);
        serveHTTP(p);
    }
}
