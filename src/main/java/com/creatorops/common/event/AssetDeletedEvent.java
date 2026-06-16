package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AssetDeletedEvent extends DomainEvent {
    public AssetDeletedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String name) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.ASSET_DELETED,
            EntityType.ASSET,
            "Asset '" + name + "' was deleted",
            null
        );
    }
}
