/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.function.IntPredicate;

import com.transyslab.commons.tools.Random;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.Segment;
import com.transyslab.roadnetwork.SurvStation;
import com.transyslab.roadnetwork.Vehicle;

/**
 * @author its312
 *
 */
public class MesoVehicle extends Vehicle {

	protected final int FLAG_PROCESSED = 0x10000000;
	protected MesoTrafficCell trafficCell_; // pointer to current traffic cell
	protected MesoVehicle leading_; // downstream vehicle
	protected MesoVehicle trailing_; // upstream vehicle
	protected int SensorIDFlag_;
	protected int flags_; // indicator for internal use
	// protected boolean countFlags_;

	// These variables are use to cache the calculation for speeding
	// up

	protected float spaceInSegment_; // update each time it enter a new segment

	protected static int[] stepCounter_ = new int[Constants.THREAD_NUM]; // iteration counter
	protected static int[] vhcCounter_ = new int[Constants.THREAD_NUM];  	//在网车辆数统计
	public MesoVehicle() {
		trafficCell_ = null;
		leading_ = null;
		trailing_ = null;
	}

	public void toggleFlag(int flag) {
		flags_ ^= flag;
	}
	public int flag(int mask) {// 0xffffffff
		return (flags_ & mask);
	}
	public void setFlag(int s) {
		flags_ |= s;
	}
	public void unsetFlag(int s) {
		flags_ &= ~s;
	}
	public static int nVehicles(){
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return vhcCounter_[threadid];
	}
	public static void setVehicleCounter(int vhcnum){
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		vhcCounter_[threadid] = vhcnum;
	}
	public MesoLink getNextMesoLink() {
		return (MesoLink) nextLink_;
	}

	public MesoLink mesoLink() {
		return (MesoLink) link();
	}

	public MesoSegment mesoSegment() {
		return (MesoSegment) segment();
	}

	@Override
	public Link link() 
	{
		return trafficCell_ != null ? trafficCell_.link() : (Link) null;
	}
	@Override
	public Segment segment() 
	{
		return trafficCell_ != null ? trafficCell_.segment() : (Segment) null;
	}

	public MesoTrafficCell trafficCell() {
		return trafficCell_;
	}

	public void leading(MesoVehicle pv) {
		leading_ = pv;
	}
	public void trailing(MesoVehicle pv) {
		trailing_ = pv;
	}
	public MesoVehicle leading() {
		return leading_;
	}
	public MesoVehicle trailing() {
		return trailing_;
	}
	public float dnPos() {
		return distance_;
	}
	public float upPos() {
		return distance_ + spaceInSegment_;
	}
	public float spaceInSegment() {
		return spaceInSegment_;
	}
	public void calcSpaceInSegment() {
		spaceInSegment_ = length_ / segment().nLanes();
	}

	public MesoVehicle leadingVehicleInStream() {
		float dndis = distanceFromDownNode();

		if (dndis > MesoParameter.getInstance().channelizeDistance()) {
			return leading_;
		}

		MesoVehicle front = leading_;

		// Find the first vehicle in the same traffic stream that goes
		// to the same direction as this vehicle

		while (front != null && front.nextLink_ != nextLink_) {
			front = front.leading_;
		}

		return front;
	}

	public float gapDistance(MesoVehicle front) {
		if (front == null)
			return Constants.FLT_INF;

		MesoSegment ps = front.trafficCell_.segment();
		if (trafficCell_.segment() == ps) { // same segment
			return distance_ - front.upPos();
		}
		else { // different segment
			float gap = (float) (distance_ + (ps.getLength() - front.upPos()));
			return gap;
		}
	}

	public void updateSpeed() // interpolate speed
	{
		int i;

		if (trafficCell_.nHeads_ > 1 && nextLink_ != null && segment().isHead() != 0) {
			i = nextLink_.getDnIndex();
		}
		else {
			i = 0;
		}

		float headspd = trafficCell_.headSpeed(i);
		float headpos = trafficCell_.headPosition(i);

		if (distance_ <= headpos) {
			currentSpeed_ = headspd;
		}
		else if (distance_ >= trafficCell_.tailPosition()) {
			currentSpeed_ = trafficCell_.tailSpeed();
		}
		else {
			float dx = trafficCell_.tailPosition() - headpos;
			if (dx < 0.1) {
				currentSpeed_ = headspd;
			}
			else {
				float dv = (trafficCell_.tailSpeed() - headspd) / dx;
				currentSpeed_ = headspd + dv * (distance_ - headpos);
			}
		}

		// This avoid large gaps in queue stream

		if (currentSpeed_ < MesoParameter.getInstance().minSpeed()
				&& gapDistance(leading_) > MesoParameter.getInstance().minHeadwayGap()) {
			currentSpeed_ = MesoParameter.getInstance().minSpeed();
		}
	}
	/*
	 * public int init(int id, int ori, int des, int type_id, int path_id ) //
	 * virtual int type_id = 0, int path_id = -1 { int error = superInit(id,
	 * ori, des, type_id, path_id); if (error < 0) return 1; // show a warning
	 * msg enterPretripQueue(); return 0; }
	 */

