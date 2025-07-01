package core;

import org.apache.commons.math3.distribution.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Attacker {
  private static final Pattern distributionPattern =
      Pattern.compile("^([a-zA-Z]+)(?:\\((?:([0-9.]+)(?:,([0-9.]+))?)?\\))?(?:\\*([a-zA-Z]+)(?:\\((?:([0-9.]+)(?:,([0-9.]+))?)?\\))?)?$");

  private static final Pattern EdistributionPattern =
      Pattern.compile("^([a-zA-Z]+)(?:\\((?:([0-9.]+)(?:, ([0-9.]+))?)?\\))?$");

  protected Set<AttackStep> activeAttackSteps = new HashSet<>();
  public boolean verbose = false;
  private static final String defaultProfile = "attackerProfile.ttc";
  protected static Map<String, Double> ttcHashMap = new HashMap<>();

  public Attacker() {
    verbose = false;
  }

  public Attacker(boolean verbose) {
    this.verbose = verbose;
  }

  public void addAttackPoint(AttackStep attackPoint) {
    attackPoint.ttc = 0;
    activeAttackSteps.add(attackPoint);
  }

  public void addRandomAttackPoint(long randomSeed) {
    AttackStep attackPoint = AttackStep.randomAttackStep(randomSeed);
    System.out.println("Attack point: " + attackPoint.fullName());
    addAttackPoint(attackPoint);
  }

  private AttackStep getShortestActiveStep() {
    AttackStep shortestStep = null;
    double shortestTtc = Double.MAX_VALUE;
    for (AttackStep attackStep : activeAttackSteps) {
      if (attackStep.ttc < shortestTtc) {
        shortestTtc = attackStep.ttc;
        shortestStep = attackStep;
      }
    }
    return shortestStep;
  }

  public void reset() {
    for (AttackStep attackStep : AttackStep.allAttackSteps) {
      attackStep.ttc = Double.MAX_VALUE;
    }
  }

  private void debugPrint(String str) {
    if (verbose) {
      System.out.println(str);
    }
  }

  public void customizeTtc(String name, String distribution) {
    ttcHashMap.put(name, Attacker.parseDistribution(distribution, isDefense(name)));
  }

  public static double parseDistribution(String dist, boolean defense) {
    Matcher matcher = distributionPattern.matcher(dist);
    matcher.matches();
    double ttcA = 0;
    double ttcB = 1;
    // pram
    double a = 0;
    double b = 0;
    double c = 0;
    double d = 0;
    try {
      a = Double.valueOf(matcher.group(2));
      b = Double.valueOf(matcher.group(3));
    } catch (Exception e) {
    }
    try {
      c = Double.valueOf(matcher.group(5));
      d = Double.valueOf(matcher.group(6));
    } catch (Exception e) {
    }
    //if(matcher.group(4)!=null) // for debug
      //System.out.println(matcher.group(4));
    String parmA = "Zero";
    String parmB = "Zero";
    try {
      parmA = matcher.group(1);
      parmB = matcher.group(4);
    } catch (Exception e) {
      System.out.println("Error Match Group: " + dist);
    }

    // ttcA
    switch (matcher.group(1)) {
      case "Bernoulli":
        Random rand = new Random();
        double r = rand.nextDouble();
        if (defense) {
          ttcA = r < a ? Double.MAX_VALUE : 0;
        } else {
          ttcA = r < a ? 0 : Double.MAX_VALUE;
        }
        break;
      case "Binomial":
        //ttcA = a * b;
        BinomialDistribution binomial = new BinomialDistribution((int)a, b);
        ttcA = binomial.sample();
        break;
      case "Exponential":
        //ttcA = 1 / a;
        ExponentialDistribution exponential = new ExponentialDistribution(a);
        ttcA = exponential.sample();
        break;
      case "Gamma":
        //ttcA = a / b;
        GammaDistribution gamma = new GammaDistribution(a, b);
        ttcA = gamma.sample();
        break;
      case "Infinity":
        ttcA = Double.MAX_VALUE;
        break;
      case "LogNormal":
        //ttcA = Math.exp(a + b / 2);
        LogNormalDistribution logNormal = new LogNormalDistribution(a, b);
        ttcA = logNormal.sample();
        break;
      case "Pareto":
        //ttcA = (a <= 1 ? Double.MAX_VALUE : a * b / (a - 1));
        ParetoDistribution pareto = new ParetoDistribution(a, b);
        ttcA = pareto.sample();
        break;
      case "TruncatedNormal":
        //ttcA = a;
        NormalDistribution normal = new NormalDistribution(a, b);
        ttcA = normal.sample();
        break;
      case "Uniform":
        //ttcA = (a + b) / 2;
        UniformRealDistribution uniformReal = new UniformRealDistribution(a, b);
        ttcA = uniformReal.sample();
        break;
      case "Zero":
        ttcA = 0;
        break;
      default:
        System.err.println(String.format("No matching distribution for: %s", dist));
        break;
    }

    // ttcB
    if(matcher.group(4)!=null)
    {
      if(ttcA <= 0)
        ttcA = 1.0;
      else if(ttcA == Double.MAX_VALUE)
        return ttcA;
      switch (matcher.group(4)) {
        case "Bernoulli":
          Random rand = new Random();
          double r = rand.nextDouble();
          if (defense) {
            ttcB = r < c ? Double.MAX_VALUE : 0;
          } else {
            ttcB = r < c ? 0 : Double.MAX_VALUE;
          }
          break;
        case "Binomial":
          //ttcA = a * b;
          BinomialDistribution binomial = new BinomialDistribution((int)c, d);
          ttcB = binomial.sample();
          break;
        case "Exponential":
          //ttcA = 1 / a;
          ExponentialDistribution exponential = new ExponentialDistribution(c);
          ttcB = exponential.sample();
          break;
        case "Gamma":
          //ttcA = a / b;
          GammaDistribution gamma = new GammaDistribution(c, d);
          ttcB = gamma.sample();
          break;
        case "Infinity":
          ttcB = Double.MAX_VALUE;
          break;
        case "LogNormal":
          //ttcA = Math.exp(a + b / 2);
          LogNormalDistribution logNormal = new LogNormalDistribution(c, d);
          ttcB = logNormal.sample();
          break;
        case "Pareto":
          //ttcA = (a <= 1 ? Double.MAX_VALUE : a * b / (a - 1));
          ParetoDistribution pareto = new ParetoDistribution(c, d);
          ttcB = pareto.sample();
          break;
        case "TruncatedNormal":
          //ttcA = a;
          NormalDistribution normal = new NormalDistribution(c, d);
          ttcB = normal.sample();
          ttcB = ttcB >= 0 ? ttcB : 0;
          break;
        case "Uniform":
          //ttcA = (a + b) / 2;
          UniformRealDistribution uniformReal = new UniformRealDistribution(c, d);
          ttcB = uniformReal.sample();
          break;
        case "Zero":
          ttcB = 0;
          break;
        default:
          System.err.println(String.format("No matching distribution for: %s", dist));
          break;
      }
    }
    return ttcA * ttcB;
  }

  public static double parseDistributionE(String dist, boolean defense) {
    Matcher matcher = distributionPattern.matcher(dist);
    matcher.matches();
    double ttcA = 0;
    double ttcB = 1;
    // pram
    double a = 0;
    double b = 0;
    double c = 0;
    double d = 0;
    try {
      a = Double.valueOf(matcher.group(2));
      b = Double.valueOf(matcher.group(3));
    } catch (Exception e) {
    }
    try {
      c = Double.valueOf(matcher.group(5));
      d = Double.valueOf(matcher.group(6));
    } catch (Exception e) {
    }
    //if(matcher.group(4)!=null) // for debug
    //System.out.println(matcher.group(4));
    String parmA = "Zero";
    String parmB = "Zero";
    try {
      parmA = matcher.group(1);
      parmB = matcher.group(4);
    } catch (Exception e) {
      System.out.println("Error Match Group: " + dist);
    }

    // ttcA
    switch (matcher.group(1)) {
      case "Bernoulli":
        Random rand = new Random();
        double r = rand.nextDouble();
        if (defense) {
          ttcA = r < a ? Double.MAX_VALUE : 0;
        } else {
          ttcA = r < a ? 0 : Double.MAX_VALUE;
        }
        break;
      case "Binomial":
        ttcA = a * b;
        //BinomialDistribution binomial = new BinomialDistribution((int)a, b);
        //ttcA = binomial.sample();
        break;
      case "Exponential":
        ttcA = 1 / a;
        //ExponentialDistribution exponential = new ExponentialDistribution(a);
        //ttcA = exponential.sample();
        break;
      case "Gamma":
        ttcA = a / b;
        //GammaDistribution gamma = new GammaDistribution(a, b);
        //ttcA = gamma.sample();
        break;
      case "Infinity":
        ttcA = Double.MAX_VALUE;
        break;
      case "LogNormal":
        ttcA = Math.exp(a + b / 2);
        //LogNormalDistribution logNormal = new LogNormalDistribution(a, b);
        //ttcA = logNormal.sample();
        break;
      case "Pareto":
        ttcA = (a <= 1 ? Double.MAX_VALUE : a * b / (a - 1));
        //ParetoDistribution pareto = new ParetoDistribution(a, b);
        //ttcA = pareto.sample();
        break;
      case "TruncatedNormal":
        ttcA = a;
        //NormalDistribution normal = new NormalDistribution(a, b);
        //ttcA = normal.sample();
        break;
      case "Uniform":
        ttcA = (a + b) / 2;
        //UniformRealDistribution uniformReal = new UniformRealDistribution(a, b);
        //ttcA = uniformReal.sample();
        break;
      case "Zero":
        ttcA = 0;
        break;
      default:
        System.err.println(String.format("No matching distribution for: %s", dist));
        break;
    }

    // ttcB
    if(matcher.group(4)!=null)
    {
      if(ttcA <= 0)
        ttcA = 1.0;
      else if(ttcA == Double.MAX_VALUE)
        return ttcA;
      switch (matcher.group(4)) {
        case "Bernoulli":
          Random rand = new Random();
          double r = rand.nextDouble();
          if (defense) {
            ttcB = r < c ? Double.MAX_VALUE : 0;
          } else {
            ttcB = r < c ? 0 : Double.MAX_VALUE;
          }
          break;
        case "Binomial":
          ttcB = c * d;
          //BinomialDistribution binomial = new BinomialDistribution((int)c, d);
          //ttcB = binomial.sample();
          break;
        case "Exponential":
          ttcB = 1 / c;
          //ExponentialDistribution exponential = new ExponentialDistribution(c);
          //ttcB = exponential.sample();
          break;
        case "Gamma":
          ttcB = c / d;
          //GammaDistribution gamma = new GammaDistribution(c, d);
          //ttcB = gamma.sample();
          break;
        case "Infinity":
          ttcB = Double.MAX_VALUE;
          break;
        case "LogNormal":
          ttcB = Math.exp(c + d / 2);
          //LogNormalDistribution logNormal = new LogNormalDistribution(c, d);
          //ttcB = logNormal.sample();
          break;
        case "Pareto":
          ttcB = (c <= 1 ? Double.MAX_VALUE : c * d / (c - 1));
          //ParetoDistribution pareto = new ParetoDistribution(c, d);
          //ttcB = pareto.sample();
          break;
        case "TruncatedNormal":
          ttcB = c;
          //NormalDistribution normal = new NormalDistribution(c, d);
          //ttcB = normal.sample();
          //ttcB = ttcB >= 0 ? ttcB : 0;
          break;
        case "Uniform":
          ttcB = (c + d) / 2;
          //UniformRealDistribution uniformReal = new UniformRealDistribution(c, d);
          //ttcB = uniformReal.sample();
          break;
        case "Zero":
          ttcB = 0;
          break;
        default:
          System.err.println(String.format("No matching distribution for: %s", dist));
          break;
      }
    }
    return ttcA * ttcB;
  }


  private boolean isDefense(String name) {
    ///*/
    //name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
    for (Defense defense : Defense.allDefenses) {
      if (defense.disable.fullName().equals(name)) {
        return true;
      }
    }
    return false;
     //*/
  }

  private Map<String, Double> readProfile(Properties profile) {
    Map<String, Double> profileMap = new HashMap<>();
    for (String name : profile.stringPropertyNames()) {
      // Local ttc overrides ttcfile
      if (ttcHashMap.containsKey(name)) {
        profileMap.put(name, ttcHashMap.get(name));
      } else {
        profileMap.put(name, parseDistribution(profile.getProperty(name), isDefense(name)));
      }
    }
    ttcHashMap.clear();
    return profileMap;
  }

  private Map<String, Double> readProfileE(Properties profile) {
    Map<String, Double> profileMap = new HashMap<>();
    for (String name : profile.stringPropertyNames()) {
      // Local ttc overrides ttcfile
      if (ttcHashMap.containsKey(name)) {
        profileMap.put(name, ttcHashMap.get(name));
      } else {
        profileMap.put(name, parseDistributionE(profile.getProperty(name), isDefense(name)));
      }
    }
    ttcHashMap.clear();
    return profileMap;
  }

  public void attack() {
    try {
      attack(new File(getClass().getClassLoader().getResource(defaultProfile).toURI()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public void attack(String profilePath) {
    attack(new File(profilePath));
  }

  public void attack(File profileFile) {
    Properties profile = new Properties();
    try {
      profile.load(new FileInputStream(profileFile));
    } catch (IOException e) {
      System.err.println("Could not open profile: " + profileFile.getPath());
      System.exit(1);
    }
    attack(profile);
  }

  public void attack(Properties profile) {
    AttackStep.ttcHashMap = readProfile(profile); //
    debugPrint("debug attacking");

    debugPrint(
        String.format(
            "The model contains %d assets and %d attack steps.",
            Asset.allAssets.size(), AttackStep.allAttackSteps.size()));
    AttackStep currentAttackStep = null;
    debugPrint(String.format("AttackStep.allAttackSteps = %s", AttackStep.allAttackSteps));

    for (AttackStep attackStep : AttackStep.allAttackSteps) {
      attackStep.setExpectedParents();
      debugPrint(
          String.format(
              "The expected parents of %s are %s",
              attackStep.fullName(), attackStep.expectedParents));
    }

    //drawAttackGraph();

    ///*
    for (Defense defense : Defense.allDefenses) {
      if (!defense.isEnabled() ) {
        addAttackPoint(defense.disable);
      }
      else {
        if(AttackStep.ttcHashMap.get(defense.disable.fullName()) < Double.MAX_VALUE){
          addAttackPoint(defense.disable);
        }
      }
    }
    //*/

    while (!activeAttackSteps.isEmpty()) {
      debugPrint(String.format("activeAttackSteps = %s", activeAttackSteps));
      currentAttackStep = getShortestActiveStep();
      debugPrint(String.format("Updating children of %s", currentAttackStep.fullName()));
      currentAttackStep.updateChildren(activeAttackSteps);
      //System.out.println(currentAttackStep.fullName());
      activeAttackSteps.remove(currentAttackStep);
    }
  }

  public void attack(String profilePath, String parm) {
    File profileFile;
    try {
      profileFile = new File(getClass().getClassLoader().getResource(defaultProfile).toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    Properties profile = new Properties();
    try {
      profile.load(new FileInputStream(profileFile));
    } catch (IOException e) {
      System.err.println("Could not open profile: " + profileFile.getPath());
      System.exit(1);
    }
    AttackStep.ttcHashMap = readProfileE(profile); //
    debugPrint("debug attacking");

    debugPrint(
            String.format(
                    "The model contains %d assets and %d attack steps.",
                    Asset.allAssets.size(), AttackStep.allAttackSteps.size()));
    AttackStep currentAttackStep = null;
    debugPrint(String.format("AttackStep.allAttackSteps = %s", AttackStep.allAttackSteps));

    for (AttackStep attackStep : AttackStep.allAttackSteps) {
      attackStep.setExpectedParents();
      debugPrint(
              String.format(
                      "The expected parents of %s are %s",
                      attackStep.fullName(), attackStep.expectedParents));
    }

    //drawAttackGraph();

    ///*
    for (Defense defense : Defense.allDefenses) {
      if (!defense.isEnabled() ) {
        addAttackPoint(defense.disable);
      }
      else {
        if(AttackStep.ttcHashMap.get(defense.disable.fullName()) < Double.MAX_VALUE){
          addAttackPoint(defense.disable);
        }
      }
    }
    //*/

    while (!activeAttackSteps.isEmpty()) {
      debugPrint(String.format("activeAttackSteps = %s", activeAttackSteps));
      currentAttackStep = getShortestActiveStep();
      debugPrint(String.format("Updating children of %s", currentAttackStep.fullName()));
      currentAttackStep.updateChildren(activeAttackSteps);
      //System.out.println(currentAttackStep.fullName());
      activeAttackSteps.remove(currentAttackStep);
    }
  }

  public void simulate(AttackStep target, List<AttackStep> attackerPoints, int n, int k,
                       HashMap<Integer, List<Double>> outResult, HashMap<Integer, GraphViz> outResultGiv) {
    // out: outResult, outResultGiv
    HashMap<String, Integer> idxMap = new HashMap<String, Integer>();
    int idx = 1;
    int oldSize = idxMap.size();
    // loop simulate
    for (int i=0; i<n; i++)
    {
      // init Attacker
      this.reset();
      for(AttackStep enter : attackerPoints)
      {
        this.addAttackPoint(enter);
      }
      //this.attack();
      this.attack();

      // get path
      target.tracePath(idx, idxMap, outResult, outResultGiv);

      // update idx of new path
      if(idxMap.size() != oldSize)
      {
        idx++;
        oldSize = idxMap.size() ;
      }
    }

    // print result
    if (outResult.size() == 0) {
      System.out.println("| " + target.fullName() + " was not reached.");
    }
    else {
      // sort
      LinkedHashMap<Integer, List<Double>> sort_res = outResult.entrySet().stream().sorted(
              ((o1, o2) -> o2.getValue().size()-o1.getValue().size())
      ).collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (oldval, newval) -> oldval,
              LinkedHashMap::new
      ));
      // print topK
      int pIdx = 1;
      for (int i : sort_res.keySet()) {
        if(pIdx > k)
          break;
        // ===print===
        System.out.println("Path"+ pIdx + "->" + target.fullName() + ": " + (double)outResult.get(i).size()*100/n + "%");
        //ttcChart(outResult.get(i), pIdx+"_"+target.fullName());
        try {
          outResultGiv.get(i).renameDotNameBySortedIdx(pIdx);
          outResultGiv.get(i).run();
        } catch (Exception e) {
          e.printStackTrace();
        }
        // === ===
        pIdx ++;
      }
    }
  }

  public void simulate(AttackStep target, List<AttackStep> attackerPoints, int n, int k,
                       HashMap<Integer, List<Double>> outResult, HashMap<Integer, GraphViz> outResultGiv, String path)
  {
    // out: outResult, outResultGiv
    HashMap<String, Integer> idxMap = new HashMap<String, Integer>();
    int idx = 1;
    int oldSize = idxMap.size();
    // loop simulate
    for (int i=0; i<n; i++)
    {
      // init Attacker
      this.reset();
      for(AttackStep enter : attackerPoints)
      {
        this.addAttackPoint(enter);
      }
      //this.attack();
      if (path.equals("E"))
      {
        this.attack("E","E");
      }
      else
      {
        this.attack(path);
      }


      // get path
      target.tracePath(idx, idxMap, outResult, outResultGiv);

      // update idx of new path
      if(idxMap.size() != oldSize)
      {
        idx++;
        oldSize = idxMap.size() ;
      }
    }

    // print result
    if (outResult.size() == 0) {
      System.out.println("| " + target.fullName() + " was not reached.");
    }
    else {
      // sort
      LinkedHashMap<Integer, List<Double>> sort_res = outResult.entrySet().stream().sorted(
              ((o1, o2) -> o2.getValue().size()-o1.getValue().size())
      ).collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (oldval, newval) -> oldval,
              LinkedHashMap::new
      ));
      // print topK
      int pIdx = 1;
      for (int i : sort_res.keySet()) {
        if(pIdx > k)
          break;
        // ===print===
        System.out.println("Path"+ pIdx + "->" + target.fullName() + ": " + (double)outResult.get(i).size()*100/n + "%");
        //ttcChart(outResult.get(i), pIdx+"_"+target.fullName());
        try {
          outResultGiv.get(i).renameDotNameBySortedIdx(pIdx);
          outResultGiv.get(i).run();
        } catch (Exception e) {
          e.printStackTrace();
        }
        // === ===
        pIdx ++;
      }
    }
  }

  public void drawAttackGraph(AttackStep source, AttackStep target)
  {
    for (AttackStep attackStep : AttackStep.allAttackSteps) {
      attackStep.setExpectedParents();
      debugPrint(
              String.format(
                      "The expected parents of %s are %s",
                      attackStep.fullName(), attackStep.expectedParents));
    }
    // === graph start ===
    //GraphViz gViz=new GraphViz(".\\out", "D:\\ProgramFile\\Graphviz\\bin\\dot.exe", "AttackGraph");
    GraphViz gViz=new GraphViz(".\\out", ".\\dependence\\Graphviz\\bin\\dot.exe", "AttackGraph_"+target.fullName());
    gViz.start_graph();
    gViz.addln("graph [dpi=300];");
    //gViz.addln("rankdir=LR;");
    // ===============
    Stack<AttackStep> S = new Stack<AttackStep>();
    S.add(target);
    AttackStep curr;
    HashSet<AttackStep> visited = new HashSet<AttackStep>();
    //visited.add(target);
    HashMap<AttackStep, ArrayList<AttackStep>> backGraph = new HashMap<AttackStep, ArrayList<AttackStep>>();
    //backward search
    while (!S.empty())
    {
      curr = S.pop();
      visited.add(curr);
      for(AttackStep aPar : curr.expectedParents)
      {
        String aParType = aPar.getClass().getName();
        //System.out.println(aParType.substring(aParType.length()-7));
        if(aParType.substring(aParType.length()-7).equals("Disable"))
        {
          //gViz.addln("\""+aPar.fullName()+"\"" + " -> " + "\""+curr.fullName()+"\"" + " [ label = \"Defence\" ];");
          //gViz.addln("\""+aPar.fullName()+"\"" + " [shape=box];");
        }
        else
        {
          if(!visited.contains(aPar)){
            S.push(aPar);
            //visited.add(aPar);
          }

          if(backGraph.containsKey(aPar))
          {
            backGraph.get(aPar).add(curr);
          }
          else
          {
            backGraph.put(aPar, new ArrayList<AttackStep>());
            backGraph.get(aPar).add(curr);
          }

          /*
          if(curr.getClass().getSuperclass() == AttackStepMax.class)
          {
            //gViz.addln("\""+aPar.fullName()+"\"" + " -> " + "\""+curr.fullName()+"\"" + " [ label = \"And\" ];");
            //gViz.addln("\""+curr.fullName()+"\"" + " [shape=ellipse];");
          }
          else
          {
            //gViz.addln("\""+aPar.fullName()+"\"" + " -> " + "\""+curr.fullName()+"\"" + " [ label = \"Or\" ];");
            //gViz.addln("\""+curr.fullName()+"\"" + " [shape=diamond];");
          }
           */
        }
      }
    }
    //forward search : out->gViz
    visited.clear();
    forward_search(backGraph, source, target,gViz, visited);
    // === graph draw ===
    gViz.end_graph();
    try {
      gViz.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
    // ===============
    return ;
  }

  public boolean forward_search(HashMap<AttackStep, ArrayList<AttackStep>> backGraph,
                                AttackStep curr, AttackStep target,
                                GraphViz out_gViz, HashSet<AttackStep> visited)
  {
    //System.out.println(curr.fullName());
    if(curr.fullName().equals(target.fullName()))
    {
      return true;
    }
    visited.add(curr);
    boolean ret = false;
    ArrayList<AttackStep> childList = backGraph.get(curr);
    for(AttackStep ch : childList)
    {
      if(!visited.contains(ch))
      {
        boolean isPath = forward_search(backGraph, ch, target, out_gViz, visited);
        ret = isPath || ret;
      }
      if(true)
      {
        ///*
        String currType = curr.getClass().getName();
        //System.out.println(currType.substring(aParType.length()-7));
        if (currType.substring(currType.length() - 7).equals("Disable")) {
          //gViz.addln("\""+curr.fullName()+"\"" + " -> " + "\""+ch.fullName()+"\"" + " [ label = \"Defence\" ];");
          //gViz.addln("\""+curr.fullName()+"\"" + " [shape=box];");
        } else {
          if (curr.getClass().getSuperclass() == AttackStepMax.class) {
            out_gViz.addln("\"" + curr.fullName() + "\"" + " -> " + "\"" + ch.fullName() + "\"" );//+ " [ label = \"And\" ];");
            out_gViz.addln("\"" + curr.fullName() + "\"" + " [shape=ellipse];");
          } else {
            out_gViz.addln("\"" + curr.fullName() + "\"" + " -> " + "\"" + ch.fullName() + "\"" );//+ " [ label = \"Or\" ];");
            out_gViz.addln("\"" + curr.fullName() + "\"" + " [shape=diamond];");
          }
        }
        //*/
      }
    }
    return ret;
  }
}


