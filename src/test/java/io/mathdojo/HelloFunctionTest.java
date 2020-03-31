package io.mathdojo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.adapter.azure.AzureSpringBootRequestHandler;
import org.springframework.test.context.junit4.SpringRunner;

import io.mathdojo.model.Greeting;
import io.mathdojo.model.User;

@RunWith(SpringRunner.class)
public class HelloFunctionTest {

    @MockBean
    private ExecutionContext mockExecContext; // = mock(ExecutionContext.class);

    @Before
    public void setUp() {
        Logger testLogger = mock(Logger.class);

        Mockito.when(mockExecContext.getLogger()).thenReturn(testLogger);
    }
    
    @Test
    public void test() {
        Greeting result = new HelloFunction().hello(mockExecContext).apply(new User("foo"));
        assertThat(result.getMessage()).isEqualTo("Welcome, foo");
    }

    @Test
    public void start() throws Exception {
        AzureSpringBootRequestHandler<User, Greeting> handler = new AzureSpringBootRequestHandler<>(
                HelloFunction.class);
        Greeting result = handler.handleRequest(new User("foo"), mockExecContext);
        handler.close();
        assertThat(result.getMessage()).isEqualTo("Welcome, foo");
    }
}
