public class VectorStore {
    private final MemoryArena arena;
    private static final int LENGTH_OFFSET = 0;
    private static final int CAPACITY_OFFSET = 4;
    private static final int DATA_PTR_OFFSET = 8;
    private static final int HEADER_SIZE = 12;
    private static final int ELEMENT_SIZE = 4;
    private static final double GROWTH_FACTOR = 1.5;

    public VectorStore(MemoryArena arena) {
        this.arena = arena;
    }

    public int createVector(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative");
        }
        
        int vectorAddr = arena.alloc(HEADER_SIZE);
        arena.putInt(vectorAddr + LENGTH_OFFSET, 0);
        arena.putInt(vectorAddr + CAPACITY_OFFSET, initialCapacity);
        
        int dataAddr = -1;
        if (initialCapacity > 0) {
            dataAddr = arena.alloc(initialCapacity * ELEMENT_SIZE);
        }
        arena.putInt(vectorAddr + DATA_PTR_OFFSET, dataAddr);
        
        return vectorAddr;
    }

    public int getLength(int vectorAddr) {
        checkVectorPtr(vectorAddr);
        return arena.getInt(vectorAddr + LENGTH_OFFSET);
    }

    public int getCapacity(int vectorAddr) {
        checkVectorPtr(vectorAddr);
        return arena.getInt(vectorAddr + CAPACITY_OFFSET);
    }

    public int getDataPtr(int vectorAddr) {
        checkVectorPtr(vectorAddr);
        return arena.getInt(vectorAddr + DATA_PTR_OFFSET);
    }

    public void append(int vectorAddr, int value) {
        checkVectorPtr(vectorAddr);
        int length = getLength(vectorAddr);
        int capacity = getCapacity(vectorAddr);
        
        if (length >= capacity) {
            grow(vectorAddr);
            capacity = getCapacity(vectorAddr);
        }
        
        int dataPtr = getDataPtr(vectorAddr);
        if (dataPtr == -1) {
            throw new RuntimeException("Vector data pointer is null");
        }
        
        int elementAddr = dataPtr + (length * ELEMENT_SIZE);
        arena.putInt(elementAddr, value);
        
        arena.putInt(vectorAddr + LENGTH_OFFSET, length + 1);
    }

    public int get(int vectorAddr, int index) {
        checkVectorPtr(vectorAddr);
        int length = getLength(vectorAddr);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "Index " + index + " out of bounds for vector of length " + length
            );
        }
        
        int dataPtr = getDataPtr(vectorAddr);
        if (dataPtr == -1) {
            throw new RuntimeException("Vector data pointer is null");
        }
        
        int elementAddr = dataPtr + (index * ELEMENT_SIZE);
        return arena.getInt(elementAddr);
    }

    public void set(int vectorAddr, int index, int value) {
        checkVectorPtr(vectorAddr);
        int length = getLength(vectorAddr);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "Index " + index + " out of bounds for vector of length " + length
            );
        }
        
        int dataPtr = getDataPtr(vectorAddr);
        if (dataPtr == -1) {
            throw new RuntimeException("Vector data pointer is null");
        }
        
        int elementAddr = dataPtr + (index * ELEMENT_SIZE);
        arena.putInt(elementAddr, value);
    }

    private void grow(int vectorAddr) {
        int oldCapacity = getCapacity(vectorAddr);
        int newCapacity = oldCapacity == 0 ? 1 : (int)(oldCapacity * GROWTH_FACTOR);
        if (newCapacity == oldCapacity) {
            newCapacity = oldCapacity + 1;
        }
        
        int oldDataPtr = getDataPtr(vectorAddr);
        int oldLength = getLength(vectorAddr);
        
        int newDataPtr = arena.alloc(newCapacity * ELEMENT_SIZE);
        
        if (oldDataPtr != -1 && oldLength > 0) {
            for (int i = 0; i < oldLength; i++) {
                int oldElementAddr = oldDataPtr + (i * ELEMENT_SIZE);
                int value = arena.getInt(oldElementAddr);
                int newElementAddr = newDataPtr + (i * ELEMENT_SIZE);
                arena.putInt(newElementAddr, value);
            }
        }
        
        arena.putInt(vectorAddr + CAPACITY_OFFSET, newCapacity);
        arena.putInt(vectorAddr + DATA_PTR_OFFSET, newDataPtr);
    }

    public void printVector(int vectorAddr) {
        checkVectorPtr(vectorAddr);
        int length = getLength(vectorAddr);
        System.out.print("[");
        for (int i = 0; i < length; i++) {
            System.out.print(get(vectorAddr, i));
            if (i < length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("] (capacity: " + getCapacity(vectorAddr) + ")");
    }

    private void checkVectorPtr(int ptr) {
        if (ptr < 0) {
            throw new InvalidPointerException(ptr, HEADER_SIZE, arena.used(), arena.capacity());
        }
        if (ptr + HEADER_SIZE > arena.used()) {
            throw new InvalidPointerException(ptr, HEADER_SIZE, arena.used(), arena.capacity());
        }
    }
}

