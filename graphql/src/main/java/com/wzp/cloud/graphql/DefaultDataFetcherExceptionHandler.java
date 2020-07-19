package com.wzp.cloud.graphql;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDataFetcherExceptionHandler implements DataFetcherExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(DefaultDataFetcherExceptionHandler.class);

    private static final String CODE_INTERNAL = "1";
    private static final String CODE_NOT_LOGIN = "2";
    private static final String CODE_FORBIDDEN = "3";

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        GraphQLContext context = handlerParameters.getDataFetchingEnvironment().getContext();
        DefaultGraphQLConfigurer configurer = context.get("configurer");

        DefaultGraphQLError error = getError(handlerParameters, configurer);
        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

    private DefaultGraphQLError getError(DataFetcherExceptionHandlerParameters handlerParameters, DefaultGraphQLConfigurer configurer) {
        Throwable exception = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();

        if (configurer != null && configurer.getErrorHandler() != null) {
            ErrorHandler.HandledError handledError = configurer.getErrorHandler().handleError(exception);
            if (handledError != null) {
                return new DefaultGraphQLError(path, handledError.getMessage(), sourceLocation, handledError.getErrorCode());
            }
        }

        // 未处理的异常需要打印日志
        logger.error(exception.getMessage(), exception);
        return new DefaultGraphQLError(path, exception.getMessage(), sourceLocation, code(exception));
    }

    private String code(Throwable exception) {
        if (exception instanceof ForbiddenException) {
            return CODE_FORBIDDEN;
        } else if (exception instanceof NotLoginException) {
            return CODE_NOT_LOGIN;
        } else {
            return CODE_INTERNAL;
        }
    }
}
