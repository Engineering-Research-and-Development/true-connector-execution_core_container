package it.eng.idsa.businesslogic.multipart.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.multipart.MultipartMessageKey;


/**
 * The MultipartMessageService
 */
@Service
@Transactional
public class MultipartMessageService {
	
	private static final Logger logger = LogManager.getLogger(MultipartMessageService.class);
	
	private static final String REGEX_BOUNDARY = "(.*?)boundary=(.*);.*";
	private static final String REGEX_NAME = "(.*?)name=\"(.*)\"(.*?)";
	private static final Predicate<String> predicateLineContentType = (line) -> line.trim().startsWith(MultipartMessageKey.CONTENT_TYPE.label.toLowerCase());
	private static final Predicate<String> predicateLineContentDisposition = (line) -> line.trim().startsWith(MultipartMessageKey.CONTENT_DISPOSITION.label.toLowerCase());
	private static final Predicate<String> predicateLineContentLength = (line) -> line.trim().startsWith(MultipartMessageKey.CONTENT_LENGTH.label.toLowerCase());
	private static final Predicate<String> predicateLineEmpty = (line) -> line.trim().isEmpty();
	private static final char[] BOUNDARY_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private static final String DEFAULT_CONTENT_TYPE = "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8";
	private static final String DEFAULT_CONTENT_DISPOSITION = MultipartMessageKey.CONTENT_DISPOSITION.label + ": form-data; name=";

	public MultipartMessage parseMultipartMessage(String message) {
		return parseMultipartMessage(message, null);
	}
	
	public MultipartMessage parseMultipartMessage(String message, String contentType) {
		
		Optional<String> boundaryFromMessage;
		Optional<String> boundaryFromContentType = Optional.of("");
		
		// Get boundary from the message
		boundaryFromMessage = getMessageBoundaryFromMessage(message);
		if(boundaryFromMessage.isPresent()) {
			logger.info("Boundary from the multipart message is: " + boundaryFromMessage.get());
		}else {
			logger.info("Boundary does not exist in the multipart message");
			//TODO: Throw exception.
		}
		
		String BOUNDARY = boundaryFromMessage.get();
		
		// Get boundary from the Content-Type
		if(contentType!=null) {
			boundaryFromContentType = getMessageBoundaryFromContentType(contentType);
			if(boundaryFromContentType.isPresent()) {
				logger.info("Boundary from the content type is: " + boundaryFromContentType.get());
				if(!BOUNDARY.substring(2).equals(boundaryFromContentType.get())) {
					// Overide boundary in the ContentType with the boundary in the multipart message
					contentType = replaceContentTypeWithNewBoundary(BOUNDARY, contentType);
				}
			} else {
				logger.info("Boundary does not exist in the content type");
				// TODO: Throw exception
			}
		}
		
		Predicate<String> predicateLineBoundary = (line) -> line.startsWith(BOUNDARY);
		List<List<String>> multipartMessageParts = getMultipartMessagesParts(predicateLineBoundary, message);
		
		MultipartMessage multipartMessage = createMultipartMessage(multipartMessageParts, contentType);
		
		return multipartMessage;
	}
	
	public String multipartMessagetoString(MultipartMessage message) {
		return multipartMessagetoString(message, true);
	}
	
