package com.codingame.gameengine.core;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
class OutputData extends LinkedList<String> {
    private OutputCommand command;

    public OutputData(OutputCommand command) {
        this.command = command;
    }

    public boolean add(String s) {
        if (s != null)
            return super.add(s);
        return false;
    }

    public void addAll(String[] data) {
        if (data != null)
            super.addAll(Arrays.asList(data));
    }
    
    public void addAll(List<String> data) {
        if (data != null)
            super.addAll(data);
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        StringBuilder sb = new StringBuilder();
        for (String line : this) {
            sb.append(line).append('\n');
        }
        String content = sb.toString().trim();
        int length = (content.length() > 0) ? content.split("\r\n|\r|\n").length : 0;            

        PrintWriter out = new PrintWriter(writer);
        out.println(this.command.format(length));
        out.print(content);

        return writer.toString().trim();
    }
}