	@Override
	public void initialize() // virtual, called by init()
	{
		flags_ = 0;
		SensorIDFlag_ = -100000;
		int prefix = type_ & (~Constants.VEHICLE_CLASS); // prefix, e.g., HOV
		int vehicle_class = (type_ & Constants.VEHICLE_CLASS);

		Random theRandomizer = Random.getInstance().get(Random.Departure);

		if (vehicle_class == 0) {
			vehicle_class = theRandomizer.drandom(MesoParameter.getInstance().nVehicleClasses(),
					MesoParameter.getInstance().vehicleClassCDF());
		}
		type_ = vehicle_class;

		// Attach some extra bits to "type" based on given probabilities if
		// the prefix is not specified

		if (prefix != 0) {
			type_ |= prefix;
		}
		else {
			if (theRandomizer.brandom(MesoParameter.getInstance().etcRate()) != 0) {

				// This implementation treats all ETC vehicles as AVI

				type_ |= (Constants.VEHICLE_ETC | Constants.VEHICLE_PROBE);
			}

			if (theRandomizer.brandom(MesoParameter.getInstance().guidedRate()) != 0) {
				type_ |= Constants.VEHICLE_GUIDED;
			}

			if (theRandomizer.brandom(MesoParameter.getInstance().hovRate()) != 0) {
				type_ |= Constants.VEHICLE_HOV;
			}
		}
		mileage_ = 0.0f;
	}

	public void enterPretripQueue() {
		// int error_quota = 100;

		if (nextLink_ == null) { // No path
			/*
			 * if (!theStatus.nErrors()) { //未处理 theStatus.osLogFile() <<
			 * LOG_FILE_HEADER_MSG << endl; } if (error_quota > 0) {
			 * theStatus.osLogFile() //未处理 << code_ << "\t" //未处理 <<
			 * theSimulationClock.currentTime() << "\t" //未处理 <<
			 * oriNode().code() << "-" << desNode().code() << "\t" //未处理 <<
			 * endl; } else if (error_quota == 0) { theStatus.osLogFile() //未处理
			 * << endl << PATH_ERROR_MSG //未处理 << endl; }
			 */
			// error_quota --;
			// theStatus.nErrors(+1);
			MesoVehicleList.getInstance().recycle(this);
			return;
		}
		getNextMesoLink().queue(this);
		trafficCell_ = null;
		float spd = ((MesoSegment) nextLink_.getStartSegment()).maxSpeed();
		distance_ = (float) (-spd * SimulationClock.getInstance().getStepSize());
	}

