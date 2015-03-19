package eu.europeana.api2.v2.model.json.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.definitions.edm.beans.RichBean;

public class RichView extends ApiView implements RichBean {

	private String[] isShownBy;
	private String[] dcDescription;
	private String[] edmLandingPage;

	public RichView(RichBean bean, String profile, String wskey, long uid,
			boolean optOut) {
		super(bean, profile, wskey, uid, optOut);
		dcDescription = bean.getDcDescription();
		isShownBy = bean.getEdmIsShownBy();
		edmLandingPage = bean.getEdmLandingPage();
	}

	@Override
	public String[] getEdmIsShownBy() {
		if (ArrayUtils.isEmpty(isShownBy)) {
			return isShownBy;
		}
		// String provider = getProvider()[0];
		List<String>isShownByLinks = new ArrayList<String>();
		for (String item : isShownBy) {
			if (StringUtils.isBlank(item)) {
				continue;
			}
			isShownByLinks.add(item);
		}
		return isShownByLinks.toArray(new String[isShownByLinks.size()]);
	}

	@Override
	public String[] getDcDescription() {
		return dcDescription;
	}

	@Override
	public String[] getEdmLandingPage() {
		return edmLandingPage;
	}
}
