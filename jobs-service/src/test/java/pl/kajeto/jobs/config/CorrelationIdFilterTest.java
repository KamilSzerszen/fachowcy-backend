package pl.kajeto.jobs.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  private String runAndCaptureMdc(MockHttpServletRequest request, MockHttpServletResponse response)
      throws Exception {
    AtomicReference<String> seen = new AtomicReference<>();
    filter.doFilter(
        request,
        response,
        new MockFilterChain() {
          @Override
          public void doFilter(
              jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
            seen.set(MDC.get(CorrelationIdFilter.MDC_KEY));
          }
        });
    return seen.get();
  }

  @Test
  void keepsIncomingCorrelationId() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationIdFilter.HEADER, "abc-123");
    MockHttpServletResponse response = new MockHttpServletResponse();

    String inMdc = runAndCaptureMdc(request, response);

    assertThat(inMdc).isEqualTo("abc-123");
    assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("abc-123");
    assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
  }

  @Test
  void generatesCorrelationIdWhenMissing() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    String inMdc = runAndCaptureMdc(new MockHttpServletRequest(), response);

    assertThat(inMdc).isNotBlank();
    assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo(inMdc);
  }
}
