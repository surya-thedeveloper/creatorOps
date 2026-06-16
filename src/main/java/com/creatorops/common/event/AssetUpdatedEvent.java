package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AssetUpdatedEvent extends DomainEvent {
    public AssetUpdatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String name) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.ASSET_UPDATED,
            EntityType.ASSET,
            "Asset '" + name + "' was updated",
            null
        );
    }
}
