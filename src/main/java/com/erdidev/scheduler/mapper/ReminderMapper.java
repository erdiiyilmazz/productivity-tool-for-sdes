package com.erdidev.scheduler.mapper;

import com.erdidev.scheduler.dto.ReminderDto;
import com.erdidev.scheduler.model.Reminder;
import com.erdidev.scheduler.model.ReminderNotificationChannel;
import com.erdidev.scheduler.enums.NotificationChannel;
import org.mapstruct.*;

import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, 
        imports = {ZoneId.class})
public interface ReminderMapper {
    
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "notificationChannels", ignore = true)
    @Mapping(target = "reminderTime", expression = "java(dto.getReminderTime().toLocalDateTime())")
    Reminder toEntity(ReminderDto dto);

    @Mapping(source = "task.id", target = "taskId")
    @Mapping(target = "notificationChannels", expression = "java(mapChannelsToDto(entity.getNotificationChannels()))")
    @Mapping(target = "reminderTime", expression = "java(entity.getReminderTime().atZone(ZoneId.systemDefault()))")
    ReminderDto toDto(Reminder entity);

    @AfterMapping
    default void mapNotificationChannels(@MappingTarget Reminder reminder, ReminderDto dto) {
        if (dto.getNotificationChannels() != null) {
            Set<ReminderNotificationChannel> channels = dto.getNotificationChannels().stream()
                .map(channel -> {
                    ReminderNotificationChannel notificationChannel = new ReminderNotificationChannel();
                    notificationChannel.setNotificationChannel(channel);
                    notificationChannel.setChannelType(channel);
                    notificationChannel.setChannelDetails("default");
                    return notificationChannel;
                })
                .collect(Collectors.toSet());
            reminder.setNotificationChannels(channels);
        }
    }
    
    default Set<NotificationChannel> mapChannelsToDto(Set<ReminderNotificationChannel> channels) {
        if (channels == null) return null;
        return channels.stream()
            .map(ReminderNotificationChannel::getNotificationChannel)
            .collect(Collectors.toSet());
    }
    
    default NotificationChannel mapToSingle(Set<NotificationChannel> channels) {
        return channels != null && !channels.isEmpty() ? channels.iterator().next() : null;
    }
} 