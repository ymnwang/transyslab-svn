/**
 *
 */
package com.transyslab.simcore.mesots;

/**
 * @author its312
 *
 */
public class MesoStatus {

	protected String logFile_;
	protected int nErrors_; // number of errors (a fatal error
	protected int nMsgs_;
	// will terminate the program)
	// 未处理 ofstream osLogFile_; // error log

	protected int nActive_; // number of vehicle in network
	protected int nArrived_; // number of vehicle arrived
	protected int nNoPath_; // no path
	protected int nInQueue_; // number of vehicle queuing outside
	protected int nCells_; // number of traffic cells

	private MesoStatus() {
		nErrors_ = 0;
		nMsgs_ = 0;
		logFile_ = null;
	}
	private static MesoStatus theStatus = new MesoStatus();
	public static MesoStatus getInstance() {
		return theStatus;
	}/*
		 * public ofstream osLogFile() { return osLogFile_; }
		 *
		 * public void openLogFile() { logFile_ =
		 * Copy(ToolKit.outfile("meso.out")); osLogFile_.open(logFile_); }
		 * public void closeLogFile() { osLogFile_.close(); if (nErrors_!=0) {
		 * //未处理 cout << endl << nErrors_ << " error(s) detected. " //未处理 <<
		 * "See <" << logFile_ << "> for details." << endl; } else if (!nMsgs_)
		 * { remove(logFile_); } } public void clean() { nActive_ = 0; nArrived_
		 * = 0; nNoPath_ = 0; nInQueue_ = 0; nCells_ = 0; if (logFile_!=null) {
		 * //未处理 delete [] logFile_; logFile_ = null; } }
		 *
		 * public int nActive(int n) { return nActive_ += n; } public int
		 * nArrived(int n) { return nArrived_ += n; } public int nNoPath(int n)
		 * { return nNoPath_ += n; } public int nInQueue(int n) { return
		 * nInQueue_ += n; } public int nCells(int n) { return nCells_ += n; }
		 *
		 * public int nActive() { return nActive_; } public int nArrived() {
		 * return nArrived_; } public int nInQueue() { return nInQueue_; }
		 * public int nCells() { return nCells_; }
		 *
		 * public void nErrors(int n) { nErrors_ += n; } public int nErrors() {
		 * return nErrors_; } public void nMsgs(int n) { nMsgs_ += n; }
		 *
		 * public void report(){
		 *
		 * } //未处理 public void print(ostream &os = cout); //未处理extern
		 * MESO_Status *theStatus;
		 */
}
