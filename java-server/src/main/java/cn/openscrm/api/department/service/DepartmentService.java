package cn.openscrm.api.department.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.DepartmentPo;
import cn.openscrm.api.persistence.mapper.DepartmentPoMapper;
import cn.openscrm.api.wework.DepartmentInfo;
import cn.openscrm.api.wework.DepartmentListResponse;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentPoMapper departmentMapper;
    private final WeWorkClient weWorkClient;
    private final OpenScrmProperties properties;
    private final SnowflakeIdGenerator idGenerator;

    public DepartmentService(DepartmentPoMapper departmentMapper,
                             WeWorkClient weWorkClient,
                             OpenScrmProperties properties,
                             SnowflakeIdGenerator idGenerator) {
        this.departmentMapper = departmentMapper;
        this.weWorkClient = weWorkClient;
        this.properties = properties;
        this.idGenerator = idGenerator;
    }

    public void sync(String extCorpId) {
        DepartmentListResponse response = weWorkClient.listDepartments(extCorpId, properties.getWeWork().getContactSecret());
        for (DepartmentInfo item : response.getDepartment()) {
            upsert(extCorpId, item);
        }
    }

    public PageResponse<DepartmentPo> query(String extCorpId, Integer extParentId, long page, long pageSize) {
        LambdaQueryWrapper<DepartmentPo> wrapper = new LambdaQueryWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .eq(extParentId != null && extParentId > 0, DepartmentPo::getExtParentId, extParentId)
                .orderByDesc(DepartmentPo::getOrder);
        IPage<DepartmentPo> result = departmentMapper.selectPage(Page.of(page, pageSize), wrapper);
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public DepartmentTreeNode tree(String extCorpId, Integer extId) {
        DepartmentPo root = departmentMapper.selectOne(new LambdaQueryWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .eq(extId != null && extId > 0, DepartmentPo::getExtId, extId)
                .last("limit 1"));
        if (root == null) {
            return null;
        }
        return buildTree(extCorpId, root);
    }

    private DepartmentTreeNode buildTree(String extCorpId, DepartmentPo root) {
        DepartmentTreeNode node = new DepartmentTreeNode(root);
        List<DepartmentPo> children = departmentMapper.selectList(new LambdaQueryWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .eq(DepartmentPo::getExtParentId, root.getExtId())
                .orderByDesc(DepartmentPo::getOrder));
        for (DepartmentPo child : children) {
            node.getSubDepartments().add(buildTree(extCorpId, child));
        }
        return node;
    }

    private void upsert(String extCorpId, DepartmentInfo source) {
        DepartmentPo existing = departmentMapper.selectOne(new LambdaQueryWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .eq(DepartmentPo::getExtId, source.getId().intValue())
                .last("limit 1"));
        DepartmentPo item = existing == null ? new DepartmentPo() : existing;
        if (item.getId() == null) {
            item.setId(idGenerator.nextId());
        }
        item.setExtCorpId(extCorpId);
        item.setExtId(source.getId().intValue());
        item.setName(StringUtils.hasText(source.getName()) ? source.getName() : "");
        item.setExtParentId(source.getParentId() == null ? 0 : source.getParentId().intValue());
        item.setOrder(source.getOrder());
        if (existing == null) {
            departmentMapper.insert(item);
        } else {
            departmentMapper.updateById(item);
        }
    }

    public static class DepartmentTreeNode extends DepartmentPo {

        private final List<DepartmentTreeNode> subDepartments = new ArrayList<>();

        public DepartmentTreeNode(DepartmentPo source) {
            setId(source.getId());
            setExtCorpId(source.getExtCorpId());
            setExtId(source.getExtId());
            setName(source.getName());
            setExtParentId(source.getExtParentId());
            setOrder(source.getOrder());
            setWelcomeMsgId(source.getWelcomeMsgId());
            setStaffNum(source.getStaffNum());
            setCreatedAt(source.getCreatedAt());
            setUpdatedAt(source.getUpdatedAt());
            setDeletedAt(source.getDeletedAt());
        }

        public List<DepartmentTreeNode> getSubDepartments() {
            return subDepartments;
        }
    }
}
