package it.eng.idsa.businesslogic.configuration;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import it.eng.idsa.businesslogic.usagecontrol.service.UcRestCallService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class UsageControlConfiguration {
	
	@Bean
	@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true'")
	public UcRestCallService ucRestCallService(@Value("${spring.ids.ucapp.baseUrl}") String usageControlBaseUrl) {
		return new Retrofit.Builder()
				.baseUrl(usageControlBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(UcRestCallService.class);
	}
	
	@Bean
	@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true'")
	public Gson gson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter writer, ZonedDateTime zdt) throws IOException {
                writer.value(zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }

            @Override
            public ZonedDateTime read(JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
        }).enableComplexMapKeySerialization().create();

        return gson;
    }

}
