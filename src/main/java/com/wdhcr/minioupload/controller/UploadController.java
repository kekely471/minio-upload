package com.wdhcr.minioupload.controller;

import com.wdhcr.minioupload.component.MinioComp;
import com.wdhcr.minioupload.config.MinioConfiguration;
import com.wdhcr.minioupload.domain.R;
import com.wdhcr.minioupload.domain.ResultFileVo;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class UploadController {

    @Autowired
    private MinioConfiguration configuration;
//
//    @Value("${self.apiUrl}")
//    private String apiUrl;

    @Autowired
    private MinioComp minioComp;

    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file
            , @RequestParam(name = "fileName",required = false) String fileName
            , @RequestParam(name = "bucketName",required = false) String bucketName) {
        if (StringUtils.isEmpty(bucketName)){
            bucketName = configuration.getBucketName();
        }
        if (StringUtils.isEmpty(fileName)){
            fileName = file.getOriginalFilename();
        }
        minioComp.upload(file, bucketName, fileName);
        String url = minioComp.getUrl(fileName, bucketName, 7, TimeUnit.DAYS);
        ResultFileVo resultFileVo = new ResultFileVo();
        resultFileVo.setDirectlyUrl(url);
        resultFileVo.setExpiryTime("7days");
        resultFileVo.setUrl("localhost:8081/getPerpetualUrl/"+bucketName+"/"+fileName);
        return R.success(resultFileVo);
    }

    @GetMapping("/policy")
    public R policy(@RequestParam("fileName") String fileName
            , @RequestParam("bucketName") String bucketName) {
        Map policy = minioComp.getPolicy(fileName, bucketName, ZonedDateTime.now().plusMinutes(10));
        return R.success(policy);
    }

    @GetMapping("/uploadUrl")
    public R uploadUrl(@RequestParam("fileName") String fileName
            , @RequestParam("bucketName") String bucketName) {
        String url = minioComp.getPolicyUrl(fileName, bucketName, Method.PUT, 2, TimeUnit.MINUTES);
        return R.success(url);
    }

    @GetMapping("/url")
    public R getUrl(@RequestParam("fileName") String fileName
            , @RequestParam("bucketName") String bucketName) {
        String url = minioComp.getUrl(fileName, bucketName, 7, TimeUnit.DAYS);
        return R.success(url);
    }

    @GetMapping("/getPerpetualUrl/{bucketName}/{fileName}")
    public void getPerpetualUrl(@PathVariable("bucketName") String bucketName, @PathVariable("fileName") String fileName, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = minioComp.getUrl(fileName, bucketName, 7, TimeUnit.DAYS);
        resp.sendRedirect(url);
    }



}
