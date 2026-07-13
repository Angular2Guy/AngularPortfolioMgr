package ch.xxx.manager.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SecSubmissionDto {
    private FilingsDto filingsDto;

    public FilingsDto getFilings() { return filingsDto; }
    public void setFilings(FilingsDto filingsDto) { this.filingsDto = filingsDto; }

    public static class FilingsDto {
        private RecentDto recentDto;

        public RecentDto getRecent() { return recentDto; }
        public void setRecent(RecentDto recentDto) { this.recentDto = recentDto; }
    }

    public static class RecentDto {
        private List<String> form;

        @JsonProperty("accessionNumber")
        private List<String> accessionNumber;

        @JsonProperty("primaryDocument")
        private List<String> primaryDocument;

        // Getter und Setter
        public List<String> getForm() { return form; }
        public void setForm(List<String> form) { this.form = form; }
        public List<String> getAccessionNumber() { return accessionNumber; }
        public void setAccessionNumber(List<String> accessionNumber) { this.accessionNumber = accessionNumber; }
        public List<String> getPrimaryDocument() { return primaryDocument; }
        public void setPrimaryDocument(List<String> primaryDocument) { this.primaryDocument = primaryDocument; }
    }
}
