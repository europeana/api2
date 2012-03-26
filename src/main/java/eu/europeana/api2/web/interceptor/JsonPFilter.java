package eu.europeana.api2.web.interceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.europeana.api2.utils.GenericResponseWrapper;

public class JsonPFilter implements Filter {

	@Override
	public void doFilter(ServletRequest sReq, ServletResponse sRes, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) sReq;
		HttpServletResponse response = (HttpServletResponse) sRes;

		@SuppressWarnings("unchecked")
		Map<String, String[]> parms = request.getParameterMap();

		if (parms.containsKey("callback")) {
			OutputStream out = response.getOutputStream();
			GenericResponseWrapper wrapper = new GenericResponseWrapper(response);
			chain.doFilter(request, wrapper);
			out.write(new String(parms.get("callback")[0] + "(").getBytes());
			out.write(wrapper.getData());
			out.write(new String(");").getBytes());
			out.close();
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// do nothing
	}

}
