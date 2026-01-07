package com.example.mrd.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "securities")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(as = SecurityData.class)
public class SecurityData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cusip;
    private String isin;
    private String cins;
    @JsonProperty("issuer_code")
    private String issuerCode;
    @JsonProperty("issue_date")
    private LocalDate issueDate;
    private String ticker;
    private String currency;
    private String country;

    @Column(length = 1000)
    private String securityDesc;
    private String securityType;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    public SecurityData() {
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCusip() {
        return cusip;
    }

    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getCins() {
        return cins;
    }

    public void setCins(String cins) {
        this.cins = cins;
    }

    public String getIssuerCode() {
        return issuerCode;
    }

    public void setIssuerCode(String issuerCode) {
        this.issuerCode = issuerCode;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSecurityDesc() {
        return securityDesc;
    }

    public void setSecurityDesc(String securityDesc) {
        this.securityDesc = securityDesc;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return "SecurityData{" +
                "id=" + id +
                ", cusip='" + cusip + '\'' +
                ", isin='" + isin + '\'' +
                ", cins='" + cins + '\'' +
                ", issuerCode='" + issuerCode + '\'' +
                ", issueDate=" + issueDate +
                ", ticker='" + ticker + '\'' +
                ", currency='" + currency + '\'' +
                ", country='" + country + '\'' +
                ", securityDesc='" + securityDesc + '\'' +
                ", securityType='" + securityType + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
