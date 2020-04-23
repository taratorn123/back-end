package com.angular.donationblock.controller;

import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.Report;
import com.angular.donationblock.entity.User;
import com.angular.donationblock.form.CampaignForm;
import com.angular.donationblock.form.ReportForm;
import com.angular.donationblock.form.UserForm;
import com.angular.donationblock.model.TransactionModel;
import com.angular.donationblock.repository.AccountDonationRepository;
import com.angular.donationblock.repository.CampaignRepository;
import com.angular.donationblock.repository.ReportRepository;
import com.angular.donationblock.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.requests.TooManyRequestsException;
import org.stellar.sdk.responses.TransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
public class CampaignController {
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired 
    private ReportRepository reportRepository;

    @GetMapping("/campaigns-list")
    public List<Campaign> getCampaigns()
    {
    	List<Campaign> output = new ArrayList<Campaign>();
    	for(Campaign campaign : campaignRepository.findAll())
    	{
    		
    	}
        return campaignRepository.findAll();
    }

    @PostMapping("/campaignUser")
    public Integer saveCampaignUser(@RequestBody CampaignForm campaignForm)
    {
        User user = userRepository.findById(campaignForm.getUserId()).get();
        Campaign campaign = campaignRepository.findById(campaignForm.getCampaignId()).get();
        campaign.setUser(user);
        campaignRepository.save(campaign);

        return 1;
    }

    @PostMapping("/campaigns")
    public Long addCampaign(@RequestParam("myFile") MultipartFile image, @RequestParam Map<String, String> file) throws IOException
    {
        Campaign campaign = new Campaign();
        Campaign campaignTemp = new Campaign();


        campaign.setTargetDonation(file.get("targetDonation"));
        campaign.setCampaignName(file.get("campaignName"));
        campaign.setCategory(file.get("category"));
        campaign.setFundRaisingAs(file.get("fundRaisingAs"));
        campaign.setCampaignDetail(file.get("campaignDetail"));
        campaign.setCoverImagePath(file.get("coverImagePath"));
        campaign.setActive(true);
        campaignTemp = campaignRepository.save(campaign); // save null first go get ID generated
        campaign.setId(campaignTemp.getId());
        String directoryName = "D:\\GithubJr\\front-end\\src\\assets\\img\\"+campaignTemp.getId()+"\\coverImage\\";

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

        return campaign.getId();
    }

    @GetMapping("/campaigns/{campaignId}")
    public Campaign getCampaignData(@PathVariable Long campaignId)
    {
        Optional<Campaign> temp = campaignRepository.findById(campaignId);
        if(temp.isPresent())
            return campaignRepository.findById(campaignId).get();
        else
            return new Campaign();
    }

    @GetMapping("/userscampaigns/{userId}")
    public List<Campaign> getUserCampaign(@PathVariable Long userId) 
    {
        List<Campaign> temp = campaignRepository.findAllByUserId(userId);
        return temp;
    }
    
    @PostMapping("/inactivateCampaign")
    public boolean inactivateCampaign(@RequestBody String campaignId)
    {
    	Campaign campaign = campaignRepository.findById(Long.parseLong(campaignId)).get();
    	campaign.setActive(false);
    	campaignRepository.save(campaign);
    	for(Report report: reportRepository.findAllByCampaignId(campaign.getId()))
    	{
    		report.setDeleted(true);
    		reportRepository.save(report);
    	}
    	
    	System.out.println("Inactivated");
    	return true;
    }
    @GetMapping("/getInactiveCampaign")
    public List<Campaign> getInactiveCampaign()
    {
    	return campaignRepository.findAllByActive(false);
    }
    @PostMapping("/activeCampaign")
    public boolean activateCampaign(@RequestBody String campaignId)
    {
    	Campaign campaign = campaignRepository.findById(Long.parseLong(campaignId)).get();
    	campaign.setActive(true);
    	campaignRepository.save(campaign);
    	System.out.println("Campaign "+campaign.getId()+" "+campaign.getCampaignName()+" Activated");
    	return true;
    }

}
