/* 
 * ArticleTable.java 
 * This is a class for storing Articles in a hash table implemented 
 * using separate chaining.
 */
    
public class ArticleTable {
    
    private int M = 2521;            // size of table - initially set to large prime close to 2500
    private Node[] AT = new Node[M]; // hash table
    private Node pointer;            // for iterator: pointer to current Node
    private int counter;             // keep track of how many Nodes iterator has traversed
    private int row;                 // keep track of what row in table iterator is on
    
    public void initialize(Article[] A) {
        for(int i = 0; i < A.length; ++i) 
            insert(A[i]); 
    }
    
    // insert article into hash table using title as hash key
    // assumed that duplicate entries are not allowed
    public void insert(Article a) {
        if(member(a))
            return;
        int loc = hash(a.getTitle());
        AT[loc] = insertHelper(AT[loc], a);
    } 
    
    
    // reconstructs LL with a added to the end
    private static Node insertHelper(Node p, Article a) {
        if(p == null)
            return new Node(a);
        else
            p.next = insertHelper(p.next, a);
        
        return p;
    }     
    
    // delete article corresponding to title from table
    public void delete(String title) {
        int loc = hash(title);
        AT[loc] = deleteHelper(AT[loc], title);     
    }
        
    // Recursively reconstructs LL without article with title s
    private static Node deleteHelper(Node p, String s) {
        if(p == null)
            return p;
        else if(p.data.getTitle().compareTo(s) == 0)   //found article, cut it off
            return p.next;
        else
            p.next = deleteHelper(p.next, s);
        
        return p;
    }
    
    // is a in the table?
    public boolean member(Article a) {
        return member(a.getTitle());
    }
    
    public boolean member(String title) {
        return (lookup(title) != null); 
    }
    
    // returns article corresponding to given title, or null if not found
    public Article lookup(String title) {
        int loc = hash(title);
        Node n = lookup(AT[loc], title); 
        if(n != null)
            return n.data; 
        return null; 
    }
    
    // returns node corresponding to given key
    private static Node lookup(Node t, String key) {
        if (t == null)
            return null;
        else if (key.compareTo(t.data.getTitle()) == 0) {
            return t;
        } else 
            return lookup(t.next, key); 
    }
    
    // counts number of articles in table
    public int size() {
        int count = 0;
        for(int i = 0; i < AT.length; i++) {
            count += length(AT[i]);
        }
        return count;
    }
    
    // Iterator Methods
    
    // sets pointer to first Node in table
    public void reset() {
        if(size() == 0) {
            System.out.println("table is empty! setting pointer to null...");
            pointer = null;
            return;
        }
        
        for(int i = 0; i < AT.length; i++) {
            if(AT[i] != null) {               // find first non-empty bucket
                pointer = AT[i];
                row = i;
                break;
            }
        }
        
        counter = 1;
    }

    // are there any more articles?
    public boolean hasNext() {
        return pointer != null && counter <= size();         // defensive coding: pointer should be null and counter = size + 1
    }                                                        // when all articles have been traversed
    
    // saves and returns current article, and moves pointer to next in table (or null if at end)
    // should always be used with hasNext for checking
    public Article next() {
        Article temp = pointer.data;
        
        if(pointer.next != null || counter == size())         // if not at end of bucket, point to next item in list
            pointer = pointer.next;                           // or if pointing to last item in table (counter == size), set pointer to null
        else {
            for(int i = row + 1; i < AT.length; i++) {        // else check for next non-empty bucket 
                if(AT[i] != null) {
                    pointer = AT[i];                          // update pointer and row accordingly
                    row = i;
                    break;
                }
            }
        }
        
        counter++;                                            // increment counter and return saved article
        return temp;
    }


    /*
     * Helper Methods
     */
    
    // simple hash function using a relatively large prime and title of article
    private int hash(String title) {
        int h = 0;
        for(int i = 0; i < title.length(); i++)
            h += (1871 * title.charAt(i));
        return h % M;      
    }
    
