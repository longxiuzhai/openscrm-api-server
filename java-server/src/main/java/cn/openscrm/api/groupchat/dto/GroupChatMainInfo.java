package cn.openscrm.api.groupchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatMainInfo {

    private String extChatId;

    private String name;

    private String ownerName;
}
