package kgfsl.stalk.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonRawValue;
import kgfsl.genie.fileupload.FileLog;
import kgfsl.genie.fileupload.FileStatus;
import kgfsl.stalk.criteria.ViewWrapper;
import kgfsl.stalk.entity.DPBSDA;
import kgfsl.stalk.job.FileUploadScheduler;
import kgfsl.stalk.repository.FileUploadAuditRepository2;
import kgfsl.stalk.repository.UserInformationRepository;
import kgfsl.stalk.service.DPBSDAFileUploadService;
import kgfsl.stalk.utility.CommonUtil;

@RestController
@RequestMapping("/master/depository/dpBSDApanExempt")
//@Scope("request")
public class DPBSDAPANEXEMPT  {

	private static final Logger LOGGER = LoggerFactory.getLogger(DPBSDAController.class);

	@Autowired
	DPBSDAFileUploadService service;

	@Autowired
	CommonUtil commonUtil;

	FileUploadAuditRepository2 auditRepo;

	@Autowired
	UserInformationRepository userInfoRepo;

	@Autowired
	private FileUploadScheduler fileUploadSchedule;

	@RequestMapping(value = "/viewfiltermetadata", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Map<String, Map<String, Object>> getViewFilterMetaData() throws Exception {
		return service.viewFilterMetaData();
	}

	@RequestMapping(value = "/view", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> view(@RequestBody ViewWrapper view) throws Exception {
		return service.view(view.getFilterList(), view.getPageNumber(), view.getPageSize(), view.getSortField(),
				view.getSortOrder(), view.getGlobalFilter());
	}

	@RequestMapping(value = "/searchedrecord", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
	@ResponseBody
	public DPBSDA getSearchedRecord(@RequestBody String id) {
		return service.find(Long.parseLong(id));
	}

	@RequestMapping(value = { "/fileUpload" }, method = RequestMethod.POST)
	public @JsonRawValue FileLog fileUpload(@RequestBody Map<String, String> fileMap) throws Exception {
		Date date = new SimpleDateFormat("dd-MM-yyyy").parse(fileMap.get("date"));
		String dateString2 = new SimpleDateFormat("yyyy-MM-dd").format(date);
		SimpleDateFormat dwnDtFmt = new SimpleDateFormat("yyyy-MM-dd");
		String status = "Progress";
		FileLog fileLog = new FileLog();
		FileStatus statusLog = new FileStatus();
		try {
			commonUtil.fileUploadAuditSave(status, fileMap.get("code"), dwnDtFmt.parse(dateString2));
			fileUploadSchedule.scheduleSimpleTrigger("DPBSDAFileUpload", fileMap);
		} catch (Exception e) {
			LOGGER.error("DPBSDAPANEXEMPTFileUpload:fileUploadProcess():: " + e.getMessage(), e);
		}
		statusLog.setStatus(status);
		fileLog.setFileStatus(statusLog);
		return fileLog;
	}

	@RequestMapping(value = "/getTotalNoOfRecords", method = RequestMethod.GET)
	@ResponseBody
	public long getTotalNoOfRecords() throws Exception {
		return service.getTotalNoOfRecords();
	}

	@RequestMapping(value = "/getResultCount", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public int getResultCount(@RequestBody ViewWrapper view) throws Exception {
		return service.getResultCount(view.getFilterList(), view.getSortField(), view.getSortOrder(),
				view.getGlobalFilter());
	}
}
