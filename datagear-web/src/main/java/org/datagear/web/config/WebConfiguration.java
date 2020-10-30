/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.datagear.util.IOUtil;
import org.datagear.web.controller.MainController;
import org.datagear.web.freemarker.CustomFreeMarkerView;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.datagear.web.util.EnumCookieThemeResolver;
import org.datagear.web.util.SubContextPathRequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web配置。
 * 
 * @author datagear@163.com
 */
@Configuration
@ComponentScan(basePackageClasses = MainController.class)
public class WebConfiguration extends WebMvcConfigurationSupport
{
	private CoreConfiguration coreConfiguration;

	private Environment environment;

	@Autowired
	public WebConfiguration(CoreConfiguration coreConfiguration, Environment environment)
	{
		super();
		this.coreConfiguration = coreConfiguration;
		this.environment = environment;
	}

	public CoreConfiguration getCoreConfiguration()
	{
		return coreConfiguration;
	}

	public void setCoreConfiguration(CoreConfiguration coreConfiguration)
	{
		this.coreConfiguration = coreConfiguration;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/org/datagear/web/static/");
	}

	@Override
	protected RequestMappingHandlerMapping createRequestMappingHandlerMapping()
	{
		SubContextPathRequestMappingHandlerMapping bean = new SubContextPathRequestMappingHandlerMapping();
		bean.setAlwaysUseFullPath(true);
		bean.setSubContextPath(this.environment.getProperty("subContextPath"));

		return bean;
	}

	@Override
	protected void addInterceptors(InterceptorRegistry registry)
	{
		ThemeChangeInterceptor interceptor = new ThemeChangeInterceptor();
		registry.addInterceptor(interceptor);
	}

	@Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		ObjectMapper objectMapper = this.coreConfiguration.objectMapperFactory().getObjectMapper();
		MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(objectMapper);

		converters.add(messageConverter);
		super.addDefaultHttpMessageConverters(converters);
	}

	@Override
	protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer(
			FormattingConversionService mvcConversionService, Validator mvcValidator)
	{
		ConfigurableWebBindingInitializer bean = super.getConfigurableWebBindingInitializer(mvcConversionService,
				mvcValidator);

		// XXX 父类方法不会注册应用自定义的FormattingConversionService，所以这里重新设置

		bean.setConversionService(this.coreConfiguration.conversionService().getObject());

		return bean;
	}

	@Override
	protected void configureViewResolvers(ViewResolverRegistry registry)
	{
		FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
		viewResolver.setViewClass(CustomFreeMarkerView.class);
		viewResolver.setContentType("text/html;charset=" + IOUtil.CHARSET_UTF_8);
		viewResolver.setExposeRequestAttributes(true);
		viewResolver.setAllowRequestOverride(true);
		viewResolver.setCache(true);
		viewResolver.setPrefix("");
		viewResolver.setSuffix(".ftl");

		registry.viewResolver(viewResolver);
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		FreeMarkerConfigurer bean = new FreeMarkerConfigurer();

		Properties settings = new Properties();
		settings.setProperty("datetime_format", "yyyy-MM-dd HH:mm:ss");
		settings.setProperty("date_format", "yyyy-MM-dd");
		settings.setProperty("number_format", "#.##");

		Map<String, Object> variables = new HashMap<>();
		variables.put("writeJson", new WriteJsonTemplateDirectiveModel(this.coreConfiguration.objectMapperFactory()));

		bean.setTemplateLoaderPath("classpath:org/datagear/web/templates/");
		bean.setDefaultEncoding(IOUtil.CHARSET_UTF_8);
		bean.setFreemarkerSettings(settings);
		bean.setFreemarkerVariables(variables);

		return bean;
	}

	@Override
	protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver()
	{
		return new DeliverContentTypeExceptionHandlerExceptionResolver();
	}

	@Bean("themeSource")
	public ResourceBundleThemeSource themeSource()
	{
		ResourceBundleThemeSource bean = new ResourceBundleThemeSource();
		bean.setBasenamePrefix("org.datagear.web.theme.");

		return bean;
	}

	@Bean("themeResolver")
	public EnumCookieThemeResolver themeResolver()
	{
		EnumCookieThemeResolver bean = new EnumCookieThemeResolver();
		return bean;
	}

	@Bean("localeResolver")
	public AcceptHeaderLocaleResolver localeResolver()
	{
		AcceptHeaderLocaleResolver bean = new AcceptHeaderLocaleResolver();
		return bean;
	}

	@Bean("multipartResolver")
	public MultipartResolver multipartResolver()
	{
		CommonsMultipartResolver bean = new CommonsMultipartResolver();
		return bean;
	}
}
