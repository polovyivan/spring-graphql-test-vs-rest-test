package com.polovyi.ivan.exeption.handler;

import com.polovyi.ivan.exeption.BadRequestException;
import com.polovyi.ivan.exeption.NotFoundException;
import com.polovyi.ivan.exeption.UnprocessableEntityException;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.kickstart.execution.error.GraphQLErrorHandler;
import graphql.language.SourceLocation;
import graphql.schema.CoercingParseValueException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GraphQLExceptionHandler implements GraphQLErrorHandler {

    @Override
    public List<GraphQLError> processErrors(List<GraphQLError> list) {
        log.info("[GraphQLExceptionHandler] Processing error list...");
        return list.stream()
                .map(this::getNested)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<GraphQLError> getNested(GraphQLError error) {
        log.info("[GraphQLExceptionHandler] Getting nested errors... ");
        if (error instanceof ExceptionWhileDataFetching) {
            ExceptionWhileDataFetching exceptionError = (ExceptionWhileDataFetching) error;
            Throwable exception = exceptionError.getException();
            List<SourceLocation> locations = error.getLocations();
            if (exception instanceof ConstraintViolationException) {
                log.info("[GraphQLExceptionHandler] ConstraintViolationException type");
                return handleConstraintViolationException((ConstraintViolationException) exception, locations);
            }

            if (exception instanceof NotFoundException) {
                log.info("[GraphQLExceptionHandler] NotFoundException type");
                NotFoundException notFoundException = (NotFoundException) exception;
                notFoundException.setLocations(locations);
                return List.of(notFoundException);
            }

            if (exception instanceof UnprocessableEntityException) {
                log.info("[GraphQLExceptionHandler] UnprocessableEntityException type");
                UnprocessableEntityException unprocessableEntityException = (UnprocessableEntityException) exception;
                unprocessableEntityException.setLocations(locations);
                return List.of(unprocessableEntityException);
            }
        }
        if (error instanceof CoercingParseValueException) {
            List<SourceLocation> locations = error.getLocations();
            log.info("[ControllerAdvice] Processing MethodArgumentTypeMismatchException...");
            String fieldName = StringUtils.replace(StringUtils.substringBetween(error.getMessage(), "Variable ", " has"),
                    "'", "");
            String message = String.format("Field %s has an invalid format.", fieldName);
            BadRequestException badRequestException = new BadRequestException(message, locations);
            return List.of(badRequestException);
        }
        log.info("[GraphQLExceptionHandler] Returning error as is");
        return List.of(error);
    }

    private List<GraphQLError> handleConstraintViolationException(ConstraintViolationException exception,
            List<SourceLocation> locations) {
        log.info("[GraphQLExceptionHandler] Creating lis of BadRequestException...");
        return exception.getConstraintViolations().stream()
                .map(constraint -> new BadRequestException(constraint.getMessageTemplate(), locations))
                .map(badRequestException -> (GraphQLError) badRequestException)
                .collect(Collectors.toList());
    }
}