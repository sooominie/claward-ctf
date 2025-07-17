package claward.spring.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class HttpService {

    public String fetchUrl(String targetUrl) {
        try {
            // EC2 메타데이터 접근은 명시적으로 거부
            if (targetUrl.contains("169.254.169.254")) {
                return "요청 실패: EC2 메타데이터 접근은 허용되지 않습니다.";
            }

            // 외부 URL 정상 요청 처리
            URL url = new URL(targetUrl);
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
            e.printStackTrace();  // 서버 로그로 확인 가능
            return "요청 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}