    // returns length of LL
    private static int length(Node t) {
        if(t == null)
            return 0;
        else
            return 1 + length(t.next); 
    }
    
    // for debugging, prints size of table, min, max, and mean length of buckets and st dev.
    private void printStats() {
        System.out.println("AT (length " + AT.length + "):");
        System.out.println("size of table: " + size());
        
        int min = length(AT[0]);
        int max = min;
        double total = min;
        for(int i = 1; i < AT.length; i++) {
            int temp = length(AT[i]);
            if(temp > max)
                max = temp;
            if(temp < min)
                min = temp;
            total += temp;
        }
        
        double mean = total/AT.length;
        
        System.out.println("min length bucket: " + min);
        System.out.println("max length bucket: " + max);
        System.out.println("mean bucket length: " + mean);
        
        total = 0;
        for(int i = 0; i < AT.length; i++)
            total += Math.pow(length(AT[i]) - mean, 2);
        
        System.out.println("st dev: " + Math.sqrt(total/AT.length));
    }
    
    // simple unit test
    public static void main(String[] args) {
        String dbPath = "articles/";
        
        DatabaseIterator db = new DatabaseIterator(dbPath);
        int numArticles = db.getNumArticles();
        
        System.out.println("Read " + numArticles + " articles from disk.");
        
        ArticleTable T = new ArticleTable(); 
        
        // create array - taken from getArticleList in Minipedia.java
        Article[] A = new Article[numArticles];
        for(int i = 0; i < numArticles; ++i)
            A[i] = db.next();
        
        T.initialize(A);
        T.printStats();
        System.out.println();
        
        System.out.println("testing iterator.... counts number of articles and prints title of every 100th article");
        T.reset();
        int count = 0;
        while(T.hasNext()) {
            Article a = T.next();
            if(T.counter % 100 == 0)
                System.out.println(a.getTitle());
            count++;
        }
        System.out.println();
        System.out.println("After iterating through, count should be\n" + T.size());
        System.out.println(count);
        System.out.println();
        
        Article a = A[77];
        System.out.println("Testing member for \"" + a.getTitle() + "\"... should be\ntrue");
        System.out.println(T.member(a));
        System.out.println();
        
        System.out.println("Trying to find 'Singapore' in table...");
        System.out.println(T.lookup("Singapore"));
        System.out.println();
        
        System.out.println("Trying to delete 'Malaysia'...");
        System.out.println("\tis 'Malaysia' in table?");
        System.out.println("\t" + T.member("Malaysia"));
        a = T.lookup("Malaysia");
        System.out.println("Deleting...");
        T.delete("Malaysia");
        System.out.println("\tcheck: is 'Malaysia' in table?");
        System.out.println("\t" + T.member("Malaysia"));
        System.out.println();
        
        System.out.println("Inserting 'Malaysia' back into relevance...");
        T.insert(a);
        System.out.println("Now lookup and print:");
        System.out.println(T.lookup("Malaysia"));
        
        System.out.println("Trying to lookup and print 'Tim Lim'...");
        System.out.println(T.lookup("Tim Lim"));
        System.out.println();
        System.out.println("Trying to delete 'Tim Lim'... nothing should happen");
        T.delete("Tim Lim");
        System.out.println();
        
        System.out.println("Trying to insert 'Malaysia' again... nothing should happen");
        T.insert(a);
        System.out.println();
        
        
        System.out.println("checking table stats... (should be identical to original stats above)");
        T.printStats();
            
    }
    
    
    // inner node class for LL of Articles
    private static class Node {
        public Article data;
        public Node next;
        
        public Node(Article data, Node n) {
            this.data = data;
            this.next = n;
        }
        
        public Node(Article data) {
            this(data, null);
        }
        
        public String toString() {
            if(this.data == null)
                return null;
            return this.data.getTitle() + " -> " + this.next;
        }
    }
}