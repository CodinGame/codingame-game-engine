package com.codingame.gameengine.runner;

enum ReportItemType {
    ERROR, WARNING, MISSING_MANDATORY_FILE, INFO
}

class ReportItem {
    private String message;
    private ReportItemType type;
    private String link;

    public ReportItem(ReportItemType type, String message) {
        super();
        this.message = message;
        this.type = type;
    }

    public ReportItem(ReportItemType type, String message, String link) {
        this(type, message);
        this.link = link;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ReportItemType getType() {
        return type;
    }

    public void setType(ReportItemType type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}