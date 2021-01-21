package kr.ac.snu.imageshare.net;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampDeserializer implements JsonDeserializer<Timestamp> {
    @Override
    public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

		try {
	    	Date tempDate = format.parse(json.getAsJsonPrimitive().getAsString());
	    	return new Timestamp(tempDate.getTime());
		}
		catch(Exception e) {}

		return null;
    }
}
