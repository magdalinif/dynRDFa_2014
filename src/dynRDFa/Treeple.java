package dynRDFa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * The Treeple class groups together statements that share the same subject. A TreeMap
 * holds as keys the predicates, and as values, ArrayLists of statement objects. The objects might 
 * be literals or resources. This structure is chosen because in addition to the subject,
 * some object might share the same predicate too (e.g. authors). A treeple might look like:
 * S P1 O
 * 	 P2 O O..
 * 	 ....
 * 	 PN O
 * Aside from that, the class holds the following structures: Map that contains the namespace 
 * prefix mappings, and a HashMap with all the statement objects that are Resources, and as a result 
 * subjects in other statements. Information about the year and the hyperlink related with a
 * publication are provided through the classes' getters. Since the name Triple is reserved by Jena, 
 * and a TreeMap is used, Treeples seems like an appropriate name.
 * @author magda
 *
 */
public class Treeple {
	//the subject of a statement.
	private Resource subject;
	/* a TreeMap where: key= predicate, and value= an ArrayList of objects 
	 * that have the same subject and predicate. The ArrayList is chosen because
	 * there might be only one object (eg book title) ore many (authors). */
	private TreeMap<Property,ArrayList<RDFNode>> predobs;
	/* a Jena PrefixMapping interface, a factory class will be used to create an object.
	 * Note that a Model *is* a PrefixMapping, so all the PrefixMapping operations apply to Models. */
	private PrefixMapping pm;
	//the prefix mappings that are retrieved from pm: the object that implements the PrefixMapping interface.
	private Map<String,String> expoPrefixMappings;
	/* a structure that stores statement objects that are resources, not literals (in our case the authors). 
	 * The resources are used as keys, and the treeple objects that correspond to them, as values. */
	private HashMap<Resource,Treeple> objectLookup; 
	/* the year of publication. By default it is set to 0, since a treeple might not refer to a publication.
	 * If the treeple refers to a publication, the year is set using a class method. */
	private int year;
	/* the the hyperlink related to a publication. By default it is set to 0, since a treeple might not refer to a publication.
	 * If the treeple refers to a publication, the year is set using a class method. */
	private String link;
	
	/**
	 * Class constructor: initializes the private fields.
	 */
	public Treeple(){
		predobs = new TreeMap<Property,ArrayList<RDFNode>>(new Comparator<Property>() {
		    public int compare(Property a, Property b) {
		    	/* when the properties are URIs sort them lexicographically, by extracting the characters after the last /
		    	 * in our case, these characters represent publication IDs.	 */
		    	String s1 = a.getURI().substring(a.getURI().lastIndexOf('/') + 1);
		    	String s2 = b.getURI().substring(b.getURI().lastIndexOf('/') + 1);
		    	return s1.compareTo(s2);
		       
		    }
		});
		//Factory class to create an unspecified kind of PrefixMapping.
		pm = PrefixMapping.Factory.create();
	}
	
	/**
	 * Setter
	 * @param subject is the subject of a treeple.
	 */
	public void insertSubject(Resource subject){
		this.subject = subject;
	}
	
	/**
	 * Setter: puts a predicate in the TreeMap if it doesn't already exist.
	 * Put the object that is related with the predicate in the TreeMap
	 * @param predicate is the predicate to be inserted on the TreeMap
	 * @param object is the object to be inserted on the TreeMap
	 */
	public void insertPredicateObjects(Property predicate,RDFNode object){
		ArrayList<RDFNode> tempObj = new ArrayList<RDFNode>();
		//the predicate already exists. S P o o o o (multiple creator case)
		if(predobs.containsKey(predicate)){
			//get the already existing object list for this key-predicate
			tempObj = predobs.get(predicate);
			//add the newly seen object (creator in this case)
			tempObj.add(object);
			//put it all back in
			predobs.put(predicate, tempObj);
		}
		//the predicate has not been seen before
		else{
			tempObj.add(object);
			predobs.put(predicate, tempObj);
		}
	}
	
	/**
	 * Setter
	 * @param objectLookup is the HashMap that stores statement objects that are Resources.
	 * All the treeple objects hold a copy of the same objectLookup.
	 */
	public void setObjectLookup(HashMap<Resource,Treeple> objectLookup){
		this.objectLookup = objectLookup;
	}
	
