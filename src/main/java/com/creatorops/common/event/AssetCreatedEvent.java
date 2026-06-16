package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AssetCreatedEvent extends DomainEvent {
    public AssetCreatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String name) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.ASSET_CREATED,
            EntityType.ASSET,
            "Asset '" + name + "' was added",
            null
        );
    }
}
