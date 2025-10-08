//package com.wfm.experts.modules.wfm.employee.leave.service.impl;
//
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
//import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestNotificationService;
//import com.wfm.experts.notificationengine.dto.NotificationRequest;
//import com.wfm.experts.notificationengine.service.NotificationOrchestrationService;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class LeaveRequestNotificationServiceImpl implements LeaveRequestNotificationService {
//
//    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestNotificationServiceImpl.class);
//    private final NotificationOrchestrationService notificationOrchestrationService;
//
//    @Override
//    public void sendLeaveRequestSubmissionNotifications(LeaveRequest leaveRequest, LeaveRequestApproval approval) {
//        // Notify the approver with actionable data
//        sendActionableNotification(
//                "leave_request_submitted_approver_in_app",
//                "leave_request_submitted_approver",
//                approval,
//                leaveRequest,
//                "A new leave request from " + leaveRequest.getEmployee().getPersonalInfo().getFullName() + " is pending your approval."
//        );
//
//        // Notify the employee
//        sendStandardNotification(
//                "leave_request_submitted_employee_in_app",
//                "leave_request_submitted_employee",
//                leaveRequest.getEmployee(),
//                leaveRequest,
//                "Your leave request has been submitted successfully."
//        );
//    }
//
//    @Override
//    public void sendLeaveRequestApprovalNotifications(LeaveRequest leaveRequest, Employee approver, LeaveRequestApproval nextApproval) {
//        // Build the dynamic part of the message for the employee
//        String nextApproverInfo = "";
//        if (nextApproval != null && nextApproval.getApprover() != null) {
//            nextApproverInfo = " It is now pending approval from " + nextApproval.getApprover().getPersonalInfo().getFullName() + ".";
//        }
//
//        // Notify the employee
//        Map<String, Object> employeePayload = createBasePayload(leaveRequest, approver);
//        employeePayload.put("nextApproverInfo", nextApproverInfo);
//        sendNotificationWithPayload(
//                "leave_request_approved_employee_in_app",
//                "leave_request_approved_employee",
//                leaveRequest.getEmployee(),
//                "Your leave request has been approved by " + approver.getPersonalInfo().getFullName() + ".",
//                employeePayload,
//                null
//        );
//
//        // If there's a next approver, notify them
//        if (nextApproval != null) {
//            Map<String, Object> nextApproverPayload = createBasePayload(leaveRequest, nextApproval.getApprover());
//            nextApproverPayload.put("previousApproverName", approver.getPersonalInfo().getFullName());
//            sendNotificationWithPayload(
//                    "leave_request_approved_next_approver_in_app",
//                    "leave_request_approved_next_approver",
//                    nextApproval.getApprover(),
//                    "A leave request approved by " + approver.getPersonalInfo().getFullName() + " is now pending your approval.",
//                    nextApproverPayload,
//                    nextApproval.getId()
//            );
//        }
//    }
//
//    @Override
//    public void sendLeaveRequestRejectionNotifications(LeaveRequest leaveRequest, Employee rejecter) {
//        Map<String, Object> payload = createBasePayload(leaveRequest, rejecter);
//        payload.put("rejecterName", rejecter.getPersonalInfo().getFullName());
//        sendNotificationWithPayload(
//                "leave_request_rejected_employee_in_app",
//                "leave_request_rejected_employee",
//                leaveRequest.getEmployee(),
//                "Your leave request has been rejected by " + rejecter.getPersonalInfo().getFullName() + ".",
//                payload,
//                null
//        );
//    }
//
//    @Override
//    public void sendLeaveRequestCancellationNotifications(LeaveRequest leaveRequest) {
//        sendStandardNotification(
//                "leave_request_cancelled_employee_in_app",
//                "leave_request_cancelled_employee",
//                leaveRequest.getEmployee(),
//                leaveRequest,
//                "Your leave request has been successfully cancelled."
//        );
//    }
//
//    private Map<String, Object> createBasePayload(LeaveRequest leaveRequest, Employee recipient) {
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("employeeName", leaveRequest.getEmployee().getPersonalInfo().getFullName());
//        payload.put("leaveType", leaveRequest.getLeavePolicy().getPolicyName());
//        payload.put("startDate", leaveRequest.getStartDate().toString());
//        payload.put("endDate", leaveRequest.getEndDate().toString());
//        payload.put("approverName", recipient.getPersonalInfo().getFullName());
//        return payload;
//    }
//
//    private void sendStandardNotification(String inAppTemplateId, String emailTemplateId, Employee recipient, LeaveRequest leaveRequest, String defaultMessage) {
//        Map<String, Object> payload = createBasePayload(leaveRequest, recipient);
//        sendNotificationWithPayload(inAppTemplateId, emailTemplateId, recipient, defaultMessage, payload, null);
//    }
//
//    private void sendActionableNotification(String inAppTemplateId, String emailTemplateId, LeaveRequestApproval approval, LeaveRequest leaveRequest, String defaultMessage) {
//        Map<String, Object> payload = createBasePayload(leaveRequest, approval.getApprover());
//        sendNotificationWithPayload(inAppTemplateId, emailTemplateId, approval.getApprover(), defaultMessage, payload, approval.getId());
//    }
//
//    private void sendNotificationWithPayload(String inAppTemplateId, String emailTemplateId, Employee recipient, String defaultMessage, Map<String, Object> payload, Long approvalId) {
//        try {
//            // --- For In-App Notifications ---
//            Map<String, Object> inAppPayload = new HashMap<>(payload);
//            inAppPayload.put("inAppTitle", "Leave Request Update");
//            inAppPayload.put("inAppMessage", defaultMessage);
//
//            if (approvalId != null) {
//                Map<String, Object> additionalData = new HashMap<>();
//                additionalData.put("notificationType", "LEAVE_APPROVAL_PENDING");
//                additionalData.put("leaveRequestId", payload.get("leaveRequestId")); // Assuming leaveRequestId is in payload
//                additionalData.put("approvalId", approvalId);
//                inAppPayload.put("additionalData", additionalData);
//            }
//
//            NotificationRequest inAppRequest = new NotificationRequest(
//                    recipient.getEmployeeId(),
//                    NotificationRequest.ChannelType.IN_APP,
//                    recipient.getEmployeeId(),
//                    inAppTemplateId,
//                    inAppPayload
//            );
//            notificationOrchestrationService.processNotificationRequest(inAppRequest);
//
//            // --- For Email Notifications ---
//            NotificationRequest emailRequest = new NotificationRequest(
//                    recipient.getEmployeeId(),
//                    NotificationRequest.ChannelType.EMAIL,
//                    recipient.getEmail(),
//                    emailTemplateId,
//                    payload
//            );
//            notificationOrchestrationService.processNotificationRequest(emailRequest);
//
//        } catch (Exception e) {
//            logger.error("Failed to send notification for recipient {}: {}", recipient.getEmployeeId(), e.getMessage(), e);
//        }
//    }
//}

