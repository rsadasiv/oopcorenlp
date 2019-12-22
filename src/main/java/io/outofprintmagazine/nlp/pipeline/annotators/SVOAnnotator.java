package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class SVOAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SVOAnnotator.class);

	
	public SVOAnnotator() {
		super();
		this.setTags(Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ"));
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public SVOAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	

	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSVOAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSVOAnnotationAggregate.class;
	}
	

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.LemmaAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class
					
				)
			)
		);
	}
	
/*	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			try {
				SemanticGraph dependencyParse = sentence.dependencyParse();
				Collection<TypedDependency> vdeps = dependencyParse.typedDependencies();
				for (TypedDependency dependency : vdeps) {
					GrammaticalRelation rn = dependency.reln();
					if (rn.getShortName().equalsIgnoreCase("ROOT")) {
						Map<String,BigDecimal> scoreMap = (Map<String, BigDecimal>) dependency.dep().backingLabel().get(getAnnotationClass());
						if (scoreMap == null) {
							scoreMap = new HashMap<String,BigDecimal>();
						}
						if (scoreMap.get("verb") == null) {
							scoreMap.put("verb", new BigDecimal(1));
						}
						else {
							scoreMap.put("verb", new BigDecimal(1).add(scoreMap.get("verb")));
						}	
						dependency.dep().backingLabel().set(getAnnotationClass(), scoreMap);
					}
					else if (rn.getShortName().equals("acl:relcl")) {
						Map<String,BigDecimal> scoreMap = (Map<String, BigDecimal>) dependency.dep().backingLabel().get(getAnnotationClass());
						if (scoreMap == null) {
							scoreMap = new HashMap<String,BigDecimal>();
						}
						if (scoreMap.get("verb") == null) {
							scoreMap.put("verb", new BigDecimal(1));
						}
						else {
							scoreMap.put("verb", new BigDecimal(1).add(scoreMap.get("verb")));
						}	
						dependency.dep().backingLabel().set(getAnnotationClass(), scoreMap);
					}
					else if (rn.getShortName().equals("parataxis")) {
						if (getTags().contains(dependency.dep().tag())) {
							Map<String,BigDecimal> scoreMap = (Map<String, BigDecimal>) dependency.dep().backingLabel().get(getAnnotationClass());
							if (scoreMap == null) {
								scoreMap = new HashMap<String,BigDecimal>();
							}
							if (scoreMap.get("verb") == null) {
								scoreMap.put("verb", new BigDecimal(1));
							}
							else {
								scoreMap.put("verb", new BigDecimal(1).add(scoreMap.get("verb")));
							}	
							dependency.dep().backingLabel().set(getAnnotationClass(), scoreMap);
						}
					}
					else if (rn.getShortName().equals("nsubj") || rn.getShortName().equals("nsubjpass") || rn.getShortName().equals("csubj") || rn.getShortName().equals("csubjpass")) {
						if (getTags().contains(dependency.gov().tag())) {
							//Subject of transitive verb. John hit the ball to the fence -> John
							Map<String,BigDecimal> scoreMap = (Map<String, BigDecimal>) dependency.dep().backingLabel().get(getAnnotationClass());
							if (scoreMap == null) {
								scoreMap = new HashMap<String,BigDecimal>();
							}
							if (scoreMap.get("subject") == null) {
								scoreMap.put("subject", new BigDecimal(1));
							}
							else {
								scoreMap.put("subject", new BigDecimal(1).add(scoreMap.get("subject")));
							}	
							dependency.dep().backingLabel().set(getAnnotationClass(), scoreMap);
						}
					}
					else if (rn.getShortName().equals("dobj")) {
						if (getTags().contains(dependency.gov().tag())) {
							//Object of transitive verb. John hit the ball to the fence -> ball
							Map<String,BigDecimal> scoreMap = (Map<String, BigDecimal>) dependency.dep().backingLabel().get(getAnnotationClass());
							if (scoreMap == null) {
								scoreMap = new HashMap<String,BigDecimal>();
							}
							if (scoreMap.get("object") == null) {
								scoreMap.put("object", new BigDecimal(1));
							}
							else {
								scoreMap.put("object", new BigDecimal(1).add(scoreMap.get("object")));
							}	
							dependency.dep().backingLabel().set(getAnnotationClass(), scoreMap);
						}
					}
					else if (rn.getShortName().equals("iobj")) {
						if (getTags().contains(dependency.gov().tag())) {
							//Object of transitive verb. John hit the ball to the fence -> fence
							Map<String,BigDecimal> scoreMap = (Map<String, BigDecimal>) dependency.dep().backingLabel().get(getAnnotationClass());
							if (scoreMap == null) {
								scoreMap = new HashMap<String,BigDecimal>();
							}
							if (scoreMap.get("indirectObject") == null) {
								scoreMap.put("indirectObject", new BigDecimal(1));
							}
							else {
								scoreMap.put("indirectObject", new BigDecimal(1).add(scoreMap.get("indirectObject")));
							}	
							dependency.dep().backingLabel().set(getAnnotationClass(), scoreMap);
						}
					}
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		score(document);
	}*/
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			try {
				SemanticGraph dependencyParse = sentence.dependencyParse();
				CoreLabel verb = null;
				CoreLabel subject = null;
				CoreLabel directObject = null;
				CoreLabel indirectObject = null;
				List<CoreLabel> auxVerbs = new ArrayList<CoreLabel>();
				List<CoreLabel> conjSubjects = new ArrayList<CoreLabel>();
				List<CoreLabel> conjObjects = new ArrayList<CoreLabel>();
				List<CoreLabel> conjiObjects = new ArrayList<CoreLabel>();
				Collection<TypedDependency> vdeps = dependencyParse.typedDependencies();
				for (TypedDependency dependency : vdeps) {
					GrammaticalRelation rn = dependency.reln();
					if (rn.getShortName().equalsIgnoreCase("ROOT")) {
						verb = dependency.dep().backingLabel();
					}
				}
				for (TypedDependency dependency : vdeps) {
					GrammaticalRelation rn = dependency.reln();
					if (subject == null && rn.getShortName().equals("nsubj") && dependency.gov().backingLabel().equals(verb)) {
						subject = dependency.dep().backingLabel();
					}
				}
				for (TypedDependency dependency : vdeps) {
					GrammaticalRelation rn = dependency.reln();
					if (directObject == null && rn.getShortName().equals("dobj") && dependency.gov().backingLabel().equals(verb)) {
						directObject = dependency.dep().backingLabel();
					}
					else if (indirectObject == null && rn.getShortName().equals("iobj") && dependency.gov().backingLabel().equals(verb)) {
						indirectObject = dependency.dep().backingLabel();
					}
				}
				for (TypedDependency dependency : vdeps) {
					GrammaticalRelation rn = dependency.reln();
					if (rn.getShortName().equals("aux") && dependency.gov().backingLabel().equals(verb)) {
						auxVerbs.add(dependency.dep().backingLabel());
					}
					else if (rn.getShortName().equals("cop") && dependency.gov().backingLabel().equals(verb)) {
						auxVerbs.add(dependency.dep().backingLabel());
					}
					else if (rn.getShortName().equals("conj")) {
						if (dependency.gov().backingLabel().equals(subject)) {
							conjSubjects.add(dependency.dep().backingLabel());
						}
						else if (dependency.gov().backingLabel().equals(directObject)) {
							conjObjects.add(dependency.dep().backingLabel());
						}
						else if (dependency.gov().backingLabel().equals(indirectObject)) {
							conjiObjects.add(dependency.dep().backingLabel());
						}
					}
				}
				if (verb != null && subject != null && directObject != null) {
					Map<String,BigDecimal> verbScore = new HashMap<String,BigDecimal>();
					verbScore.put("verb", new BigDecimal(1));
					verb.set(getAnnotationClass(), verbScore);
					Map<String,BigDecimal> subjectScore = new HashMap<String,BigDecimal>();
					subjectScore.put("subject", new BigDecimal(1));
					subject.set(getAnnotationClass(), subjectScore);
					Map<String,BigDecimal> directObjectScore = new HashMap<String,BigDecimal>();
					directObjectScore.put("object", new BigDecimal(1));
					directObject.set(getAnnotationClass(), directObjectScore);
					if (indirectObject != null) {
						Map<String,BigDecimal> indirectObjectScore = new HashMap<String,BigDecimal>();
						indirectObjectScore.put("indirectObject", new BigDecimal(1));
						indirectObject.set(getAnnotationClass(), indirectObjectScore);						
					}
					for (CoreLabel auxVerb : auxVerbs) {
						Map<String,BigDecimal> auxVerbScore = new HashMap<String,BigDecimal>();
						auxVerbScore.put("auxVerb", new BigDecimal(1));
						auxVerb.set(getAnnotationClass(), auxVerbScore);						
					}
					for (CoreLabel conjSubject : conjSubjects) {
						Map<String,BigDecimal> conjSubjectScore = new HashMap<String,BigDecimal>();
						conjSubjectScore.put("subject", new BigDecimal(1));
						conjSubject.set(getAnnotationClass(), conjSubjectScore);						
					}
					for (CoreLabel conjObject : conjObjects) {
						Map<String,BigDecimal> conjObjectScore = new HashMap<String,BigDecimal>();
						conjObjectScore.put("object", new BigDecimal(1));
						conjObject.set(getAnnotationClass(), conjObjectScore);						
					}
					for (CoreLabel conjiObject : conjiObjects) {
						Map<String,BigDecimal> conjiObjectScore = new HashMap<String,BigDecimal>();
						conjiObjectScore.put("indirectObject", new BigDecimal(1));
						conjiObject.set(getAnnotationClass(), conjiObjectScore);						
					}
				}
			}

			catch (Exception e) {
				logger.error(e);
			}
		}
		score(document);
	}


	@Override
	public String getDescription() {
		return "CoreNLP dependency parse subject-verb-object-indirectObject-auxVerb: nsubj, root, dobj, iobj. http://universaldependencies.org/docsv1/en/dep/index.html";
	}
}
