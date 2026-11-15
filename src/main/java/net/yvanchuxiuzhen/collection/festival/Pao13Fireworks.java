package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.util.RandomSource;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** Pao13 八档原生烟花组件和延迟动作；FireworkRocketEntity 负责目标版本伤害、音效与爆炸显示。 */
final class Pao13Fireworks {
    private Pao13Fireworks() { }
    static void emit(ServerLevel level, Pao13Entity source, int type) {
        RandomSource random = level.random;
        switch (type) {
            case 1 -> { for (int i = 0; i < 4; i++) rocket(level, source, random.nextInt(-8, 9), random.nextInt(-8, 9), random.nextInt(-8, 9), List.of(ex(1, 16770972, 15609133, true, false))); }
            case 2 -> { rocket(level, source, 0, 0, 0, List.of(ex(1, 15990612, 16771698, true, false))); delayed(level, source, 8, 1, 16726050, 16604192, false); delayed(level, source, 8, 0, 11466751, 2848219, true); delayed(level, source, 16, 1, 16752371, 16751359, true); delayed(level, source, 24, 1, 16773038, 16764003, false); delayed(level, source, 24, 0, 11466751, 2848219, true); }
            case 3 -> { int[][] colors={{2,11466751,2848219,0},{2,16646062,14405675,0},{2,16762942,16763721,0},{2,16776131,16773961,1},{2,16711680,16711680,1},{2,5111667,117408,1},{2,15597312,15597312,0},{2,16035583,10705119,0}}; for (int[] c: colors) rocket(level,source,random.nextInt(-10,11),random.nextInt(-10,11),random.nextInt(-10,11),List.of(ex(c[0],c[1],c[2],c[3]==1,false))); }
            case 4 -> rocket(level, source, 0, 0, 0, List.of(ex(1,16711680,16383744,true,false), ex(1,16711680,15597312,false,false), ex(1,16775680,16580493,false,true)));
            case 5 -> { for (int d: new int[]{0,8,16,32}) { int x=random.nextInt(-10,11),y=random.nextInt(-10,11),z=random.nextInt(-10,11); if(d==0) rocket(level,source,x,y,z,List.of(ex(1,16383744,16383744,false,true))); else FestivalFireworkDelays.run(d,()->rocket(level,source,x,y,z,List.of(ex(1,16383744,16383744,false,true)))); } }
            case 6 -> { int[][] colors={{0,16383744,16749824},{0,16749824,15597312},{0,16749824,16711680},{0,16711680,16747008},{0,65470,7536384},{0,11954687,16758005},{0,16738665,11483391},{0,10740479,2720490}}; for(int[] c:colors) rocket(level,source,random.nextInt(-8,9),random.nextInt(-8,9),random.nextInt(-8,9),List.of(ex(c[0],c[1],c[2],false,true))); }
            case 7 -> { for(int i=0;i<8;i++) particle(level,source,30); }
            default -> { particle(level,source,60); for(int d:new int[]{8,16,32,48}) FestivalFireworkDelays.run(d,()->particle(level,source,60)); }
        }
    }
    private static FireworkExplosion ex(int shape,int color,int fade,boolean trail,boolean twinkle){return new FireworkExplosion(FireworkExplosion.Shape.byId(shape),new IntArrayList(new int[]{color}),new IntArrayList(new int[]{fade}),trail,twinkle);}
    private static void delayed(ServerLevel l,Pao13Entity s,int d,int shape,int c,int f,boolean t){FestivalFireworkDelays.run(d,()->rocket(l,s,0,0,0,List.of(ex(shape,c,f,false,t))));}
    private static void rocket(ServerLevel level,Pao13Entity source,double dx,double dy,double dz,List<FireworkExplosion> explosions){ItemStack stack=new ItemStack(Items.FIREWORK_ROCKET);stack.set(DataComponents.FIREWORKS,new Fireworks(0,explosions));FireworkRocketEntity rocket=new FireworkRocketEntity(level,source.getX()+dx,source.getY()+dy+1,source.getZ()+dz,stack);level.addFreshEntity(rocket);}
    private static void particle(ServerLevel level,Pao13Entity source,int count){level.sendParticles(Particles.type("pao_030"+(1+level.random.nextInt(6))),source.getX(),source.getY(),source.getZ(),count,0,0,0,3);}
}