	/**
	 * Sets the HashMap that holds all the namespace prefix mappings, the year and hyperlink related 
	 * with a publication, by iterating through the predicate/objects TreeMap. The method distinguishes 
	 * statement objects that are Resources from those that are Literals, so that the prefix mappings are
	 * correct.
	 * @param resultsModel is a Jena Model that holds various statements resulting from a CONSTRUCT query
	 */
	public void setTreepleStructures(Model resultsModel){
		//for each predicate and objects in the TreeMap
		for (Map.Entry<Property, ArrayList<RDFNode>> entry : predobs.entrySet()) {
			//retrieve predicate
			Property key = entry.getKey();
			//retrieve objects
			ArrayList<RDFNode> value = entry.getValue();
			/*
			 * set a namespace prefix in the prefixMapping object. (URIPrefix e.g. dc, Namespace e.g. http://purl.org/dc/elements/1.1/)
			 * Note that a Model is a PrefixMapping, so all the PrefixMapping operations apply to Models.
			 */
			pm.setNsPrefix(resultsModel.getNsURIPrefix(key.getNameSpace()),key.getNameSpace());
				
			//for each statement object
			for(int i=0;i<value.size();i++){
				StmtIterator iter;
				//the statement object is a Resource (author case)
				if(!(value.get(i).isLiteral())&&(resultsModel.contains(value.get(i).asResource(),(Property)null,(RDFNode)null))){
					//retrieve all the statements that have the statement object as a subject.
					iter = resultsModel.listStatements(value.get(i).asResource(),(Property)null,(RDFNode)null);
					while(iter.hasNext()){
						Statement stmnt = iter.nextStatement();
						//set a namespace prefix) 
			    		pm.setNsPrefix(resultsModel.getNsURIPrefix(stmnt.getPredicate().getNameSpace()),stmnt.getPredicate().getNameSpace());
			    		}
			    	}
				//the statement object is a Literal
			    else{
			    	//set the hyperlink related with this publication
			    	link = "http://lpis.csd.auth.gr/paper_details.asp?publicationID="+subject.toString().substring(subject.toString().lastIndexOf("/")+1,subject.toString().length());
			    	//set the publication year
			    	if(value.get(i).toString().contains("^^http://www.w3.org/2001/XMLSchema#int")&&(value.get(i).toString().substring(0,value.get(i).toString().lastIndexOf("^")-1).length()==4)){
			    		year = Integer.parseInt(value.get(i).toString().substring(0,value.get(i).toString().lastIndexOf("^")-1));
			    	}
			    }
			}
		}
		//retrieve the namespace prefix map and put it in a private field
		expoPrefixMappings = pm.getNsPrefixMap();
	}
	
