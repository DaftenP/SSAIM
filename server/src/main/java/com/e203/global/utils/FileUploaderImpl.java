package com.e203.global.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FileUploaderImpl implements FileUploader {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.cloudfront}")
    private String cloudFront;

    @Override
    public String upload(MultipartFile profileImage) {
        String fileName = profileImage.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(profileImage.getSize());
        metadata.setContentType(profileImage.getContentType());


        try {
            s3Client.putObject(bucket, fileName, profileImage.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String url = s3Client.getUrl(bucket, fileName).toString();

        return cloudFront + url.split("/")[url.split("/").length - 1];
    }
}
