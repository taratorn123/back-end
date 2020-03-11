package com.angular.donationblock.controller;

import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.User;
import com.angular.donationblock.form.TransactionForm;
import com.angular.donationblock.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
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
import java.util.ArrayList;
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
    @GetMapping("/getHistoryTransaction")
    public List<TransactionForm> getTransaction(@RequestParam Long campaignId)
    {
    	System.out.println("Get history Transaction");
    	Server server = new Server(StellarConfig.stellarServer);
    	String responseAcc = campaignRepository.findById(campaignId).get().getUser().getPublicKey();
    	System.out.println(responseAcc);
    	PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(responseAcc);
    	System.out.println("Received paymentRequest");
    	paymentsRequest.stream(new EventListener <OperationResponse>()
        {
   
    		private KeyPair account;

    		@SuppressWarnings("unlikely-arg-type")
    		@Override
    		public void onEvent(OperationResponse payment) 
    		{
	  			System.out.println("Test");
	  			account = KeyPair.fromAccountId(responseAcc);
	          	if (payment instanceof PaymentOperationResponse) 
	          	{
	          		if (((PaymentOperationResponse) payment).getTo().equals(account)) 
	          		{
	          			return;
	          		}
	          		String hash = ((PaymentOperationResponse) payment).getTransactionHash();
	          		try 
	          		{
	  					TransactionResponse tmp = server.transactions().transaction(hash);
	  					System.out.println(tmp.getSourceAccount());
	  					System.out.println(tmp.getEnvelopeXdr());
	  				} 
	          		catch (IOException e) 
	          		{
	  					// TODO Auto-generated catch block
	  					e.printStackTrace();
	  				}
	          		String source = ((PaymentOperationResponse) payment).getFrom();
	          		String amount = ((PaymentOperationResponse) payment).getAmount();
	
	          		Asset asset = ((PaymentOperationResponse) payment).getAsset();
	          		String assetName;
	          		if (asset.equals(new AssetTypeNative())) 
	          		{
	  	              assetName = "lumens";
	  	            } 
	  	            else 
	  	            {
	  	              StringBuilder assetNameBuilder = new StringBuilder();
	  	              assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getCode());
	  	              assetNameBuilder.append(":");
	  	              assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getIssuer());
	  	              assetName = assetNameBuilder.toString();
	  	            }
	  	
	  	            StringBuilder output = new StringBuilder();
	  	            output.append(amount);
	  	            output.append(" ");
	  	            output.append(assetName);
	  	            output.append(" from ");
	  	            output.append(((PaymentOperationResponse) payment).getFrom());
	  	            System.out.println(output.toString());
	          	}
    		}

    		@Override
    		public void onFailure(shadow.com.google.common.base.Optional<Throwable> arg0,
				shadow.com.google.common.base.Optional<Integer> arg1) 
    		{
    			// TODO Auto-generated method stub
			
    		}
        });
    	//List<TransactionForm> output = new ArrayList<TransactionForm>();
		return null;
    	
    }
}
