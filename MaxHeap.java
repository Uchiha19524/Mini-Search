/* 
 * Maxheap.java
 * 
 * Class for maxheap of Articles and doubles, for use with cosine similarity calculations in MiniGoogle.java.
 */
public class MaxHeap {
    
    private final int SIZE = 10;                          // initial size
    private int next = 0;                                 // limit of elements in array
    private double[] A = new double[SIZE];                // implements tree by storing elements in level order
    private Article[] B = new Article[SIZE];              // cosine sim stored in A and corresponding Article stored in B at same index
    // comparisons made using cosine sim
    
    // standard resize to avoid overflow for both arrays
    private void resize() {
        double[] tempA = new double[A.length*2];
        Article[] tempB = new Article[B.length*2];
        for(int i = 0; i < A.length; ++i) {
            tempA[i] = A[i];
            tempB[i] = B[i];
        }
        A = tempA;
        B = tempB;
    }
    
    // methods to move up and down tree as array     
    private int parent(int i) { return (i-1) / 2; }
    private int lchild(int i) { return 2 * i + 1; }
    private int rchild(int i) { return 2 * i + 2; }
    
    private boolean isLeaf(int i) { return (lchild(i) >= next); }
    private boolean isRoot(int i) { return i == 0; }
    
    // swap for both arrays using indices
    private void swap(int i, int j) {
        double temp = A[i];
        A[i] = A[j];
        A[j] = temp;
        
        Article a = B[i];
        B[i] = B[j];
        B[j] = a;
    }
    
    // basic data structure methods
    
    public boolean isEmpty() {
        return (next == 0);
    }
    
    public int size() {
        return (next);
    }
    
    // insert cosine sim and Article into A and B at next available location
    // and fix any violations of heap property on path up to root
    public void insert(double k, Article a) {
        if(size() == A.length)
            resize();
        
        A[next] = k; 
        B[next] = a;
        
        int i = next;
        int p = parent(i); 
        while(!isRoot(i) && A[i] > A[p]) {
            swap(i,p);
            i = p;
            p = parent(i); 
        }
        
        ++next;
    }
    
    // Remove top (maximum) elements, and replace with last elements in level
    // order; fix any violations of heap property on a path downwards
    // only returns article corresponding to largest cosine similarity value
    public Article getMax() {
        if(isEmpty()) {
            System.out.println("attempted to get max but heap is empty! returning null...");
            return null;
        }
        --next;
        swap(0,next);                // swap root with last element
        int i = 0;                   // i is location of new key as it moves down tree
        
        // while there is a maximum child and element out of order, swap with max child
        int mc = maxChild(i); 
        while(!isLeaf(i) && A[i] < A[mc]) { 
            swap(i,mc);
            i = mc; 
            mc = maxChild(i);
        }
        
        return B[next];
    }
    
    
    // Modified version of getMax, use to return string representation of the max cosine similarity element and its 
    // correponding Article
    // note: also removes top element (use getMax or this, but not both)
    public String getMaxAsString() {
        if(isEmpty()) {
            System.out.println("attempted to get max but heap is empty! returning null...");
            return null;
        }
        --next;
        swap(0,next);                // swap root with last element
        int i = 0;                   // i is location of new key as it moves down tree
        
        // while there is a maximum child and element out of order, swap with max child
        int mc = maxChild(i); 
        while(!isLeaf(i) && A[i] < A[mc]) { 
            swap(i,mc);
            i = mc; 
            mc = maxChild(i);
        }
        
        ///     printHeapAsTree(); 
        
        return "(Cosine similarity: " + A[next] + ")\n\n" + B[next];
    }
    
    // return index of maximum child of i or -1 if i is a leaf node (no children)
    int maxChild(int i) {
        if(lchild(i) >= next)
            return -1;
        if(rchild(i) >= next)
            return lchild(i);
        else if(A[lchild(i)] > A[rchild(i)])
            return lchild(i);
        else
            return rchild(i); 
    }
    
    // debug method
    
    public void printHeap() {
        for(int i = 0; i < A.length; ++i)
            System.out.print(B[i] + " (" + A[i] + ")\n");
        System.out.println("\t next = " + next);
    }
    
    public void printHeapAsTree() {
        printHeapTreeHelper(0, ""); 
        System.out.println(); 
    }
    
    public void printHeapTreeHelper(int i, String indent) {
        if(i < next) {
            printHeapTreeHelper(rchild(i), indent + "   "); 
            System.out.println(indent + B[i] + " (" + A[i] + ")");
            printHeapTreeHelper(lchild(i), indent + "   "); 
        }
    }   
}
