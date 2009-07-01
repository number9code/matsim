package playground.ciarif.retailers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.matsim.core.api.facilities.ActivityFacilities;
import org.matsim.core.api.facilities.ActivityFacility;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;

public class MakeATableFromXMLFacilities {
//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private FileWriter fw = null;
	private BufferedWriter out = null;
	private Object facilities;
			
	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public MakeATableFromXMLFacilities(String outfile) {
		super();
		//this.retailers = retailers;
		try {
			fw = new FileWriter(outfile);
			System.out.println(outfile);
			out = new BufferedWriter(fw);
			out.write("Fac_id\tfac_x\tfac_y\tLink_id\tcapacity\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("    done.");
	}

	//////////////////////////////////////////////////////////////////////
	// final method
	//////////////////////////////////////////////////////////////////////

	public final void close() {
		try {
			out.flush();
			out.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	public void write(ActivityFacilities facilities) {
		try {
			this.facilities = facilities;
			
			for (ActivityFacility f : facilities.getFacilities().values()) {
				if (f.getActivityOption("shop")!=null){
					out.write(f.getId()+ "\t");
					out.write(f.getCoord().getX()+ "\t");
					out.write(f.getCoord().getY()+"\t");
					out.write(f.getLink().getId()+"\t");
					out.write(f.getActivityOption("shop").getCapacity()+"\n");
					}
				}
			out.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