	public String multipartMessagetoString(MultipartMessage message, boolean includeHttpHeaders) {
		
		StringBuilder multipartMessageString = new StringBuilder();
		String boundary = generateBoundary();
	    final String SEPARTOR_BOUNDARY = "--" + boundary;
	    final String END_SEPARTOR_BOUNDARY = "--" + boundary + "--";
	    boolean payloadTester = ((message.getPayloadContent() == null) ? false : (!message.getPayloadContent().isEmpty()));
	    boolean signatureTester = ((message.getSignatureContent() == null) ? false : (!message.getSignatureContent().isEmpty()));
		
		// Append httpHeaders
	    String httpHeadersString;
		if(includeHttpHeaders) {
			if(message.getHttpHeaders().isEmpty()) {
				httpHeadersString = setDefaultHttpHeaders(message.getHttpHeaders());
			} else {
				setContentTypeInMultipartMessage(message, SEPARTOR_BOUNDARY);
				httpHeadersString = message.getHttpHeaders()
						                          .entrySet()
						                          .parallelStream()
						                          .map(e -> e.getValue().toString())
						                          .collect(Collectors.joining(System.lineSeparator()));
			}
			multipartMessageString.append(httpHeadersString  + System.lineSeparator());
			multipartMessageString.append(System.lineSeparator());
		}
		
		// Append separator boundary
		multipartMessageString.append(SEPARTOR_BOUNDARY + System.lineSeparator());
		
		// Append headerHeader
		String headerHeaderString;
		if(message.getHeaderHeader().isEmpty()) {
			headerHeaderString = setDefaultPartHeader(message.getHeaderContentString(), MultipartMessageKey.NAME_HEADER.label);
		} else {
			headerHeaderString = message.getHeaderHeader()
					                    .entrySet()
					                    .parallelStream()
					                    .map(e -> e.getValue().toString())
					                    .collect(Collectors.joining(System.lineSeparator()));
		}
		multipartMessageString.append(headerHeaderString + System.lineSeparator());
		
		// Append headerContent
		multipartMessageString.append(message.getHeaderContentString() + System.lineSeparator());
		
		// Append payload
		if(payloadTester) {
			// Append separator boundary
			multipartMessageString.append(SEPARTOR_BOUNDARY + System.lineSeparator());
			
			// Append payloadHeader
			String payloadHeader;
			if(message.getPayloadHeader().isEmpty()) {
				payloadHeader = setDefaultPartHeader(message.getPayloadContent(), MultipartMessageKey.NAME_PAYLOAD.label);
			} else {
				payloadHeader = message.getPayloadHeader()
						                .entrySet()
						                .parallelStream()
						                .map(e -> e.getValue().toString())
						                .collect(Collectors.joining(System.lineSeparator()));
			}
			multipartMessageString.append(payloadHeader + System.lineSeparator());
			multipartMessageString.append(System.lineSeparator());
			
			// Append payloadContent
			multipartMessageString.append(message.getPayloadContent() + System.lineSeparator());	
		}
		
		// Append signature
		if(signatureTester) {
			// Append separator boundary
			multipartMessageString.append(SEPARTOR_BOUNDARY + System.lineSeparator());
			
			// Append signatureHeader
			String signatureHeaderString;
			if(message.getSignatureHeader().isEmpty()) {
				signatureHeaderString = setDefaultPartHeader(message.getSignatureContent(), MultipartMessageKey.NAME_SIGNATURE.label);
			} else {
				signatureHeaderString = message.getSignatureHeader()
	                       					   .entrySet()
	                                           .parallelStream()
	                                           .map(e -> e.getValue().toString())
	                                           .collect(Collectors.joining(System.lineSeparator()));
			}
			multipartMessageString.append(signatureHeaderString + System.lineSeparator());
			multipartMessageString.append(System.lineSeparator());
			
			// Append signatureContent
			multipartMessageString.append(message.getSignatureContent() + System.lineSeparator());
		}
		
		// Append end separator boundary
		multipartMessageString.append(END_SEPARTOR_BOUNDARY);
		
		return multipartMessageString.toString();
	}

	private String setDefaultHttpHeaders(Map<String, String> httpHeaders) {
		StringBuffer defaultContentType = new StringBuffer();
		defaultContentType.append(httpHeaders.getOrDefault(MultipartMessageKey.CONTENT_TYPE.label, "multipart/mixed"));
		httpHeaders.put(MultipartMessageKey.CONTENT_TYPE.label, defaultContentType.toString());
		
		String defaultHttpHeadersToString =  httpHeaders
												.entrySet()
												.parallelStream()
												.map(e -> e.getKey().toString() + ": " + e.getValue().toString())
												.collect(Collectors.joining(System.lineSeparator()));
		return defaultHttpHeadersToString;
	}

	private String setDefaultPartHeader(String headerContentString, String partName) {
		StringBuffer defaultHeaderHeaderString = new StringBuffer();
		defaultHeaderHeaderString.append(DEFAULT_CONTENT_DISPOSITION + "\"" + partName + "\"" + System.lineSeparator());
		defaultHeaderHeaderString.append(MultipartMessageKey.CONTENT_LENGTH.label + ": " + headerContentString.length() + System.lineSeparator());
		return defaultHeaderHeaderString.toString();
	}

	private void setContentTypeInMultipartMessage(MultipartMessage message, String boundary) {
		Optional<Entry<String, String>> contentTypeLine = Optional.empty();
		if(message.getHttpHeaders().containsKey(MultipartMessageKey.CONTENT_TYPE.label)) {
			contentTypeLine = message.getHttpHeaders()
		       .entrySet()
		       .parallelStream()
		       .filter(e -> (predicateLineContentType.test(e.getKey().toLowerCase())))
		       .findFirst();
		}

		if(contentTypeLine.isEmpty()) {
			setDefaultContentType(message, boundary);
		} else {
			setNewBoundaryInContentType(message, boundary, contentTypeLine);
		}
	}

