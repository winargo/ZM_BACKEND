package com.billermanagement.services;

import com.billermanagement.persistance.domain.AuditTrail;
import com.billermanagement.persistance.domain.Biller;
import com.billermanagement.persistance.domain.Partner;
import com.billermanagement.persistance.repository.AuditTrailRepository;
import com.billermanagement.vo.ui.BillerVO;
import com.billermanagement.vo.ui.PartnerVO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditTrailService {

    @Autowired
    AuditTrailRepository auditTrailRepository;

    public List<AuditTrail> findByOwnerId(int ownerId) {
        return auditTrailRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public void partner(Integer id, Partner oldValue, PartnerVO newValue, String user){
        saveAuditTrail(id, user, "PIC Name",
            newValue.getPicName(), oldValue.getPicName());

        saveAuditTrail(id, user, "PIC Address",
            newValue.getPicAddress(), oldValue.getPicAddress());

        saveAuditTrail(id, user, "PIC ID Number",
            newValue.getPicIdNumber(), oldValue.getPicIdNumber());

        saveAuditTrail(id, user, "PIC Tin Number",
            newValue.getPicTinNumber(), oldValue.getPicTinNumber());

        saveAuditTrail(id, user, "PIC Phone Number",
            newValue.getPicPhoneNumber(), oldValue.getPicPhoneNumber());

        saveAuditTrail(id, user, "PIC Email",
            newValue.getPicEmail(), oldValue.getPicEmail());

        saveAuditTrail(id, user, "Bank Name",
            newValue.getCoopBankName(), oldValue.getCoopBankName());

        saveAuditTrail(id, user, "Account Number",
            newValue.getCoopAccountNumber(), oldValue.getCoopAccountNumber());

        saveAuditTrail(id, user, "Account Name",
            newValue.getCoopAccountName(), oldValue.getCoopAccountName());

        saveAuditTrail(id, user, "Settlement Period",
            newValue.getCoopSettlementPeriod(), oldValue.getCoopSettlementPeriod());

        saveAuditTrail(id, user, "Period of Cooperation",
            newValue.getCoopPeriod(), oldValue.getCoopPeriod());

        saveAuditTrail(id, user, "Type of Cooperation",
            newValue.getCoopType(), oldValue.getCoopType());

        saveAuditTrail(id, user, "Nominal of Cooperation",
            String.valueOf(newValue.getCoopNominal()), String.valueOf(oldValue.getCoopNominal()));

        saveAuditTrail(id, user, "Attachment Deed of Cooperation",
            newValue.getAttachAmendmentDeed(), oldValue.getAttachAmendmentDeed());

        saveAuditTrail(id, user, "Attachment SK Kemenhumkam",
            newValue.getAttachSkKemenkumham(), oldValue.getAttachSkKemenkumham());

        saveAuditTrail(id, user, "Attachment Deed of Amendement",
            newValue.getAttachAmendmentDeed(), oldValue.getAttachAmendmentDeed());

        saveAuditTrail(id, user, "Attachment Company TIN",
            newValue.getAttachTin(), oldValue.getAttachTin());

        saveAuditTrail(id, user, "Attachment NIB",
            newValue.getAttachNib(), oldValue.getAttachNib());

        saveAuditTrail(id, user, "Attachment Person In Charge ID",
            newValue.getAttachPic(), oldValue.getAttachPic());

        saveAuditTrail(id, user, "Attachment Statement Letter",
            newValue.getAttachStatementLetter(), oldValue.getAttachStatementLetter());

        saveAuditTrail(id, user, "Attachment Business Photo",
            newValue.getAttachBusinessPhoto(), oldValue.getAttachBusinessPhoto());
    }

    @Transactional
    public void biller(Integer id, Biller oldValue, BillerVO newValue, String user){
        saveAuditTrail(id, user, "Account Number",
            newValue.getDepositAccount(), oldValue.getDepositAccount());

        saveAuditTrail(id, user, "Bank Name",
            newValue.getDepositBankName(), oldValue.getDepositBankName());

        saveAuditTrail(id, user, "Branch Name",
            newValue.getDepositBranch(), oldValue.getDepositBranch());

        saveAuditTrail(id, user, "Virtual Account",
            newValue.getDepositVA(), oldValue.getDepositVA());

        saveAuditTrail(id, user, "System IP",
            newValue.getReconSftpIp(), oldValue.getReconSftpIp());

        saveAuditTrail(id, user, "System Port",
            String.valueOf(newValue.getReconSftpPort()), String.valueOf(oldValue.getReconSftpPort()));

        saveAuditTrail(id, user, "System Folder",
            newValue.getReconSftpFolder(), oldValue.getReconSftpFolder());

        saveAuditTrail(id, user, "System Email",
            newValue.getReconEmail(), oldValue.getReconEmail());

        saveAuditTrail(id, user, "PKS",
            newValue.getPks(), oldValue.getPks());

        saveAuditTrail(id, user, "API",
            newValue.getApi(), oldValue.getApi());
;    }

    @Transactional
    private void saveAuditTrail(int ownerId, String user, String field, String newValue, String oldValue) {
        if (!newValue.equals(oldValue))  {
            AuditTrail auditTrail = new AuditTrail();
            auditTrail.setOwnerID(ownerId);
            auditTrail.setUserApp(user);
            auditTrail.setField(field);
            auditTrail.setValueAfter(newValue);
            auditTrail.setValueBefore(oldValue);
            auditTrailRepository.save(auditTrail);
        }
    }
}
