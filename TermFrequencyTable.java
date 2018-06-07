/*
 * TermFrequencyTable.java
 * 
 * This class stores words from two Strings, allowing users
 * to calculate the cosine similarity of the two.
 * Implemented using a separate chaining hash table.
 * Blacklist checking and preprocessing done in this class for 
 * cosine similarity calculations
 */

public class TermFrequencyTable {
    
    private int M = 179;                 // slightly larger prime used to accomodate blacklist
    private Node[] TFT = new Node[M];
    private String[] S1;                   // docNum = 0
    private String[] S2;                   // docNum = 1
    private Node P;            // pointer for iterator
    private int C;             // counter to keep track of how many Nodes iterator has traversed
    private int R;             // keep track of what row in table iterator is on
    
    private final String [] blackList = { "the", "of", "and", "a", "to", "in", "is", 
    "you", "that", "it", "he", "was", "for", "on", "are", "as", "with", 
    "his", "they", "i", "at", "be", "this", "have", "from", "or", "one", 
    "had", "by", "word", "but", "not", "what", "all", "were", "we", "when", 
    "your", "can", "said", "there", "use", "an", "each", "which", "she", 
    "do", "how", "their", "if", "will", "up", "other", "about", "out", "many", 
    "then", "them", "these", "so", "some", "her", "would", "make", "like", 
    "him", "into", "time", "has", "look", "two", "more", "write", "go", "see", 
    "number", "no", "way", "could", "people",  "my", "than", "first", "water", 
    "been", "call", "who", "oil", "its", "now", "find", "long", "down", "day", 
    "did", "get", "come", "made", "may", "part" }; 
    
    public void initialize(String s1, String s2) {
        // start by inserting all the terms from the blacklist (set blacklist flag to true).
        // much more efficient than having to iterate through the the blacklist to check for every single term when
        // calculating cosine similarity.
        insertBlackList();
        
        S1 = preprocess(s1).split("\\s");            // split by whitespace into array of terms
        S2 = preprocess(s2).split("\\s");
        
        for(int i = 0; i < S1.length; i++) {        // insert terms from first string
            insert(S1[i], 0);
        }
        
        for(int i = 0; i < S2.length; i++)          // insert terms from second string
            insert(S2[i], 1);
    }
    
    // create a new string consisting of only letters and whitespace from s and turn to lowercase
    private static String preprocess(String s) {
        String temp = "";
        Character c;
        for(int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if(Character.isWhitespace(c) || Character.isLetter(c))
                temp += c;
        }
        
        temp = temp.toLowerCase();
        
        return temp;
    }
    
    // insert a term from a document docNum (= 0 or 1) into the table; if the term is not already present, add it
    // to the table with a termFreq of 1 for docNum, i.e., if p is the new node added, then p.termFreq[docNum] = 1. 
    // If the term IS already there, just increment the appropriate termFreq value. 
    public void insert(String term, int docNum) {
        // comment this in to trace insertions
        // System.out.println("Inserting '" + term + "' ...");
        
        int loc = hash(term);
        TFT[loc] = insertHelper(TFT[loc], term, docNum);
    }
    
    
    // if r is null, create new Node (constructor sets termFreq[docNum] to 1, termFreq for other doc to 0)
    // else if t is already present, increment termFreq
    private static Node insertHelper(Node r, String t, int docNum) {
        if(r == null)
            return new Node(t, docNum);
        else if(r.term.equals(t)) {
            r.incFreq(docNum);
            return r;
        }
        
        r.next = insertHelper(r.next, t, docNum);
        
        return r;
    }
    
    // private method for inserting terms from blacklist into table when initializing
    private void insertBlackList() {
        int loc;
        for(int i = 0; i < blackList.length; i++) {
            loc = hash(blackList[i]);
            TFT[loc] = blackListHelper(TFT[loc], blackList[i]);
        }
    }
    
    // helper method for insertBlackList, modified from insertHelper as a different Node constructor is used.
    private static Node blackListHelper(Node r, String t) {
        if(r == null)
            return new Node(t, true);
        else if(r.term.equals(t))
            r.inBlackList = true;
        
        r.next = blackListHelper(r.next, t);
        return r;
    }
            
