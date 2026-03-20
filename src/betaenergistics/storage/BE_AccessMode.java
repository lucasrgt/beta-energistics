package betaenergistics.storage;

public enum BE_AccessMode {
    INSERT_EXTRACT,
    INSERT_ONLY,
    EXTRACT_ONLY;

    public boolean allowsInsert() {
        return this != EXTRACT_ONLY;
    }

    public boolean allowsExtract() {
        return this != INSERT_ONLY;
    }
}
