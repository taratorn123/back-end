package com.angular.donationblock.controller;

import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.User;
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
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class CampaignController {
    @Autowired
    private CampaignRepository campaignRepository;

    @GetMapping("/campaigns-list")
    public List<Campaign> getCampaigns() {
        return (List<Campaign>) campaignRepository.findAll();
    }

    @PostMapping("/campaigns")
    public String addCampaign(@RequestParam("myFile") MultipartFile image, @RequestParam Map<String, String> file) throws IOException {
          Campaign campaign = new Campaign();
          Campaign campaignTemp = new Campaign();

          campaign.setTargetDonation(file.get("targetDonation"));
          campaign.setCampaignName(file.get("campaignName"));
          campaign.setCategory(file.get("category"));
          campaign.setFundRaisingAs(file.get("fundRaisingAs"));
          campaign.setCampaignDetail(file.get("campaignDetail"));
          campaign.setCoverImagePath(file.get("coverImagePath"));

          campaignTemp = campaignRepository.save(campaign);
          campaign.setId(campaignTemp.getId());
          String directoryName = "D:\\project\\front-end\\src\\assets\\img\\"+campaignTemp.getId()+"\\coverImage\\";

          File directory = new File(directoryName);
          if (! directory.exists())
          {
              directory.mkdirs();
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
          }
          File dest = new File(directoryName+"\\"+image.getOriginalFilename()+"\\");
          image.transferTo(dest);
          campaign.setCoverImagePath("../../assets/img/"+campaignTemp.getId()+"/coverImage/"+image.getOriginalFilename());
          campaignRepository.save(campaign);

        return "success";
    }

    @GetMapping("/campaigns/{campaignID}")
    public Campaign getCampaignData(@PathVariable Long campaignID){
        Optional<Campaign> temp = campaignRepository.findById(campaignID);
        if(temp.isPresent())
            return campaignRepository.findById(campaignID).get();
        else
            return new Campaign();
    }
}
