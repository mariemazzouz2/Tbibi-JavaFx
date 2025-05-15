// src/main/java/entities/Prediction.java
package entities;

public class Prediction {
    private int id;
    private int dossierId;
    private boolean hypertension;
    private boolean heart_disease;
    private String smoking_history;
    private float bmi;
    private float hbA1c_level;
    private float blood_glucose_level;
    private boolean diabete;

    public Prediction() {}

    public Prediction(int id, int dossierId, boolean hypertension, boolean heart_disease,
                      String smoking_history, float bmi, float hbA1c_level, float blood_glucose_level) {
        this.id = id;
        this.dossierId = dossierId;
        this.hypertension = hypertension;
        this.heart_disease = heart_disease;
        this.smoking_history = smoking_history;
        this.bmi = bmi;
        this.hbA1c_level = hbA1c_level;
        this.blood_glucose_level = blood_glucose_level;
    }

    public Prediction(int dossierId, boolean hypertension, boolean heart_disease,
                      String smoking_history, float bmi, float hbA1c_level, float blood_glucose_level) {
        this.dossierId = dossierId;
        this.hypertension = hypertension;
        this.heart_disease = heart_disease;
        this.smoking_history = smoking_history;
        this.bmi = bmi;
        this.hbA1c_level = hbA1c_level;
        this.blood_glucose_level = blood_glucose_level;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDossierId() { return dossierId; }
    public void setDossierId(int dossierId) { this.dossierId = dossierId; }

    public boolean isHypertension() { return hypertension; }
    public void setHypertension(boolean hypertension) { this.hypertension = hypertension; }

    public boolean isheart_disease() { return heart_disease; }
    public void setheart_disease(boolean heart_disease) { this.heart_disease = heart_disease; }

    public String getsmoking_history() { return smoking_history; }
    public void setsmoking_history(String smoking_history) { this.smoking_history = smoking_history; }

    public float getBmi() { return bmi; }
    public void setBmi(float bmi) { this.bmi = bmi; }

    public float gethbA1c_level() { return hbA1c_level; }
    public void sethbA1c_level(float hbA1c_level) { this.hbA1c_level = hbA1c_level; }

    public float getBloodGlucoseLevel() { return blood_glucose_level; }
    public void setBloodGlucoseLevel(float blood_glucose_level) { this.blood_glucose_level = blood_glucose_level; }

    public boolean isDiabete() { return diabete; }
    public void setDiabete(boolean diabete) { this.diabete = diabete; }

    @Override
    public String toString() {
        return "Prediction{" +
                "id=" + id +
                ", dossierId=" + dossierId +
                ", hypertension=" + hypertension +
                ", heart_disease=" + heart_disease +
                ", smoking_history='" + smoking_history + '\'' +
                ", bmi=" + bmi +
                ", hbA1c_level=" + hbA1c_level +
                ", blood_glucose_level=" + blood_glucose_level +
                ", diabete=" + diabete +
                '}';
    }
}