	/** 
	 * toString() is overridden so that a velocity engine and template can access a treeple. Velocity cannot
	 * utilize methods that accept arguments. Since RDFa is not  considered a matter of presentation,
	 * but a matter of program-logic, the RDFa tags are declared here.
	 * All the information that is related to a publication is wrapped around RDFa tags depending on its type,
	 * and then concatenated to a string. In this sense, the method is not reusable, but doing otherwise would
	 * increase the velocity template complexity which is now only responsible for the presentation level.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		//the publication hyperlink is used as a subject for the RDF content chunks in the html document.
		String str = "<div about=\"http://lpis.csd.auth.gr/paper_details.asp?publicationID="
		+subject.toString().substring(subject.toString().lastIndexOf("/")+1,subject.toString().length()) +"\" typeof=\"bibo:Document\">"; //RDFa S
		str += "\n"; //presentation
        
		//iterate through predicates and objects
		for (Map.Entry<Property, ArrayList<RDFNode>> entry : predobs.entrySet()) {
		    Property key = entry.getKey();
		    ArrayList<RDFNode> value = entry.getValue();
		    
		    for(int i=0;i<value.size();i++){
		    	
		    	//the statement object is a Resource, it is consequently contained in objectLookup
		    	if(objectLookup.containsKey(value.get(i))){
		    		str += "<span rel=\"";  //RDFa P
				    str += getKeyByValue(expoPrefixMappings,key.getNameSpace()); //RDFa P
				    str += ":";	//RDFa P
				    str += key.getLocalName(); //RDFa P
				    str += "\">"; //RDFa P
				    str += "\n"; //presentation
		    		
				    //predicates and objects of the statement object that is a Resource (authors in our case)
		    	    for (Map.Entry<Property, ArrayList<RDFNode>> e:  objectLookup.get(value.get(i)).predobs.entrySet()) {
		    		    Property k = e.getKey();
		    		    ArrayList<RDFNode> v = e.getValue();
    	    		   
 		    		    str += "<span property=\""; //RDFa P
 		    		    str += getKeyByValue( objectLookup.get(value.get(i)).expoPrefixMappings,k.getNameSpace()); //RDFa P
 		    		    
 		    		    str += ":"; //RDFa P
 		    		    str += k.getLocalName(); //RDFa P
 		    		    //str += "\">"; //RDFa P

 		    		    //Author ID case. @content can be used to indicate a plain literal
 		    		    if(v.toString().contains("^^http://www.w3.org/2001/XMLSchema#int")){
 		    		    	str += "\" content=\"";
 		    		    	str += Integer.parseInt(v.toString().substring(1,v.toString().lastIndexOf("^")-1))-80000;
 		    		    	str += "\">";
 		    		    }
 		    		    else{
 		    		    	str += "\">"; //RDFa P
 		    		    	str += v.toString().substring(1, v.toString().length()-1);  //RDFa O
 		    		    }
 		    		    
 		    		   str += " </span>"; //RDFa P
 		    		   str += "\n"; //presentation
		    	    }
		    	    str += "</span>"; //RDFa P
		    	    str += ", "; //presentation
		    	    str += "\n"; //presentation
		    	}
		    	else{
		    		//do not produce Predicates and Objects for objects marked as N/A
		    		if(value.get(i).toString().contains("N/A")){
		    			str += "";
		    			continue;
		    			}
		    		str += "<span property=\""; //RDFa P
				    str += getKeyByValue(expoPrefixMappings,key.getNameSpace()); //RDFa P
				    str += ":"; //RDFa P
				    str += key.getLocalName(); //RDFa P
				    str += "\">"; //RDFa P
				    
		    		//id and year not as typed literals in NTriple syntax
 		    		if(value.get(i).toString().contains("^^http://www.w3.org/2001/XMLSchema#int")){
 		    			str += value.get(i).asLiteral().getValue(); //RDFa O
 		    		}
		    		else{	
		    			str += value.get(i); //RDFa O
		    		}
		    		str += "</span>"; //RDFa P
		    		str += ", "; //presentation
		    		str += "\n"; //presentation
		    	}
		    } 
		}
		str += "</div>"; //RDFa P
		return str;
	}
	
	/**
	 * Getter
	 * @return the subject of a treeple.
	 */
	public Resource getSubject(){
		return subject;
	}
	
	/**
	 * Getter
	 * @return the predicates and objects of a treeple.
	 */
	public TreeMap<Property,ArrayList<RDFNode>> getPredobs(){
		return predobs;
	}
	
	/**
	 * Prints the namespace prefix mappings.
	 */
	public void printMappings(){
		for (Map.Entry<String, String> entry : expoPrefixMappings.entrySet())
		{
		    System.out.println(entry.getKey() + "--->" + entry.getValue());
		}
	}
	
	/**
	 * Getter
	 * @return the namespace prefix mappings.
	 */
	public Map<String,String> getMappings(){
		return expoPrefixMappings;
	}
	
	/**
	 * Getter
	 * @return the publication year related with the treeple.
	 */
	public int getYear(){
		return year;
	}
	
	/**
	 * Getter
	 * @return the hyperlink of the publication related with the treeple.
	 */
	public String getLink(){
		return link;
	}
	
	/**
	 * For one-to-one relationships, the method accepts a value, and finds the
	 * corresponding key.
	 * @param map a Map<String,String>.
	 * @param value a String value in the Map.
	 * @return the key that corresponds to the value argument.
	 */
	public static String getKeyByValue(Map<String,String> map,String value) {
		
	    for (Entry<String,String> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return value;
	}
	
	/**
	 * For one-to-one relationships, the method accepts a value, and finds the
	 * corresponding key.
	 * @param map a HashMap<Resource,Treeple> (objectLookup).
	 * @param value a treeple in the HashMap.
	 * @return the key that corresponds to the value argument.
	 */
	public static Resource getKeyByValue(HashMap<Resource,Treeple> map,Treeple value) {
	    for (Entry<Resource,Treeple> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

}
