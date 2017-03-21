package com.digirati.themathmos.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.exception.SearchException;
import com.digirati.themathmos.exception.SearchQueryException;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.OAAnnotationSearchService;
import com.digirati.themathmos.service.OASearchService;
import com.digirati.themathmos.service.TextSearchService;




@RestController(OASearchController.CONTROLLER_NAME)
public class OASearchController {
    
    
    public static final String CONTROLLER_NAME = "oaSearchController";
    
    private OAAnnotationSearchService oaAnnotationSearchService;
    private TextSearchService textSearchService;
    private OASearchService oaSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    
    
    private ControllerUtility controllerUtility;
    
    @Autowired
    public OASearchController(OAAnnotationSearchService oaAnnotationSearchService,AnnotationAutocompleteService annotationAutocompleteService,
	    TextSearchService textSearchService,OASearchService searchService
	    ) {
	
        this.oaAnnotationSearchService = oaAnnotationSearchService;
        this.annotationAutocompleteService = annotationAutocompleteService;
        this.textSearchService = textSearchService;
        this.oaSearchService = searchService;
        this.controllerUtility = new ControllerUtility();
    }
    
    
    //autocomplete parameter defaults to 1 if not specified
    public static final String PARAM_MIN = "min";
    
    
    private static final String OA_MIXED_SEARCH_REQUEST_PATH = "/search/oa";      
    private static final String OA_MIXED_AUTOCOMPLETE_REQUEST_PATH = "/autocomplete/oa";
    
    private static final String WITHIN_OA_MIXED_SEARCH_REQUEST_PATH = "/{withinId}/search/oa";       
    private static final String WITHIN_OA_MIXED_AUTOCOMPLETE_REQUEST_PATH = "/{withinId}/autocomplete/oa";
    
  
    
    @CrossOrigin
    @RequestMapping(value = OA_MIXED_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchOAMixedGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = false) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
	    HttpServletRequest request) {
	
	
	String queryString = controllerUtility.createQueryString(request);
	String type = null;
	if(StringUtils.isEmpty(query) && StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    throw new SearchQueryException("Please enter either a query, moitvation, date or user to search ");	    
	}
	ServiceResponse<Map<String, Object>> serviceResponse;
	
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = oaSearchService.getAnnotationPage(query, queryString, page, null, type);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = textSearchService.getTextPositions(query, queryString, false, page, false, null);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = oaAnnotationSearchService.getAnnotationPage(query, motivation, date, user, queryString, page, null, type); 
	    }else{
		serviceResponse = oaSearchService.getAnnotationPage(query, queryString, page, null, type);
	    }
	}
	

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }
    
    @CrossOrigin
    @RequestMapping(value = WITHIN_OA_MIXED_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchOAWithinMixedGet(
	    @PathVariable String withinId,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = false) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
	    HttpServletRequest request) {
	
	String within = withinId;
	
	String type = null;
	String queryString = controllerUtility.createQueryString(request);
	
	if(StringUtils.isEmpty(query) && StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    throw new SearchQueryException("Please enter either a query, moitvation, date or user to search ");	    
	}
	ServiceResponse<Map<String, Object>> serviceResponse;
	
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = oaSearchService.getAnnotationPage(query, queryString, page, within, type);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = textSearchService.getTextPositions(query, queryString, false, page, false, within);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = oaAnnotationSearchService.getAnnotationPage(query, motivation, date, user, queryString, page, within, type); 
	    }else{
		serviceResponse = oaSearchService.getAnnotationPage(query, queryString, page, within, type);
	    }
	}
	

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }
    
    
    

    @RequestMapping(value = OA_MIXED_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteOAMixedGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = PARAM_MIN, required = false) String min, 
	    HttpServletRequest request) {

	
	String queryString = controllerUtility.createQueryString(request);

	ServiceResponse<Map<String, Object>> serviceResponse;
	
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, false, null);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = annotationAutocompleteService.getTerms(query, min, queryString, false, null);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = annotationAutocompleteService.getTerms(query, motivation, date, user, min, queryString, false, null);
	    }else{
		serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, false, null);
	    }
	}
	
	
	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

    }
    
    @RequestMapping(value = WITHIN_OA_MIXED_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteOAWithinMixedGet(
	    @PathVariable String withinId,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = PARAM_MIN, required = false) String min, 
	    HttpServletRequest request) {

	String within = withinId;
	
	String queryString = controllerUtility.createQueryString(request);

	ServiceResponse<Map<String, Object>> serviceResponse;
	
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, false, within);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = annotationAutocompleteService.getTerms(query, min, queryString, false, within);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = annotationAutocompleteService.getTerms(query, motivation, date, user, min, queryString, false, within);
	    }else{
		serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, false, within);
	    }
	}

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

    }
    
}
