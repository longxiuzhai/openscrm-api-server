package cn.openscrm.api.msgarch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MsgArchSyncResponse {

    private final boolean accepted;
    private final String message;
}
