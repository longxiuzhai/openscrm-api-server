package cn.openscrm.api.cluemanual.service;

import cn.openscrm.api.cluemanual.dto.ClueManualDeleteRequest;
import cn.openscrm.api.cluemanual.dto.ClueManualRequest;
import cn.openscrm.api.cluemanual.dto.ClueManualUpdateRequest;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ClueManualService {

    private static final String EVENT_TYPE_CLUE_MANUAL = "clue_manual_event";
    private static final String EVENT_NAME_CLUE_MANUAL = "clue_manual_event";

    private final CustomerEventPoMapper customerEventMapper;
    private final SnowflakeIdGenerator idGenerator;

    public CustomerEventPo create(ClueManualRequest request, String extCorpId, String extCreatorId) {
        validateCreate(request);
        CustomerEventPo event = new CustomerEventPo();
        event.setId(idGenerator.nextId());
        event.setExtCorpId(extCorpId);
        event.setExtCreatorId(extCreatorId);
        event.setExtStaffId(request.getExtStaffId());
        event.setExtCustomerId(request.getExtCustomerId());
        event.setContent(request.getContent());
        event.setEventType(EVENT_TYPE_CLUE_MANUAL);
        event.setEventName(EVENT_NAME_CLUE_MANUAL);
        customerEventMapper.insert(event);
        return event;
    }

    public CustomerEventPo update(Long id, ClueManualUpdateRequest request, String extCorpId) {
        if (id == null || request == null || !StringUtils.hasText(request.getContent())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        CustomerEventPo event = requireEvent(id, extCorpId);
        event.setContent(request.getContent());
        event.setEventType(EVENT_TYPE_CLUE_MANUAL);
        event.setEventName(EVENT_NAME_CLUE_MANUAL);
        customerEventMapper.updateById(event);
        return event;
    }

    public long delete(ClueManualDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return customerEventMapper.update(null, new UpdateWrapper<CustomerEventPo>()
                .eq("ext_corp_id", extCorpId)
                .in("id", request.getIds())
                .isNull("deleted_at")
                .set("deleted_at", LocalDateTime.now()));
    }

    private void validateCreate(ClueManualRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getExtStaffId())
                || !StringUtils.hasText(request.getExtCustomerId())
                || !StringUtils.hasText(request.getContent())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
    }

    private CustomerEventPo requireEvent(Long id, String extCorpId) {
        CustomerEventPo event = customerEventMapper.selectById(id);
        if (event == null || event.getDeletedAt() != null || !extCorpId.equals(event.getExtCorpId())) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return event;
    }
}