	private void setNewBoundaryInContentType(MultipartMessage message, String boundary,
			                                 Optional<Entry<String, String>> contentTypeLineEntry) {
		String contentTypeLine = contentTypeLineEntry.get().getValue();
		String contentTypeLineWithNewBoundary = replaceContentTypeWithNewBoundary(boundary, contentTypeLine);
		// Set new Content-Type with the new boundary
		message.getHttpHeaders()
		       .entrySet()
		       .parallelStream()
		       .filter(e -> (predicateLineContentType.test(e.getKey().toLowerCase())))
		       .findFirst()
		       .get()
		       .setValue(contentTypeLineWithNewBoundary);
	}
	
	private void setDefaultContentType(MultipartMessage message, String boundary) {
		String contentTypeLineWithNewBoundary = replaceContentTypeWithNewBoundary(boundary, DEFAULT_CONTENT_TYPE);
		message.getHttpHeaders().put(MultipartMessageKey.CONTENT_TYPE.label, contentTypeLineWithNewBoundary);
	}
	
	private String replaceContentTypeWithNewBoundary(String boundary, String contentTypeLine) {
		Pattern pattern = Pattern.compile(REGEX_BOUNDARY);
		Matcher matcher = pattern.matcher(contentTypeLine);
		matcher.find();
		StringBuilder stringBuilder = new StringBuilder(matcher.group(2));
		String contentTypeLineWithNewBoundary = contentTypeLine.replace(stringBuilder, boundary.substring(2));
		return contentTypeLineWithNewBoundary;
	}

	private MultipartMessage createMultipartMessage(List<List<String>> multipartMessageParts, String contentType) {
		MultipartMessageBuilder multipartMessageBuilder = new MultipartMessageBuilder();
		
		if(contentType!=null) {
			Map<String, String> httpHeader  = new HashMap<String, String>() {{
			    put(MultipartMessageKey.CONTENT_TYPE.label, contentType);
			}}; 
			multipartMessageBuilder.withHttpHeader(httpHeader);
		}
		
		multipartMessageParts.parallelStream()
		                     .forEach
		                     	(
		                    		 part -> {
		                    			 String partName = getMultipartMessagePartName(part);
		                    			 Map<String, String> partHeader = getPartHeader(part);
		                    			 String partContent = getPartContent(part);
		                    			 fillMultipartMessage(multipartMessageBuilder, partName, partHeader, partContent);
		                    		 }
		                     	);
		MultipartMessage multipartMessage = multipartMessageBuilder.build(); 
		return multipartMessage;
	}

	private String getMultipartMessagePartName(List<String> part) {
		Pattern pattern = Pattern.compile(REGEX_NAME);
		String lineContentType = part.parallelStream()
				       .filter(row -> predicateLineContentDisposition.test(row.toLowerCase())).findFirst()
				       .get();
		Matcher matcher = pattern.matcher(lineContentType);
		matcher.find();
		String partName = matcher.group(2).toLowerCase();
	    return partName;
	}
	
	private Map<String, String> getPartHeader(List<String> part) {	
		Map<String, String> partHeader = new HashMap<String, String>();
		
		String partContetDisposition = part.parallelStream().filter(line -> predicateLineContentDisposition.test(line.toLowerCase())).findFirst().get();
		partHeader.put(MultipartMessageKey.CONTENT_DISPOSITION.label, partContetDisposition);
		
		String partContentLength = part.parallelStream().filter(line -> predicateLineContentLength.test(line.toLowerCase())).findFirst().get();
		partHeader.put(MultipartMessageKey.CONTENT_LENGTH.label, partContentLength);
		
		return partHeader;
	}
	
	private String getPartContent(List<String> part) {		
		OptionalInt  startPostionContent = IntStream.range(0, part.size())
											   .filter(index ->
												   (
														(
														    !predicateLineContentDisposition.test(part.get(index).toLowerCase()) &&
			                            					!predicateLineContentLength.test(part.get(index).toLowerCase()) &&
			                            					!predicateLineEmpty.test(part.get(index))
			                            				)
													)
												 )
											   .findFirst();
											   
		String partContent=null;
		if(startPostionContent.isPresent()) {									     
			partContent = IntStream.range(startPostionContent.getAsInt(), part.size())
					               .mapToObj(index -> part.get(index)+System.getProperty("line.separator"))
					               .collect(Collectors.joining());
					               
		}
				                            		
		return partContent;
	}
	
	private void fillMultipartMessage(MultipartMessageBuilder multipartMessageBuilder, String partName,
			                          Map<String, String> partHeader, String partContent) {
		 
		if(partName.equals(MultipartMessageKey.NAME_HEADER.label)) {
			multipartMessageBuilder.withHeaderHeader(partHeader);
			multipartMessageBuilder.withHeaderContent(partContent.trim());
		} else {
			if(partName.equals(MultipartMessageKey.NAME_PAYLOAD.label)) {
				multipartMessageBuilder.withPayloadHeader(partHeader);
				multipartMessageBuilder.withPayloadContent(partContent.trim());
			} else {
				if(partName.equals(MultipartMessageKey.NAME_SIGNATURE.label)) {
					multipartMessageBuilder.withSignatureHeader(partHeader);
					multipartMessageBuilder.withSignatureContent(partContent.trim());
				}
			}
		}
	}

