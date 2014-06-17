package dynRDFa;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * The VelociDataModel class serves as the go-between for application and template. It creates 
 * initializes a VelocityEngine and loads the template. It also creates a context object which
 *  is most easily viewed as a simple map that stores objects by key. More specifically, the 
 *  context stores objects of type java.lang.Object, keyed by objects of typejava.lang.String. 
 *  In essence, the dynamic portion of a template's content is specified through the keys used 
 *  to look up that content in the context. The $ prefix in the template simply lets the 
 *  template engine know that the following text potentially corresponds to a context key 
 *  that requires special processing. The html documents are produced by invoking the merge 
 *  method of the template object.
 * @author magda
 *
 */
public class VelociDataModel {
	//template engine
	private VelocityEngine ve;
	
	/**
	 * Constructor: creates an VelocityEngine object.
	 */
	public VelociDataModel(){
		//get velocity engine
		ve = new VelocityEngine();
		ve.setProperty("output.encoding", "UTF-8");
	}
	
	/**
	 * The non-Singleton model is used. An instance of VelocityEngine has been
	 * created, and now its init() method is invoked.
	 */
	public void vdmInitializer(){
		//intialize velocity engine
		ve.init();
	}

	/**
	 * Retrieves the template, creates and populates the context, generates the right 
	 * type of html document and merges template and context. As a result the template
	 * is rendered in the document.
	 * @param exportableModel is the object that holds all the necessary information
	 * that will populate the model.
	 */
	public void builder(ExportableModel exportableModel){
        //retrieve template.
        Template t = ve.getTemplate("conf/template.vm","UTF-8");
        //create a context .
        VelocityContext context = new VelocityContext();
        //populate the context.
        context.put("mappings", exportableModel.getMappings());
        context.put("treeples", exportableModel.getTreeples());
        context.put("years", exportableModel.getPublicationsPerYear());
        context.put("pageid",exportableModel.getID());
        //render the template into a fileWriter.
		try {
			//retrieve type of file and create the corresponding html document
			String file = "conf/webpages/type"+exportableModel.getID()+".html";
			FileWriter writer = new FileWriter(new File(file));
			//merge causes the references in the template to be replaced with data obtained from the context.
			t.merge( context, writer );
			writer.flush();
			writer.close();
		}catch(ResourceNotFoundException e2 ) {
			System.out.println("cannot find template ");
		} catch(ParseErrorException e ) {
			System.out.println("Syntax error in template : " + e);
		} catch (IOException e) {
			e.printStackTrace();
		}
      
    }
}
