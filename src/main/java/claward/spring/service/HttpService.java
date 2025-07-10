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
            // 만약 EC2 메타데이터 주소면 IMDSv2 토큰 발급 먼저
            if (targetUrl.contains("169.254.169.254")) {
                // 1단계: IMDSv2 토큰 요청
                URL tokenUrl = new URL("http://169.254.169.254/latest/api/token");
                HttpURLConnection tokenConn = (HttpURLConnection) tokenUrl.openConnection();
                tokenConn.setRequestMethod("PUT");
                tokenConn.setRequestProperty("X-aws-ec2-metadata-token-ttl-seconds", "21600");
                tokenConn.connect();

                // 토큰 받아오기
                BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenConn.getInputStream()));
                String token = tokenReader.readLine();
                tokenReader.close();

                // 2단계: 메타데이터에 토큰 포함해서 요청
                URL url = new URL(targetUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("X-aws-ec2-metadata-token", token);
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

            } else {
                // 일반적인 외부 URL 요청
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
            }

        } catch (Exception e) {
            return "요청 실패: " + e.getMessage();
        }
    }
}
