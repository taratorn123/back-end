package com.angular.donationblock.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.angular.donationblock.entity.Report;
import com.angular.donationblock.form.ReportForm;
import com.angular.donationblock.model.ReportNumberModel;
import com.angular.donationblock.repository.CampaignRepository;
import com.angular.donationblock.repository.ReportRepository;
import com.angular.donationblock.repository.UserRepository;

@RestController
@CrossOrigin
public class ReportController 
{
	@Autowired
	private CampaignRepository campaignRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ReportRepository reportRepository;

	@PostMapping("/reportCampaign")
	public boolean reportCampaign(@RequestBody ReportForm report)
	{
		System.out.println("Recieving report");
		System.out.println(report.getCampaignId()+" "+report.getDetail()+" "+report.getUserId());
		try
		{
			Report reportSystem = new Report(
					userRepository.findById(report.getUserId()).get(),
					campaignRepository.findById(report.getCampaignId()).get(),
	    			report.getDetail());
			reportRepository.save(reportSystem);
			System.out.println("Report : Saving data into database");
			return true;
		}
		catch(Exception e)
		{
			System.out.println(e);
			return false;
		}
	}
//	@GetMapping("/getAllReport")
//	public List<ReportForm> getAllReport()
//	{
//		List<ReportForm> output = new ArrayList<ReportForm>();
//		List<Report> systemReport = reportRepository.findAll();
//		for(Report report: systemReport)
//		{
//			if(!report.isDeleted())
//			{
//				System.out.println(report.getCampaign().getId()+" "+report.getUser().getId()+" "+report.getDetail());
//				output.add(new ReportForm(report.getCampaign().getId(),
//						report.getUser().getId(),
//						report.getDetail(),
//						report.getTimestamp(),
//						report.getCampaign().getCampaignName()));
//			}
//		}
//		return output;
//	}
	@GetMapping("/getReportNumber")
	public List<ReportNumberModel> getReportNumber()
	{
		Map<Long,List<ReportForm>> hmap = new HashMap<Long, List<ReportForm>>();
		List<ReportNumberModel> reportNumber = new ArrayList<ReportNumberModel>();
		List<ReportForm> reportForm = new ArrayList<ReportForm>();
		List<Report> systemReport = reportRepository.findAll();
		for(Report report: systemReport)
		{
			if(!report.isDeleted())
			{
				if(hmap.containsKey(report.getCampaign().getId()))
				{
					reportForm.add(new ReportForm(report.getCampaign().getId(),
							report.getUser().getId(),
							report.getDetail(),
							report.getTimestamp(),
							report.getCampaign().getCampaignName()));
				}
				else
				{
					reportForm = new ArrayList<ReportForm>();
					reportForm.add(new ReportForm(report.getCampaign().getId(),
						report.getUser().getId(),
						report.getDetail(),
						report.getTimestamp(),
						report.getCampaign().getCampaignName()));
					hmap.put(report.getCampaign().getId(),reportForm);
				}
			}
		}
		for (Entry<Long, List<ReportForm>> entry : hmap.entrySet()) 
		{
			reportNumber.add(new ReportNumberModel(entry.getKey(),
					campaignRepository.findById(entry.getKey()).get().getCampaignName(),entry.getValue().size()));
		}
		return reportNumber;

		
	}
	@PostMapping("/getReportDetail")
	public List<ReportForm> getReportDetail(@RequestBody String campaignId)
	{
		List<ReportForm> reportForm = new ArrayList<ReportForm>();
		List<Report> systemReport = reportRepository.findAllByCampaignId(Long.parseLong(campaignId));
		for(Report report: systemReport)
		{
			if(!report.isDeleted())
			{
				reportForm.add(new ReportForm(report.getCampaign().getId(),report.getUser().getId(),
						report.getDetail(),report.getTimestamp(),report.getCampaign().getCampaignName()));
			}
		}
		System.out.println("Sending Report detail");
		return reportForm;
	}
}
