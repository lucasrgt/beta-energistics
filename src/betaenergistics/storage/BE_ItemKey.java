package betaenergistics.storage;

/**
 * Immutable key representing a unique item type in the storage system.
 * Two items with the same ID and damage value are considered identical.
 */
public class BE_ItemKey {
    public final int itemId;
    public final int damageValue;

    public BE_ItemKey(int itemId, int damageValue) {
        this.itemId = itemId;
        this.damageValue = damageValue;
    }

    public BE_ItemKey(int itemId) {
        this(itemId, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BE_ItemKey)) return false;
        BE_ItemKey other = (BE_ItemKey) o;
        return itemId == other.itemId && damageValue == other.damageValue;
    }

    @Override
    public int hashCode() {
        return itemId * 31 + damageValue;
    }

    @Override
    public String toString() {
        return "ItemKey(" + itemId + ":" + damageValue + ")";
    }
}