//package com.wfm.experts.modules.wfm.employee.leave.service.impl;
//
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
//import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestNotificationService;
//import com.wfm.experts.notificationengine.dto.NotificationRequest;
//import com.wfm.experts.notificationengine.service.NotificationOrchestrationService;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class LeaveRequestNotificationServiceImpl implements LeaveRequestNotificationService {
//
//    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestNotificationServiceImpl.class);
//    private final NotificationOrchestrationService notificationOrchestrationService;
//
//    @Override
//    public void sendLeaveRequestSubmissionNotifications(LeaveRequest leaveRequest, LeaveRequestApproval approval) {
//        // Notify the approver with actionable data
//        sendActionableNotification(
//                "leave_request_submitted_approver_in_app",
//                "leave_request_submitted_approver",
//                approval,
//                leaveRequest,
//                "A new leave request from " + leaveRequest.getEmployee().getPersonalInfo().getFullName() + " is pending your approval."
//        );
//
//        // Notify the employee
//        sendStandardNotification(
//                "leave_request_submitted_employee_in_app",
//                "leave_request_submitted_employee",
//                leaveRequest.getEmployee(),
//                leaveRequest,
//                "Your leave request has been submitted successfully."
//        );
//    }
//
//    @Override
//    public void sendLeaveRequestApprovalNotifications(LeaveRequest leaveRequest, Employee approver, LeaveRequestApproval nextApproval) {
//        // Build the dynamic part of the message for the employee
//        String nextApproverInfo = "";
//        if (nextApproval != null && nextApproval.getApprover() != null) {
//            nextApproverInfo = " It is now pending approval from " + nextApproval.getApprover().getPersonalInfo().getFullName() + ".";
//        }
//
//        // Notify the employee
//        Map<String, Object> employeePayload = createBasePayload(leaveRequest, approver);
//        employeePayload.put("nextApproverInfo", nextApproverInfo);
//        sendNotificationWithPayload(
//                "leave_request_approved_employee_in_app",
//                "leave_request_approved_employee",
//                leaveRequest.getEmployee(),
//                "Your leave request has been approved by " + approver.getPersonalInfo().getFullName() + ".",
//                employeePayload,
//                null
//        );
//
//        // If there's a next approver, notify them
//        if (nextApproval != null) {
//            Map<String, Object> nextApproverPayload = createBasePayload(leaveRequest, nextApproval.getApprover());
//            nextApproverPayload.put("previousApproverName", approver.getPersonalInfo().getFullName());
//            sendNotificationWithPayload(
//                    "leave_request_approved_next_approver_in_app",
//                    "leave_request_approved_next_approver",
//                    nextApproval.getApprover(),
//                    "A leave request approved by " + approver.getPersonalInfo().getFullName() + " is now pending your approval.",
//                    nextApproverPayload,
//                    nextApproval.getId()
//            );
//        }
//    }
//
//    @Override
//    public void sendLeaveRequestRejectionNotifications(LeaveRequest leaveRequest, Employee rejecter) {
//        Map<String, Object> payload = createBasePayload(leaveRequest, rejecter);
//        payload.put("rejecterName", rejecter.getPersonalInfo().getFullName());
//        sendNotificationWithPayload(
//                "leave_request_rejected_employee_in_app",
//                "leave_request_rejected_employee",
//                leaveRequest.getEmployee(),
//                "Your leave request has been rejected by " + rejecter.getPersonalInfo().getFullName() + ".",
//                payload,
//                null
//        );
//    }
//
//    @Override
//    public void sendLeaveRequestCancellationNotifications(LeaveRequest leaveRequest) {
//        sendStandardNotification(
//                "leave_request_cancelled_employee_in_app",
//                "leave_request_cancelled_employee",
//                leaveRequest.getEmployee(),
//                leaveRequest,
//                "Your leave request has been successfully cancelled."
//        );
//    }
//
//    private Map<String, Object> createBasePayload(LeaveRequest leaveRequest, Employee recipient) {
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("employeeName", leaveRequest.getEmployee().getPersonalInfo().getFullName());
//        payload.put("leaveType", leaveRequest.getLeavePolicy().getPolicyName());
//        payload.put("startDate", leaveRequest.getStartDate().toString());
//        payload.put("endDate", leaveRequest.getEndDate().toString());
//        payload.put("approverName", recipient.getPersonalInfo().getFullName());
//        return payload;
//    }
//
//    private void sendStandardNotification(String inAppTemplateId, String emailTemplateId, Employee recipient, LeaveRequest leaveRequest, String defaultMessage) {
//        Map<String, Object> payload = createBasePayload(leaveRequest, recipient);
//        sendNotificationWithPayload(inAppTemplateId, emailTemplateId, recipient, defaultMessage, payload, null);
//    }
//
//    private void sendActionableNotification(String inAppTemplateId, String emailTemplateId, LeaveRequestApproval approval, LeaveRequest leaveRequest, String defaultMessage) {
//        Map<String, Object> payload = createBasePayload(leaveRequest, approval.getApprover());
//        sendNotificationWithPayload(inAppTemplateId, emailTemplateId, approval.getApprover(), defaultMessage, payload, approval.getId());
//    }
//
//    private void sendNotificationWithPayload(String inAppTemplateId, String emailTemplateId, Employee recipient, String defaultMessage, Map<String, Object> payload, Long approvalId) {
//        try {
//            // --- For In-App Notifications ---
//            Map<String, Object> inAppPayload = new HashMap<>(payload);
//            inAppPayload.put("inAppTitle", "Leave Request Update");
//            inAppPayload.put("inAppMessage", defaultMessage);
//
//            if (approvalId != null) {
//                Map<String, Object> additionalData = new HashMap<>();
//                additionalData.put("notificationType", "LEAVE_APPROVAL_PENDING");
//                additionalData.put("leaveRequestId", payload.get("leaveRequestId")); // Assuming leaveRequestId is in payload
//                additionalData.put("approvalId", approvalId);
//                inAppPayload.put("additionalData", additionalData);
//            }
//
//            NotificationRequest inAppRequest = new NotificationRequest(
//                    recipient.getEmployeeId(),
//                    NotificationRequest.ChannelType.IN_APP,
//                    recipient.getEmployeeId(),
//                    inAppTemplateId,
//                    inAppPayload
//            );
//            notificationOrchestrationService.processNotificationRequest(inAppRequest);
//
//            // --- For Email Notifications ---
//            NotificationRequest emailRequest = new NotificationRequest(
//                    recipient.getEmployeeId(),
//                    NotificationRequest.ChannelType.EMAIL,
//                    recipient.getEmail(),
//                    emailTemplateId,
//                    payload
//            );
//            notificationOrchestrationService.processNotificationRequest(emailRequest);
//
//        } catch (Exception e) {
//            logger.error("Failed to send notification for recipient {}: {}", recipient.getEmployeeId(), e.getMessage(), e);
//        }
//    }
//}

