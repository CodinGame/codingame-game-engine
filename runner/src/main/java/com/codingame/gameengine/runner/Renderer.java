package com.codingame.gameengine.runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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

    private Path generateView(int playerCount, String jsonResult) {
        Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");
        deleteFolder(tmpdir.toFile());
        tmpdir.toFile().mkdirs();

        File temp = tmpdir.resolve("test.html").toFile();

        try (PrintWriter writer = new PrintWriter(new FileWriter(temp))) {
            FileUtils.copyDirectory(new File(GameRunner.class.getResource("/view").getFile()), tmpdir.toFile());

            File[] listFiles = tmpdir.resolve("js").toFile().listFiles();
            Arrays.sort(listFiles);
            String scripts = Stream.of(listFiles)
                    .map(file -> "<script type='module' src='js/" + file.getName() + "'></script>")
                    .collect(Collectors.joining("\n"));
            for (String f : IOUtils.readLines(GameRunner.class.getResourceAsStream("/view/test_tpl.html"))) {
                f = f.replace("[DATA]", jsonResult);
                f = f.replace("[PLAYERCOUNT]", String.valueOf(playerCount));
                f = f.replace("[VIEWERJS]", scripts);
                writer.println(f);
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot generate the html file", e);
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
