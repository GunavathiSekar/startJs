package kgfsl.stalk.service.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.RandomStringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kgfsl.genie.core.utility.CustomException;
import kgfsl.genie.criteria.DynamicFilter;
import kgfsl.genie.fileupload.AbstractFileUploadService;
import kgfsl.genie.fileupload.BaseFileUploadRepository;
import kgfsl.genie.fileupload.FileLog;
import kgfsl.genie.fileupload.FileStatus;
import kgfsl.genie.fileupload.utility.FileReaderUtil;
import kgfsl.stalk.entity.DPBSDA;
import kgfsl.stalk.entity.FileDwnd;
import kgfsl.stalk.entity.FileUploadAudit2;
import kgfsl.stalk.entity.UserInformation;
import kgfsl.stalk.repository.DPBSDARepository;
import kgfsl.stalk.repository.FileDwndRepository;
import kgfsl.stalk.repository.FileUploadAuditRepository2;
import kgfsl.stalk.repository.UserInformationRepository;
import kgfsl.stalk.util.SendMessage;

@Service
public class DPBSDAFileUploadService extends AbstractFileUploadService<DPBSDA, Long> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DPBSDAFileUploadService.class);

	@Autowired
	private DPBSDARepository dpClientIdRepository;

	@Autowired
	protected DynamicFilter dynamicFilter;

	@Autowired
	private FileDwndRepository history;

	@Autowired
	UserInformationRepository userInfoRepo;
	
	@Autowired
	JdbcTemplate template;

	@Autowired
	FileUploadAuditRepository2 auditRepo;

	@Autowired
	SendMessage sendMsg;
	
	Connection connection =null;
	PreparedStatement ps=null;
	
	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

	private Long userId;

	public Long getUserId(Long userId) {
		return this.userId = userId;
	}

	@Override
	protected BaseFileUploadRepository<DPBSDA, Long> getRepository() {
		return null;
	}

	@Override
	protected List<DPBSDA> process(List<DPBSDA> records) throws CustomException {
		StringBuilder ss = new StringBuilder();
		try {
			if (addDpClientId(records)) {
				saveFileDwnd(records);
			}
			FileLog setLog = new FileLog();
			FileStatus fileStat = new FileStatus();
			fileStat.setFileCode(getFileCode());
			fileStat.setStatus("Successfull");
			setLog.setFileStatus(fileStat);
			sendMsg.onReciveMessage(setLog);
		} catch (Exception e) {
			LOGGER.error("DPBSDAFileUploadService.process()::" + e, e.getMessage());
			appendError("DPBSDAFileUploadService.process()::" + e.getMessage());
			ss.append(e.getMessage());
			Map<String, Set<String>> info = new HashMap<>();
			Set<String> infoOrders = new HashSet<>();
			infoOrders.add(ss.toString());
			if (!infoOrders.isEmpty()) {
				info.put("", infoOrders);
			}
			if (!infoOrders.isEmpty()) {
				setErrors(infoOrders);
			}
			getStatus().setUploadedBy(this.userId);
		}
		return null;
	}

	List<String> headerType = new ArrayList<String>();
	public String BSDADpId;
	public String BSDAUserId;
	double netWorth=0.00;

	public boolean addDpClientId(List<DPBSDA> records) throws CustomException, SQLException {

		String batchId = RandomStringUtils.randomAlphanumeric(15).toUpperCase();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		UserInformation userName = userInfoRepo.findById(this.userId);
		FileLog setLog = new FileLog();
		FileStatus fileStat = new FileStatus();
		fileStat.setStatus("Progress");
		setLog.setFileStatus(fileStat);
		setLog.getFileStatus().setFileCode(getFileConfig().getCode());
		setLog.getFileStatus().setFileDate(getFileDate());
		setLog.getFileStatus().setUploadedBy(this.userId);
		setLog.getFileStatus().setDescription(userName.getUserName());
		sendMsg.onReciveMessage(setLog);
		StringBuilder ss = new StringBuilder();
		connection = template.getDataSource().getConnection();
		connection.setAutoCommit(false);
		List<DPBSDA> dpClientIdList = new ArrayList<>();
		String query = "insert into DBC_DP_BSDA_CLNT_DTL_T("
				+ "DBC_DP_ID,DBC_USER_ID,DBC_DP_CLNT_ID,DBC_PRCSS_FLAG,DBC_VOC_DATE,sessionid,uploadedBy,uploadedAt,fileDate,DBC_Net_Worth) values (?,?,?,?,?,?,?,?,?,?)";
		ps = connection.prepareStatement(query);
		
		try {
			for(DPBSDA data:records)
			{
				if (data.getNetWorth()!=null && !data.getNetWorth().equals(""))
				{
					data.setDpNetWorth(Double.parseDouble(data.getNetWorth()));
				  }
				else
				{
					data.setDpNetWorth(netWorth);
				}
				
				 String bsdaDpId ="072400";
				 String bsdaUserId ="ADMIN";
				 String processFlag="Y";
				 ps.setString(1,bsdaDpId);
				 ps.setString(2, bsdaUserId);
				 ps.setString(3,data.getDpClientId());
				 ps.setString(4, processFlag);
				 ps.setDate(5,new java.sql.Date(new Date().getTime()));
				 ps.setString(6,batchId);
				 ps.setLong(7, userId);
				 ps.setDate(8,new java.sql.Date(new Date().getTime()));
				 ps.setDate(9,new java.sql.Date(new Date().getTime()));
				 ps.setDouble(10,data.getDpNetWorth());
				ps.addBatch();
				LOGGER.error("DPBSDAFileUploadService::Records Inserted Sucessfully");
			}
			if(ps!=null)
			ps.executeBatch();
			connection.commit();
			connection.setAutoCommit(true);
			procedureCallForBSDAUpload("BSDAUPLD", java.sql.Date.valueOf(java.time.LocalDate.now()), batchId);

		} catch (Exception e) {
			LOGGER.error("DPBSDAFileUploadService.addDpmList()::" + e, e.getMessage());
			appendError("DPBSDAFileUploadService.addDpmList()::" + e.getMessage());
			if(connection!=null)
				try {
					connection.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			ss.append(e.getMessage());
			Map<String, Set<String>> info = new HashMap<>();
			Set<String> infoOrders = new HashSet<>();
			infoOrders.add(ss.toString());
			if (!infoOrders.isEmpty()) {
				info.put("", infoOrders);
			}
			if (!infoOrders.isEmpty()) {
				setErrors(infoOrders);
			}
		}
		finally
		{
			if(ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(connection!=null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Map<String, Set<String>> info = new HashMap<>();
		Set<String> infoOrders = new HashSet<>();
		if (ss.toString().length() > 0)
			infoOrders.add(ss.toString());
		if (!infoOrders.isEmpty()) {
			info.put("", infoOrders);
			if (info.size() > 0) {
				setMismatch(info);
			}
		}
		int invalidRecords = getProcessRecords().size() - dpClientIdList.size();
		long inValidCount = invalidRecords;
		String validCount = String.valueOf(dpClientIdList.size());

		if (infoOrders.isEmpty()) {
			infoOrders.add("File uploaded successfully with no of rows " + validCount);
			info.put("", infoOrders);
			setInformation(info);
		}
		FileStatus fileStatus = new FileStatus();

		fileStatus.setInvalidRecords(inValidCount);
		fileStatus.setValidRecords(Long.parseLong(validCount));
		fileStatus.setUploadedBy(userId);
		fileStatus.setTotalRecords(Long.valueOf(getProcessRecords().size()));
		setStatus(fileStatus);

		return true;
	}

	private java.sql.Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Autowired
	@Qualifier("dataSource")
	private DataSource objDataSource;

	public void procedureCallForBSDAUpload(String regMode, Date date, String batchId) throws CustomException {
		try {
			Connection connection = objDataSource.getConnection();
			//connection.setAutoCommit(false);
			PreparedStatement ps = connection.prepareStatement("{call Pr_Cdsl_BSDA_Prcss_update(?,?,?)}");
			ps.setString(1, regMode);
			ps.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
			ps.setString(3, batchId);
			ps.execute();
			connection.commit();
			connection.setAutoCommit(true);
			ps.close();
			connection.close();
			LOGGER.info("successfully procedure is called");
		} catch (SQLException e) {
			LOGGER.error("DPBSDAFileUploadService.procedureCallForBSDAUpload()::" + e, e.getMessage());
			appendError("DPBSDAFileUploadService.procedureCallForBSDAUpload()::" + e.getMessage());
			if(connection!=null)
				try {
					connection.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
		}
		finally
		{
			if(ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(connection!=null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void saveFileDwnd(List<DPBSDA> fileDownload) {
		FileDwnd his = new FileDwnd();
		SimpleDateFormat time = new SimpleDateFormat("hh:mm");
		DateFormat dateHis = new SimpleDateFormat("dd-MM-yyyy");
		Date timeDate = new Date();
		String hisTime = "" + time.format(new Date());
		SimpleDateFormat batchFormat = new SimpleDateFormat("ddMMyyyyhhmmssSS");
		StringBuilder ss = new StringBuilder();
		try {
			if (!fileDownload.isEmpty() && fileDownload.get(0) != null) {
				his.setBatchNo(batchFormat.format(timeDate));
				his.setDwnldType("DP");
				his.setDepyCode("CDSL");
				his.setFileCode(getFileCode());
				String hisdate = dateHis.format(timeDate);
				his.setDwnldDate(new SimpleDateFormat("dd-MM-yyyy").parse(hisdate));
				his.setDwnldTime("" + hisTime);
				his.setFileDate(getFileDate());
				his.setNoOfRec(fileDownload.size());
				his.setUploadedBy(this.userId);
				his.setUploadedAt(new Date());
				history.save(his);
			}
		} catch (Exception e) {
			LOGGER.error("DPBSDAFileUploadService.saveFileDwnd()::" + e, e.getMessage());
			appendError("DPBSDAFileUploadService.saveFileDwnd()::" + e.getMessage());
			ss.append(e.getMessage());
			Map<String, Set<String>> info = new HashMap<>();
			Set<String> infoOrders = new HashSet<>();
			infoOrders.add(ss.toString());
			if (!infoOrders.isEmpty()) {
				info.put("", infoOrders);
			}
			if (!infoOrders.isEmpty()) {
				setErrors(infoOrders);
			}
			getStatus().setUploadedBy(this.userId);
		}

	}

	@Override
	protected boolean validateFooters(List<String> arg0) throws CustomException {
		return true;
	}

	@Override
	protected boolean validateHeaders(List<String> headers) throws CustomException {
		headerType = new ArrayList<>();
		try {
			String[] _objects = FileReaderUtil.byteToStringArray(getFileByte());
			if (_objects.length > 0) {
				String commonvalue = _objects[0];
				String dpId = commonvalue.toString();
				BSDADpId = dpId.substring(0, 6);
				BSDAUserId = dpId.substring(6, 11);
			}
		} catch (Exception e) {

		}
		return true;
	}

	@Override
	protected String getFile(String args0, Date args1) throws Exception, CustomException {
		return getDailyFiles(args1);
	}

	protected String getDailyFiles(Date fileDate) throws Exception {
		String convertedPath = fileConfigService.getFileName(getFileConfig(), fileDate);

		if (getFileTransfer() != null && getFileTransfer().checkFileStatus(convertedPath)) {
			return convertedPath;
		}
		appendError("File not found in the directory : " + getFileConfig().getPath() + "/" + convertedPath);
		return null;
	}

	@Override
	protected void preSave() {
	}

	@Override
	protected long getUploadedBy() {
		return this.userId;
	}
	

	@Override
	protected kgfsl.genie.fileupload.spec.FileUploadAudit writeAndGetAudit()
			throws IOException, Exception, CustomException {
		DateFormat dateFormatNeeded = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormatNeeded.format(getFileDate());
		Date file_date = dateFormatNeeded.parse(strDate);

		FileUploadAudit2 lastRec = auditRepo.findTop1ByFileCodeAndFileDateOrderByUploadAtDesc(getFileCode(), file_date);
		long getId = lastRec.getId();
		FileLog logs = new FileLog();
		kgfsl.genie.fileupload.spec.FileUploadAudit audit = new kgfsl.genie.fileupload.spec.FileUploadAudit();
		audit.setFileBackup("File_Backup");
		audit.setFileCode(getFileConfig().getCode());
		audit.setFileDate(getFileDate());
		if (this.getFileName() != null)
			audit.setFileName(this.getFileName());
		audit.setProcessDate(new Date());
		audit.setUploadBy(this.userId);
		audit.setUploadAt(new Date());

		audit.setTotalRecords(getStatus().getTotalRecords());
		audit.setInValidRecords(getStatus().getInvalidRecords());
		audit.setValidRecords(getStatus().getValidRecords());
		logs.setFileStatus(getStatus());
		audit.setLogPath(writeLog());
		audit.setUploadStatus(getStatus().getStatus());
		audit.setLogFile(getLogMessage().getBytes());

		audit.setId(getId);
		lastRec.setEndTime(new Date());		
		return audit;
	}
}
