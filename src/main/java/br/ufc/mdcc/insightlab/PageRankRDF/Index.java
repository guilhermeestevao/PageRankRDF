package br.ufc.mdcc.insightlab.PageRankRDF;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;


public class Index {

	private static Index instance;
	private HDT hdt;
	private Map<String, List<String>> indexOfInstances;
	private Map<String, List<String>> indexesOfClasses;
	private Map<String, Integer> countInstances;
	private Map<String, Integer> mapCountSujeito;
	private Map<String, Integer> mapCountObjeto;
	private Map<String, Integer> mapCountPredicado;
	private Map<BigInteger, Integer> mapCountAll;

	private static final String CLASS_URI_DEFINITION = "http://www.w3.org/2000/01/rdf-schema#Class";
	private static final String OWL_CLASS_URI_DEFINITION = "http://www.w3.org/2002/07/owl#Class";
	private static final String SUBCLASS_RELATION_URI_DEFINITION = "http://www.w3.org/2000/01/rdf-schema#subClassOf";															
	private static final String RELATION_URI_DEFINITION = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static final String LABEL_URI_DEFINITION = "http://www.w3.org/2000/01/rdf-schema#label";


	private Index(HDT hdt){
		indexesOfClasses = new HashMap<String, List<String>>();
		indexOfInstances = new HashMap<String, List<String>>();
		countInstances = new HashMap<String, Integer>();
		mapCountSujeito = new HashMap<String, Integer>();
		mapCountPredicado = new HashMap<String, Integer>();
		mapCountObjeto = new HashMap<String, Integer>();
		mapCountAll = new HashMap<BigInteger, Integer>();
		this.hdt = hdt;
		loadClassesIndex();
	}

	public static Index getInstance(HDT hdt){
		if(instance == null)
			instance = new Index(hdt);

		return instance;
	}

	public double[][] getMatriz(){
		return getMatrix();
	}


