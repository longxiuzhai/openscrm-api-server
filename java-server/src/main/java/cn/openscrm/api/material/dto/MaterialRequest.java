package cn.openscrm.api.material.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialRequest {

    private String title;

    @JsonProperty("material_type")
    private String materialType;

    @JsonProperty("file_url")
    private String fileUrl;

    private String link;

    @JsonProperty("file_size")
    private Long fileSize;

    private String digest;

    @JsonProperty("material_tag_list")
    private List<String> materialTagList;
}
