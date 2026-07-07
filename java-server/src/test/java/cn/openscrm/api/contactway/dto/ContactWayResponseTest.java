package cn.openscrm.api.contactway.dto;

import static org.assertj.core.api.Assertions.assertThat;

import cn.openscrm.api.persistence.entity.ContactWayGroupPo;
import cn.openscrm.api.persistence.entity.ContactWayPo;
import cn.openscrm.api.persistence.entity.TagPo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ContactWayResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void contactWayResponseFlattensPrimaryModelAndKeepsCustomerTags() throws Exception {
        ContactWayPo contactWay = new ContactWayPo();
        contactWay.setId(100L);
        contactWay.setName("渠道码 A");
        contactWay.setConfigId("config-1");
        TagPo tag = new TagPo();
        tag.setExtId("et-1");
        tag.setName("高意向");
        ContactWayResponse response = new ContactWayResponse();
        response.setContactWay(contactWay);
        response.setCustomerTags(Collections.singletonList(tag));

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.has("contactWay")).isFalse();
        assertThat(json.get("id").asLong()).isEqualTo(100L);
        assertThat(json.get("name").asText()).isEqualTo("渠道码 A");
        assertThat(json.get("configId").asText()).isEqualTo("config-1");
        assertThat(json.get("customer_tags").get(0).get("extId").asText()).isEqualTo("et-1");
    }

    @Test
    void contactWayGroupResponseFlattensGroupModelAndKeepsComputedCount() {
        ContactWayGroupPo group = new ContactWayGroupPo();
        group.setId(200L);
        group.setName("默认分组");

        JsonNode json = objectMapper.valueToTree(new ContactWayGroupResponse(group, 7L));

        assertThat(json.has("contactWayGroup")).isFalse();
        assertThat(json.get("id").asLong()).isEqualTo(200L);
        assertThat(json.get("name").asText()).isEqualTo("默认分组");
        assertThat(json.get("count").asLong()).isEqualTo(7L);
    }
}