	private void loadClassesIndex() {
		// TODO Auto-generated method stub
		IteratorTripleString it;
		try {
			it = hdt.search("", RELATION_URI_DEFINITION, OWL_CLASS_URI_DEFINITION);

			while(it.hasNext()) {
				TripleString triple = it.next();
				String subject = triple.getSubject().toString();
				add(subject);
				getSublclasses(subject);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}finally {
			countInstances();

			
			/*
			double[][] matrix = getMatrix();

			int count = 0;

			System.out.println();
			for(int i = 0; i <matrix.length; i++ ){
				System.out.println();
				for(int j = 0; j <matrix.length; j++){
					double value = matrix[i][j];
					System.out.print(" "+value);
				}



			}
			System.out.println();
			System.out.println(count);
			 */
		}


	}


	private void getSublclasses(String clasDefintion) {

		try {
			IteratorTripleString it = hdt.search(clasDefintion, SUBCLASS_RELATION_URI_DEFINITION, "");
			List<String> subclasses = new ArrayList<String>();

			while(it.hasNext()){
				TripleString triple = it.next();				
				String object = triple.getObject().toString();
				subclasses.add(object);
			}

			indexesOfClasses.put(clasDefintion, subclasses);

		} catch (NotFoundException e) {
			System.err.println("Fim da reccursão");
			return;
		}

	}

	private void add(String classDefinition){

		try {
			IteratorTripleString it = hdt.search("", RELATION_URI_DEFINITION, classDefinition);
			int count = 0;
			while(it.hasNext()){
				String instance = it.next().getSubject().toString();		
				add(classDefinition, instance);
				count++;
			}
			countInstances.put(classDefinition, count);
		} catch (NotFoundException e) {
			return;
		}finally{


		}

	}

	private void add(String classDefinition, String instance){
		if(!indexOfInstances.containsKey(instance)){
			List<String> classes = new ArrayList<String>();
			classes.add(classDefinition);
			indexOfInstances.put(instance, classes);
		}else{
			indexOfInstances.get(instance).add(classDefinition);
		}
	}


	private String getClassInstance(String instance){

		List<String> list = indexOfInstances.get(instance);

		if(list.size() == 0){
			return list.get(0);
		}else{
			Map<String, Integer> sizes = new HashMap<String, Integer>();

			for(String classDefinition: list){	
				int size = countInstances.get(classDefinition);
				sizes.put(classDefinition, size);
			}
			Entry<String, Integer> min = null;
			for(Entry<String, Integer> entry : sizes.entrySet()){
				if (min == null || min.getValue() > entry.getValue()) {
					min = entry;
				}
			}

			return min.getKey();
		}


	}



	private double totalCorrelation(String classSujeito, String predicado, String classObjeto){


		/*
		 * 
		 * A métrica consistem em :
		 totalcorrelation = -log ( P( Si, pi, Oi ) / (P(Si) P(pi) P(Oi)) )
		 * 
		 */


		int pSi = mapCountSujeito.get(classSujeito);

		int pOi = mapCountObjeto.get(classObjeto);

		int pPi = mapCountPredicado.get(predicado);

		String plainString = classSujeito+predicado+classObjeto;

		BigInteger hash = md5(plainString);

		int numerador = mapCountAll.get(hash);

		double denominador = pSi * pOi * pPi;

		double fracao = numerador / denominador;

		double totalCorrelation = -Math.log(fracao);

		return totalCorrelation;
	}

	private void countInstances(){

		IteratorTripleString it;
		try {
			it = hdt.search("", "", "");

			while(it.hasNext()) {
				TripleString triple = it.next();
				String subject = triple.getSubject().toString();

				String predicate = triple.getPredicate().toString();

				String object = triple.getObject().toString();

				if(indexOfInstances.containsKey(subject) && indexOfInstances.containsKey(object)){
					sujeito(subject);
					predicado(predicate);
					objeto(object);
					all(subject, predicate, object);

				}

			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}finally {

		}

	}

	private void all(String subject, String predicate, String object) {

		String classSubject = getClassInstance(subject);
		String classObject = getClassInstance(object);
		String cleanString = classSubject+predicate+classObject;

		BigInteger key = md5(cleanString);

		if(mapCountAll.containsKey(key)){
			int count = mapCountAll.get(key);
			count++;
			mapCountAll.put(key, count);
		}else{
			mapCountAll.put(key, 1);
		}

	}

	private BigInteger md5(String s){
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(s.getBytes(),0,s.length());
			return new BigInteger(1,m.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;

	}

	private void sujeito(String sujeito){
		String classe = getClassInstance(sujeito);

		if(mapCountSujeito.containsKey(classe)){
			int count = mapCountSujeito.get(classe);
			count++;
			mapCountSujeito.put(classe, count);
		}else{
			mapCountSujeito.put(classe, 1);
		}
	}

	private void predicado(String predicado){
		if(mapCountPredicado.containsKey(predicado)){
			int count = mapCountPredicado.get(predicado);
			count++;
			mapCountPredicado.put(predicado, count);
		}else{
			mapCountPredicado.put(predicado, 1);
		}
	}

	private void objeto(String objeto){
		String classe = getClassInstance(objeto);

		if(mapCountObjeto.containsKey(classe)){
			int count = mapCountObjeto.get(classe);
			count++;
			mapCountObjeto.put(classe, count);
		}else{
			mapCountObjeto.put(classe, 1);
		}
	}

	public double[][] getMatrix(){

		long nShared = hdt.getDictionary().getNshared();
		long nSubjects = hdt.getDictionary().getNsubjects();
		long nObjects= hdt.getDictionary().getNobjects();

		int size= new Long(nSubjects + nObjects - nShared).intValue();

		System.err.println("Nª de nós "+size);

		String[] nodes = new String[size];
		double[][] matrix = new double[size][size];

		int position = 0;

		for (long id=1; id<=nShared; id++){
			nodes[position++] = hdt.getDictionary().idToString(id, TripleComponentRole.SUBJECT).toString();
		}

		for (long id=nShared+1; id<=nObjects; id++){
			nodes[position++] = hdt.getDictionary().idToString(id, TripleComponentRole.OBJECT).toString();
		}

		for (long id=1; id<=(nSubjects-nShared); id++){
			nodes[position++] = hdt.getDictionary().idToString(id+nShared, TripleComponentRole.SUBJECT).toString();
		}

		for(int i = 0; i<size; i++){

			double sum = 0d;

			for(int j = 0; j<size; j++){

				String node_i = nodes[i];
				String node_j = nodes[j];
				double value = getValue(node_i, node_j);

				sum  += value;

				matrix[i][j] = value;
			}

			//Normaliza valores
			if(sum != 0){
				for(int j = 0; j <size; j++){
					double value = matrix[i][j];
					matrix[i][j] = value / sum ;
				}
			}

		}

		return matrix;
	}

	private double getValue(String node_i, String node_j){
		int value = 0;
		IteratorTripleString it = null;

		try {
			it = hdt.search(node_i, "", node_j);
		} catch (NotFoundException e) {
			try {
				it = hdt.search(node_j, "", node_i);
			} catch (NotFoundException e1) {
				value = 0;
			}
		}finally{
			if(it != null){		

				if(it.hasNext()){
					TripleString triple = it.next();
					return getValue(triple);
				}

			}else{
				value = 0;
			}

		}
		return value;
	}

	private double getValue(TripleString triple){
		String sujeito = triple.getSubject().toString();
		String object = triple.getObject().toString();
		String predicate = triple.getPredicate().toString();

		if(indexOfInstances.containsKey(sujeito) && indexOfInstances.containsKey(object)){
			String classSubject = getClassInstance(sujeito);
			String classObject = getClassInstance(object);
			double value = totalCorrelation(classSubject, predicate, classObject);
			return value;
		}else{
			return 0d;
		}
	}

}
