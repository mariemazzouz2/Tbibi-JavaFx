package entities;

public enum TypeConsultation {
    PHYSIQUE("Physique"),
    VIRTUELLE("Virtuelle");

    private final String displayName;

    TypeConsultation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return name();
    }

    public static TypeConsultation fromString(String text) {
        if (text != null) {
            String normalized = text.trim().toUpperCase();
            for (TypeConsultation type : TypeConsultation.values()) {
                if (normalized.equals(type.name()) || 
                    normalized.equals(type.getDisplayName().toUpperCase())) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("No TypeConsultation constant for value: " + text);
    }
}
