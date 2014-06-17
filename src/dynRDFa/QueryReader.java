package dynRDFa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The class parses a text file with a set of queries in it and then 
 * provides a way to hash into the set of queries using a name identifier. 
 * The application uses it for single query files.
 */
public class QueryReader {
	   /**
	    * Map of String representations of queries, hashed by name
	    */
	   private Map<String, String> _queries = null;
	   
	   /**
	    * Create a new query reader instance with the supplied map
	    * of queries
	    * @param queries The map of queries to store
	    */
	   private QueryReader(Map<String, String> queries)
	   {
	      _queries = queries;
	   }
	   
	   /**
	    * Get the string representing the specified query
	    * @param queryName The name of the query to return
	    * @return A string representation of the query
	    */
	   public String getQuery(String queryName)
	   {
	      String query = null;
	      
	      if(_queries.containsKey(queryName))
	      {
	         query = _queries.get(queryName);
	      }
	      
	      return query;
	   }
	   
	   /**
	    * Create a new instance of QueryReader populated with 
	    * queries from the provided file
	    * @param file The file from which to load queries
	    * @return Returns the new instance of QueryReader
	    * @throws IOException
	    */
	   public static QueryReader createQueryReader(String file) throws IOException
	   {
	      BufferedReader reader = new BufferedReader(new FileReader(file));
	      Map<String, String> queryMap = new HashMap<String, String>();
	      
	      String currLine = null;
	      StringBuilder currQuery = null;
	      String currQueryName = null;
	      while(null != (currLine = reader.readLine()))
	      {
	         if(currLine.startsWith("#query "))
	         {
	            currQueryName = currLine.substring(7).trim();
	            currQuery = new StringBuilder();
	         }
	         else if(currLine.startsWith("#end"))
	         {
	            if(!queryMap.containsKey(currQueryName))
	            {
	               queryMap.put(currQueryName, currQuery.toString());
	            }
	            currQuery = null;
	            currQueryName = null;
	         }
	         else
	         {
	            if(null != currQuery)
	            {
	               currQuery.append(currLine + " \r\n");
	            }
	         }
	      }
	      
	      return new QueryReader(queryMap);
	   }
}