    // return the cosine similarity of the terms for the two documents stored in this table; 
    // checking for whether words are in blacklist done here - if in blacklist, not counted.
    public double cosineSimilarity() {
        double sumProd = 0;     // numerator: sum of products of termFreqs
        // for denominator:
        double sumASquared = 0;  // sum of termFreqs squared for first doc
        double sumBSquared = 0;  // sum of termFreqs square for second doc
        Node temp;
        
        this.reset();
        while(this.hasNext()) {
            temp = this.next();
            if(!temp.inBlackList) { 
                sumProd += (temp.termFreq[0] * temp.termFreq[1]);       // A*B
                sumASquared += (temp.termFreq[0] * temp.termFreq[0]);   // A*A
                sumBSquared += (temp.termFreq[1] * temp.termFreq[1]);   // B*B
            }
        }
        
        // mutiply the square roots of the squared sums, and divide sum of products by it.
        return (sumProd / (Math.sqrt(sumASquared) * Math.sqrt(sumBSquared)));
    }

    // Iterator methods: adapted from methods in ArticleTable.java
    // made private as these are only used within other methods (cosineSimilarity and iterateAll)
    
    // initialize iterator to first element in table
    private void reset() {
        // check if table is empty
        if(size() == 0) {
            System.out.println("table is empty! setting pointer to null...");
            P = null;
            return;
        }
        
        for(int i = 0; i < TFT.length; i++) {   // initialize pointer, counter and row for iterator
            if(TFT[i] != null) {                // find first nonempty bucket
                P = TFT[i];
                C = 1;
                R = i;
                return;
            }
        }
    }

    // are there any more terms?
    private boolean hasNext() {
        return P != null && C <= size();         // pointer should be null and counter = size + 1
    }                                            // when all articles have been traversed
    
    // saves and returns current node, and moves pointer to next in table (or null if at end)
    // should always be used with hasNext for checking
    private Node next() {
        Node temp = P;
        
        if(P.next != null || C == size())         // if not at end of bucket, point to next item in list
            P = P.next;                                 // or if pointing to last item in table (counter == size), set pointer to null
        else {
            for(int i = R + 1; i < TFT.length; i++) {        // else check for next non-empty bucket 
                if(TFT[i] != null) {
                    P = TFT[i];                          // update pointer and row accordingly
                    R = i;
                    break;
                }
            }
        }
        
        C++;                                            // increment counter and return saved term
        return temp;
    }
    
    // just a helper method to iterate through entire table, printing string and respective termFreqs
    // also checks total count to make sure insertions were done right
    private void iterateAll() {
        this.reset();
        Node temp;
        int count = 0;;
        
        System.out.println("Iterating through all elements in array, printing terms and termFreqs - (doc1, doc2)");
        while(this.hasNext()) {
            temp = this.next();
            System.out.print("Term " + (C - 1) + ": " + "'" + temp.term + "' (" + temp.termFreq[0] + ", " + temp.termFreq[1] + ")");
            if(temp.inBlackList)
                System.out.print(" (blacklisted)");
            System.out.println();
            count += temp.termFreq[0] + temp.termFreq[1];
        }
        
        System.out.println();
        System.out.println("Checking term count (includes terms on articles from blacklist but not terms not in either article), should be\n" + (S1.length + S2.length));
        System.out.println(count);
    }
        
    
    /*
     * Helper Methods
     */
    
    // simple hash function using a relatively large prime and title of article
    private int hash(String term) {
        int h = 0;
        for(int i = 0; i < term.length(); i++)
            h += (1871 * term.charAt(i));
        return h % M;      
    }
    
    // returns length of LL
    private static int length(Node t) {
        if(t == null)
            return 0;
        else
            return 1 + length(t.next); 
    }
    
    // counts number of terms in table
    private int size() {
        int count = 0;
        for(int i = 0; i < TFT.length; i++) {
            count += length(TFT[i]);
        }
        return count;
    }
    
    // for debugging, prints size of table, min, max, and mean length of buckets and st dev.
    private void printStats() {
        System.out.println("TFT (length " + TFT.length + "):");
        System.out.println("size of table: " + size());
        
        int min = length(TFT[0]);
        int max = min;
        double total = min;
        for(int i = 1; i < TFT.length; i++) {
            int temp = length(TFT[i]);
            if(temp > max)
                max = temp;
            if(temp < min)
                min = temp;
            total += temp;
        }
        
        double mean = total/TFT.length;
        
        System.out.println("min length bucket: " + min);
        System.out.println("max length bucket: " + max);
        System.out.println("mean bucket length: " + mean);
        
        total = 0;
        for(int i = 0; i < TFT.length; i++)
            total += Math.pow(length(TFT[i]) - mean, 2);
        
        System.out.println("st dev: " + Math.sqrt(total/TFT.length));
    }
    
