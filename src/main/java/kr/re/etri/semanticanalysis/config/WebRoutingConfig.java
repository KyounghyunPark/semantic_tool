package kr.re.etri.semanticanalysis.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebRoutingConfig implements WebMvcConfigurer {

//	@Override
//	public void addViewControllers(ViewControllerRegistry registry) {
//		registry.addViewController("/urlNotFound").setViewName("forward:/index.html");
//	}
//
//	@Bean
//	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {
//		return container -> {
//			container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/urlNotFound"));
//		};
//	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/").resourceChain(true)
				.addResolver(new PathResourceResolver() {
					@Override
					protected org.springframework.core.io.Resource getResource(String resourcePath, Resource location)
							throws IOException {
						Resource requestedResource = location.createRelative(resourcePath);
						return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
								: new ClassPathResource("/static/index.html");
					}
				});
	}
}
