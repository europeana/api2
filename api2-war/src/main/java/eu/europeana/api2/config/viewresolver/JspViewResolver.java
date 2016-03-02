package eu.europeana.api2.config.viewresolver;

import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
public class JspViewResolver extends InternalResourceViewResolver {

    public JspViewResolver() {
        setViewClass(JstlView.class);
        setPrefix("/WEB-INF/jsp/");
        setSuffix(".jsp");
    }
}