	public int enterNetwork() {
		if (getNextMesoLink().isJammed() != 0)
			return 0;

		// Delete this vehicle from the link queue.

		getNextMesoLink().dequeue(this);

		getNextMesoLink().append(this);
		OnRouteChoosePath(nextLink_.getDnNode());
		updateSpeed();

		//统计在网车辆数
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		//进入路网车辆，计数+1
		vhcCounter_[threadid]++;
		
		return 1;
	}
	public void appendTo(MesoTrafficCell cell) {
		MesoSegment ps = cell.segment();
		trafficCell_ = cell;
		calcSpaceInSegment();
		distance_ += ps.getLength();
		currentSpeed_ = cell.tailSpeed();

		// Make sure not crash into front vehicle

		if (leading_ != null) { // no overtaking
			float pos = leading_.upPos() + MesoParameter.getInstance().minHeadwayGap() / ps.nLanes();
			distance_ = Math.max(distance_, pos);
		}
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		if ((stepCounter_[threadid] & 0x1) != 0) {
			setFlag(FLAG_PROCESSED);
		}
		//奇数步长：flag =  FLAG_PROCESSED异或FLAG_PROCESSED = 0
  	    //偶数步长：flag = 0异或FLAG_PROCESSED = FLAG_PROCESSED
		markAsProcessed();
	}
    public void appendSnapshotTo(MesoTrafficCell cell)
    {
    	  MesoSegment ps = cell.segment();
    	  trafficCell_ = cell;
    	  calcSpaceInSegment();
    }
	public void move() // update position
	{

		// There is a strange bug that I have not figured out after a week
		// of effort. This is a dirty fix and it works only on sgi.
		// 溢出,finite()返回1表示数值正常，返回0代表溢出（无穷大）
		/*
		 * if (!finite(distance_) || !finite(currentSpeed_)) {
		 * theStatus.osLogFile() //未处理 << "Vehicle " << code_ << " (" //未处理 <<
		 * oriNode().code() << "-" //未处理 << desNode().code() << ") removed at "
		 * //未处理 << theSimulationClock.currentTime() << " from " //未处理 <<
		 * link().code() << ":" << segment().code() << endl;
		 *
		 * theStatus.nErrors(+1); theStatus.nNoPath(+1); theStatus.nActive(-1);
		 * trafficCell_.remove(this);
		 * MESO_VehicleList.getInstance().recycle(this); return; }
		 */

		// Prevent to be processed twice if it moved into a new link

		markAsProcessed();

		// Leading vehicle in the same traffic stream

		MesoVehicle front = leadingVehicleInStream();

		float frequency = (float) (1.0 / SimulationClock.getInstance().getStepSize());
		float oldpos = distance_;
		int gone = 0;

		// Calculate the current speed

		updateSpeed();

		// Position at the end of this interval

		distance_ -= currentSpeed_ * SimulationClock.getInstance().getStepSize();

		if (front != null) { // no overtaking
			float pos = front.upPos() + MesoParameter.getInstance().minHeadwayGap() / segment().nLanes();
			distance_ = Math.max(distance_, pos);
		}

		// 节段是否有检测器
		if (segment().getSurvList() != null) {
			ListIterator<SurvStation> i = segment().getSurvList().listIterator();
			while (i.hasNext()) {
				SurvStation tmp = i.next();
				// 车辆是否经过检测断面
				// 两个检测器之间距离需超过8米
				if (distance_ <= tmp.position() && tmp.position() - distance_ < 8 && SensorIDFlag_ != tmp.getCode()) {
					SensorIDFlag_ = tmp.getCode();
					tmp.sectionMeasure(currentSpeed_);
				}

			}
		}

		if (distance_ < 0.0) { // cross segment

			if ((gone = transpose()) == 0) { // can not moved out
				distance_ = 0.0f; // the maximum it can move
			}
		}

		if (gone < 0) { // removed from the network
			return;
		}
		else if (gone == 0) { // still in the same segment
			if (distance_ >= oldpos) { // not moved
				currentSpeed_ = 0.0f;
				return; // no need to enter advance()
			}
			else {
				currentSpeed_ = (oldpos - distance_) * frequency;
			}
		}
		else { // moved into a downstream segment
			currentSpeed_ = (float) ((oldpos + segment().getLength() - distance_) * frequency);
		}

		advance(); // sort position in list
	}
	public int transpose() // returns 1 if it success
	{
		int error_quota = 100;

		MesoSegment ps = mesoSegment();

		if (ps.isMoveAllowed() == 0)
			return 0;

		mileage_ += ps.getLength(); // record mileage

		int done = 0;
		MesoSegment ds = (MesoSegment) segment().getDnSegment();

		if (ds != null) {

			if (ds.isJammed() == 0) {
				trafficCell_.remove(this);
				ds.append(this);
				done = 1;
			}

		}
		else if (nextLink_ != null) { // cross link

			if (getNextMesoLink().isJammed() == 0) {

				/*
				 * if (theEngine.chosenOutput(OUTPUT_VEHICLE_PATH_RECORDS)) {
				 * writePathRecord(theFileManager.osPathRecord()); }
				 *//*
					 * if(code_ == 108){ System.out.println(done); }
					 */
				mesoLink().recordTravelTime(this);

				// Estimate the time this vehicle enter the link

				float dt;
				if (currentSpeed_ > 1.0) {
					dt = distance_ / currentSpeed_;
				}
				else {
					dt = (float) (0.5 * SimulationClock.getInstance().getStepSize());
				}
				timeEntersLink_ = (float) (SimulationClock.getInstance().getCurrentTime() - dt);

				trafficCell_.remove(this);

				if (MesoNetwork.getInstance().isNeighbor(link(), nextLink_) != 0) {
					getNextMesoLink().append(this);
					OnRouteChoosePath(nextLink_.getDnNode());
					done = 1;
				}
				else {
					/*
					 * if (!theStatus.nErrors()) { //未处理 theStatus.osLogFile()
					 * << LOG_FILE_HEADER_MSG << endl; }
					 *
					 * if (error_quota > 0) { theStatus.osLogFile() //未处理 <<
					 * code_ << "\t" //未处理 << theSimulationClock.currentTime()
					 * << "\t" //未处理 << oriNode().code() << "-" <<
					 * desNode().code() << "\t" //未处理 << segment().code(); }
					 * else if (error_quota == 0) { theStatus.osLogFile() <<
					 * endl << PATH_ERROR_MSG << endl;
					 */

					error_quota--;

					// theStatus.nErrors(1);
					// theStatus.nNoPath(1);
					mesoLink().recordTravelTime(this);
					removeFromNetwork();
					done = -1;
				}
			}

		}
		else { // arrive destination

			mesoLink().recordTravelTime(this);
			removeFromNetwork();
			done = -1;
		}

		if (done != 0) {
			ps.scheduleNextEmitTime();

			if (done > 0) {
				calcSpaceInSegment();
			}
		}

		return done;
	}

