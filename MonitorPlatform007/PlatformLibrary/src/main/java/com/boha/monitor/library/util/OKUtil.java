package com.boha.monitor.library.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.util.Log;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by aubreyM on 16/01/15.
 */

/**
 * Helper class to wrap OKHttp library and provide methods
 * that access data from Monitor Platform server
 */
public class OKUtil {


    static Gson gson = new Gson();
   // public static final String DEV_URL = "http://10.0.0.102:40405/mp/gatex";
    public static final String PROD_URL = "http://bohamaker.com:3030/mp/gatex";

    //public static final String DEV_URL_CACHED = "http://192.168.1.253:40405/mp/cachedRequests";

    //public static final String URL = "http://10.0.0.102:8080/mp/gatex";
    public static final String DEV_URL = "http://10.0.0.102:8080/mp/gatex";
    //public static final String DEV_URL = "http://10.0.0.102:40405/mp/gatex";
   // public static final String PROD_URL = "http://bohamaker.com:3030/mp/gatex";

   // public static final String DEV_URL_CACHED = "http://10.0.0.102:40405/mp/cachedRequests";
    public static final String DEV_URL_CACHED = "http://10.0.0.102:8080/mp/cachedRequests";


    public static final String PROD_URL_CACHED = "http://bohamaker.com:3030/mp/cachedRequests";

    static final String FAILED_RESPONSE_NOT_SUCCESSFUL = "Request failed. Response not successful";
    static final String FAILED_DATA_EXTRACTION = "Request failed. Unable to extract data from response";
    static final String FAILED_IO = "Request failed. Communication links are not working";
    static final String FAILED_UNPACK = "Unable to unpack zipped response";

    static final String LOG = OKUtil.class.getSimpleName();

    public interface OKListener {
        void onResponse(ResponseDTO response);

        void onError(String message);
    }


    private void configureTimeouts(OkHttpClient client) {
        client.setConnectTimeout(40, TimeUnit.SECONDS);
        client.setReadTimeout(60, TimeUnit.SECONDS);
        client.setWriteTimeout(40, TimeUnit.SECONDS);

    }

    private String getURL(Context ctx) {
        boolean isDebuggable = 0 != (ctx.getApplicationInfo().flags
                &= ApplicationInfo.FLAG_DEBUGGABLE);
        if (isDebuggable) {
            return DEV_URL;
          //  return PROD_URL;
        } else {
            return DEV_URL;
         //  return PROD_URL;
        }
    }

    private String getURLCached(Context ctx) {

        boolean isDebuggable = 0 != (ctx.getApplicationInfo().flags
                &= ApplicationInfo.FLAG_DEBUGGABLE);
        if (isDebuggable) {
            return DEV_URL_CACHED;
         //   return PROD_URL_CACHED;
        } else {
            return DEV_URL_CACHED;
        //   return PROD_URL_CACHED;
        }
    }

    /**
     * Async GET call with OKListener to return data to caller
     *
     * @param req
     * @param listener
     * @throws OKHttpException
     */
    public void sendGETRequest(final Context ctx, final RequestDTO req,
                               final OKListener listener) throws OKHttpException {
        String mURL = getURL(ctx);
        OkHttpClient client = new OkHttpClient();
        configureTimeouts(client);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(mURL).newBuilder();
        urlBuilder.addQueryParameter("JSON", gson.toJson(req));
        String url = urlBuilder.build().toString();

        Log.w(LOG, "### sending request to server, requestType: "
                + req.getRequestType()
                + "\n" + url);

        Request okHttpRequest = new Request.Builder()
                .url(url)
                .build();

        execute(client, okHttpRequest, req.isZipResponse(), listener);
    }

    public void sendPOSTRequest(final Context ctx,
                                final RequestList req,
                                final OKListener listener) throws OKHttpException {

        String url = getURLCached(ctx);
        OkHttpClient client = new OkHttpClient();
        configureTimeouts(client);
        RequestBody body = new FormEncodingBuilder()
                .add("JSON", gson.toJson(req))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.w(LOG, "### sending request to server, requestList: "
                + req.getRequests().size()
                + "\n" + url);
        execute(client, request, false, listener);


    }

    public ResponseDTO sendSynchronousGET(final Context ctx, final RequestDTO req) throws OKHttpException, IOException {
        final long start = System.currentTimeMillis();
        String mURL = getURL(ctx);
        OkHttpClient client = new OkHttpClient();
        configureTimeouts(client);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(mURL).newBuilder();
        urlBuilder.addQueryParameter("JSON", gson.toJson(req));
        String url = urlBuilder.build().toString();

        Log.w(LOG, "### sending request to server, requestType: "
                + req.getRequestType()
                + "\n" + url);

        Request okHttpRequest = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(okHttpRequest).execute();
        ResponseDTO m;
        if (req.isZipResponse()) {
            m = processZipResponse(response, null);
        } else {
            m = processResponse(response, null);
        }
        
        final long end = System.currentTimeMillis();
        Log.e(LOG, "### Server responded, " + okHttpRequest.urlString() + "\nround trip elapsed: " + getElapsed(start, end)
                + ", server elapsed: " + m.getElapsedRequestTimeInSeconds()
                + ", statusCode: " + m.getStatusCode()
                + "\nmessage: " + m.getMessage());
        return m;
    }

