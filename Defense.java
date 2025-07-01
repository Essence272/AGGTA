package core;

import java.util.HashSet;
import java.util.Set;

public class Defense {
  public AttackStep disable;
  public boolean defaultValue = true;
  public static Set<Defense> allDefenses = new HashSet<>();
  String assetName = "Anonymous";

  public Defense(String name) {
    allDefenses.add(this);
    this.assetName = name;
  }

  public boolean isEnabled() {
    return defaultValue;
  }

  public void setEnabled(boolean bool) {
    this.defaultValue = bool;
  }

  public static void setAllDefenses(boolean bool) {
    for (Defense defense : Defense.allDefenses) {
      defense.setEnabled(bool);
    }
  }
}
