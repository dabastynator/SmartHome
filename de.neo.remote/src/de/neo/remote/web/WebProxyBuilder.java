package de.neo.remote.web;

import java.lang.reflect.Proxy;
import de.neo.remote.web.WebProxyBuilder;

public class WebProxyBuilder {

	private String mEndPoint;

	private Class<?> mInterface;

	private String mSecurityToken;

	public WebProxyBuilder setEndPoint(String endPoint) {
		mEndPoint = endPoint;
		return this;
	}

	public WebProxyBuilder setInterface(Class<?> interface1) {
		mInterface = interface1;
		return this;
	}

	public WebProxyBuilder setSecurityToken(String securityToken) {
		mSecurityToken = securityToken;
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> T create() {
		WebProxy proxy = new WebProxy(mEndPoint, mSecurityToken);
		Object object = Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class[] { mInterface }, proxy);
		return (T) object;
	}

}
