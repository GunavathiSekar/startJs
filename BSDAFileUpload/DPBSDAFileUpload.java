package kgfsl.stalk.fileuploadImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import kgfsl.genie.fileupload.FileLog;
import kgfsl.genie.fileupload.FileStatus;
import kgfsl.stalk.entity.FileUploadAudit2;
import kgfsl.stalk.fileupload.FileUploadAbstract;
import kgfsl.stalk.repository.FileUploadAuditRepository2;
import kgfsl.stalk.service.impl.DPBSDAFileUploadService;
import kgfsl.stalk.util.CommonUtil;
import kgfsl.stalk.util.SendMessage;

public class DPBSDAFileUpload extends FileUploadAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(DPBSDAFileUpload.class);

	private TaskExecutor taskExecutor;

	@Autowired
	SendMessage sendMsg;

	@Autowired
	private DPBSDAFileUploadService dpClientIdUpldService;
	

	@Autowired
	FileUploadAuditRepository2 auditRepo;

	public DPBSDAFileUpload(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	@Override
	protected String fileUploadProcess(Map<String, String> fileMapData) {
		String result = "Success";
		try {
			if (this.taskExecutor != null) {
				this.taskExecutor.execute(new Runnable() {
					public void run() {
						fileUploadMainProcess(fileMapData);
					}
				});
			}
		} catch (Exception e) {
			result = e.getMessage();
			LOGGER.error("DPBSDAFileUpload:fileUploadProcess():: " + e.getMessage(), e);
		}
		return result;
	}

	private void fileUploadMainProcess(Map<String, String> fileMapData) {
		try {
			LOGGER.info("DPBSDAFileUpload:fileUploadProcess started");
			dpClientIdUpldService.getUserId(Long.parseLong(fileMapData.get("userId")));
			
			DateFormat dateHis = new SimpleDateFormat("dd-MM-yyyy");
			String date = fileMapData.get("date").toString();
			Date strDate = dateHis.parse(date);	
			Date file_date = CommonUtil.dateConvertion(strDate);
			
			FileUploadAudit2 lastRec = auditRepo.findTop1ByFileCodeAndFileDateOrderByUploadAtDesc(fileMapData.get("code"), file_date);
			lastRec.setStartTime(new Date());
			this.auditRepo.save(lastRec);
			
			
//			fileMapData.put("date", fileMapData.get("fileDate"));
			FileLog logDetail = dpClientIdUpldService.uploadFile(fileMapData);
			System.out.println(logDetail);
			if (logDetail.getError().isEmpty()) {
				LOGGER.info("DPBSDAFileUpload:fileUploadProcess ended successfully");
			} else {
				LOGGER.info("DPBSDAFileUpload:fileUploadProcess ended with some error: " + logDetail.getError());
			}
		} catch (Exception e) {
			LOGGER.error("DPBSDAFileUpload:fileUploadProcess():: " + e.getMessage(), e);
		}
	}
	
//	private void fileUploadMainProcess(Map<String, String> fileMapData) {
//		try {
//			LOGGER.info("DPBSDAFileUpload:fileUploadProcess started");
//			dpClientIdUpldService.getUserId(Long.parseLong(fileMapData.get("userId")));
//			
//			DateFormat dateHis = new SimpleDateFormat("dd-MM-yyyy");
//			String date = fileMapData.get("date").toString();
//			Date strDate = dateHis.parse(date);	
//			Date file_date = CommonUtil.dateConvertion(strDate);
//			
//			FileUploadAudit2 lastRec = auditRepo.findTop1ByFileCodeAndFileDateOrderByUploadAtDesc(fileMapData.get("code"), file_date);
//			lastRec.setStartTime(new Date());
//			this.auditRepo.save(lastRec);
//			
//			FileLog logDetail = dpClientIdUpldService.uploadFile(fileMapData);
//			if (logDetail.getError().isEmpty()) {
//				FileLog setLog = new FileLog();
//				FileStatus fileStat = new FileStatus();
//				fileStat.setFileCode(fileMapData.get("code"));
//				fileStat.setStatus("Successful");
//				setLog.setFileStatus(fileStat);
//				sendMsg.onReciveMessage(setLog);
//				LOGGER.info("DPBSDAFileUpload:fileUploadProcess ended successfully");
//			} else {
//				FileLog setLog = new FileLog();
//				FileStatus fileStat = new FileStatus();
//				fileStat.setFileCode(fileMapData.get("code"));
//				fileStat.setStatus("Error");
//				setLog.setFileStatus(fileStat);
//				sendMsg.onReciveMessage(setLog);
//				LOGGER.info("DPBSDAFileUpload:fileUploadProcess ended with some error: " + logDetail.getError());
//			}
//		} catch (Exception e) {
//			LOGGER.error("DPBSDAFileUpload:fileUploadProcess():: " + e.getMessage(), e);
//			FileLog setLog = new FileLog();
//			FileStatus fileStat = new FileStatus();
//			fileStat.setFileCode(fileMapData.get("code"));
//			fileStat.setStatus("Error");
//			setLog.setFileStatus(fileStat);
//			sendMsg.onReciveMessage(setLog);
//		}
//	}

}
