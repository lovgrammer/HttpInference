package kr.ac.snu.imageshare.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.sql.Timestamp;

public class GsonUtils {

	public static Gson getGsonObject() {
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat("yyyyMMdd");
		builder.registerTypeAdapter(Timestamp.class, new TimestampDeserializer());
		Gson gson = builder.create();

		return gson;
    }
}
