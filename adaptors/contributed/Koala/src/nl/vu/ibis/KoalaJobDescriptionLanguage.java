package nl.vu.ibis;

import java.util.List;

import org.koala.JDL.JobDescriptionLanguage;

public class KoalaJobDescriptionLanguage implements JobDescriptionLanguage {

	public String add(String tag, String value) {

		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.add(" + tag + ", " + value + ")");
		
		
		
		return null;
	}

	public String add(String tag, String value, String cds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.add(" + tag + ", " + value + ", " + cds + ")");
		return null;
	}

	public String getCDS() {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.getCDS()");
		return null;
	}

	public List<String> getComponentsDescription(String jds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.getComponentsDescription(" + jds + ")");
		return null;
	}

	public List<String> getMulti(String tag) throws Exception {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.getMulti(" + tag + ")");
		return null;
	}

	public List<String> getMulti(String tag, String cds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.getMulti(" + tag + ", " + cds + ")");
		return null;
	}

	public String getSingle(String tag) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.getSingle(" + tag + ")");
		return null;
	}

	public String getSingle(String tag, String cds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.getSingle(" + tag + ", " + cds 
				+ ")");
		return null;
	}

	public String prepareCDSforSubmission(int compNo) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.prepareCDSforSubmission(" + compNo 
				+ ")");
		return null;
	}

	public String prepareCDSforSubmission(int compNo, String jds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.prepareCDSforSubmission(" + compNo 
				+ ", " + jds + ")");
		return null;
	}

	public String remove(String tag) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.remove(" + tag + ")");
		return null;
	}

	public String remove(String tag, String cds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.remove(" + tag + ", " + cds + ")");
		return null;
	}

	public String set(String tag, String value) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.set(" + tag + ", " + value + ")");
		return null;
	}

	public String set(String tag, String value, String cds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.set(" + tag + ", " + value + ", " 
				+ cds + ")");
		return null;
	}

	public void setCDS(String cds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.setCDS(" + cds + ")");
	}

	public boolean validateJDF() {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.validateJDF()");
		return true;
	}

	public boolean validateJDF(String jds) {
		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaobDescriptionLanguage.validateJDF(" + jds + ")");
		return true;
	}
}
