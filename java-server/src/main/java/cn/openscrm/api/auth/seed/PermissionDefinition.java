package cn.openscrm.api.auth.seed;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PermissionDefinition {

    private final String biz;
    private final String operation;
    private final String name;

    public String identity() {
        return biz + "_" + operation;
    }
}
