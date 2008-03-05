/*
 * MPA Source File: SgeResponse.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	14.10.2005 (13:52:12) by doerl $ Last Change: 10/14/05 (2:25:00 PM) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.gridlab.gat.resources.Job;

/**
 * @author  doerl
 */
class PbsResponse implements Serializable {
    private static final long serialVersionUID = -7940226296813758221L;
    private static final DateFormat sFormatter = new SimpleDateFormat("HH:mm:ss");
    private String mId = "";
    private String mName;
    private String mUser;
    private Date mTimeUse;
    private String mState;
    private String mQueue;

    private PbsResponse() {
    }

    public static PbsResponse parseJobLine(String msg) {
        StringTokenizer st = new StringTokenizer(msg);
        PbsResponse job = new PbsResponse();
        try {
            job.mId = (String) st.nextElement();
            job.mName = (String) st.nextElement();
            job.mUser = (String) st.nextElement();
            String timeUse = (String) st.nextElement();
            try {
                job.mTimeUse = sFormatter.parse(timeUse);
            }
            catch (ParseException ex) {
                job.mTimeUse = new Date(0L);
                //				System.err.println("DateFormat parse error: " + timeUse);
            }
            job.mState = (String) st.nextElement();
            if (st.hasMoreTokens()) {
                job.mQueue = (String) st.nextElement();
            }
        }
        catch (RuntimeException ex) {
        }
        return job;
    }

    public synchronized boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        try {
            PbsResponse other = (PbsResponse) obj;
            return equalsObj(mId, other.mId) && equalsObj(mName, other.mName)
                && equalsObj(mUser, other.mUser) && equalsObj(mState, other.mState)
                && equalsObj(mTimeUse, other.mTimeUse) && equalsObj(mQueue, other.mQueue);
        }
        catch (ClassCastException ex) {
            return false;
        }
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getQueue() {
        return mQueue;
    }

    public Date getTimeUse() {
        return mTimeUse;
    }

    public int getState() {
        if (mState == null) {
            System.err.println("Internal error in sge adaptor, no state returned");
        }
        if (mState.indexOf('W') >= 0) { // waiting
            return Job.SCHEDULED;
        }
        if (mState.indexOf('Q') >= 0) { // queued
            return Job.SCHEDULED;
        }
        if (mState.indexOf('T') >= 0) { // transition
            return Job.SCHEDULED;
        }
//         if (mState.indexOf('H') >= 0) { 
//             return Job.HOLD;
//         }
        if (mState.indexOf('S') >= 0) { // suspended
            return Job.RUNNING;
        }
        if (mState.indexOf('R') >= 0) { // running
            return Job.RUNNING;
        }
        if (mState.indexOf('E') >= 0) { // exiting
            return Job.STOPPED;
        }
        return Job.STOPPED;
    }

    public String getUser() {
        return mUser;
    }

    public int hashCode() {
        int result = mId.hashCode();
        if (mName != null) {
            result += mName.hashCode();
        }
        if (mUser != null) {
            result += mUser.hashCode();
        }
        if (mTimeUse != null) {
            result += mTimeUse.hashCode();
        }
        if (mState != null) {
            result += mState.hashCode();
        }
        if (mQueue != null) {
            result += mQueue.hashCode();
        }
        return result;
    }

    private static boolean equalsObj(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }
}
