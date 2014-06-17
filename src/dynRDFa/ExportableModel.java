package dynRDFa;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * The ExportableModel class packs together all the information that the velocity 
 * engine and template need to consume in order to generate the RDFa enriched web pages.
 * This includes an ArrayList of treeple objects (a treeple is a group of statements), the 
 * prefix mappings, the publications per year, and the type of web page that needs to 
 * be generated.
 * @author magda
 */
public class ExportableModel {
	/*holds all the treeple objects that resulted from the SELECT query that 
	 *corresponds to the id. Essentially, a treeple is statements grouped together, with
	 *the addition of some useful data structures. */
	private ArrayList<Treeple> treeples; 
	//prefix mappings that need to be declared in the html documents.
	private  ArrayList<String> mappings ;
	//TreeMap with key: year and value: number of publications.
	private TreeMap<Integer,Integer> publicationsPerYear;
	//the type of web page to be generated.
	private int id;
	
	/**
	 * Setter 
	 * @param treeples are all the treeple objects that resulted from the SELECT query that 
	 * corresponds to the id. Essentially, a treeple is statements grouped together, with
	 * the addition of some useful data structures.
	 */
	public void setTreeples(ArrayList<Treeple> treeples){
		this.treeples = treeples;
	}

	/**
	 * Getter an ArrayList of all the treeple objects that resulted from the SELECT query that 
	 * corresponds to the id. Essentially, a treeple is statements grouped together, with
	 * the addition of some useful data structures
	 * @return
	 */
	public ArrayList<Treeple> getTreeples(){
		return treeples;
	}
	
	/**
	 * Setter
	 * @param mappings are the prefix mappings.
	 */
	public void setMappings(ArrayList<String> mappings){
		this.mappings = mappings;
	}
	
	/**
	 * Getter
	 * @return the prefix mappings.
	 */
	public ArrayList<String> getMappings(){
		return mappings;
	}
	
	/**
	 * Setter
	 * @param publicationsPerYear
	 */
	public void setPublicationsPerYear(TreeMap<Integer,Integer> publicationsPerYear){
		this.publicationsPerYear = publicationsPerYear;
	}
	
	/**
	 * Getter
	 * @return The TreeMap that contains the publications per year.
	 */
	public TreeMap<Integer,Integer> getPublicationsPerYear(){
		return publicationsPerYear;
	}
	
	/**
	 * Setter
	 * @param id is the type of the web page that will be generated.
	 */
	public void setID(int id){
		this.id = id;
	}
	
	/**
	 * Getter 
	 * @return the type of the web page that will be generated.
	 */
	public int getID(){
		return id;
	}

}