	private Optional<String> getMessageBoundaryFromContentType(String contentType) {
		String boundary = null;
		Pattern pattern = Pattern.compile(REGEX_BOUNDARY);
		Matcher matcher = pattern.matcher(contentType);
		matcher.find();
		boundary = matcher.group(2);
		return Optional.ofNullable(boundary);
	}
	
	private Optional<String> getMessageBoundaryFromMessage(String message) {
		String boundary = null;
		Stream<String> lines = message.lines();
		boundary = lines.filter(line -> line.startsWith("--"))
				        .findFirst()
				        .get();
		return Optional.ofNullable(boundary);
	}
	
	private List<List<String>> getMultipartMessagesParts(Predicate<String> predicateLineBoundary, String multipart) {
		// Devide multipart message on the lines
		Stream<String> lines = multipart.lines();
		
		// create list of the lines from the multipart message
		List<String> linesInMultipartMessage = lines.collect(Collectors.toList());
		
		// Find all boundary postions
		List<Integer> positionBoundaries = findPostionBoundaries(linesInMultipartMessage, predicateLineBoundary);
		
		// Find the position of the first boundary
		Integer postionStartBoundary = positionBoundaries.parallelStream().findFirst().get();
		// Find the position of the last boundary
		Integer postionLastBoundary = positionBoundaries.parallelStream().reduce((a, b) -> b).get();
 
		// Prepare list for the spleeting
		List<String> linesMultipartMessagePreparedForSpliting = getLinesMultipartMessagePreparedForSpliting(
				linesInMultipartMessage, postionStartBoundary, postionLastBoundary);
		
		// create each of the part of the multipart messages to be lists of the lines
		List<List<String>> multipartMessageParts = createMultipartMessageParts(predicateLineBoundary, linesMultipartMessagePreparedForSpliting);
		
		return multipartMessageParts;
	}

	private List<String> getLinesMultipartMessagePreparedForSpliting(List<String> linesInMultipartMessage,
			Integer postionStartBoundary, Integer postionLastBoundary) {
		List<String> linesMultipartMessagePreparedForSpliting = IntStream.range(0, linesInMultipartMessage.size())
        		 .mapToObj(index -> 
        		 				(
        		 					index > postionStartBoundary && index < postionLastBoundary) ? linesInMultipartMessage.get(index) : null
        		 				)
        		 .filter(element -> element!=null)
        		 .collect(Collectors.toList());
		return linesMultipartMessagePreparedForSpliting;
	}

	private List<Integer> findPostionBoundaries(List<String> linesInMultipartMessage, Predicate<String> predicateLineBoundary) {
		// Find postion of the all boundary
		List<Integer> list = IntStream.range(0, linesInMultipartMessage.size())
		         .mapToObj(index -> 
		         					(
		         							predicateLineBoundary.test(linesInMultipartMessage.get(index).replace(System.lineSeparator(), ""))
		         					)
		         					? index : null
		        		 )
		         .filter(element -> element!=null)
		         .collect(Collectors.toList());
		return list;
	}

	private List<List<String>> createMultipartMessageParts(Predicate<String> predicateLineBoundary,
			List<String> linesMultipartMessagePreparedForSpliting) {
		int[] indexesSepartor = 
			      Stream.of(IntStream.of(-1), IntStream.range(0, linesMultipartMessagePreparedForSpliting.size())
			      .filter(i -> predicateLineBoundary.test(linesMultipartMessagePreparedForSpliting.get(i))), IntStream.of(linesMultipartMessagePreparedForSpliting.size()))
			      .flatMapToInt(s -> s).toArray();		 
		return
			      IntStream.range(0, indexesSepartor.length - 1)
			               .mapToObj(i -> linesMultipartMessagePreparedForSpliting.subList(indexesSepartor[i] + 1, indexesSepartor[i + 1]))
			               .collect(Collectors.toList());
	}
	
	private String generateBoundary() {
		StringBuilder buffer = new StringBuilder();
		Random rand = new Random();
		int count = rand.nextInt(11) + 30;
		IntStream.range(0, count)
		         .forEach( i ->
			                 buffer.append(BOUNDARY_CHARS[rand.nextInt(BOUNDARY_CHARS.length)])
		                 );
		return buffer.toString();
	}
	
}
