package me.szilprog.simplenpc;

public class PermissionData {
    public String usePermission;
    public String useMessage;
    public String seePermission;

    public PermissionData(String usePermission, String useMessage, String seePermission) {
        this.usePermission = usePermission;
        this.useMessage = useMessage;
        this.seePermission = seePermission;
    }
}
