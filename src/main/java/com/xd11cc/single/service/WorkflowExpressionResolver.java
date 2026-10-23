package com.xd11cc.single.service;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import org.xml.sax.InputSource;

@Component
public class WorkflowExpressionResolver {
    private static final String FLOWABLE_NS = "http://flowable.org/bpmn";
    private static final Pattern DELEGATE_BEAN = Pattern.compile("^\\$\\{([a-zA-Z][a-zA-Z0-9_]*)}$");
    private static final Pattern SIMPLE_VARIABLE_EXPRESSION = Pattern.compile("^\\$\\{([a-zA-Z][a-zA-Z0-9_.-]*)}$");
    private static final Pattern VARIABLE_KEY = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]*$");

    @Autowired
    private IWorkflowExpressionConfigService expressionConfigService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ObjectMapper objectMapper;

    public String resolve(String xml, boolean validateDelegateBean) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList nodes = document.getElementsByTagName("*");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                resolveUserTaskAttribute(element, "assignee");
                resolveUserTaskAttribute(element, "candidateUsers");
                resolveUserTaskAttribute(element, "candidateGroups");
                String configKey = getAttribute(element, "expressionConfigKey");
                if (StringUtils.isBlank(configKey)) {
                    // 未走表达式配置的元素，仍需校验其“直接书写”的 delegateExpression：
                    // 否则 ${notExistBean} 这类引用会被放行到运行时才抛异常。
                    if (validateDelegateBean) validateDeclaredDelegate(element);
                    continue;
                }
                String parameters = getAttribute(element, "expressionParameters");
                String expression = expressionConfigService.resolveExpression(configKey, parameters);
                applyExpression(element, expression, validateDelegateBean);
            }
            if (validateDelegateBean) validateDeploymentStructure(document);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.EXPRESSION_RESOLVE_FAILED, new Object[]{e.getMessage()});
        }
    }

    public void validateStartVariables(String xml, Map<String, Object> variables) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList processes = document.getElementsByTagNameNS("*", "process");
            for (int i = 0; i < processes.getLength(); i++) {
                String definitions = ((Element) processes.item(i)).getAttributeNS(FLOWABLE_NS, "variables");
                if (StringUtils.isBlank(definitions)) continue;
                for (JsonNode definition : objectMapper.readTree(definitions)) {
                    if (!"START_INPUT".equals(definition.path("source").asText())) continue;
                    String key = definition.path("key").asText();
                    Object value = variables != null ? variables.get(key) : null;
                    if (definition.path("required").asBoolean(false) && (value == null || StringUtils.isBlank(String.valueOf(value)))) {
                        throw new ServiceException(SystemErrorEnum.PROCESS_VARIABLE_REQUIRED_MISSING, new Object[]{key});
                    }
                    if (value != null) validateVariableType(key, definition.path("type").asText(), value);
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.PROCESS_VARIABLE_VALIDATE_FAILED, new Object[]{e.getMessage()});
        }
    }

    private void validateVariableType(String key, String type, Object value) {
        if ("NUMBER".equals(type)) {
            try { new java.math.BigDecimal(String.valueOf(value)); }
            catch (Exception e) { throw new ServiceException(SystemErrorEnum.PROCESS_VARIABLE_TYPE_NUMBER, new Object[]{key}); }
        } else if ("BOOLEAN".equals(type) && !(value instanceof Boolean)
                && !"true".equalsIgnoreCase(String.valueOf(value)) && !"false".equalsIgnoreCase(String.valueOf(value))) {
            throw new ServiceException(SystemErrorEnum.PROCESS_VARIABLE_TYPE_BOOLEAN, new Object[]{key});
        } else if (("USER_LIST".equals(type) || "GROUP_LIST".equals(type)) && !(value instanceof Collection)) {
            throw new ServiceException(SystemErrorEnum.PROCESS_VARIABLE_TYPE_COLLECTION, new Object[]{key});
        }
    }

    private void validateDeploymentStructure(Document document) throws Exception {
        Map<String, String> variableSources = new HashMap<>();
        NodeList processes = document.getElementsByTagNameNS("*", "process");
        for (int i = 0; i < processes.getLength(); i++) {
            Element process = (Element) processes.item(i);
            String variablesJson = process.getAttributeNS(FLOWABLE_NS, "variables");
            if (StringUtils.isBlank(variablesJson)) continue;
            JsonNode variables = objectMapper.readTree(variablesJson);
            if (!variables.isArray()) throw new ServiceException(SystemErrorEnum.PROCESS_VARIABLE_DEFINITION_INVALID);
            Set<String> keys = new HashSet<>();
            for (JsonNode variable : variables) {
                String key = variable.path("key").asText();
                String source = variable.path("source").asText();
                if (!VARIABLE_KEY.matcher(key).matches() || !keys.add(key)) {
                    throw new ServiceException(SystemErrorEnum.PROCESS_VARIABLE_KEY_INVALID, new Object[]{key});
                }
                variableSources.put(key, source);
            }
        }

        NodeList loops = document.getElementsByTagNameNS("*", "multiInstanceLoopCharacteristics");
        for (int i = 0; i < loops.getLength(); i++) {
            Element loop = (Element) loops.item(i);
            Matcher matcher = SIMPLE_VARIABLE_EXPRESSION.matcher(loop.getAttributeNS(FLOWABLE_NS, "collection").trim());
            if (!matcher.matches()) continue;
            String variableKey = matcher.group(1);
            if (!"RESOLVER".equals(variableSources.get(variableKey))) continue;
            Element task = (Element) loop.getParentNode();
            String taskId = task.getAttribute("id");
            NodeList flows = document.getElementsByTagNameNS("*", "sequenceFlow");
            boolean hasIncoming = false;
            for (int j = 0; j < flows.getLength(); j++) {
                Element flow = (Element) flows.item(j);
                if (!taskId.equals(flow.getAttribute("targetRef"))) continue;
                hasIncoming = true;
                if (!hasTakeExpressionListener(flow)) {
                    throw new ServiceException(SystemErrorEnum.MULTI_INSTANCE_NO_TAKE_LISTENER, new Object[]{flow.getAttribute("id")});
                }
            }
            if (!hasIncoming) throw new ServiceException(SystemErrorEnum.MULTI_INSTANCE_NO_INCOMING, new Object[]{taskId});
        }
    }

    private boolean hasTakeExpressionListener(Element flow) {
        NodeList listeners = flow.getElementsByTagNameNS(FLOWABLE_NS, "executionListener");
        for (int i = 0; i < listeners.getLength(); i++) {
            Element listener = (Element) listeners.item(i);
            if ("take".equals(listener.getAttribute("event"))
                    && StringUtils.isNotBlank(getAttribute(listener, "expressionConfigKey"))) return true;
        }
        return false;
    }

    private void resolveUserTaskAttribute(Element element, String property) {
        String configKey = getAttribute(element, property + "ExpressionConfigKey");
        if (StringUtils.isBlank(configKey)) return;
        String parameters = getAttribute(element, property + "ExpressionParameters");
        String expression = expressionConfigService.resolveExpression(configKey, parameters);
        element.setAttributeNS(FLOWABLE_NS, "flowable:" + property, expression);
    }

    private void applyExpression(Element element, String expression, boolean validateDelegateBean) {
        String localName = element.getLocalName();
        if ("sequenceFlow".equals(localName)) {
            Element condition = directChild(element, "conditionExpression");
            if (condition == null) throw new ServiceException(SystemErrorEnum.EXPRESSION_CONDITION_NODE_MISSING);
            condition.setTextContent(expression);
            return;
        }
        if ("multiInstanceLoopCharacteristics".equals(localName)) {
            element.setAttributeNS(FLOWABLE_NS, "flowable:collection", expression);
            return;
        }
        if ("field".equals(localName)) {
            element.setAttribute("expression", expression);
            return;
        }
        boolean flowableExtensionElement = "taskListener".equals(localName) || "executionListener".equals(localName);
        // 监听器的 delegateExpression 常常通过 expressionConfigKey 注入：
        // 注入前元素上还没有该属性，因此不能只依赖 hasAttribute，
        // 否则 validateBean 永远不会执行，引用不存在 Bean 的流程会被放行到运行时才报错。
        boolean hasDeclaredDelegate = element.hasAttributeNS(FLOWABLE_NS, "delegateExpression")
                || element.hasAttribute("delegateExpression");
        boolean hasExplicitExpression = element.hasAttribute("expression")
                || element.hasAttributeNS(FLOWABLE_NS, "expression");
        boolean delegate = hasDeclaredDelegate
                || (flowableExtensionElement && !hasExplicitExpression && isDelegateBeanExpression(expression));
        String attribute = delegate ? "delegateExpression" : "expression";
        if (flowableExtensionElement) element.setAttribute(attribute, expression);
        else element.setAttributeNS(FLOWABLE_NS, "flowable:" + attribute, expression);
        if (delegate && validateDelegateBean) validateBean(expression);
    }

    /**
     * 判断表达式是否为“纯 Bean 引用”形式（{@code ${beanName}}）。
     * <p>只有这种形式才适合作为 delegateExpression 校验 Bean 是否存在；
     * 含方法调用或参数的表达式（如 {@code ${foo.bar(x)}}）不在此列。</p>
     */
    private boolean isDelegateBeanExpression(String expression) {
        return expression != null && DELEGATE_BEAN.matcher(expression.trim()).matches();
    }

    private Element directChild(Element parent, String localName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element && localName.equals(node.getLocalName())) return (Element) node;
        }
        return null;
    }

    /**
     * 校验元素上“直接书写”的 delegateExpression 所引用的 Bean 是否存在。
     * <p>
     * 覆盖未使用 expressionConfigKey 的流程定义：这类定义不经过 applyExpression，
     * 若不做此校验，引用不存在 Bean 的流程会被成功部署，直到运行时触发监听器才报 500。
     * </p>
     */
    private void validateDeclaredDelegate(Element element) {
        String localName = element.getLocalName();
        boolean isListener = "taskListener".equals(localName) || "executionListener".equals(localName);
        if (!isListener) {
            return;
        }
        String delegate = element.getAttributeNS(FLOWABLE_NS, "delegateExpression");
        if (StringUtils.isBlank(delegate)) {
            delegate = element.getAttribute("delegateExpression");
        }
        if (StringUtils.isNotBlank(delegate)) {
            validateBean(delegate);
        }
    }

    private void validateBean(String expression) {
        Matcher matcher = DELEGATE_BEAN.matcher(expression.trim());
        if (matcher.matches() && !applicationContext.containsBean(matcher.group(1))) {
            throw new ServiceException(SystemErrorEnum.EXPRESSION_BEAN_NOT_FOUND, new Object[]{matcher.group(1)});
        }
    }

    private String getAttribute(Element element, String name) {
        String value = element.getAttributeNS(FLOWABLE_NS, name);
        return StringUtils.isNotBlank(value) ? value : element.getAttribute(name);
    }
}
