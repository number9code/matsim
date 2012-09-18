package playground.kai.bvwp;

import org.matsim.core.basic.v01.IdImpl;
import playground.kai.bvwp.Values.Entry;
import playground.kai.bvwp.Values.Mode;
import playground.kai.bvwp.Values.Type;

/**
 * @author Ihab
 *
 */

class Scenario3 { // Relationsbezogen_mit_generalisierten_Kosten_GV

	static ScenarioForEvalData createNullfall() {
		// set up the base case:
		ScenarioForEvalData nullfall = new ScenarioForEvalData() ;
	
		// construct values for one OD relation:
		Values nullfallForOD = new Values() ;
		nullfall.setValuesForODRelation(new IdImpl("BC"), nullfallForOD ) ;
		{
			// construct values for the road mode for this OD relation:
			ValuesForAMode roadValues = nullfallForOD.getByMode(Mode.road) ;
			{
				// freight traffic:
				ValuesForAUserType gvValues = roadValues.getByDemandSegment(Type.GV) ;
				gvValues.setByEntry( Entry.XX, 2000. ) ; // tons
				gvValues.setByEntry( Entry.km, 41. ) ;
			}				
			
			// rail values are just a copy of the road values:
			ValuesForAMode railValues = roadValues.createDeepCopy() ;
			nullfallForOD.setValuesForMode( Mode.rail, railValues ) ;
		}
		
		// return the base case:
		return nullfall;
	}

	static ScenarioForEvalData createPlanfall(ScenarioForEvalData nullfall) {
		// (construct the policy case.  The base case can be used to simplify things ...)
		
		// The policy case is initialized as a complete copy of the base case:
		ScenarioForEvalData planfall = nullfall.createDeepCopy() ;
		
		// we are now looking at one specific OD relation (for this scenario, there is only one!)
		Values planfallForOD = planfall.getByODRelation(new IdImpl("BC")) ;
		{
			// modify the travel times for the rail mode:
			ValuesForAMode railValues = planfallForOD.getByMode( Mode.rail ) ;
			railValues.getByDemandSegment(Type.GV).incByEntry( Entry.km, -1. ) ;
			
			// modify some demand (presumably as a result):
			double delta = 100. ;
			railValues.getByDemandSegment(Type.GV).incByEntry( Entry.XX, delta ) ;
			planfall.getByODRelation(new IdImpl("BC")).getByMode(Mode.road).getByDemandSegment(Type.GV).incByEntry(Entry.XX, -delta ) ;
		}
		return planfall;
	}

}

