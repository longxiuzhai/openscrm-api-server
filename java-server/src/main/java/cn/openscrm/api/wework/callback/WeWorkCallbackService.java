package cn.openscrm.api.wework.callback;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Slf4j
@Service
public class WeWorkCallbackService {

    private final WeWorkCallbackCrypto crypto;
    private final List<WeWorkCallbackRegistrar> registrars;
    private final Map<WeWorkCallbackEventKey, WeWorkCallbackHandler> handlers = new HashMap<>();

    public WeWorkCallbackService(WeWorkCallbackCrypto crypto, List<WeWorkCallbackRegistrar> registrars) {
        this.crypto = crypto;
        this.registrars = registrars;
    }

    @PostConstruct
    public void registerDefaultHandlers() {
        register("event", "change_external_contact", "add_external_contact", this::logOnly);
        register("event", "change_external_contact", "del_external_contact", this::logOnly);
        register("event", "change_external_contact", "edit_external_contact", this::logOnly);
        register("event", "change_contact", "update_user", this::logOnly);
        register("event", "change_external_contact", "del_follow_user", this::logOnly);
        register("event", "change_contact", "create_party", this::logOnly);
        register("event", "change_contact", "update_party", this::logOnly);
        register("event", "change_contact", "delete_party", this::logOnly);
        register("event", "change_external_tag", "create", this::logOnly);
        register("event", "change_external_tag", "update", this::logOnly);
        register("event", "change_external_tag", "delete", this::logOnly);
        register("event", "change_external_chat", "create", this::logOnly);
        register("event", "change_external_chat", "update", this::logOnly);
        register("event", "change_external_chat", "dismiss", this::logOnly);
        for (WeWorkCallbackRegistrar registrar : registrars) {
            registrar.register(this);
        }
    }

    public String echo(String msgSignature, String timestamp, String nonce, String echoStr) {
        if (!crypto.verify(timestamp, nonce, echoStr, msgSignature)) {
            throw new BizException(ErrorCode.CHECK_SIGN_FAILED);
        }
        return crypto.decrypt(echoStr);
    }

    public void handle(String msgSignature, String timestamp, String nonce, String body) {
        String encrypted = extractEncrypt(body);
        if (!crypto.verify(timestamp, nonce, encrypted, msgSignature)) {
            throw new BizException(ErrorCode.CHECK_SIGN_FAILED);
        }
        String plainXml = crypto.decrypt(encrypted);
        WeWorkCallbackMessage message = parseMessage(plainXml);
        WeWorkCallbackHandler handler = handlers.get(new WeWorkCallbackEventKey(
                message.getMsgType(), message.getEvent(), message.getChangeType()));
        if (handler == null) {
            log.warn("unsupported WeWork callback event msgType={}, event={}, changeType={}, xml={}",
                    message.getMsgType(), message.getEvent(), message.getChangeType(), plainXml);
            throw new BizException(ErrorCode.UNKNOWN_EVENT_TYPE);
        }
        handler.handle(message);
    }

    public void register(String msgType, String event, String changeType, WeWorkCallbackHandler handler) {
        handlers.put(new WeWorkCallbackEventKey(msgType, event, changeType), handler);
    }

    private String extractEncrypt(String body) {
        String encrypted = parseXmlFields(body).get("Encrypt");
        if (!StringUtils.hasText(encrypted)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return encrypted;
    }

    private WeWorkCallbackMessage parseMessage(String xml) {
        Map<String, String> fields = parseXmlFields(xml);
        WeWorkCallbackMessage message = new WeWorkCallbackMessage();
        message.setRawXml(xml);
        message.setFields(fields);
        message.setToUserName(fields.get("ToUserName"));
        message.setFromUserName(fields.get("FromUserName"));
        message.setCreateTime(parseLong(fields.get("CreateTime")));
        message.setMsgType(fields.get("MsgType"));
        message.setEvent(fields.get("Event"));
        message.setChangeType(fields.get("ChangeType"));
        message.setAgentId(parseLong(fields.get("AgentID")));
        return message;
    }

    private Map<String, String> parseXmlFields(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = document.getDocumentElement();
            Map<String, String> fields = new LinkedHashMap<>();
            collectXmlFields(root, "", fields);
            return fields;
        } catch (Exception e) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
    }

    private void collectXmlFields(Element element, String path, Map<String, String> fields) {
        String name = element.getNodeName();
        String currentPath = StringUtils.hasText(path) ? path + "." + name : name;
        NodeList children = element.getChildNodes();
        boolean hasElementChild = false;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                hasElementChild = true;
                collectXmlFields((Element) children.item(i), currentPath, fields);
            }
        }
        if (!hasElementChild) {
            String value = element.getTextContent();
            fields.putIfAbsent(name, value);
            fields.put(currentPath, value);
        }
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void logOnly(WeWorkCallbackMessage message) {
        log.info("received WeWork callback msgType={}, event={}, changeType={}, fields={}",
                message.getMsgType(), message.getEvent(), message.getChangeType(), message.getFields());
    }
}
