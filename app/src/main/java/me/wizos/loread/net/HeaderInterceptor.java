package me.wizos.loread.net;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;


public class HeaderInterceptor implements Interceptor {

	private String mAuthToken;

	public HeaderInterceptor(String authToken) {
		mAuthToken = authToken;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();

		Request.Builder builder = request.newBuilder()
			.addHeader("AppId", API.INOREADER_APP_ID)
			.addHeader("AppKey", API.INOREADER_APP_KEY);

		if (mAuthToken != null) {
			builder.addHeader("Authorization", "GoogleLogin auth=" + mAuthToken);
		}

		return chain.proceed(builder.build());
	}
}