package com.wfm.experts.modules.wfm.employee.leave.service.impl;

import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestNotificationService;
import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.service.NotificationOrchestrationService;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveRequestNotificationServiceImpl implements LeaveRequestNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestNotificationServiceImpl.class);
    private final NotificationOrchestrationService notificationOrchestrationService;

    @Override
    public void sendLeaveRequestSubmissionNotifications(LeaveRequest leaveRequest, LeaveRequestApproval approval) {
        // Notify the approver with actionable data
        sendActionableNotification(
                "leave_request_submitted_approver_in_app",
                "leave_request_submitted_approver",
                approval,
                leaveRequest,
                "A new leave request from " + leaveRequest.getEmployee().getPersonalInfo().getFullName() + " is pending your approval."
        );

        // Notify the employee
        sendStandardNotification(
                "leave_request_submitted_employee_in_app",
                "leave_request_submitted_employee",
                leaveRequest.getEmployee(),
                leaveRequest,
                "Your leave request has been submitted successfully."
        );
    }

    @Override
    public void sendLeaveRequestApprovalNotifications(LeaveRequest leaveRequest, Employee approver, LeaveRequestApproval nextApproval) {
        // Build the dynamic part of the message for the employee
        String nextApproverInfo = "";
        if (nextApproval != null && nextApproval.getApprover() != null) {
            nextApproverInfo = " It is now pending approval from " + nextApproval.getApprover().getPersonalInfo().getFullName() + ".";
        }

        // Notify the employee
        Map<String, Object> employeePayload = createBasePayload(leaveRequest, approver);
        employeePayload.put("nextApproverInfo", nextApproverInfo);
        sendNotificationWithPayload(
                "leave_request_approved_employee_in_app",
                "leave_request_approved_employee",
                leaveRequest.getEmployee(),
                "Your leave request has been approved by " + approver.getPersonalInfo().getFullName() + ".",
                employeePayload,
                null
        );

        // If there's a next approver, notify them
        if (nextApproval != null) {
            Map<String, Object> nextApproverPayload = createBasePayload(leaveRequest, nextApproval.getApprover());
            nextApproverPayload.put("previousApproverName", approver.getPersonalInfo().getFullName());
            sendNotificationWithPayload(
                    "leave_request_approved_next_approver_in_app",
                    "leave_request_approved_next_approver",
                    nextApproval.getApprover(),
                    "A leave request approved by " + approver.getPersonalInfo().getFullName() + " is now pending your approval.",
                    nextApproverPayload,
                    nextApproval.getId()
            );
        }
    }

    @Override
    public void sendLeaveRequestRejectionNotifications(LeaveRequest leaveRequest, Employee rejecter) {
        Map<String, Object> payload = createBasePayload(leaveRequest, rejecter);
        payload.put("rejecterName", rejecter.getPersonalInfo().getFullName());
        sendNotificationWithPayload(
                "leave_request_rejected_employee_in_app",
                "leave_request_rejected_employee",
                leaveRequest.getEmployee(),
                "Your leave request has been rejected by " + rejecter.getPersonalInfo().getFullName() + ".",
                payload,
                null
        );
    }

    @Override
    public void sendLeaveRequestCancellationNotifications(LeaveRequest leaveRequest) {
        sendStandardNotification(
                "leave_request_cancelled_employee_in_app",
                "leave_request_cancelled_employee",
                leaveRequest.getEmployee(),
                leaveRequest,
                "Your leave request has been successfully cancelled."
        );
    }

    private Map<String, Object> createBasePayload(LeaveRequest leaveRequest, Employee recipient) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("employeeName", leaveRequest.getEmployee().getPersonalInfo().getFullName());
        payload.put("leaveType", leaveRequest.getLeavePolicy().getPolicyName());
        payload.put("startDate", leaveRequest.getStartDate().toString());
        payload.put("endDate", leaveRequest.getEndDate().toString());
        payload.put("approverName", recipient.getPersonalInfo().getFullName());
        payload.put("leaveStatus", leaveRequest.getStatus().name());
        payload.put("leaveRequestId", leaveRequest.getId());
        return payload;
    }

    private void sendStandardNotification(String inAppTemplateId, String emailTemplateId, Employee recipient, LeaveRequest leaveRequest, String defaultMessage) {
        Map<String, Object> payload = createBasePayload(leaveRequest, recipient);
        sendNotificationWithPayload(inAppTemplateId, emailTemplateId, recipient, defaultMessage, payload, null);
    }

    private void sendActionableNotification(String inAppTemplateId, String emailTemplateId, LeaveRequestApproval approval, LeaveRequest leaveRequest, String defaultMessage) {
        Map<String, Object> payload = createBasePayload(leaveRequest, approval.getApprover());
        sendNotificationWithPayload(inAppTemplateId, emailTemplateId, approval.getApprover(), defaultMessage, payload, approval.getId());
    }

    private void sendNotificationWithPayload(String inAppTemplateId, String emailTemplateId, Employee recipient, String defaultMessage, Map<String, Object> payload, Long approvalId) {
        try {
            // --- For In-App Notifications ---
            Map<String, Object> inAppPayload = new HashMap<>(payload);
            inAppPayload.put("inAppTitle", "Leave Request Update");
            inAppPayload.put("inAppMessage", defaultMessage);

            Map<String, Object> additionalData = new HashMap<>();

            // Populate additionalData for actionable/status tracking
            additionalData.put("leaveRequestId", payload.get("leaveRequestId"));
            additionalData.put("leaveStatus", payload.get("leaveStatus"));

            if (approvalId != null) {
                additionalData.put("notificationType", "LEAVE_APPROVAL_PENDING");
                additionalData.put("approvalId", approvalId);
            } else {
                // If it's not a pending approval, it's a general status update
                additionalData.put("notificationType", "LEAVE_STATUS_UPDATE");
            }

            inAppPayload.put("additionalData", additionalData);


            NotificationRequest inAppRequest = new NotificationRequest(
                    recipient.getEmployeeId(),
                    NotificationRequest.ChannelType.IN_APP,
                    recipient.getEmployeeId(),
                    inAppTemplateId,
                    inAppPayload
            );
            notificationOrchestrationService.processNotificationRequest(inAppRequest);

            // --- For Email Notifications ---
            NotificationRequest emailRequest = new NotificationRequest(
                    recipient.getEmployeeId(),
                    NotificationRequest.ChannelType.EMAIL,
                    recipient.getEmail(),
                    emailTemplateId,
                    payload
            );
            notificationOrchestrationService.processNotificationRequest(emailRequest);

        } catch (Exception e) {
            logger.error("Failed to send notification for recipient {}: {}", recipient.getEmployeeId(), e.getMessage(), e);
        }
    }
}