	// Advance the vehicle to a position in the vehicle list that
	// corresponding to its current value of "distance_". This function
	// is invoked when a vehicle is moved (including moved into a
	// downstream segment), so that the vehicles in macro vehicle list is
	// always sorted by their position.
	public void advance() // update position in list
	{
		// (0) Check if this vehicle should be advanced in the list

		if (leading_ == null || distance_ >= leading_.distance_) {

			// no need to advance this vehicle in the list

			return;
		}

		// (1) Find the vehicle's position in the list

		MesoVehicle front = leading_;

		while (front != null && distance_ < front.distance_) {
			front = front.leading_;
		}

		// (2) Take this vehicle out from the list

		leading_.trailing_ = trailing_;

		if (trailing_ != null) {
			trailing_.leading_ = leading_;
		}
		else { // last vehicle in the segment
			trafficCell_.lastVehicle_ = leading_;
		}

		// (3) Insert this vehicle after the front

		// (3.1) Pointers with the leading vehicle

		leading_ = front;

		if (leading_ != null) {
			trailing_ = leading_.trailing_;
			leading_.trailing_ = this;
		}
		else {
			trailing_ = trafficCell_.firstVehicle_;
			trafficCell_.firstVehicle_ = this;
		}

		// (3.2) Pointers with the trailing vehicle

		if (trailing_ != null) {
			trailing_.leading_ = this;
		}
		else { // this vehicle becomes the last one
			trafficCell_.lastVehicle_ = this;
		}
	}

	public float expectedPosition() {
		return 0;
	}

	@Override
	public int enRoute() { // virtual
		return attr(Constants.ATTR_ACCESSED_INFO);
	}

	// Call route choice model

	public void changeRoute() {
		// 未处理 if (!(isSpFlag(INFO_FLAG_VMS_BASED))) {
		// These bit is usuallly set when it view a ENROUTE_VMS. Here we
		// bypass the requirement for location based information access if
		// the system is not VMS/Beacon based.
		// 未处理 setAttr(ATTR_ACCESSED_INFO);
		// }
		if (link() != null) { // alreay in the network
			OnRouteChoosePath(link().getDnNode());
		}
		else { // in spillback queue
			OnRouteChoosePath(nextLink_.getUpNode());
		}
	}

	// Called when a vehicle arrived its desitination

	public void removeFromNetwork() {

		trafficCell_.remove(this);
		MesoVehicleList.getInstance().recycle(this);
		//统计在网车辆数
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		//离开路网车辆，计数-1
		vhcCounter_[threadid]--;

	}
	/*
	 * public void report() { MESO_Parameter p = theParameter;
	 *
	 * int heading = 1; float t = theSimulationClock.currentTime() -
	 * departTime_; ostream os = theFileManager.osVehicle(); if (heading) { if
	 * (!theEngine.skipComment()) { //未处理 os <<
	 * "# ID/Type/Ori/DepDes/ArrDes/DepTime/"; //未处理 os <<
	 * "ArrTime/Mileage/Speed" << endl; } heading = 0; }
	 *
	 * int actual_des_node = link().getDnNode().code();
	 *
	 * //未处理 os << code_ << endc //未处理 << type_ << endc //未处理 <<
	 * oriNode().code() << endc //未处理 << desNode().code() << endc //未处理 <<
	 * actual_des_node << endc //未处理 << Fix(departTime_, (float)1.0) << endc
	 * //未处理 << Fix(theSimulationClock.currentTime(), 1.0) << endc //未处理 <<
	 * Fix(mileage_ / p.lengthFactor(), (float)1.0) << endc //未处理 <<
	 * Fix((mileage_ / t) / p.speedFactor(), (float)0.1) << endl; }
	 */

	// These are used to prevent to process a vehicle twice in a
	// single iteration

	public void markAsProcessed() {
		flags_ ^= FLAG_PROCESSED;
	}
	public int isProcessed() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		if ((stepCounter_[threadid] & 0x1) != 0) { // odd step
			if ((flags_ & FLAG_PROCESSED) == 0)
				return 1;
			else
				return 0;
		}
		else { // even step
			return (flags_ & FLAG_PROCESSED);
		}
	}
	public static void increaseCounter() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		stepCounter_[threadid]++;
	}

}
