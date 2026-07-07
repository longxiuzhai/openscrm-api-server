package cn.openscrm.api.wework.callback;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.persistence.entity.DepartmentPo;
import cn.openscrm.api.persistence.mapper.DepartmentPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentCallbackRegistrar implements WeWorkCallbackRegistrar {

    private final DepartmentPoMapper departmentMapper;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public void register(WeWorkCallbackService service) {
        service.register("event", "change_contact", "create_party", this::createDepartment);
        service.register("event", "change_contact", "update_party", this::updateDepartment);
        service.register("event", "change_contact", "delete_party", this::deleteDepartment);
    }

    private void createDepartment(WeWorkCallbackMessage message) {
        Integer extId = intField(message, "Id");
        if (extId == null) {
            log.warn("missing department Id in create_party callback fields={}", message.getFields());
            return;
        }
        DepartmentPo existing = find(message.getToUserName(), extId);
        DepartmentPo item = existing == null ? new DepartmentPo() : existing;
        if (item.getId() == null) {
            item.setId(idGenerator.nextId());
        }
        item.setExtCorpId(message.getToUserName());
        item.setExtId(extId);
        item.setName(value(message, "Name"));
        item.setExtParentId(intField(message, "ParentId"));
        item.setOrder(intField(message, "Order"));
        if (existing == null) {
            departmentMapper.insert(item);
        } else {
            departmentMapper.updateById(item);
        }
    }

    private void updateDepartment(WeWorkCallbackMessage message) {
        Integer extId = intField(message, "Id");
        if (extId == null) {
            log.warn("missing department Id in update_party callback fields={}", message.getFields());
            return;
        }
        DepartmentPo item = find(message.getToUserName(), extId);
        if (item == null) {
            createDepartment(message);
            return;
        }
        if (StringUtils.hasText(value(message, "Name"))) {
            item.setName(value(message, "Name"));
        }
        Integer parentId = intField(message, "ParentId");
        if (parentId != null) {
            item.setExtParentId(parentId);
        }
        departmentMapper.updateById(item);
    }

    private void deleteDepartment(WeWorkCallbackMessage message) {
        Integer extId = intField(message, "Id");
        if (extId == null) {
            log.warn("missing department Id in delete_party callback fields={}", message.getFields());
            return;
        }
        departmentMapper.delete(new LambdaQueryWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, message.getToUserName())
                .eq(DepartmentPo::getExtId, extId));
    }

    private DepartmentPo find(String extCorpId, Integer extId) {
        return departmentMapper.selectOne(new LambdaQueryWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .eq(DepartmentPo::getExtId, extId)
                .last("limit 1"));
    }

    private String value(WeWorkCallbackMessage message, String key) {
        return message.getFields().get(key);
    }

    private Integer intField(WeWorkCallbackMessage message, String key) {
        String value = value(message, key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.valueOf(value);
    }
}
