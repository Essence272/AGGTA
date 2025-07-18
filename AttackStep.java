package core;

import static java.lang.Math.abs;
import java.util.*;

public class AttackStep {

  // This is the file that specifies all of the TTC probability distributions,
  // thus amounting to an attacker capability profile.
  public static final double oneSecond = 0.00001157407;
  public static final double infinity = Double.MAX_VALUE;
  public double ttc = Double.MAX_VALUE;
  public Set<AttackStep> expectedParents = new HashSet<>();
  public Set<AttackStep> visitedParents = new HashSet<>();
  public static List<AttackStep> allAttackSteps = new ArrayList<>();
  public String assetName;
  public String assetClassName;
  private int explanationDepth = 10;
  private boolean explained = false;
  protected static Map<String, Double> ttcHashMap = new HashMap<>();

  public Set<AttackStep> pathParents = new HashSet<>(); // Path

  public static Set<String> usedDefence = new HashSet<>(); // For draw defence
  public static Set<String> usedAttackSteps = new HashSet<>(); // For draw defence

  public AttackStep() {
    this("Anonymous");
  }

  public AttackStep(String name) {
    this.assetName = name;
    allAttackSteps.add(this);
  }

  public void bfs_edge(GraphViz gViz ,AttackStep curr){
    if(curr.pathParents.isEmpty())
    {
      //System.out.println(curr.fullName());
      return ;
    }

    for(AttackStep aPar : curr.pathParents)
    {
      String aParType = aPar.getClass().getName();
      //System.out.println(aParType.substring(aParType.length()-7));
      if(aParType.substring(aParType.length()-7).equals("Disable"))
      {
        gViz.addln("\""+aPar.fullName()+"\"" + " -> " + "\""+curr.fullName()+"\"" + "[ label = \"Defence\" ];");
        gViz.addln("\""+aPar.fullName()+"\"" + " [shape=box];");
//        gViz.addln("\""+aPar.fullName()+"\"" + "[ style=filled color=greenyellow ];");// "Mini Unix" [distortion="0.039386", orientation=2, skew="-0.461120", color=greenyellow];
        usedDefence.add(aPar.fullName());
      }
      else
      {
        usedAttackSteps.add(curr.fullName());
        usedAttackSteps.add(aPar.fullName());
        if(curr.getClass().getSuperclass() == AttackStepMax.class)
        {
          gViz.addln("\""+aPar.fullName()+"\"" + " -> " + "\""+curr.fullName()+"\"" );//+ "[ label = \"And\" ];");
          gViz.addln("\""+curr.fullName()+"\"" + " [shape=ellipse];");
        }
        else
        {
          gViz.addln("\""+aPar.fullName()+"\"" + " -> " + "\""+curr.fullName()+"\"" );//+ "[ label = \"Or\" ];");
          gViz.addln("\""+curr.fullName()+"\"" + " [shape=diamond];");//diamond
        }
      }

      bfs_edge(gViz, aPar);
    }
    return ;
  }

  public void tracePath(int idx, HashMap<String, Integer> idxMap ,
                        HashMap<Integer, List<Double>> outResult, HashMap<Integer, GraphViz> outResultGiv) {
    // Path
    if (ttc == Double.MAX_VALUE) {
      //System.out.println("| " + fullName() + " was not reached.");
    }
    else {
      //System.out.println("| " + fullName() + " was reached in " + Double.toString(ttc) + " days.");

      // === graph start ===
      usedDefence.clear();
      usedAttackSteps.clear();
      GraphViz gViz=new GraphViz(".\\out", ".\\dependence\\Graphviz\\bin\\dot.exe", "_"+this.fullName());
      gViz.start_graph();
      gViz.addln("graph [dpi=350];");
      //gViz.addln("rankdir=LR;");

      bfs_edge(gViz,this);

      // === graph draw ===
      gViz.addln("subgraph cluster_attackSteps {");
      gViz.addln("label=\"AttackPath\"");
      for(String s: usedAttackSteps)
      {
        gViz.addln("\""+ s +"\";");
      }
      gViz.addln("}");

      gViz.end_graph();

      // === get result ===
        // update idxMap
      String pathName = gViz.getGraph().toString();
      if(!idxMap.containsKey(pathName)) // is a new path?
      {
        // if newPath: add to idxMap & draw path graph
        idxMap.put(pathName, idx);
      }
        // update result
      if(outResult.containsKey(idxMap.get(pathName))) // if oldPath: cnt++
      {
        //result.replace(idxMap.get(pathName), result.get(idxMap.get(pathName))+1);
        outResult.get(idxMap.get(pathName)).add(ttc);
      }
      else // if newPath: add to result
      {
        //result.put(idxMap.get(pathName), 1);
        outResult.put(idxMap.get(pathName), new ArrayList<>());
        outResult.get(idxMap.get(pathName)).add(ttc);

        outResultGiv.put(idxMap.get(pathName), gViz);
      }
      // ===============
    }

  }

