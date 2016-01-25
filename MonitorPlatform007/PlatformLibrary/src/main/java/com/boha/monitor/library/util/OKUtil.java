package com.boha.monitor.library.util;

import android.os.Environment;
import android.util.Log;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;
import okio.Okio;

/**
 * Created by aubreyM on 16/01/15.
 */

/**
 * Helper class to wrap OKHttp library and provide methods
 * that access data from Monitor Platform server
 */
public class OKUtil {

    static OkHttpClient client = new OkHttpClient();
    static Gson gson = new Gson();
    public static final String URL = "http://192.168.1.111:8080/mp/gatex";
    static String GATEWAY_SERVLET = "gatex?";

    static final String FAILED_UNKOWN_ERROR = "Request failed. Unknown error";
    static final String FAILED_IO = "Request failed. Communications not working";
    static final String FAILED_UNPACK = "Unable to unpack zipped response";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    static final String LOG = OKUtil.class.getSimpleName();
    public interface OKListener {
        void onResponse(ResponseDTO response);

        void onError(String message);
    }


    public static void configureTimeouts() {
        client.setConnectTimeout(20, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.setWriteTimeout(20, TimeUnit.SECONDS);

    }
    static long start;
    static File directory;

    /**
     * Async GET call with OKListener to return data to caller
     * @param req
     * @param listener
     * @throws OKHttpException
     */
    public static void doGet(final RequestDTO req,
                             final OKListener listener) throws OKHttpException {
        directory = Environment.getDataDirectory();
        start = System.currentTimeMillis();
        configureTimeouts();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(URL).newBuilder();
        urlBuilder.addQueryParameter("JSON", gson.toJson(req));
        String url = urlBuilder.build().toString();
        Log.w(LOG, "### sending request to server, requestType: "
                + req.getRequestType()
                + "\n" + url);
        Request okHttpRequest = new Request.Builder()
                .url(url)
                .build();

        execute(okHttpRequest, req.isZipResponse(), listener);

    }

    public static void doPost(final RequestList req, final OKListener listener) throws OKHttpException {
        start = System.currentTimeMillis();
        directory = Environment.getDataDirectory();
        RequestBody body = new FormEncodingBuilder()
                .add("JSON", gson.toJson(req))
                .build();
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        execute(request, false, listener);


    }

    private static void execute(final Request req, final boolean zipResponseRequested, final OKListener listener) {

        Callback callback = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                long end = System.currentTimeMillis();
                Log.e(LOG, "### Server responded with ERROR, round trip elapsed: " + getElapsed(start, end));
                listener.onError(FAILED_IO);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                long end = System.currentTimeMillis();

                if (!response.isSuccessful()) {
                    listener.onError(FAILED_UNKOWN_ERROR);
                    return;
                }
                ResponseDTO serverResponse = new ResponseDTO();
                if (zipResponseRequested) {
                    try {
                        File file = new File(directory, "myData.zip");
                        File uFile = new File(directory, "data.json");
                        BufferedSink sink = Okio.buffer(Okio.sink(file));
                        sink.writeAll(response.body().source());
                        sink.close();

                        String aa = ZipUtil.unpack(file, uFile);
                        Log.i(LOG, "### Data received size: " + getLength(file.length())
                                + " unpacked: " + getLength(uFile.length()));
                        serverResponse = gson.fromJson(aa, ResponseDTO.class);
                        try {
                            file.delete();
                            uFile.delete();
//                            Log.e(LOG,"Temporary unpacking files deleted OK");
                        } catch (Exception e) {
                            Log.e(LOG, "Temporary unpacking files NOT deleted. " + e.getMessage());
                        }
                        if (serverResponse.getStatusCode() == 0) {
                            listener.onResponse(serverResponse);
                        } else {
                            listener.onError(serverResponse.getMessage());
                        }
                    } catch (Exception e) {
                        listener.onError(FAILED_UNPACK);
                    }


                } else {
                    try {
                        String json = response.body().string();
                        serverResponse = gson.fromJson(json, ResponseDTO.class);
                        Log.i(LOG, "### Data received: " + getLength(json.length()));
                        if (serverResponse.getStatusCode() == 0) {
                            listener.onResponse(serverResponse);
                        } else {
                            listener.onError(serverResponse.getMessage());
                        }
                    } catch (Exception e) {
                        listener.onError(FAILED_UNKOWN_ERROR);
                    }


                }
                Log.e(LOG, "### Server responded, round trip elapsed: " + getElapsed(start, end)
                        + ", server elapsed: " + serverResponse.getElapsedRequestTimeInSeconds()
                        + ", statusCode: " + serverResponse.getStatusCode()
                        + "\nmessage: " + serverResponse.getMessage());
            }
        };

        client.newCall(req).enqueue(callback);
    }


    static String buildPostUrl(RequestDTO req) {
        StringBuilder sb = new StringBuilder();
        sb.append(URL)
                .append(GATEWAY_SERVLET);
        return sb.toString();
    }

    static String getElapsed(long start, long end) {
        BigDecimal bs = new BigDecimal(start);
        BigDecimal be = new BigDecimal(end);
        BigDecimal a = be.subtract(bs).divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP);

        return a.doubleValue() + " seconds";
    }
    static String getLength(long length) {
        BigDecimal bs = new BigDecimal(length);
        BigDecimal a = bs.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP);

        return a.doubleValue() + " KB";
    }
}
