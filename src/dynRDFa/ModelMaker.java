package dynRDFa;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.fuberlin.wiwiss.d2rq.jena.ModelD2RQ;

/**
 * The purpose of the ModelMaker class is to provide methods that transform a relational
 * (mySQL) database to structures that can be utilized to generate RDFa enriched web pages.
 * At first a D2RQModel is created using a mapping file and a mySQL database. It contains 
 * all the statements that can be produced from the database, expressed in the default D2RQ 
 * vocabulary. Subsequently, a Jena Model is created using a CONSTRUCT query where the desired
 * attributes and vocabulary are dictated. SELECT queries retrieve only the necessary statements.
 * The query results are used to populate structures (exportableModel) that can be consumed by 
 * the classes responsible for generating the web pages.
 * @author magda
 */
public class ModelMaker {
	//Jena Model view on the data in the D2RQ-mapped mysql database. Works as an initial model.
	private Model m; 
	//Constructed model, contains desired vocabulary contained in the corresponding SPARQL query.
	private Model resultsModel; 
	/*ArrayList that stores all the treeple objects resulting from the SPARQL select queries and the methods of the class.
	 *Essentially, a treeple is statements grouped together, with the addition of some useful data structures.  */
	private ArrayList<Treeple> treeples; 
	//HashMap that stores statement objects that are resources, not literals (in our case authors).
	private HashMap<Resource,Treeple> objectLookup; 
	//ExportableModel object that packs together all the necessary info that a velocity template consumes.
	private ExportableModel exportableModel; 
	//represents the type of file to be created by velocity(valid: 0 to 5).
	private int choice; 
    
