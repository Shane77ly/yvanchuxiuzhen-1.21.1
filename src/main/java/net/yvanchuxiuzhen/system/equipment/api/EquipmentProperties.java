package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;

/** /shipin 的根字段，保留动态键、字符串/整数/小数类型及顺序覆盖结果；与 lianqizong 炼器数据分离。 */
public record EquipmentProperties(Map<String, Value> fields) {
    public static final EquipmentProperties EMPTY = new EquipmentProperties(Map.of());
    public static final Codec<EquipmentProperties> CODEC = Codec.unboundedMap(Codec.STRING, Value.CODEC)
            .xmap(EquipmentProperties::new, EquipmentProperties::fields);

    public EquipmentProperties {
        fields = Map.copyOf(fields);
    }

    public enum Kind {
        STRING, INTEGER, DOUBLE
    }

    public record Value(Kind kind, String text, double number) {
        private static final Codec<Value> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(Kind::valueOf, Kind::name).fieldOf("kind").forGetter(Value::kind),
                Codec.STRING.fieldOf("text").forGetter(Value::text),
                Codec.DOUBLE.fieldOf("number").forGetter(Value::number)
        ).apply(instance, Value::new));

        public static Value text(String text) {
            return new Value(Kind.STRING, text, 0);
        }

        public static Value integer(int number) {
            return new Value(Kind.INTEGER, "", number);
        }

        public static Value decimal(double number) {
            return new Value(Kind.DOUBLE, "", number);
        }
    }

    /** 来源 CompoundTag 的数值读取：没有键或键为字符串时为 0。 */
    public double number(String key) {
        Value value = fields.get(key);
        return value == null || value.kind() == Kind.STRING ? 0 : value.number();
    }

    public String text(String key) {
        Value value = fields.get(key);
        return value != null && value.kind() == Kind.STRING ? value.text() : "";
    }
}
