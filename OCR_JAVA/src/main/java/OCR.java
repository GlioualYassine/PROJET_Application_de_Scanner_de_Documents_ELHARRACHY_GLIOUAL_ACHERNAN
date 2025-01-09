import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OCR {
    public static void main(String[] args) {
        String imagePath = "src/main/resources/img.png";
        String url = "https://api.edenai.run/v2/ocr/ocr";
        String apiKey = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZTkwYWQyMDAtYzUxYS00MWI3LThkM2ItMGM3MGY1MWRhMTVjIiwidHlwZSI6ImFwaV90b2tlbiJ9.c_Jbg7KA5On3LdaUY_eTbi85qnzApVgZ6noa4Xg_iKA";


        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create the POST request
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Authorization", apiKey);

            // Prepare the file and form data
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.out.println("Image file not found: " + imagePath);
                return;
            }

            HttpEntity multipartEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", imageFile, ContentType.IMAGE_JPEG, imageFile.getName())
                    .addTextBody("providers", "google", ContentType.TEXT_PLAIN)
                    .addTextBody("language", "en", ContentType.TEXT_PLAIN)
                    .build();

            postRequest.setEntity(multipartEntity);

            // Execute the request
            HttpResponse response = httpClient.execute(postRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String responseContent = EntityUtils.toString(response.getEntity());
                // Parse JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> result = objectMapper.readValue(responseContent, HashMap.class);
                System.out.println(result);
            } else {
                System.out.println("Request failed with status code: " + statusCode);
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

