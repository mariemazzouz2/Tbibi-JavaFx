package entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Consultation {
    // Constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    // Properties
    private Integer id;
    private TypeConsultation type;
    private String status = STATUS_PENDING;
    private String commentaire;
    private LocalDateTime dateC;
    private String meetLink;
    private Utilisateur medecin;
    private Utilisateur patient;
    private Ordonnance ordonnance;

    // Constructors
    public Consultation() {
    }

    public Consultation(TypeConsultation type, String commentaire, LocalDateTime dateC,
                        Utilisateur medecin, Utilisateur patient) {
        this.type = type;
        this.commentaire = commentaire;
        this.dateC = dateC;
        this.medecin = medecin;
        this.patient = patient;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TypeConsultation getType() {
        return type;
    }

    public Consultation setType(TypeConsultation type) {
        this.type = type;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Consultation setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Consultation setCommentaire(String commentaire) {
        this.commentaire = commentaire;
        return this;
    }

    public LocalDateTime getDateC() {
        return dateC;
    }

    public Consultation setDateC(LocalDateTime dateC) {
        this.dateC = dateC;
        return this;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public Consultation setMeetLink(String meetLink) {
        this.meetLink = meetLink;
        return this;
    }

    public Utilisateur getMedecin() {
        return medecin;
    }

    public Consultation setMedecin(Utilisateur medecin) {
        this.medecin = medecin;
        return this;
    }

    public Utilisateur getPatient() {
        return patient;
    }

    public Consultation setPatient(Utilisateur patient) {
        this.patient = patient;
        return this;
    }

    public Ordonnance getOrdonnance() {
        return ordonnance;
    }

    public Consultation setOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        return this;
    }

    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consultation that = (Consultation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", type=" + type +
                ", status='" + status + '\'' +
                ", dateC=" + dateC +
                ", patient=" + (patient != null ? patient.getId() : null) +
                ", medecin=" + (medecin != null ? medecin.getId() : null) +
                '}';
    }
}