	/**
	 * Creates a Jena model view on the data in the D2RQ-mapped mysql database. A previously created
	 * mapping file is utilized. This mapping file, called the default mapping, maps each table to
	 * a new RDFS class that is based on the table's name, and maps each column to a property based
	 * on the column's name. The model that occurs will be queried later.
	 */
	public void initializeD2RQModel(){
		//Set up the ModelD2RQ using a mapping file
	    m = new ModelD2RQ("conf/pubsmap.ttl");
	    //uncomment below to see info about the d2rq model and export it on a file
//	    System.out.println("Number of statements:"+m.size());
//		NsIterator nsiter = m.listNameSpaces();
//		int count = 1;
//		while(nsiter.hasNext()){
//			System.out.println("Namespace "+count++ +": "+nsiter.nextNs());
//		}
	    //output original model to file
//	    FileOutputStream outputStream;
//		try {
//			outputStream = new FileOutputStream("conf/out.txt");
//			m.write(outputStream, "RDF/XML");
//		    outputStream.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	
	/**
	 * Loads and executes a CONSTRUCT SPARQL query, which results in the Jena Model 
	 * that contains all the statements from the D2RQModel, expressed in the desired
	 * vocabulary. 
	 */
	public void constructModel(){
		//load our queries -construct
        QueryReader queryReader;
		try {
			queryReader = QueryReader.createQueryReader("conf/sparqls/construct.txt");
			String queryStr = queryReader.getQuery("main-construct");
		    //display the query
		    //System.out.println(queryStr);
		        
		    //create the query -construct
		    Query query = QueryFactory.create(queryStr);
		    QueryExecution qExec = QueryExecutionFactory.create(query, m);
		        
		    //execute the query - as a result a model is expected
	        resultsModel = qExec.execConstruct();
	        
	        //output the resulting graph
//	        FileOutputStream outStream = new FileOutputStream("conf/outputFile.txt");
//	        resultsModel.write(outStream, "N3");
//	        outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes inner structures, loads the select query that corresponds to the choice parameter 
	 * and executes it, calls class method {@link #createTreeples(ResultSet resultSet)} in order to generate all 
	 * the treeples that correspond to the results of the select query.
	 * @param choice is the type of file to be created by velocity.
	 */
	public void selectOnModel(int choice){
		//structures should be (re)initialized each time a select query is performed.
		treeples = new ArrayList<Treeple>();
		objectLookup = new HashMap<Resource,Treeple>();
		exportableModel = new ExportableModel();
		//load select queries.
        QueryReader queryReader;
		try {
			this.choice = choice;
			if(choice == 0){
				queryReader = QueryReader.createQueryReader("conf/sparqls/select_all_0.txt");
			}
			else if(choice == 1){
				queryReader = QueryReader.createQueryReader("conf/sparqls/select_journal_1.txt");
			}
			else if(choice == 2){
				queryReader = QueryReader.createQueryReader("conf/sparqls/select_conference_2.txt");
			}
			else if(choice == 3){
				queryReader = QueryReader.createQueryReader("conf/sparqls/select_bookchapter_3.txt");
			}
			else if(choice == 4){
				queryReader = QueryReader.createQueryReader("conf/sparqls/select_book_4.txt");
			}
			else if(choice == 5){
				queryReader = QueryReader.createQueryReader("conf/sparqls/select_techreport_5.txt");
			}
			else{
				System.out.println("Wrong choice");
				return;
			}
			String queryStr = queryReader.getQuery("main-select");
	        //display the query.
	        //System.out.println(queryStr);
	        
	        //create the select query.
	        Query query = QueryFactory.create(queryStr);
	        QueryExecution qExec = QueryExecutionFactory.create(query, resultsModel);
	        
	        //execute the select query - as a result a resultSet is expected.
	        ResultSet resultSet = qExec.execSelect();
	        
	        //call class method to generate all the treeples that correspond to the select query.
	        createTreeples(resultSet);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}

	
	/**
	 * @param resultSet is the product of the select query.
	 */
	public void createTreeples(ResultSet resultSet){
		boolean flag = false;
		//unique publication IDs will be stored here.
		ArrayList<RDFNode> resNodes = new ArrayList<RDFNode>();
		//retrieve publication IDs from the resultSet and store them as RDFNodes .
		while(resultSet.hasNext()){
			QuerySolution soln = resultSet.nextSolution();
			for(Object var : resultSet.getResultVars()){
				RDFNode node = soln.get(var.toString());
				resNodes.add(node);
			}
		}
		StmtIterator iter;
		//iterate through sorted unique publication IDs expressed as RDFNodes.
		for(int i=0; i<resNodes.size(); i++){
			//create a treeple.
			Treeple temp = new Treeple();
			//find in the model all the statements that have the current publicationID as an object (only 1 is expected in our case).
			iter = resultsModel.listStatements(null, null, resNodes.get(i));
			//get the first statement.
			Statement stmt = iter.nextStatement();
			//extract the subject.
			Resource subject = stmt.getSubject();
			//insert it in the treeple.
			temp.insertSubject(subject);
			//iterate through all the statements that hold the Resource subject as subject
			//the goal is to sort the model in descending order by year and publication id.
			//note that the publicationIDs do not represent chronological order.
			StmtIterator internalIter;
			internalIter = resultsModel.listStatements(subject, (Property) null, (RDFNode)null);
			while(internalIter.hasNext()){
				Statement internalStmt = internalIter.nextStatement();
				//retrieve predicate.
				Property predicate = internalStmt.getPredicate();
				//retrieve object.
				RDFNode object = internalStmt.getObject();
				//insert the predicate and the object in the treeple.
				temp.insertPredicateObjects(predicate, object);
				//if the object is a Resource, and not a literal itself (authors in our case).
				if( object instanceof Resource ){
					flag = true;
					//retrieve the statements that have the object as a subject.
					StmtIterator objIter = resultsModel.listStatements((Resource)object, (Property) null, (RDFNode)null);
					while(objIter.hasNext()){
						Statement objStmt = objIter.nextStatement();
						//retrieve predicate.
						Property pred = objStmt.getPredicate();
						//retrieve object.
						RDFNode obj = objStmt.getObject();
						//make a treeple for the object that is a resource (author).
						Treeple tempT = new Treeple();
						tempT.insertSubject((Resource)object);
						tempT.insertPredicateObjects(pred, obj);
						//place the treeple in the objectLookup.
						//objectLookup is a hashMap. this means that there are no duplicate keys.
						//already existing object.
						if(objectLookup.containsKey((Resource)object)){
							objectLookup.get((Resource)object).insertSubject((Resource)object);
							//avoid having duplicate names and last names.
							if(!objectLookup.get((Resource)object).getPredobs().containsKey(pred)){
								objectLookup.get((Resource)object).insertPredicateObjects(pred, obj);
							}
							
						}
						//object does not exist in hashMap.
						else{
							//place the author related treeple in the objectLookup.
							objectLookup.put((Resource)object, tempT);
						}
					}
				}
			}
			/*
			 * place the treeple in the private field of the class (arrayList). It will be
			 * a part of the exportable model. Place it in  only if it's a publication and not an author.
			 */
			treeples.add(temp);
		}

		for (Map.Entry<Resource, Treeple> entry : objectLookup.entrySet())
		{
			entry.getValue().setTreepleStructures(resultsModel);
		}

		 // for each treeple in the ArrayList, set the object's inner structures.
		for(int i=0;i<treeples.size();i++){
			//the objectLookup is "global". Each treeple holds a copy of the same objectLookup
			treeples.get(i).setObjectLookup(objectLookup);
			//sets the private fields of a treeple.
			treeples.get(i).setTreepleStructures(resultsModel);
		}        	       
	}
	
	/**
	 * Sets the private fields of the exportableModel object.
	 */
	public void initializeExportableModel(){
		exportableModel.setID(choice);
		exportableModel.setMappings(getMappings());
		exportableModel.setPublicationsPerYear(publicationsPerYear());
		exportableModel.setTreeples(getTreeples());
	}
	
	/**
	 * Getter
	 * @return the exportableModel private field.
	 */
	public ExportableModel getExportableModel(){
		return exportableModel;
	}

	/**
	 * Getter
	 * @return the treeples private field.
	 */
	public ArrayList<Treeple> getTreeples(){
		return treeples;
	}
	
	/**
	 * Prints the objectLookup private field. It contains the 
	 * objects of statements that are subjects in other statements.
	 */
	public void printObjectLookup(){
	    Iterator iterator = objectLookup.keySet().iterator();  
	    while (iterator.hasNext()) {  
	       String key = iterator.next().toString();  
	       //String value = objectLookup.get(key).toString();  
	       System.out.println(key);  
	    }  
	}
	
	/**
	 * Getter.
	 * @return an ArrayList that contains the prefix mappings in a String form,
	 * ready to be inserted in a velocity template.
	 */
	public ArrayList<String> getMappings(){
		ArrayList<String> mappings = new ArrayList<String>();
		//note that each treeple holds different mappings. Therefore it's needed to iterate through them all.
		for(int i=0;i<treeples.size();i++){ 
			for (Map.Entry<String, String> entry : treeples.get(i).getMappings().entrySet())
			{
				if(!mappings.contains(entry.getKey()+": "+entry.getValue())){
					//supports the RDFs 1.1 notation e.g.: prefix="dc: http://purl.org/dc/elements/1.1/	foaf: http://xmlns.com/foaf/0.1/"
					mappings.add(entry.getKey()+": "+entry.getValue());
				}
			}
		}
		return mappings;
	}
	
	/**
	 * Creates a TreeMap that has as a key a year, and as a value the number of publications. 
	 * @return TreeMap with publications per year.
	 */
	public TreeMap<Integer,Integer> publicationsPerYear(){
		//tree map in order to keep a descending order among the years
		TreeMap<Integer,Integer> publicationsPerYear = new TreeMap(Collections.reverseOrder());
		for(int i=0;i<treeples.size();i++){
			if(!publicationsPerYear.containsKey(treeples.get(i).getYear())){
				publicationsPerYear.put(treeples.get(i).getYear(), 1);
			}
			else
				publicationsPerYear.put(treeples.get(i).getYear(),  1+publicationsPerYear.get(treeples.get(i).getYear()));
		}
//		for (Map.Entry<Integer, Integer> entry : publicationsPerYear.entrySet())
//		{
//		    System.out.println(entry.getKey() + "--->" + entry.getValue());
//		}
		return publicationsPerYear;
	}
	
}//end of ModelMaker class

