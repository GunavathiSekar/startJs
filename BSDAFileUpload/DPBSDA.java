package kgfsl.stalk.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import kgfsl.genie.criteria.CustomTableName;
import kgfsl.genie.fileupload.BaseFileUpload;
import kgfsl.genie.makerchecker.specification.AuditColumn;

@Entity
@Table(name = "DBC_DP_BSDA_CLNT_DTL_T")
@AuditColumn
@CustomTableName(name = "DBC_DP_BSDA_CLNT_DTL_T")
@JsonInclude(Include.NON_NULL)
public class DPBSDA extends BaseFileUpload {

	private String dpClientId;
	private String dpId;
	private String bsdauserId;
	private String processFlag;
	private String vocDate;
	private String sessionId;
	private String netWorth;
	private double dpNetWorth;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Override
	public long getId() {
		return id;
	}

	@Column(name = "DBC_DP_CLNT_ID")
	public String getDpClientId() {
		return dpClientId;
	}

	public void setDpClientId(String dpClientId) {
		this.dpClientId = dpClientId;
	}

	@Column(name = "DBC_DP_ID")
	public String getDpId() {
		return dpId;
	}

	public void setDpId(String dpId) {
		this.dpId = dpId;
	}

	@Column(name = "DBC_USER_ID")
	public String getBsdauserId() {
		return bsdauserId;
	}

	public void setBsdauserId(String bsdauserId) {
		this.bsdauserId = bsdauserId;
	}
	@Column(name = "DBC_PRCSS_FLAG")
	public String getProcessFlag() {
		return processFlag;
	}

	public void setProcessFlag(String processFlag) {
		this.processFlag = processFlag;
	}

	@Column(name = "DBC_VOC_DATE")
	public String getVocDate() {
		return vocDate;
	}

	public void setVocDate(String vocDate) {
		this.vocDate = vocDate;
	}
	
	@Column(name="sessionid")
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	 @Transient
		public String getNetWorth() {
			return netWorth;
		}

		public void setNetWorth(String netWorth) {
			this.netWorth = netWorth;
		}
	    
		@Column(name = "DBC_Net_Worth")
		public double getDpNetWorth() {
			return dpNetWorth;
		}

		public void setDpNetWorth(double dpNetWorth) {
			this.dpNetWorth = dpNetWorth;
		}
}
