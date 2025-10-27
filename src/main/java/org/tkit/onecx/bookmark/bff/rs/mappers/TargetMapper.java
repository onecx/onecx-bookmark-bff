package org.tkit.onecx.bookmark.bff.rs.mappers;

import org.mapstruct.Mapper;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.TargetDTO;
import gen.org.tkit.onecx.bookmark.client.model.Target;
import gen.org.tkit.onecx.bookmark.exim.v1.client.model.EximTarget;

@Mapper
public interface TargetMapper {
    default Target mapTarget(TargetDTO targetDTO) {
        if (targetDTO == null) {
            return Target.SELF;
        }
        return switch (targetDTO) {
            case _BLANK -> Target.BLANK;
            case _SELF -> Target.SELF;
        };
    }

    default TargetDTO mapTarget(Target target) {
        if (target == null) {
            return TargetDTO._SELF;
        }
        return switch (target) {
            case BLANK -> TargetDTO._BLANK;
            case SELF -> TargetDTO._SELF;
        };
    }

    default EximTarget mapEximTarget(TargetDTO targetDTO) {
        if (targetDTO == null) {
            return EximTarget.SELF;
        }
        return switch (targetDTO) {
            case _BLANK -> EximTarget.BLANK;
            case _SELF -> EximTarget.SELF;
        };
    }

    default TargetDTO mapEximTarget(EximTarget target) {
        if (target == null) {
            return TargetDTO._SELF;
        }
        return switch (target) {
            case BLANK -> TargetDTO._BLANK;
            case SELF -> TargetDTO._SELF;
        };
    }
}
