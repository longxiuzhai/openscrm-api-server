package cn.openscrm.api.contactway.service;

import cn.openscrm.api.persistence.entity.ContactWayBackupStaffPo;
import cn.openscrm.api.persistence.entity.ContactWayScheduleStaffPo;
import cn.openscrm.api.persistence.entity.ContactWayStaffPo;
import cn.openscrm.api.persistence.mapper.ContactWayBackupStaffPoMapper;
import cn.openscrm.api.persistence.mapper.ContactWayScheduleStaffPoMapper;
import cn.openscrm.api.persistence.mapper.ContactWayStaffPoMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactWayScheduledTasks {

    private final ContactWayStaffPoMapper contactWayStaffMapper;
    private final ContactWayBackupStaffPoMapper backupStaffMapper;
    private final ContactWayScheduleStaffPoMapper scheduleStaffMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyClean() {
        contactWayStaffMapper.update(null, new LambdaUpdateWrapper<ContactWayStaffPo>()
                .set(ContactWayStaffPo::getDailyAddCustomerCount, 0L));
        backupStaffMapper.update(null, new LambdaUpdateWrapper<ContactWayBackupStaffPo>()
                .set(ContactWayBackupStaffPo::getDailyAddCustomerCount, 0L));
        scheduleStaffMapper.update(null, new LambdaUpdateWrapper<ContactWayScheduleStaffPo>()
                .set(ContactWayScheduleStaffPo::getDailyAddCustomerCount, 0L));
        log.info("contact way daily customer counters cleaned");
    }
}
