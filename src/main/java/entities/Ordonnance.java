package entities;

import java.util.Objects;

public class Ordonnance {
    // Properties
    private Integer id;
    private String description;
    private String signature;
    private Consultation consultation;

    // Constructors
    public Ordonnance() {
    }

    public Ordonnance(String description, String signature, Consultation consultation) {
        this.description = description;
        this.signature = signature;
        this.consultation = consultation;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public Ordonnance setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public Ordonnance setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public Ordonnance setConsultation(Consultation consultation) {
        this.consultation = consultation;
        return this;
    }

    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ordonnance that = (Ordonnance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", signature='" + signature + '\'' +
                ", consultation=" + (consultation != null ? consultation.getId() : null) +
                '}';
    }
}
