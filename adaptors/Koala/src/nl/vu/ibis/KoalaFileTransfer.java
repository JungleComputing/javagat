package nl.vu.ibis;

import org.koala.DM.IFileTransfer;

public class KoalaFileTransfer implements IFileTransfer {
	
	public int get(String URLFrom, String URLTo) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.get(" + URLFrom + ", " + URLTo + ")");
		return 0;
	}

	public int put(String URLFrom, String URLTo) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.put(" + URLFrom + ", " + URLTo + ")");
		return 0;
	}
	
	public long getLocalFileSize(String URL) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.getLocalFileSize(" + URL + ")");
		return 0;
	}

	public long getRemoteFileSize(String URL) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.getRemoteFileSize(" + URL + ")");
		return 0;
	}

	public boolean isLocalFilePresent(String URL) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.isLocalFilePresent(" + URL + ")");
		return false;
	}

	public boolean isRemoteFilePresent(String URL) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.isRemoteFilePresent(" + URL + ")");
		return false;
	}

	public void remoteMkdir(String URL) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.remoteMkdir(" + URL + ")");
	}

	public void stopFileTransfers() {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaFileTransfer.stopFileTransfer()");
	}
}
