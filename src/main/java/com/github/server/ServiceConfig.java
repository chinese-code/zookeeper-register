package com.github.server;



/**
 * @author  tumingjian
 */
public class ServiceConfig {
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 业务线名
     */
    private String serviceLineName;
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 服务器全路径=namespace+companyName+serviceLineName+serviceName;
     */
    private String path;
    /**
     * 服务器编号
     */
    private String sequenceNo;
    public ServiceConfig() {
    }

    public ServiceConfig(String companyName, String serviceLineName, String serviceName ) {
        this.companyName = companyName;
        this.serviceLineName = serviceLineName;
        this.serviceName = serviceName;
        this.path="/"+companyName+"/"+serviceLineName+"/"+serviceName;
    }


    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getServiceLineName() {
        return serviceLineName;
    }

    public void setServiceLineName(String serviceLineName) {
        this.serviceLineName = serviceLineName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(String sequenceNo) {
        this.sequenceNo = sequenceNo;
    }
}
