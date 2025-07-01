package core;

import java.util.Set;

public class AttackStepMin extends AttackStep {
  public AttackStepMin(String name) {
    super(name);
  }

  @Override
  public void updateTtc(AttackStep parent, double parentTtc, Set<AttackStep> activeAttackSteps) {
    expectedParents.remove(parent);
    visitedParents.add(parent);
    if (parentTtc + localTtc() < ttc) {
      // For Path
      this.pathParents.clear();
      this.pathParents.add(parent);
      // Update ttc
      ttc = parentTtc + localTtc();
      activeAttackSteps.add(this);
    }
  }
}
