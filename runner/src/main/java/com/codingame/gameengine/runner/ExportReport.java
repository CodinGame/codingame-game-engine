package com.codingame.gameengine.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum ExportStatus {
    SUCCESS, FAIL
}

class ExportReport {
    private List<ReportItem> reportItems = new ArrayList<>();
    private ExportStatus exportStatus = ExportStatus.SUCCESS;
    private String dataUrl;
    private Map<String, String> stubs = new HashMap<>();

    public List<ReportItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(List<ReportItem> reportItems) {
        this.reportItems = reportItems;
    }

    public ExportStatus getExportStatus() {
        return exportStatus;
    }

    public void setExportStatus(ExportStatus exportStatus) {
        this.exportStatus = exportStatus;
    }

    public void addItem(ReportItemType type, String message) {
        addItem(type, message, null);
    }

    public void addItem(ReportItemType type, String message, String link) {
        reportItems.add(new ReportItem(type, message, link));
        if (type == ReportItemType.ERROR || type == ReportItemType.MISSING_MANDATORY_FILE) {
            exportStatus = ExportStatus.FAIL;
        }
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public Map<String, String> getStubs() {
        return stubs;
    }

    public void setStubs(Map<String, String> stubs) {
        this.stubs = stubs;
    }
}