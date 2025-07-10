package claward.spring.controller;


import claward.spring.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class WebController {

    @Autowired
    private HttpService httpService;

    @GetMapping("/report")
    public String report(@RequestParam String url) {
        return httpService.fetchUrl(url);
    }

    @GetMapping("/view")
    @ResponseBody
    public String viewLog(@RequestParam("page") String page) throws IOException {
        String filePath = "logs/" + page;

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }
        reader.close();
        return "<pre>" + result.toString() + "</pre>"; // ← 요기!

    }




}
