package kr.ac.snu.imageshare.net;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public class ApiClient {

    private Context context;
    private PldaAPIs pldaAPIs;
    private Retrofit retrofit;


    private static class NetworkClient {

        private static Retrofit retrofit;

        private static Retrofit getRetrofitClient(final Context context, final String serverAddress) {
            if (retrofit == null) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .addNetworkInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request.Builder requestBuilder = chain.request().newBuilder();
                                // requestBuilder.header("AUTHORIZATION",
                                //         PrefUtils.getUserId(context) + ":"
                                //                 + PrefUtils.getApiKey(context));
                                return chain.proceed(requestBuilder.build());
                            }
                        })
                        .build();
                retrofit = new Retrofit.Builder()
                    .baseUrl(serverAddress)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(GsonUtils.getGsonObject()))
                    .build();
            }

            return retrofit;
        }
    }

    public ApiClient(Context context, String serverAddress) {
        this.context = context;
        retrofit = NetworkClient.getRetrofitClient(this.context, serverAddress);
        pldaAPIs = retrofit.create(PldaAPIs.class);
    }

    private interface PldaAPIs {
        // @POST("splash/")
        // Call<SplashResponse> requestSplash(@Body SplashRequest splashRequest);
	@Multipart
	@POST("segmentation/upload/")
	Call<UploadResponse> uploadFile(@Part MultipartBody.Part title,
					@Part MultipartBody.Part file);
	
	@GET
	Call<ResponseBody> fetchCaptcha(@Url String url);	
    }


    public Call uploadFile(File file, String filename) {
	Log.i("Jongyun", "exists?" + file.exists());
	RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

	// MultipartBody.Part is used to send also the actual file name
	MultipartBody.Part tbody =
            MultipartBody.Part.createFormData("title", "inputimage");
	
	MultipartBody.Part body =
            MultipartBody.Part.createFormData("image", file.getName(), requestFile);
	
        Call<UploadResponse> call = pldaAPIs.uploadFile(tbody, body);	
        return call;
    }

    public Call downloadFile(String url) {
	Call<ResponseBody> call = pldaAPIs.fetchCaptcha(url);
	return call;
    }
}
