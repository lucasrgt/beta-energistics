package betaenergistics.storage;

/**
 * Visual state of a storage, used for rendering (LED colors on disk drives, etc.)
 */
public enum BE_StorageState {
    INACTIVE,       // network offline
    EMPTY,          // no items stored
    NORMAL,         // has items, has space
    NEAR_CAPACITY,  // >75% full
    FULL            // no space left
}
