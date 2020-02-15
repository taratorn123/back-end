package com.angular.donationblock.controller;

import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class CampaignController {
    @Autowired
    private CampaignRepository campaignRepository;

    @PostMapping("/campaigns")
    public String addCampaign(@RequestParam("myFile") MultipartFile image, @RequestParam Map<String, String> file) throws IOException {
//        Campaign img = new Campaign( file.getOriginalFilename(),file.getContentType(),file.getBytes() );
          Campaign campaign = new Campaign();
          Campaign campaignTemp = new Campaign();

          campaign.setTargetDonation(file.get("targetDonation"));
          campaign.setCampaignName(file.get("campaignName"));
          campaign.setCategory(file.get("category"));
          campaign.setFundRaisingAs(file.get("fundRaisingAs"));
          campaign.setCampaignDetail(file.get("campaignDetail"));
          campaign.setCoverImagePath(file.get("coverImagePath"));

          campaignTemp = campaignRepository.save(campaign);
          campaign.setCoverImagePath("D:\\project\\donationblock-1\\src\\assets\\img\\"+campaignTemp.getId()+"\\coverImage\\");

          campaignRepository.save(campaign);

          String directoryName = "D:\\project\\donationblock-1\\src\\assets\\img\\"+campaignTemp.getId();
          String fileName = "cover";

          File directory = new File(directoryName);
          if (! directory.exists())
          {
              directory.mkdirs();
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
          }
          String directoryNameFile = directoryName+"\\"+fileName+"\\";
          File dest = new File(directoryNameFile);
          image.transferTo(dest);
//        final Campaign savedImage = campaignRepository.save(img);
//        System.out.println("Image saved");
//        return savedImage;
//        String folder = "C:\\photos\\";
//////        byte[] bytes = file.getBytes();
//            System.out.println(image.getOriginalFilename());
//            Path path = Paths.get("D:\\project\\donationblock-1\\src\\assets\\img\\"+campaignTemp.getId()+"\\coverImage\\");
//
//            String filePath = "D:\\project\\donationblock-1\\src\\assets\\img\\"+campaignTemp.getId()+"\\coverImage\\";
//            //File dest = new File(filePath);
//            image.transferTo(filePath);
////
////        return filePath;
//        byte[] temp = image.getBytes();

        return "success";
    }
}
