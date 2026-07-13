package ch.xxx.manager.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "ownershipDocument")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OwnershipDocumentDto {

    @JsonProperty("issuer")
    private IssuerDto issuerDto;

    @JsonProperty("reportingOwner")
    private ReportingOwnerDto reportingOwnerDto;

    @JsonProperty("nonDerivativeTable")
    private NonDerivativeTableDto nonDerivativeTableDto;

    // Getter und Setter
    public IssuerDto getIssuer() { return issuerDto; }
    public void setIssuer(IssuerDto issuerDto) { this.issuerDto = issuerDto; }
    public ReportingOwnerDto getReportingOwner() { return reportingOwnerDto; }
    public void setReportingOwner(ReportingOwnerDto reportingOwnerDto) { this.reportingOwnerDto = reportingOwnerDto; }
    public NonDerivativeTableDto getNonDerivativeTable() { return nonDerivativeTableDto; }
    public void setNonDerivativeTable(NonDerivativeTableDto nonDerivativeTableDto) { this.nonDerivativeTableDto = nonDerivativeTableDto; }

    // --- Sub-Klassen ---

    public static class IssuerDto {
        @JsonProperty("issuerTicker")
        private String ticker;
        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
    }

    public static class ReportingOwnerDto {
        @JsonProperty("reportingOwnerRelationship")
        private RelationshipDto relationshipDto;
        public RelationshipDto getRelationship() { return relationshipDto; }
        public void setRelationship(RelationshipDto relationshipDto) { this.relationshipDto = relationshipDto; }
    }

    public static class RelationshipDto {
        @JsonProperty("isOfficer")
        private Boolean officer;
        @JsonProperty("isDirector")
        private Boolean director;
        @JsonProperty("officerTitle")
        private String officerTitle;

        // Getter und Setter
        public Boolean getOfficer() { return officer; }
        public void setOfficer(Boolean officer) { this.officer = officer; }
        public Boolean getDirector() { return director; }
        public void setDirector(Boolean director) { this.director = director; }
        public String getOfficerTitle() { return officerTitle; }
        public void setOfficerTitle(String officerTitle) { this.officerTitle = officerTitle; }
    }

    public static class NonDerivativeTableDto {
        @JsonProperty("nonDerivativeTransaction")
        @JacksonXmlElementWrapper(useWrapping = false) // SEC nutzt keine umschließenden Tags für Listen
        private List<TransactionDto> transactionDtos;

        public List<TransactionDto> getTransactions() { return transactionDtos; }
        public void setTransactions(List<TransactionDto> transactionDtos) { this.transactionDtos = transactionDtos; }
    }

    public static class TransactionDto {
        @JsonProperty("transactionCoding")
        private TransactionCodingDto coding;
        @JsonProperty("transactionAmounts")
        private TransactionAmountsDto amounts;

        public TransactionCodingDto getCoding() { return coding; }
        public void setCoding(TransactionCodingDto coding) { this.coding = coding; }
        public TransactionAmountsDto getAmounts() { return amounts; }
        public void setAmounts(TransactionAmountsDto amounts) { this.amounts = amounts; }
    }

    public static class TransactionCodingDto {
        @JsonProperty("transactionCode")
        private String code; // Z.B. 'P' für Kauf, 'S' für Verkauf
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class TransactionAmountsDto {
        @JsonProperty("transactionShares")
        private Integer shares;
        @JsonProperty("transactionPricePerShare")
        private Double pricePerShare;
        @JsonProperty("transactionAcquiredDisposedCode")
        private String acquiredDisposed; // 'A' oder 'D'

        // Getter und Setter
        public Integer getShares() { return shares; }
        public void setShares(Integer shares) { this.shares = shares; }
        public Double getPricePerShare() { return pricePerShare; }
        public void setPricePerShare(Double pricePerShare) { this.pricePerShare = pricePerShare; }
        public String getAcquiredDisposed() { return acquiredDisposed; }
        public void setAcquiredDisposed(String acquiredDisposed) { this.acquiredDisposed = acquiredDisposed; }
    }
}
