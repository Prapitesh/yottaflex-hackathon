package com.hackathon.api.service.impl;
import java.time.Year;
import java.util.UUID;
/** Creates collision-resistant identifiers with domain prefixes. */
final class EntityIdGenerator {
 private EntityIdGenerator() { }
 static String departmentId() { return "DEP-" + suffix(); }
 static String candidateId() { return "CAND-" + suffix(); }
 static String requisitionId() { return "REQ-" + Year.now().getValue() + "-" + suffix(); }
 static String interviewId() { return "INT-" + Year.now().getValue() + "-" + suffix(); }
 static String offerId() { return "OFF-" + Year.now().getValue() + "-" + suffix(); }
 static String feedbackId(String interviewId,String interviewerId) { return "FB-" + interviewId + "-" + interviewerId; }
 static String applicationId(String requisitionId,String candidateId) { return "APP-" + requisitionId + "-" + candidateId + "-" + suffix(); }
 private static String suffix() { return UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase(); }
}