    // unit test
    public static void main(String[] args) {
        TermFrequencyTable T = new TermFrequencyTable();
        
        String dbPath = "articles/";
        
        DatabaseIterator db = new DatabaseIterator(dbPath);
        
        Article a1 = null, a2 = null;
        
        // just looping to get articles from random locations
        for(int i = 0; i < 1160; i++)
            db.next();
        if(db.hasNext())
            a1 = db.next();                   // use two articles in db - if run out of articles, will cause nullpointer exception later on.
        if(db.hasNext())
            a2 = db.next();                  // use next article in db to compare - will probably be quite similar
        
        System.out.println("database has " + db.getNumArticles() + " articles.. testing on:");
        System.out.println("\tArticle 1: " + a1.getTitle());
        System.out.println("\tArticle 2: " + a2.getTitle());
        System.out.println();
        
        System.out.println("Comparing '" + a1.getTitle() + "' to itself...");
        
        T.initialize(a1.getBody(), a1.getBody());        //comparing body of article, not whole thing
        System.out.println();
        System.out.println("After inserting:");
        T.printStats();
        System.out.println();
        
        System.out.println("testing iterator, termFreqs for all terms should be the same...");
        T.iterateAll();
        System.out.println();
        
        System.out.println("calculating cosine similarity, should be 1.0....");
        System.out.println(T.cosineSimilarity());
        System.out.println();
        
        System.out.println("creating nonsense string, re-declare T and initialize with Article 1 and it");
        String nonsense = "rgsg fsdfg ergrgg kiwfjk sddsf vsnvk sjdjk wfjksddsj scnjknwf sdnkjnwdf sdvknj rfsdoi jkerfs rwfijo fd dsf wrf ssdv wvsv wvesdv";
        System.out.println("Comparing '" + a1.getTitle() + "' to nonsense...");
        
        T = new TermFrequencyTable();
        T.initialize(a1.getBody(), nonsense);
        System.out.println();
        System.out.println("After inserting:");
        T.printStats();
        System.out.println();
        T.iterateAll();
        System.out.println();
        
        System.out.println("calculating cosine similarity, should be 0.0....");
        System.out.println(T.cosineSimilarity());
        System.out.println();
        
        System.out.println("re-declare T again, compare Articles 1 and 2:");
        System.out.println("Comparing '" + a1.getTitle() + "' with '" + a2.getTitle() + "' ...");
        
        T = new TermFrequencyTable();
        T.initialize(a1.getBody(), a2.getBody());
        System.out.println();
        System.out.println("After inserting:");
        T.printStats();
        System.out.println();
        T.iterateAll();
        System.out.println();
        
        System.out.println("calculating cosine similarity, should be between 1 and 0....");
        System.out.println(T.cosineSimilarity());
        System.out.println();
    }
    
    
    // inner node class for buckets, modified to encapsulate blacklist checking
    private static class Node {
        public String term;
        public int[] termFreq = new int[2];
        public Node next;
        public boolean inBlackList;    // boolean flag for whether a term is in the blacklist
        
        public Node(String s, Node n, int docNum) {
            this.term = s;
            this.next = n;
            this.termFreq[docNum] = 1;
            this.termFreq[(docNum + 1) % termFreq.length] = 0;
            this.inBlackList = false;                            // if not specified, term is not in blacklist
        }
        
        public Node(String s, int docNum) {
            this(s, null, docNum);
        }
        
        public Node(String s, boolean inBL) {    // constructor for inserting blacklist into table 
            this.term = s;                       // (inBL should be true)
            this.next = null;
            this.termFreq[0] = 0;
            this.termFreq[1] = 0;
            this.inBlackList = inBL;
        }
        
        public void incFreq(int docNum) {
            this.termFreq[docNum]++;
        }
        
        public String toString() {
            return this.term + " (" + this.termFreq[0] + ", " + this.termFreq[1] + ")" + " -> " + this.next;
        }
    }
    
    
}