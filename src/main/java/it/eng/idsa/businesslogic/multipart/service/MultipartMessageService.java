package it.eng.idsa.businesslogic.multipart.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
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
	private static final Predicate<String> predicateLineContentDisposition = (line) -> line.trim().startsWith(MultipartMessageKey.CONTENT_DISPOSITION.label);
	private static final Predicate<String> predicateLineContentLength = (line) -> line.trim().startsWith(MultipartMessageKey.CONTENT_LENGTH.label);
	private static final Predicate<String> predicateLineEmpty = (line) -> line.trim().isEmpty();
	
	public MultipartMessage parseMultipartMessage(String message) {
		return parseMultipartMessage(message, null);
	}
	
	public MultipartMessage parseMultipartMessage(String message, String contentType) {
		
		Optional<String> boundaryFromMessage;
		Optional<String> boundaryFromContentType;
		
		// Get boundary from the message
		boundaryFromMessage = getMessageBoundaryFromMessage(message);
		if(boundaryFromMessage.isPresent()) {
			logger.info("Boundary from the multipart message is: " + boundaryFromMessage.get());
		}else {
			logger.info("Boundary does not exist in the multipart message");
			//TODO: Throw exception.
		}
		
		// Get boundary from the Content-Type
		if(contentType!=null) {
			boundaryFromContentType = getMessageBoundaryFromContentType(contentType);
			if(boundaryFromContentType.isPresent()) {
				logger.info("Boundary from the content type is: " + boundaryFromContentType.get());
			} else {
				logger.info("Boundary does not exist in the content type");
			}
			// boundaryMessage and boundaryContentType should be the equals
			if(!boundaryFromContentType.equals(boundaryFromMessage)) {
				//TODO: Throw the exception
			}
		}
		
		String BOUNDARY = boundaryFromMessage.get();
		Predicate<String> predicateLineBoundary = (line) -> line.startsWith(BOUNDARY);
		List<List<String>> multipartMessageParts = getMultipartMessagesParts(predicateLineBoundary, message);
		
		MultipartMessage multipartMessage = createMultipartMessage(multipartMessageParts);
		
		return multipartMessage;
	}
	
	// TODO: shoul create logic for the toString
	public String toString(MultipartMessage message) {
		//String labela = MultipartMessageKey.CONTENT_DISPOSITION.label;
		return null;
	}
	
	private MultipartMessage createMultipartMessage(List<List<String>> multipartMessageParts) {
		MultipartMessageBuilder multipartMessageBuilder = new MultipartMessageBuilder();
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
		Predicate<String> predicateLineContentType = line -> line.trim().startsWith(MultipartMessageKey.CONTENT_DISPOSITION.label);
		String lineContentType = part.parallelStream()
				       .filter(row -> predicateLineContentType.test(row)).findFirst()
				       .get();
		Matcher matcher = pattern.matcher(lineContentType);
		matcher.find();
		String partName = matcher.group(2).toLowerCase();
	    return partName;
	}
	
	private Map<String, String> getPartHeader(List<String> part) {	
		Map<String, String> partHeader = new HashMap<String, String>();
		
		String partContetDisposition = part.parallelStream().filter(line -> predicateLineContentDisposition.test(line)).findFirst().get();
		partHeader.put(MultipartMessageKey.CONTENT_DISPOSITION.label, partContetDisposition);
		
		String partContentLength = part.parallelStream().filter(line -> predicateLineContentLength.test(line)).findFirst().get();
		partHeader.put(MultipartMessageKey.CONTENT_LENGTH.label, partContentLength);
		
		return partHeader;
	}
	
	private String getPartContent(List<String> part) {		
		OptionalInt  startPostionContent = IntStream.range(0, part.size())
											   .filter(index ->
												   (
														(
														    !predicateLineContentDisposition.test(part.get(index)) &&
			                            					!predicateLineContentLength.test(part.get(index)) &&
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
			multipartMessageBuilder.withHeaderContent(partContent);
		} else {
			if(partName.equals(MultipartMessageKey.NAME_PAYLOAD.label)) {
				multipartMessageBuilder.withPayloadHeader(partHeader);
				multipartMessageBuilder.withPayloadContent(partContent);
			} else {
				if(partName.equals(MultipartMessageKey.NAME_SIGNATURE.label)) {
					multipartMessageBuilder.withSignatureHeader(partHeader);
					multipartMessageBuilder.withSignatureContent(partContent);
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
	
	private static List<List<String>> getMultipartMessagesParts(Predicate<String> predicateLineBoundary, String multipart) {
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

	private static List<String> getLinesMultipartMessagePreparedForSpliting(List<String> linesInMultipartMessage,
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

	private static List<Integer> findPostionBoundaries(List<String> linesInMultipartMessage, Predicate<String> predicateLineBoundary) {
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

	private static List<List<String>> createMultipartMessageParts(Predicate<String> predicateLineBoundary,
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
	
}
