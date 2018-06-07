/*
 * MiniSearch.java
 *
 * A client program that uses the MaxHeap, DatabaseIterator, TermFrequencyTable, ArticleTable
 * and Article classes, along with additional data
 * structures, to allow a user to create, modify
 * and interact with, including search through, an encyclopedia database.
 */

import java.util.*;

public class MiniSearch {
  
  private static Article[] getArticleList(DatabaseIterator db) {
    
    // count how many articles are in the directory
    int count = db.getNumArticles(); 
    
    // now create array
    Article[] list = new Article[count];
    for(int i = 0; i < count; ++i)
      list[i] = db.next();
    
    return list; 
  }
  
  private static DatabaseIterator setupDatabase(String path) {
    return new DatabaseIterator(path);
  }
  
  private static void addArticle(Scanner s, ArticleTable T) {
    System.out.println();
    System.out.println("Add an article");
    System.out.println("==============");
    
    System.out.print("Enter article title: ");
    String title = s.nextLine();
    
    System.out.println("You may now enter the body of the article.");
    System.out.println("Press return two times when you are done.");
    
    String body = "";
    String line = "";
    do {
      line = s.nextLine();
      body += line + "\n";
    } while (!line.equals(""));
    
    T.insert(new Article(title, body));
  }
  
  
  private static void removeArticle(Scanner s, ArticleTable T) {
    System.out.println();
    System.out.println("Remove an article");
    System.out.println("=================");
    
    System.out.print("Enter article title: ");
    String title = s.nextLine();
    
    
    T.delete(title);
  }
  
  // search for related articles using key phrase. 
  private static void search(Scanner s, ArticleTable T) {
    System.out.println();
    System.out.println("Search by search phrase");
    System.out.println("=======================");
    
    System.out.print("Enter search phrase: ");
    String phrase = s.nextLine();
    
    T.reset();
    Article a = null;
    double cos;
    MaxHeap h = new MaxHeap();
    // scan through all articles in ArticleTable, insert articles
    // with cosine similarity > 0 into heap (implementing maxQueue)
    while(T.hasNext()) {
        a = T.next();
        cos = getCosineSimilarity(phrase, a.getBody());
        if(cos != 0.0)
            h.insert(cos, a);
    }
        
    //h.printHeap();
        
    System.out.println();
    
    if(h.isEmpty()) {
      System.out.println("No articles found!"); 
      //return; 
    } else {
        //print top three
        System.out.println("Top match: " + h.getMaxAsString() + "\n");
        for(int i = 2; i <= 3; i++) {
            if(!h.isEmpty())
                System.out.println("Hit #" + i + ": " + h.getMaxAsString() + "\n");
            else {
                System.out.println("no more articles found!");
                break;
            }
        }
    }
    
    
    System.out.println("Press return when finished reading.");
    s.nextLine();
  }
  
  // take two strings (where s is search term and t is body of article),
  // create a TermFreqTable and initialize with the two strings.
  // finally, extract cosine similarity and return it.
  // * blacklist checking and preprocessing is encapsulated in TermFrequencyTable.
  private static double getCosineSimilarity(String s, String t) {
      TermFrequencyTable termTbl = new TermFrequencyTable();
      termTbl.initialize(s, t);
      return termTbl.cosineSimilarity();
  }
  
  public static void main(String[] args) {
    Scanner user = new Scanner(System.in);
    
    String dbPath = "articles/";
    
    DatabaseIterator db = setupDatabase(dbPath);
    
    System.out.println("Read " + db.getNumArticles() + 
                       " articles from disk.");
    
    ArticleTable T = new ArticleTable(); 
    Article[] A = getArticleList(db);
    T.initialize(A);
    
    int choice = -1;
    do {
      System.out.println();
      System.out.println("Welcome to Mini-Google!");
      System.out.println("=====================");
      System.out.println("Make a selection from the " +
                         "following options:");
      System.out.println();
      System.out.println("Manipulating the database");
      System.out.println("-------------------------");
      System.out.println("    1. add a new article");
      System.out.println("    2. remove an article");
      System.out.println();
      System.out.println("Searching the database");
      System.out.println("----------------------");
      System.out.println("    3. search using search phrase");
      System.out.println();
      
      System.out.print("Enter a selection (1-3, or 0 to quit): ");
      
      choice = user.nextInt();
      user.nextLine();
      
      switch (choice) {
        case 0:
          return;
          
        case 1:
          addArticle(user, T);
          break;
          
        case 2:
          removeArticle(user, T);
          break;
          
        case 3:
          search(user, T);
          break;
          
        default:
          break;
      }
      
      choice = -1;
      
    } while (choice < 0 || choice > 4); 
  } 
}

