package com.familyhelpuae.centrality;

import com.familyhelpuae.family.Family;
import com.familyhelpuae.family.FamilyRepository;
import com.familyhelpuae.recommendation.RecommendationRepository;
import com.familyhelpuae.recommendation.projection.GraphEdgeProjection;
import com.familyhelpuae.recommendation.projection.GraphNodeProjection;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CentralityServiceImpl implements CentralityService {

    private final FamilyRepository familyRepository;
    private final RecommendationRepository recommendationRepository;

    public CentralityServiceImpl(FamilyRepository familyRepository, RecommendationRepository recommendationRepository) {
        this.familyRepository = familyRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public void computeAndCacheCentralityScores() {
        List<GraphNodeProjection> nodes = recommendationRepository.getAllFamilies();
        List<GraphEdgeProjection> edges = recommendationRepository.getHelpedGraphEdges();
        
        Map<String, Double> betweenness = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        
        for (GraphNodeProjection node : nodes) {
            betweenness.put(node.getFamilyId(), 0.0);
            adj.put(node.getFamilyId(), new ArrayList<>());
        }
        
        for (GraphEdgeProjection edge : edges) {
            if (adj.containsKey(edge.getSourceId()) && adj.containsKey(edge.getTargetId())) {
                adj.get(edge.getSourceId()).add(edge.getTargetId());
                adj.get(edge.getTargetId()).add(edge.getSourceId());
            }
        }

        for (GraphNodeProjection s : nodes) {
            String sId = s.getFamilyId();
            Stack<String> S = new Stack<>();
            Map<String, List<String>> P = new HashMap<>();
            Map<String, Double> sigma = new HashMap<>();
            Map<String, Integer> d = new HashMap<>();
            
            for (GraphNodeProjection n : nodes) {
                String v = n.getFamilyId();
                P.put(v, new ArrayList<>());
                sigma.put(v, 0.0);
                d.put(v, -1);
            }
            
            sigma.put(sId, 1.0);
            d.put(sId, 0);
            Queue<String> Q = new LinkedList<>();
            Q.add(sId);
            
            while (!Q.isEmpty()) {
                String v = Q.poll();
                S.push(v);
                
                for (String w : adj.get(v)) {
                    if (d.get(w) < 0) {
                        Q.add(w);
                        d.put(w, d.get(v) + 1);
                    }
                    if (d.get(w) == d.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        P.get(w).add(v);
                    }
                }
            }
            
            Map<String, Double> delta = new HashMap<>();
            for (GraphNodeProjection n : nodes) {
                delta.put(n.getFamilyId(), 0.0);
            }
            
            while (!S.isEmpty()) {
                String w = S.pop();
                for (String v : P.get(w)) {
                    delta.put(v, delta.get(v) + (sigma.get(v) / sigma.get(w)) * (1.0 + delta.get(w)));
                }
                if (!w.equals(sId)) {
                    betweenness.put(w, betweenness.get(w) + delta.get(w));
                }
            }
        }

        double maxB = betweenness.values().stream().max(Double::compareTo).orElse(1.0);
        if (maxB == 0) maxB = 1.0;

        Iterable<Family> families = familyRepository.findAll();
        for (Family f : families) {
            Double val = betweenness.get(f.getFamilyId());
            if (val != null) {
                f.setBetweennessScore((val / maxB) * 100.0);
            }
        }
        familyRepository.saveAll(families);
    }

    @Override
    public void computeAndCachePageRankScores() {
        List<GraphNodeProjection> nodes = recommendationRepository.getAllFamilies();
        List<GraphEdgeProjection> edges = recommendationRepository.getHelpedGraphEdges();
        
        if (nodes.isEmpty()) return;

        Map<String, Double> pr = new HashMap<>();
        Map<String, Double> newPr = new HashMap<>();
        double initialPR = 1.0 / nodes.size();
        
        for (GraphNodeProjection node : nodes) {
            pr.put(node.getFamilyId(), initialPR);
        }
        
        Map<String, Double> outWeightSum = new HashMap<>();
        Map<String, List<GraphEdgeProjection>> inEdges = new HashMap<>();
        
        for (GraphNodeProjection n : nodes) {
            outWeightSum.put(n.getFamilyId(), 0.0);
            inEdges.put(n.getFamilyId(), new ArrayList<>());
        }
        
        for (GraphEdgeProjection e : edges) {
            if (outWeightSum.containsKey(e.getSourceId()) && inEdges.containsKey(e.getTargetId())) {
                double weight = e.getWeight() != null ? e.getWeight() : 1.0;
                outWeightSum.put(e.getSourceId(), outWeightSum.get(e.getSourceId()) + weight);
                inEdges.get(e.getTargetId()).add(e);
            }
        }

        double damping = 0.85;
        int maxIter = 30;
        
        for (int i = 0; i < maxIter; i++) {
            for (GraphNodeProjection v : nodes) {
                String vId = v.getFamilyId();
                double sum = 0.0;
                for (GraphEdgeProjection e : inEdges.get(vId)) {
                    String uId = e.getSourceId();
                    double weight = e.getWeight() != null ? e.getWeight() : 1.0;
                    sum += pr.get(uId) * (weight / outWeightSum.get(uId));
                }
                newPr.put(vId, (1.0 - damping) / nodes.size() + damping * sum);
            }
            pr.putAll(newPr);
        }

        double maxPR = pr.values().stream().max(Double::compareTo).orElse(1.0);
        if (maxPR == 0) maxPR = 1.0;

        Iterable<Family> families = familyRepository.findAll();
        for (Family f : families) {
            Double val = pr.get(f.getFamilyId());
            if (val != null) {
                f.setNetworkTrustScore((val / maxPR) * 100.0);
            }
        }
        familyRepository.saveAll(families);
    }
}
