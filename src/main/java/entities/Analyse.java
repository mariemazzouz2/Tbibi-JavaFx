// src/main/java/entities/Analyse.java
package entities;

import java.time.LocalDate;

public class Analyse {
    private int id;
    private Integer dossierId;
    private String type;
    private LocalDate dateAnalyse;
    private String donneesAnalyse;
    private String diagnostic;

    public Analyse() {}

    public Analyse(int id, Integer dossierId, String type, LocalDate dateAnalyse, String donneesAnalyse, String diagnostic) {
        this.id = id;
        this.dossierId = dossierId;
        this.type = type;
        this.dateAnalyse = dateAnalyse;
        this.donneesAnalyse = donneesAnalyse;
        this.diagnostic = diagnostic;
    }

    public Analyse(Integer dossierId, String type, LocalDate dateAnalyse, String donneesAnalyse, String diagnostic) {
        this.dossierId = dossierId;
        this.type = type;
        this.dateAnalyse = dateAnalyse;
        this.donneesAnalyse = donneesAnalyse;
        this.diagnostic = diagnostic;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getDossierId() { return dossierId; }
    public void setDossierId(Integer dossierId) { this.dossierId = dossierId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDateAnalyse() { return dateAnalyse; }
    public void setDateAnalyse(LocalDate dateAnalyse) { this.dateAnalyse = dateAnalyse; }
    public String getDonneesAnalyse() { return donneesAnalyse; }
    public void setDonneesAnalyse(String donneesAnalyse) { this.donneesAnalyse = donneesAnalyse; }
    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    @Override
    public String toString() {
        return "Analyse{" +
                "id=" + id +
                ", dossierId=" + dossierId +
                ", type='" + type + '\'' +
                ", dateAnalyse=" + dateAnalyse +
                ", donneesAnalyse='" + donneesAnalyse + '\'' +
                ", diagnostic='" + diagnostic + '\'' +
                '}';
    }
}