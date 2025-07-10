package claward.spring.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class FlagController {

    // 1. Glue Job 시뮬레이션
    @GetMapping("/glue-job")
    public ResponseEntity<String> startGlueJob() {
        if (hasLeakedCredentials()) {
            return ResponseEntity.ok("Glue Job started! Go to /ssm-read to fetch the flag.");
        } else {
            return ResponseEntity.status(403).body("Access Denied: No credentials leaked yet.");
        }
    }

    // 2. SSM Read 시뮬레이션
    @GetMapping("/ssm-read")
    public ResponseEntity<String> readSSMParameter() {
        if (hasLeakedCredentials()) {
            return ResponseEntity.ok("🎉 FLAG: CTF{glue_ssm_access_success}");
        } else {
            return ResponseEntity.status(403).body("Access Denied: No Glue Job triggered.");
        }
    }

    // a access.log 내 SSRF 흔적 확인
    private boolean hasLeakedCredentials() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("logs/access.log"));
            return lines.stream().anyMatch(line ->
                    line.contains("/latest/meta-data/iam/security-credentials/EC2DefaultRole")
            );
        } catch (IOException e) {
            return false;
        }
    }
}
