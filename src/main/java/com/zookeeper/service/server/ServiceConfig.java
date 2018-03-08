package com.zookeeper.service.server;



/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ServiceConfig {
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 业务线名
     */
    private String serviceLine;
    private String version;
    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务器全路径=companyName+serviceLine+serviceName+version+"/host";
     */
    private String path;
    /**
     * 服务器编号
     */
    private String sequenceNo;
    public final static String SERVICE_HOST_NODE_NAME="/host";
    public ServiceConfig() {
    }

    public ServiceConfig(String companyName, String serviceLine,String serviceName,String version ) {
        this.companyName = companyName;
        this.serviceLine = serviceLine;
        this.version=version;
        this.serviceName = serviceName;
        this.path="/"+companyName+"/"+ serviceLine +"/"+serviceName+"/"+version+SERVICE_HOST_NODE_NAME;
    }

    public ServiceConfig( String companyName,String serviceLine,String serviceName) {
      this(companyName,serviceLine,serviceName,"v0");
    }

    public void resetPath(){
        this.path="/"+companyName+"/"+ serviceLine +"/"+serviceName+"/"+version+SERVICE_HOST_NODE_NAME;
    }
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getServiceLine() {
        return serviceLine;
    }

    public void setServiceLine(String serviceLine) {
        this.serviceLine = serviceLine;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
