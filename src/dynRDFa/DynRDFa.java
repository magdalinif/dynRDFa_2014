package dynRDFa;

import java.io.IOException;
import java.util.ArrayList;



/**
 * Driver class.
 * @author magda
 *
 */
public class DynRDFa {

	/**
	 * Main method. Creates and initializes ModelMaker and VelociDataModel
	 * objects in order to generate the 6 webpages.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		//suppress log4j warnings
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		ModelMaker modelM = new ModelMaker();
		//initial D2RQModel, default vocabulary
		modelM.initializeD2RQModel();
		//model with desired vocabulary
 	    modelM.constructModel();
 	    
 	    VelociDataModel vdm = new VelociDataModel();
 	    ArrayList<ExportableModel> ems = new ArrayList<ExportableModel>();
 	    //create all 6 web pages 
 	    for(int i=0;i<=5;i++){
 	    	modelM.selectOnModel(i);
 	    	modelM.initializeExportableModel();
 	    	ems.add(modelM.getExportableModel());
 	     }
 	    
 	    for(int i=0;i<ems.size();i++){
 	    	vdm.vdmInitializer();
 	    	//build
 	    	vdm.builder(ems.get(i));
 	    }
 	    
		}
	}

