package org.example.tools.ci;
import lombok.Getter;
import lombok.Setter;
import org.example.commands.items.RegisterItem;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class CustomArmor {
    private int count = 1;
    private int material;
    private String displayName;
    private List<String> lore;
    private double value;
    private HashMap<String, String> operation = new HashMap<>();
    private HashMap<String, Double> valueByStat = new HashMap<>();
    private HashMap<String, Double> effects = new HashMap<>();

    private String id;
    private boolean isArmor = true;

    public CustomArmor setMaterial(int material) {
        this.material = material;
        return this;
    }
    public CustomArmor setDisplayName(String displayName){
        this.displayName = displayName;
        return this;
    }
    public CustomArmor setLore(List<String> lore){
        this.lore = lore;
        return this;
    }

    public CustomArmor setOperation(String operation, String stat){
        this.operation.put(stat,operation);
        return this;
    }
    public CustomArmor setBonusStat(String stat, double value){
        this.valueByStat.put(stat,value);
        return this;
    }
    public CustomArmor setId(String id){
        this.id = id;
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomArmor item = (CustomArmor) o;
        return material == item.material && Objects.equals(displayName, item.displayName) && Objects.equals(lore, item.lore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, displayName, lore);
    }
}
