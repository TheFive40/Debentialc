package org.example.tools.ci;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class CustomItem {
    private int count = 1;
    private int material;
    private String displayName;
    private List<String> lore;
    private double value;
    private HashMap<String, String> operation = new HashMap<>();
    private HashMap<String, Double> valueByStat = new HashMap<>();
    private String id;
    private boolean isArmor = true;

    public CustomItem setMaterial(int material) {
        this.material = material;
        return this;
    }
    public CustomItem setDisplayName(String displayName){
        this.displayName = displayName;
        return this;
    }
    public CustomItem setLore(List<String> lore){
        this.lore = lore;
        return this;
    }

    public CustomItem setOperation(String operation, String stat){
        this.operation.put(stat,operation);
        return this;
    }
    public CustomItem setBonusStat(String stat, double value){
        this.valueByStat.put(stat,value);
        return this;
    }
    public CustomItem setId(String id){
        this.id = id;
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomItem item = (CustomItem) o;
        return material == item.material && Objects.equals(displayName, item.displayName) && Objects.equals(lore, item.lore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, displayName, lore);
    }
}
