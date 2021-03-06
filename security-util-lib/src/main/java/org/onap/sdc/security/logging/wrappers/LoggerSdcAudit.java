/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.security.logging.wrappers;

import org.apache.commons.lang3.StringUtils;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.sdc.security.logging.elements.LoggerAudit;
import org.onap.sdc.security.logging.elements.LoggerBase;
import org.onap.sdc.security.logging.elements.LoggerFactory;
import org.onap.sdc.security.logging.enums.*;
import org.slf4j.MDC;
import org.slf4j.Marker;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.UUID;

public class LoggerSdcAudit extends LoggerSdcUtilBase {

    private static String AUDIT_ON = "auditOn";
    private String className;
    private final LoggerAudit ecompLoggerAudit;

    public LoggerSdcAudit(Class<?> clazz) {
        this.className = clazz.getName();
        ecompLoggerAudit = LoggerFactory.getMdcLogger(LoggerAudit.class, org.slf4j.LoggerFactory.getLogger(clazz));
    }

    public void startLog(ContainerRequestContext requestContext) {
        ecompLoggerAudit.clear()
                .startTimer()
                .setPartnerName(getPartnerName(
                        requestContext.getHeaderString("user-agent"),
                        requestContext.getHeaderString("USER_ID"),
                        getUrl(requestContext),
                        requestContext.getHeaderString("X-ONAP-PartnerName")))
                .setServiceName(getServiceName(requestContext))
                .setKeyRequestId(LoggerBase.generateKeyRequestId())
                .setKeyInvocationId(UUID.randomUUID().toString());
        MDC.put(AUDIT_ON, "true");
    }

    public void startAuditFetchLog(String partnerName, String serviceName) {
        ecompLoggerAudit.clear()
                .startTimer()
                .setPartnerName(partnerName)
                .setServiceName(serviceName)
                .setOptClassName(serviceName);
        MDC.put(AUDIT_ON, "true");
    }

    public static boolean isFlowBeingTakenCare() {
        String auditOn = MDC.get(AUDIT_ON);
        return !StringUtils.isEmpty(auditOn) && "true".equals(auditOn);
    }

    //this function clears the MDC data that relevant for this class
    public void clearMyData(){
        ecompLoggerAudit.clear();
    }

    public void logExit(String remoteAddress,
                        ContainerRequestContext requestContext,
                        Response.StatusType statusInfo,
                        LogLevel logLevel,
                        Severity securityLevel,
                        String message,
                        Marker marker) {

        try {

            String msg = ecompLoggerAudit.getAuditMessage() == null ?
                    message : ecompLoggerAudit.getAuditMessage();
            ecompLoggerAudit.stopTimer()
                    .setRemoteHost(remoteAddress)
                    .setResponseCode(convertHttpCodeToErrorCode(statusInfo.getStatusCode()))
                    .setStatusCodeByResponseCode(Integer.toString(statusInfo.getStatusCode()))
                    .setResponseDesc(statusInfo.getReasonPhrase())
                    .setInstanceUUID(requestContext.getHeaderString(EcompHeadersConstants.X_ECOMP_INSTANCE_ID_HEADER))
                    .setOptServiceInstanceId(requestContext.getHeaderString(EcompHeadersConstants.X_ECOMP_SERVICE_ID_HEADER))
                    .setOptClassName(className)
                    .setOptAlertSeverity(securityLevel)
                    .log(marker, logLevel, msg);
        }
        catch (Exception e) {
            log.warn("Failed to write to Audit Log. Original Message: {}", message, e);
        }
        finally {
            MDC.put(AUDIT_ON,"false");
        }
    }

    public void logEntry(String remoteAddress,
                          ContainerRequestContext requestContext,
                          LogLevel logLevel,
                          Severity securityLevel,
                          String message,
                          Marker marker) {

        try {

            String msg = ecompLoggerAudit.getAuditMessage() == null ?
                    message : ecompLoggerAudit.getAuditMessage();
            ecompLoggerAudit.stopTimer()
                    .setRemoteHost(remoteAddress)
                    .setResponseCode(EcompLoggerErrorCode.SUCCESS)
                    .setStatusCode(ONAPLogConstants.ResponseStatus.INPROGRESS.name())
                    .setResponseDesc("")
                    .setInstanceUUID(requestContext.getHeaderString(EcompHeadersConstants.X_ECOMP_INSTANCE_ID_HEADER))
                    .setOptServiceInstanceId(requestContext.getHeaderString(EcompHeadersConstants.X_ECOMP_SERVICE_ID_HEADER))
                    .setOptClassName(className)
                    .setOptAlertSeverity(securityLevel)
                    .log(marker, logLevel, msg);
        }
        catch (Exception e) {
            log.warn("Failed to write to Audit Log. Original Message: {}", message, e);
        }
        finally {
            MDC.put(AUDIT_ON,"false");
        }
    }

    public void logEntry(LogLevel logLevel,
                         Severity securityLevel,
                         String message,
                         Marker marker,
                         String requestId) {

        try {

            String msg = ecompLoggerAudit.getAuditMessage() == null ?
                    message : ecompLoggerAudit.getAuditMessage();
            ecompLoggerAudit.stopTimer()
                    .setKeyRequestId(requestId)
                    .setResponseCode(EcompLoggerErrorCode.SUCCESS)
                    .setStatusCode(ONAPLogConstants.ResponseStatus.COMPLETE.name())
                    .setResponseDesc("")
                    .setOptAlertSeverity(securityLevel)
                    .log(marker, logLevel, msg);
        }
        catch (Exception e) {
            log.warn("Failed to write to Audit Log. Original Message: {}", message, e);
        }
        finally {
            MDC.put(AUDIT_ON,"false");
        }
    }

}
