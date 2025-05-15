package enums;

public enum Specialite {
    GENERALISTE("GENERALISTE"),
    CARDIOLOGUE("CARDIOLOGUE"),
    DERMATOLOGIE("DERMATOLOGUE"),
    PEDIATRIE("PEDIATRE"),
    RADIOLOGUE("RADIOLOGUE"),
    GYNECOLOGUE("Gynécologie"),
    OPHTALMOLOGUE("Ophtalmologie"),
    ORL("Oto-Rhino-Laryngologie (ORL)"),
    NEUROLOGUE("Neurologie"),
    PSYCHIATRE("Psychiatrie"),
    UROLOGUE("Urologie"),
    GASTROENTEROLOGUE("Gastroentérologie"),
    PNEUMOLOGUE("Pneumologie"),
    RHUMATOLOGUE("Rhumatologie"),
    ENDOCRINOLOGUE("Endocrinologie"),
    NEPHROLOGUE("Néphrologie"),
    CHIRURGIEN("Chirurgie Générale"),
    CHIRURGIEN_CARDIAQUE("Chirurgie Cardiaque"),
    CHIRURGIEN_ORTHOPEDIQUE("Chirurgie Orthopédique"),
    CHIRURGIEN_PLASTIQUE("Chirurgie Plastique"),
    CHIRURGIEN_NEUROLOGIQUE("Neurochirurgie"),
    CHIRURGIEN_DENTAIRE("Chirurgie Dentaire"),
    MEDECINE_INTERNE("Médecine Interne"),
    MEDECINE_NUCLEAIRE("Médecine Nucléaire"),
    ANESTHESISTE("Anesthésiologie"),
    IMMUNOLOGUE("Immunologie"),
    MEDECIN_SPORT("Médecine du Sport"),
    MEDECIN_TRAVAIL("Médecine du Travail"),
    MEDECINE_LEGALE("Médecine Légale"),
    NONE("NONE");

    private final String value;

    Specialite(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String[] getValues() {
        Specialite[] specialites = values();
        String[] values = new String[specialites.length];
        for (int i = 0; i < specialites.length; i++) {
            values[i] = specialites[i].getValue();
        }
        return values;
    }
}