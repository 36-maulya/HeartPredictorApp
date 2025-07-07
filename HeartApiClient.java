import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class HeartApiClient {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://127.0.0.1:5000/predict");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Sample test data
            String jsonInput = """
            {
                "Age": 55,
                "Sex": 1,
                "ChestPainType": 0,
                "RestingBP": 130,
                "Cholesterol": 250,
                "FastingBS": 0,
                "RestingECG": 0,
                "MaxHR": 140,
                "ExerciseAngina": 0,
                "Oldpeak": 1.5,
                "ST_Slope": 1
            }
            """;

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            // Read response from Flask API
            Scanner sc = new Scanner(conn.getInputStream());
            while (sc.hasNext()) {
                System.out.println(sc.nextLine());
            }
            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
