package playground.balac.retailers.data;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.MatsimServices;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;

import playground.balac.retailers.strategies.CatchmentAreaRetailerStrategy;
import playground.balac.retailers.strategies.CustomersFeedbackStrategy;
import playground.balac.retailers.strategies.LogitMaxLinkRetailerStrategy;
import playground.balac.retailers.strategies.MaxActivitiesRetailerStrategy;
import playground.balac.retailers.strategies.MaxLinkRetailerStrategy;
import playground.balac.retailers.strategies.MinTravelingCostsRetailerStrategy;
import playground.balac.retailers.strategies.RandomRetailerStrategy;
import playground.balac.retailers.strategies.RetailerStrategy;


public class Retailer
{
  private final Id<Retailer> id;
  private final Map<Id<ActivityFacility>, ActivityFacilityImpl> facilities = new TreeMap<>();
  private static final Logger log = Logger.getLogger(Retailer.class);
  private RetailerStrategy strategy;
  private Map<Id<ActivityFacility>, ActivityFacilityImpl> movedFacilities = new TreeMap<>();

  public Retailer(Id<Retailer> id, RetailerStrategy rs) {
    this.id = id;
    this.strategy = rs;
  }

  public final Id<Retailer> getId() {
    return this.id;
  }

  public final boolean addFacility(ActivityFacilityImpl f) {
    if (f == null) return false;
    if (this.facilities.containsKey(f.getId())) return false;
    this.facilities.put(f.getId(), f);

    return true;
  }

  public final boolean addStrategy(MatsimServices controler, String strategyName)
  {
    if (strategyName.contains("randomRetailerStrategy")) {
      this.strategy = new RandomRetailerStrategy(controler);
      return true;
    }
    if (strategyName.contains("maxLinkRetailerStrategy")) {
      this.strategy = new MaxLinkRetailerStrategy(controler);
      return true;
    }
    if (strategyName.contains("logitMaxLinkRetailerStrategy")) {
      this.strategy = new LogitMaxLinkRetailerStrategy(controler);
      return true;
    }
    if (strategyName.contains("catchmentAreaRetailerStrategy")) {
      this.strategy = new CatchmentAreaRetailerStrategy(controler);
      return true;
    }
    if (strategyName.contains("customersFeedbackStrategy")) {
      this.strategy = new CustomersFeedbackStrategy(controler);
      return true;
    }
   
    if (strategyName.contains("maxActivitiesRetailerStrategy")) {
      this.strategy = new MaxActivitiesRetailerStrategy(controler);
      return true;
    }
    if (strategyName.contains("minTravelingCostsRetailerStrategy")) {
      this.strategy = new MinTravelingCostsRetailerStrategy(controler);
      return true;
    }
    throw new RuntimeException("The strategy has been not added!");
  }

  public final ActivityFacilityImpl getFacility(Id<ActivityFacility> facId) {
    return (this.facilities.get(facId));
  }

  public final Map<Id<ActivityFacility>, ActivityFacilityImpl> getFacilities() {
    return this.facilities;
  }

  public final void runStrategy(TreeMap<Id<Link>, LinkRetailersImpl> links) {
	
    this.movedFacilities = this.strategy.moveFacilities(this.facilities, links);
  }

  public Map<Id<ActivityFacility>, ActivityFacilityImpl> getMovedFacilities() {
    return this.movedFacilities;
  }
}
