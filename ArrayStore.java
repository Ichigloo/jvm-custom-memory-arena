public class ArrayStore {
    private final MemoryArena arena;
    private static final int LENGTH_OFFSET = 0;
    private static final int DATA_OFFSET = 4;

    public ArrayStore(MemoryArena arena) {
        this.arena = arena;
    }

    public int createArray(int length, int elementSize) {
        if (length < 0) {
            throw new IllegalArgumentException("Array length cannot be negative");
        }
        if (elementSize <= 0) {
            throw new IllegalArgumentException("Element size must be positive");
        }
        
        int totalSize = DATA_OFFSET + (length * elementSize);
        int arrayAddr = arena.alloc(totalSize);
        
        arena.putInt(arrayAddr + LENGTH_OFFSET, length);
        
        return arrayAddr;
    }

    public int getLength(int arrayAddr) {
        checkArrayPtr(arrayAddr);
        return arena.getInt(arrayAddr + LENGTH_OFFSET);
    }

    public int getElementSize(int arrayAddr) {
        checkArrayPtr(arrayAddr);
        int length = getLength(arrayAddr);
        int totalSize = arena.used() - arrayAddr;
        int headerSize = DATA_OFFSET;
        int dataSize = totalSize - headerSize;
        
        if (length == 0) {
            throw new RuntimeException("Cannot determine element size for empty array");
        }
        return dataSize / length;
    }

    public int getInt(int arrayAddr, int index) {
        checkArrayPtr(arrayAddr);
        int length = getLength(arrayAddr);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "Index " + index + " out of bounds for array of length " + length
            );
        }
        int elementAddr = arrayAddr + DATA_OFFSET + (index * 4);
        return arena.getInt(elementAddr);
    }

    public void setInt(int arrayAddr, int index, int value) {
        checkArrayPtr(arrayAddr);
        int length = getLength(arrayAddr);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "Index " + index + " out of bounds for array of length " + length
            );
        }
        int elementAddr = arrayAddr + DATA_OFFSET + (index * 4);
        arena.putInt(elementAddr, value);
    }

    public void printArray(int arrayAddr) {
        checkArrayPtr(arrayAddr);
        int length = getLength(arrayAddr);
        System.out.print("[");
        for (int i = 0; i < length; i++) {
            System.out.print(getInt(arrayAddr, i));
            if (i < length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    private void checkArrayPtr(int ptr) {
        if (ptr < 0) {
            throw new InvalidPointerException(ptr, 0, arena.used(), arena.capacity());
        }
        if (ptr + DATA_OFFSET > arena.used()) {
            throw new InvalidPointerException(ptr, DATA_OFFSET, arena.used(), arena.capacity());
        }
    }
}

