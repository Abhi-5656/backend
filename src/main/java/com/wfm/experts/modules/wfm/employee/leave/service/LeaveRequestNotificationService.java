//package com.wfm.experts.modules.wfm.employee.leave.service;
//
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//
///**
// * Service interface for sending notifications related to leave requests.
// */
//public interface LeaveRequestNotificationService {
//
//    /**
//     * Sends notifications when a leave request is first submitted.
//     *
//     * @param leaveRequest The submitted leave request.
//     * @param approver     The first approver in the chain.
//     */
//    void sendLeaveRequestSubmissionNotifications(LeaveRequest leaveRequest, Employee approver);
//
//    /**
//     * Sends notifications when a leave request is approved.
//     *
//     * @param leaveRequest The approved leave request.
//     * @param nextApprover The next approver in the chain, if any.
//     */
//    void sendLeaveRequestApprovalNotifications(LeaveRequest leaveRequest, Employee nextApprover);
//
//    /**
//     * Sends a notification when a leave request is rejected.
//     *
//     * @param leaveRequest The rejected leave request.
//     */
//    void sendLeaveRequestRejectionNotifications(LeaveRequest leaveRequest);
//
//    /**
//     * Sends a notification when a leave request is cancelled by the employee.
//     *
//     * @param leaveRequest The cancelled leave request.
//     */
//    void sendLeaveRequestCancellationNotifications(LeaveRequest leaveRequest);
//}

//package com.wfm.experts.modules.wfm.employee.leave.service;
//
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//
///**
// * Service interface for sending notifications related to leave requests.
// */
//public interface LeaveRequestNotificationService {
//
//    /**
//     * Sends notifications when a leave request is first submitted.
//     *
//     * @param leaveRequest The submitted leave request.
//     * @param approval     The first approval record containing the approver and approval ID.
//     */
//    void sendLeaveRequestSubmissionNotifications(LeaveRequest leaveRequest, LeaveRequestApproval approval);
//
//    /**
//     * Sends notifications when a leave request is approved.
//     *
//     * @param leaveRequest The approved leave request.
//     * @param nextApproval The next approval record in the chain, if any.
//     */
//    void sendLeaveRequestApprovalNotifications(LeaveRequest leaveRequest, LeaveRequestApproval nextApproval);
//
//    /**
//     * Sends a notification when a leave request is rejected.
//     *
//     * @param leaveRequest The rejected leave request.
//     */
//    void sendLeaveRequestRejectionNotifications(LeaveRequest leaveRequest);
//
//    /**
//     * Sends a notification when a leave request is cancelled by the employee.
//     *
//     * @param leaveRequest The cancelled leave request.
//     */
//    void sendLeaveRequestCancellationNotifications(LeaveRequest leaveRequest);
//}

package com.wfm.experts.modules.wfm.employee.leave.service;

import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
import com.wfm.experts.tenant.common.employees.entity.Employee;

/**
 * Service interface for sending notifications related to leave requests.
 */
public interface LeaveRequestNotificationService {

    /**
     * Sends notifications when a leave request is first submitted.
     *
     * @param leaveRequest The submitted leave request.
     * @param approval     The first approval record containing the approver and approval ID.
     */
    void sendLeaveRequestSubmissionNotifications(LeaveRequest leaveRequest, LeaveRequestApproval approval);

    /**
     * Sends notifications when a leave request is approved.
     *
     * @param leaveRequest The approved leave request.
     * @param approver     The employee who approved the request.
     * @param nextApproval The next approval record in the chain, if any.
     */
    void sendLeaveRequestApprovalNotifications(LeaveRequest leaveRequest, Employee approver, LeaveRequestApproval nextApproval);

    /**
     * Sends a notification when a leave request is rejected.
     *
     * @param leaveRequest The rejected leave request.
     * @param rejecter     The employee who rejected the request.
     */
    void sendLeaveRequestRejectionNotifications(LeaveRequest leaveRequest, Employee rejecter);

    /**
     * Sends a notification when a leave request is cancelled by the employee.
     *
     * @param leaveRequest The cancelled leave request.
     */
    void sendLeaveRequestCancellationNotifications(LeaveRequest leaveRequest);

    /**
     * Sends notifications for an auto-approved leave request.
     *
     * @param leaveRequest The auto-approved leave request.
     */
    void sendAutoApprovalNotifications(LeaveRequest leaveRequest);
}