  protected void setExpectedParents() {}

  public void updateChildren(Set<AttackStep> activeAttackSteps) {}

  public void updateTtc(AttackStep parent, double parentTtc, Set<AttackStep> activeAttackSteps) {}

  protected void addExpectedParent(AttackStep parent) {
    expectedParents.add(parent);
  }

  public double localTtc() {
    return oneSecond;
    //return ttc;
  }

  public String attackStepName() {
    return decapitalize(
        this.toString()
            .substring(this.toString().lastIndexOf('$') + 1, this.toString().lastIndexOf('@')));
  }

  public String fullName() {
    return this.assetName + "." + attackStepName();
  }

  public String fullName(String i) {
    return attackStepName();
  }

  public Asset asset() {
    for (Asset asset : Asset.allAssets) {
      if (asset.name.equals(assetName)) {
        return asset;
      }
    }
    //fail("Asset name of " + fullName() + " does not correspond to any existing asset.");
    return null;
  }

  public void assertCompromisedInstantaneously() {
    if (ttc < 1.0 / 1440) {
      System.out.println("+ " + fullName() + " was reached instantaneously as expected.");
    } else {
      System.out.println(
          fullName() + ".ttc was supposed to be small, but was " + Double.toString(ttc) + ".");
      explain();
      //fail();
    }
  }

  public void assertCompromisedWithEffort() {
    if (ttc >= 1.0 / 1440 && ttc < 1000) {
      System.out.println(
          "+ " + fullName() + " was reached in " + Double.toString(ttc) + " days, as expected.");
    } else {
      System.out.println(
          fullName()
              + ".ttc was supposed to be between 1/1440 and 1000, but was "
              + Double.toString(ttc)
              + ".");
      explain();
      //fail();
    }
  }

  public void assertCompromisedInNDays(Double nDays) {
    if (ttc >= nDays && ttc < nDays + 1) {
      System.out.println(
          "+ " + fullName() + " was reached in " + Double.toString(ttc) + " days, as expected.");
    } else {
      System.out.println(
          fullName()
              + ".ttc was supposed to be between "
              + nDays.toString()
              + " and "
              + Double.toString(nDays + 1)
              + ", but was "
              + Double.toString(ttc)
              + ".");
      explain();
      //fail();
    }
  }

  public void assertUncompromised() {
    if (ttc == Double.MAX_VALUE) {
      System.out.println("+ " + fullName() + " was not reached, as expected.");
    } else {
      System.out.println(
          fullName() + ".ttc was supposed to be infinite, but was " + Double.toString(ttc) + ".");
      System.out.println(String.format("%nExplaining compromise:"));
      explainCompromise("", explanationDepth);
      //fail();
    }
  }

  public void assertUncompromisedFrom(AttackStep expectedParent) {
    if ((abs(ttc - expectedParent.ttc) == Double.MAX_VALUE)
        || (ttc == Double.MAX_VALUE && expectedParent.ttc == Double.MAX_VALUE)) {
      System.out.println(
          "+ "
              + fullName()
              + " ("
              + Double.toString(ttc)
              + ")"
              + " was not reached from "
              + expectedParent.fullName()
              + " ("
              + Double.toString(expectedParent.ttc)
              + ")"
              + " as expected.");
    } else {
      System.out.println(
          fullName()
              + ".ttc was supposed to be infinite, but was "
              + Double.toString(ttc)
              + ", while "
              + expectedParent.fullName()
              + ".ttc was "
              + Double.toString(expectedParent.ttc)
              + ".");
      System.out.println(String.format("%nExplaining compromise:"));
      explainCompromise("", explanationDepth);
      //fail();
    }
  }

  public void assertCompromisedInstantaneouslyFrom(AttackStep expectedParent) {
    if (Math.abs(ttc - expectedParent.ttc) < 0.1) {
      System.out.println(
          "+ "
              + fullName()
              + " was reached instantaneously from "
              + expectedParent.fullName()
              + " as expected.");
    } else {
      System.out.println(
          fullName()
              + ".ttc ("
              + Double.toString(ttc)
              + ") was supposed to follow "
              + expectedParent.fullName()
              + ".ttc ("
              + Double.toString(expectedParent.ttc)
              + ") immediately, but didn't.");
      if (ttc - expectedParent.ttc < 0) {
        System.out.println(
            "In fact, " + fullName() + " preceded " + expectedParent.fullName() + ".");
      }
      explain();
      //fail();
    }
  }

