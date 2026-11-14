package net.yvanchuxiuzhen.collection.decor;

import java.util.Comparator;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.economy.api.AuctionSeatRequestEvent;

/** 装饰内容族独占实体椅子的生成、乘坐和移除，不反向调用拍卖实现。 */
final class AuctionSeats {
    private AuctionSeats() { }

    static void requested(AuctionSeatRequestEvent event) {
        Vec3 center = new Vec3(event.pos().getX() + 0.5, event.pos().getY(), event.pos().getZ() + 0.5);
        if (event.operation() == AuctionSeatRequestEvent.Operation.CREATE) {
            DecorSeat seat = new DecorSeat(DecorSetup.seatType(), event.level());
            seat.moveTo(center.x, center.y, center.z, event.level().random.nextFloat() * 360, 0);
            Direction direction = seat.getDirection().getOpposite();
            float rotation = direction == Direction.NORTH ? 180 : direction == Direction.EAST ? -90 : direction == Direction.WEST ? 90 : 0;
            seat.setYRot(rotation);
            seat.setYBodyRot(rotation);
            seat.setYHeadRot(rotation);
            seat.yRotO = rotation;
            seat.yBodyRotO = rotation;
            seat.yHeadRotO = rotation;
            event.level().addFreshEntity(seat);
            return;
        }
        double width = event.operation() == AuctionSeatRequestEvent.Operation.RIDE ? 1 : 0.5;
        var seats = event.level().getEntitiesOfClass(DecorSeat.class, AABB.ofSize(center, width, width, width));
        seats.sort(Comparator.comparingDouble(seat -> seat.distanceToSqr(center)));
        if (event.operation() == AuctionSeatRequestEvent.Operation.REMOVE_ALL) seats.forEach(DecorSeat::discard);
        else if (!seats.isEmpty()) {
            if (event.operation() == AuctionSeatRequestEvent.Operation.RIDE) event.player().startRiding(seats.getFirst());
            else seats.getFirst().discard();
        }
    }
}
