package com.example.angel.addressgooglemap;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ShareExternalServer {

	public Boolean shareRegIdWithAppServer(final Context context,
			final String regId) {

        JSONObject jsonSubscribe = new JSONObject();

        try {
            jsonSubscribe.put("user","cliente2");
            jsonSubscribe.put("type","android");
            jsonSubscribe.put("token",regId);
        }catch (Exception e)
        {
            Toast.makeText(context,"Error en envio de JSON",Toast.LENGTH_LONG);
            Log.e("JSONError","Erro r en envio de JSON para suscribir");
        }

        Boolean result=false;

		try {
			URL serverUrl = null;
			try {
				serverUrl = new URL(Config.APP_SERVER_URL);
			} catch (MalformedURLException e) {
				Log.e("AppUtil", "URL Connection Error: "
						+ Config.APP_SERVER_URL, e);

			}

            byte[] bytes =  jsonSubscribe.toString().getBytes();
			HttpURLConnection httpCon = null;
			try {
				httpCon = (HttpURLConnection) serverUrl.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setUseCaches(false);
				httpCon.setFixedLengthStreamingMode(bytes.length);
				httpCon.setRequestMethod("POST");
				httpCon.setRequestProperty("Content-Type","application/json");
                httpCon.connect();

                OutputStream out = httpCon.getOutputStream();
				out.write(bytes);
				out.close();

				int status = httpCon.getResponseCode();
				if (status == httpCon.HTTP_OK) {
					result = true;
				} else {
					result = false;
				}
			} finally {
				if (httpCon != null) {
					httpCon.disconnect();
				}
			}

		} catch (IOException e) {
            Toast.makeText(context,"Error in sharing with App Server",Toast.LENGTH_LONG);
			Log.e("AppUtil", "Error in sharing with App Server: " + e);
		}
		return result;
	}
}
