package claward.spring.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Service
public class HttpService {

    public String fetchUrl(String targetUrl) {
        try {
            URI uri = new URI(targetUrl);
            String host = uri.getHost();

            // [CTF용 필터] 정확히 "169.254.169.254"인 경우만 차단
            if ("169.254.169.254".equals(host)) {
                return "요청 실패: EC2 메타데이터 접근은 허용되지 않습니다.";
            }

            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "요청 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}
