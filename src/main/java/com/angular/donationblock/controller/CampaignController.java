package com.angular.donationblock.controller;

import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.CampaignUpdate;
import com.angular.donationblock.entity.Report;
import com.angular.donationblock.entity.User;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.form.CampaignForm;
import com.angular.donationblock.form.CampaignUpdateForm;
import com.angular.donationblock.form.ReportForm;
import com.angular.donationblock.form.UserForm;
import com.angular.donationblock.model.CampaignModel;
import com.angular.donationblock.model.TransactionModel;
import com.angular.donationblock.repository.AccountDonationRepository;
import com.angular.donationblock.repository.CampaignRepository;
import com.angular.donationblock.repository.CampaignUpdateRepository;
import com.angular.donationblock.repository.ReportRepository;
import com.angular.donationblock.repository.UserRepository;
import com.angular.donationblock.repository.AccountDonationRepository;
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
import java.text.DecimalFormat;
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
    private CampaignUpdateRepository campaignUpdateRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired 
    private ReportRepository reportRepository;
    @Autowired
    private AccountDonationRepository accountDonationRepository;

    @GetMapping("/campaigns-list")
    public List<CampaignModel> getCampaignsTest1()
    {

        List<CampaignModel> output = new ArrayList<CampaignModel>();
        double totalDonate;
        for(Campaign campaign : campaignRepository.findAll())
        {
            if(campaign.isActive())
            {
                totalDonate = 0;
                for(AccountDonation transaction : accountDonationRepository.
                        findAllByCampaignId(campaign.getId()))
                {
                    totalDonate += transaction.getExchageRate()*Double.parseDouble(transaction.getAmount());
                }
                DecimalFormat df = new DecimalFormat("#.00");
                output.add(new CampaignModel(campaign.getId(),campaign.isDeleted(),campaign.getUser(),campaign.getTargetDonation(),
                        campaign.getCampaignName(),campaign.getCategory(),campaign.getFundRaisingAs(),
                        campaign.getStartDate(),campaign.getCampaignDetail(),campaign.getCoverImagePath(),
                        campaign.isActive(),df.format(totalDonate)));

            }
        }
        return output;
    }
    //Get campaigns by userId (ManageCampaignPage)
    @GetMapping("/userscampaigns/{userId}")
    public List<CampaignModel> getUserCampaign(@PathVariable Long userId)
    {
        List<CampaignModel> output = new ArrayList<CampaignModel>();
        double totalDonate;
        for(Campaign campaign : campaignRepository.findAllByUserId(userId))
        {
            if(campaign.isActive())
            {
                totalDonate = 0;
                for(AccountDonation transaction : accountDonationRepository.
                        findAllByCampaignId(campaign.getId()))
                {
                    totalDonate += transaction.getExchageRate()*Double.parseDouble(transaction.getAmount());
                }
                DecimalFormat df = new DecimalFormat("#.00");
                output.add(new CampaignModel(campaign.getId(),campaign.isDeleted(),campaign.getUser(),campaign.getTargetDonation(),
                        campaign.getCampaignName(),campaign.getCategory(),campaign.getFundRaisingAs(),
                        campaign.getStartDate(),campaign.getCampaignDetail(),campaign.getCoverImagePath(),
                        campaign.isActive(),df.format(totalDonate)));

            }
        }
        return output;
    }
    //Save both campaign and owner of the campaign to map together
    @PostMapping("/campaignUser")
    public Integer saveCampaignUser(@RequestBody CampaignForm campaignForm)
    {
        User user = userRepository.findById(campaignForm.getUserId()).get();
        Campaign campaign = campaignRepository.findById(campaignForm.getCampaignId()).get();
        campaign.setCoverImagePath(campaignForm.getCoverImagePath());
        campaign.setUser(user);
        campaignRepository.save(campaign);
        return 1;
    }

    @PostMapping("/campaigns")
    public Long addCampaign(@RequestBody Campaign campaignForm)
    {
        campaignForm.setActive(true);
        Campaign campaign = campaignRepository.save(campaignForm);
        return campaign.getId();
    }

    @PostMapping("/editCampaigns")
    public Long editCampaign(@RequestBody CampaignForm campaignForm)
    {
        Campaign campaign = campaignRepository.findById(campaignForm.getCampaignId()).get();
        campaign.setTargetDonation(campaignForm.getTargetDonation());
        campaign.setCampaignName(campaignForm.getCampaignName());
        campaign.setCategory(campaignForm.getCategory());
        campaign.setFundRaisingAs(campaignForm.getFundRaisingAs());
        campaign.setCoverImagePath(campaignForm.getCoverImagePath());
        campaign.setCampaignDetail(campaignForm.getCampaignDetail());
        campaignRepository.save(campaign);
        return campaign.getId();
    }
    @PostMapping("/updateCampaigns")
    public Long updateCampaign(@RequestBody CampaignUpdateForm campaignUpdateForm)
    {
        CampaignUpdate campaignUpdate = new CampaignUpdate();
        campaignUpdate.setCampaignUpdateDetail(campaignUpdateForm.getCampaignUpdateDetail());
        campaignUpdate.setUpdateTimestamp(campaignUpdateForm.getUpdateTimestamp());
        Campaign campaign = campaignRepository.findById(campaignUpdateForm.getCampaignId()).get();
        campaignUpdate.setCampaign(campaign);
        CampaignUpdate temp = campaignUpdateRepository.save(campaignUpdate);
        return temp.getId();
    }

    @GetMapping("/updateCampaignsId")
    public Long getLastestUpdateCampaignsId()
    {
        Optional<CampaignUpdate> temp = Optional.ofNullable(campaignUpdateRepository.findTopByOrderByIdDesc());
        if(temp.isPresent())
            return campaignUpdateRepository.findTopByOrderByIdDesc().getId();
        else
            return new CampaignUpdate().getId();
    }
    //Find campaigns by campaign's category
    @GetMapping("/getCampaignCategory/{campaignCategory}")
    public List<Campaign> getCampaignByCategory(@PathVariable String campaignCategory)
    {
        List<Campaign> temp = campaignRepository.findCampaignsByCategory(campaignCategory);
        return temp;
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


    @GetMapping("/getUpdateCampaigns/{campaignId}")
    public List<CampaignUpdate> getUpdateCampaigns(@PathVariable Long campaignId)
    {
        List<CampaignUpdate> temp = campaignUpdateRepository.findAllByCampaignId(campaignId);
        return temp;
    }
    @GetMapping("/getCommentCampaigns/{campaignId}")
    public Iterable<AccountDonation> getCommentCampaigns(@PathVariable Long campaignId)
    {
        Iterable<AccountDonation> temp = accountDonationRepository.findByCampaignIdAndCommentNotNull(campaignId);

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

    @GetMapping("getTotalDonate/{campaignId}")
    public String getTotalDonate(@PathVariable long campaignId)
    {
        double totalDonate = 0;
        String empty ="0";
        List<AccountDonation> transactions = accountDonationRepository.findAllByCampaignId(campaignId);
        if(transactions.size() == 0)
        {
            return empty;
        }
        for(AccountDonation transaction : transactions)
        {
            totalDonate += transaction.getExchageRate()*Double.parseDouble(transaction.getAmount());
        }
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(totalDonate);
    }

}