  public void assertCompromisedWithEffortFrom(AttackStep expectedParent) {
    if (Math.abs(ttc - expectedParent.ttc) >= 0.1 && ttc < Double.MAX_VALUE) {
      System.out.println(
          "+ "
              + fullName()
              + " was reached in "
              + Double.toString(ttc - expectedParent.ttc)
              + " days from "
              + expectedParent.fullName()
              + " as expected.");
    } else {
      System.out.println(
          fullName()
              + ".ttc ("
              + Double.toString(ttc)
              + ") was supposed to follow "
              + expectedParent.getClass().getName()
              + ".ttc ("
              + Double.toString(expectedParent.ttc)
              + ") with some effort, but didn't.");
      if (ttc - expectedParent.ttc < 0) {
        System.out.println(
            "In fact, " + fullName() + " preceded " + expectedParent.fullName() + ".");
      }
      explainCompromise("", explanationDepth);
      explainUncompromise("", explanationDepth);
      //fail();
    }
  }

  void reset() {
    ttc = Double.MAX_VALUE;
  }

  private void explainCompromise(String indent, int remainingExplanationSteps) {
    if (remainingExplanationSteps >= 0) {
      if (ttc != AttackStep.infinity) {
        System.out.print(
            indent + " reached " + fullName() + " [" + Double.toString(this.ttc) + "] (");
        if (this instanceof AttackStepMax) {
          System.out.print("AND");
        }
        if (this instanceof AttackStepMin) {
          System.out.print("OR");
        }
        System.out.print(") because ");
        // if (!explained) {
        explained = true;
        for (AttackStep parent : this.visitedParents) {
          System.out.print(
              " parent: " + parent.fullName() + " [" + Double.toString(parent.ttc) + "], ");
        }
        System.out.println("");
        for (AttackStep parent : this.visitedParents) {
          if (parent.ttc <= this.ttc) {
            parent.explainCompromise(indent + "  ", remainingExplanationSteps - 1);
          }
        }
        // }
      }
    }
  }

  private void explainUncompromise(String indent, int remainingExplanationSteps) {
    System.out.print(Integer.toString(remainingExplanationSteps) + " remaining explanation steps.");
    if (remainingExplanationSteps >= 0) {
      if (ttc == AttackStep.infinity) {
        if (this instanceof AttackStepMax) {
          System.out.println(
              indent
                  + " didn't reach "
                  + fullName()
                  + " ["
                  + Double.toString(this.ttc)
                  + "] (AND) because ");
          for (AttackStep parent : this.expectedParents) {
            // System.out.println(indent + " " + parent.fullName() + " was
            // not reached.");
            parent.explainUncompromise(indent + "  ", remainingExplanationSteps - 1);
          }
        }
        if (this instanceof AttackStepMin) {
          System.out.println(
              indent
                  + "  didn't reach "
                  + fullName()
                  + " ["
                  + Double.toString(this.ttc)
                  + "] (OR), because");
          if (visitedParents.isEmpty()) {
            for (AttackStep parent : this.expectedParents) {
              // System.out.println(indent + " " + parent.fullName() + "
              // was not reached.");
              parent.explainUncompromise(indent + "  ", remainingExplanationSteps - 1);
            }
          }
        }
        if (this.expectedParents.isEmpty() && visitedParents.isEmpty()) {
          System.out.println(
              indent + "  parents were neither expected nor visited, so this step is unreachable.");
        }
        for (AttackStep parent : this.visitedParents) {
          parent.explainUncompromise(indent + "  ", remainingExplanationSteps - 1);
        }
      } else {
        System.out.println(
            indent + "  but did reach " + fullName() + " [" + Double.toString(this.ttc) + "].");
      }
    }
  }

  public void explain() {
    System.out.println(String.format("%nExplaining uncompromise:"));
    explainUncompromise("", explanationDepth);
    System.out.println(String.format("%nExplaining compromise:"));
    explainCompromise("", explanationDepth);
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

  private String decapitalize(final String line) {
    return Character.toLowerCase(line.charAt(0)) + line.substring(1);
  }

  public static AttackStep randomAttackStep(long randomSeed) {
    Random random = new Random(randomSeed);
    return allAttackSteps.get(random.nextInt(allAttackSteps.size()));
  }

  public static void printAllDefenseSettings() {
    List<String> defenseNames = new ArrayList<>();
    for (AttackStep attackStep : allAttackSteps) {
      if (attackStep.assetName.equals("Disable")) {
        defenseNames.add(attackStep.fullName());
      }
    }
    Collections.sort(defenseNames);
    for (String defenseName : defenseNames) {
      System.out.println(defenseName);
    }
  }
}