    public void sendPOSTRequest(final Context ctx,
                                final RequestDTO req,
                                final OKListener listener) throws OKHttpException {

        String url = getURL(ctx);
        OkHttpClient client = new OkHttpClient();
        configureTimeouts(client);
        RequestBody body = new FormEncodingBuilder()
                .add("JSON", gson.toJson(req))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Log.w(LOG, "### sending request to server, requestType: "
                + req.getRequestType()
                + "\n" + url);
        execute(client, request, false, listener);


    }

    private void execute(OkHttpClient client, final Request req,
                         final boolean zipResponseRequested,
                         final OKListener listener) {

        final long start = System.currentTimeMillis();
        Callback callback = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                long end = System.currentTimeMillis();
                Log.e(LOG, "### Server responded with ERROR, round trip elapsed: " + getElapsed(start, end));
                listener.onError(FAILED_IO);
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (!response.isSuccessful()) {
                    Log.e(LOG, "%%%%%% ERROR from OKHttp "
                            + response.networkResponse().toString()
                            + response.message() + "\nresponse.isSuccessful: " + response.isSuccessful());
                    listener.onError(FAILED_RESPONSE_NOT_SUCCESSFUL);
                    return;
                }
                ResponseDTO serverResponse = new ResponseDTO();
                if (zipResponseRequested) {
                    processZipResponse(response, listener);

                } else {
                    processResponse(response, listener);
                }
                response.body().close();
                long end = System.currentTimeMillis();
                Log.e(LOG, "### Server responded, " + req.urlString() + "\nround trip elapsed: " + getElapsed(start, end)
                        + ", server elapsed: " + serverResponse.getElapsedRequestTimeInSeconds()
                        + ", statusCode: " + serverResponse.getStatusCode()
                        + "\nmessage: " + serverResponse.getMessage());
            }
        };

        client.newCall(req).enqueue(callback);

    }

    private ResponseDTO processResponse(Response response, OKListener listener) {
        ResponseDTO serverResponse = new ResponseDTO();
        try {
            String json = response.body().string();
            serverResponse = gson.fromJson(json, ResponseDTO.class);
            Log.w(LOG, "### Data received: " + getLength(json.length()));
            if (listener != null) {
                if (serverResponse.getStatusCode() == 0) {
                    listener.onResponse(serverResponse);
                } else {
                    listener.onError(serverResponse.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(LOG, "OKUtil Failed to get data from response body", e);
            if (listener != null) {
                listener.onError(FAILED_DATA_EXTRACTION);
            } else {
                serverResponse.setStatusCode(88);
                serverResponse.setMessage("Failed to get server response data");
            }
        }
        return serverResponse;
    }

    private ResponseDTO processZipResponse(Response response, OKListener listener) {
        final File directory = Environment.getExternalStorageDirectory();
        ResponseDTO serverResponse = new ResponseDTO();
        try {
            File file = new File(directory, "myData.zip");
            File uFile = new File(directory, "data.json");

            InputStream is = response.body().byteStream();
            OutputStream os = new FileOutputStream(file);
            IOUtils.copy(is, os);
            String json = ZipUtil.unpack(file, uFile);
            Log.i(LOG, "### Data received size: " + getLength(file.length())
                    + " ==> unpacked: " + getLength(uFile.length()));
            serverResponse = gson.fromJson(json, ResponseDTO.class);
            try {
                boolean OK1 = file.delete();
                boolean OK2 = uFile.delete();
                if (OK1 && OK2) {
                    Log.e(LOG, "Temporary unpacking files deleted OK");
                }
            } catch (Exception e) {
                Log.e(LOG, "Temporary unpacking files NOT deleted. " + e.getMessage());
            }

        } catch (Exception e) {
            Log.e(LOG, "Failed to unpack file", e);
            try {
                response.body().close();
                if (listener != null) {
                    listener.onError(FAILED_UNPACK);
                } else {
                    serverResponse.setStatusCode(88);
                    serverResponse.setMessage("Error processing data from the server");
                    return serverResponse;
                }
            } catch (IOException e1) {
                Log.e(LOG, "failed", e);
            }

        }
        if (listener != null) {
            if (serverResponse.getStatusCode() == 0) {
                listener.onResponse(serverResponse);
            } else {
                listener.onError(serverResponse.getMessage());
            }
        }

        return serverResponse;
    }

    String getElapsed(long start, long end) {
        BigDecimal bs = new BigDecimal(start);
        BigDecimal be = new BigDecimal(end);
        BigDecimal a = be.subtract(bs).divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP);

        return a.doubleValue() + " seconds";
    }

    String getLength(long length) {
        BigDecimal bs = new BigDecimal(length);
        BigDecimal a = bs.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP);

        return a.doubleValue() + " KB";
    }

}
