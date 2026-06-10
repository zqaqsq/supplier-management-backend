package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.dto.AiQueryRequest;
import com.example.suppliermanagement.dto.AiQueryResponse;
import com.example.suppliermanagement.dto.SupplierDTO;
import com.example.suppliermanagement.model.Supplier;
import com.example.suppliermanagement.service.AiQueryService;
import com.example.suppliermanagement.service.SupplierService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiQueryServiceImpl implements AiQueryService {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.endpoint:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String apiEndpoint;

    @Value("${ai.model:qwen-plus}")
    private String model;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Autowired
    private SupplierService supplierService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = 
            "你是一个供应商管理系统的查询助手。你的任务是将用户的中文自然语言问题解析成结构化的查询条件。" + "\n" +
            "供应商数据表的字段如下：" + "\n" +
            "- name: 供应商名称" + "\n" +
            "- qualification: 资质等级 (A, B, C, D)" + "\n" +
            "- region: 地区/省份" + "\n" +
            "- city: 城市" + "\n" +
            "- contactPerson: 联系人" + "\n" +
            "- contactPhone: 联系电话" + "\n" +
            "- businessScope: 经营范围" + "\n" +
            "- status: 经营状态 (在业, 注销, 吊销, 歇业)" + "\n" +
            "- creditCode: 统一社会信用代码" + "\n" +
            "你必须只返回一个JSON对象，不要有任何其他文字。格式如下：" + "\n" +
            "{\"qualification\": \"资质等级关键词，如 A、B、C、D，或 null 表示不限\"," + "\n" +
            "\"region\": \"地区关键词，如 北京、上海、广东，或 null 表示不限\"," + "\n" +
            "\"city\": \"城市关键词，或 null\"," + "\n" +
            "\"status\": \"经营状态关键词，如 在业、注销，或 null\"," + "\n" +
            "\"keyword\": \"供应商名称或经营范围的模糊搜索关键词，或 null\"," + "\n" +
            "\"limit\": \"返回数量，默认 20\"}" + "\n" +
            "示例：" + "\n" +
            "用户输入：\"找北京的A级供应商\"" + "\n" +
            "返回：{\"qualification\": \"A\", \"region\": \"北京\", \"city\": null, \"status\": null, \"keyword\": null, \"limit\": 20}" + "\n" +
            "用户输入：\"深圳有哪些在业供应商\"" + "\n" +
            "返回：{\"qualification\": null, \"region\": \"广东\", \"city\": \"深圳\", \"status\": \"在业\", \"keyword\": null, \"limit\": 20}" + "\n" +
            "用户输入：\"过去三个月新增了几家供应商\"" + "\n" +
            "返回：{\"qualification\": null, \"region\": null, \"city\": null, \"status\": null, \"keyword\": null, \"limit\": 100}" + "\n" +
            "现在用户的问题是：";

    @Override
    public AiQueryResponse querySuppliers(AiQueryRequest request) {
        AiQueryResponse response = new AiQueryResponse();
        String question = request.getQuestion();

        Map<String, Object> conditions;

        if (aiEnabled && apiKey != null && !apiKey.isEmpty()) {
            conditions = parseWithAi(question);
        } else {
            conditions = parseWithPattern(question);
        }

        response.setConditions(conditions);

        List<Supplier> suppliers = executeQuery(conditions);
        List<SupplierDTO> dtos = new ArrayList<>();
        for (Supplier s : suppliers) {
            dtos.add(supplierService.convertToDTO(s));
        }

        response.setResults(dtos);
        response.setTotalCount(dtos.size());
        response.setSummary(generateSummary(question, dtos.size()));

        return response;
    }

    private Map<String, Object> parseWithAi(String question) {
        Map<String, Object> conditions = new HashMap<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.add(Map.of("role", "user", "content", question));
            body.put("messages", messages);
            body.put("max_tokens", 200);
            body.put("temperature", 0.1);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    apiEndpoint, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(resp.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText();

            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            JsonNode parsed = objectMapper.readTree(content);

            if (parsed.has("qualification") && !parsed.get("qualification").isNull()) {
                conditions.put("qualification", parsed.get("qualification").asText());
            }
            if (parsed.has("region") && !parsed.get("region").isNull()) {
                conditions.put("region", parsed.get("region").asText());
            }
            if (parsed.has("city") && !parsed.get("city").isNull()) {
                conditions.put("city", parsed.get("city").asText());
            }
            if (parsed.has("status") && !parsed.get("status").isNull()) {
                conditions.put("status", parsed.get("status").asText());
            }
            if (parsed.has("keyword") && !parsed.get("keyword").isNull()) {
                conditions.put("keyword", parsed.get("keyword").asText());
            }
            if (parsed.has("limit")) {
                conditions.put("limit", parsed.get("limit").asInt());
            }

        } catch (Exception e) {
            System.err.println("[AiQueryService] AI 解析失败，回退到模式匹配: " + e.getMessage());
            return parseWithPattern(question);
        }

        return conditions;
    }

    private Map<String, Object> parseWithPattern(String question) {
        Map<String, Object> conditions = new HashMap<>();
        String q = question.trim();

        String[] regions = {"北京", "上海", "广东", "深圳", "广州", "浙江", "杭州", "江苏", "南京", "苏州",
                "四川", "成都", "湖北", "武汉", "湖南", "长沙", "河南", "郑州", "河北", "山东", "青岛",
                "福建", "厦门", "安徽", "陕西", "西安", "重庆"};
        for (String r : regions) {
            if (q.contains(r)) {
                conditions.put("region", r);
                break;
            }
        }

        if (q.contains("A级") || q.contains("A级的") || q.contains("A 级")) {
            conditions.put("qualification", "A");
        } else if (q.contains("B级") || q.contains("B级的") || q.contains("B 级")) {
            conditions.put("qualification", "B");
        } else if (q.contains("C级") || q.contains("C级的") || q.contains("C 级")) {
            conditions.put("qualification", "C");
        } else if (q.contains("D级") || q.contains("D级的") || q.contains("D 级")) {
            conditions.put("qualification", "D");
        }

        String[] statuses = {"在业", "注销", "吊销", "歇业", "存续"};
        for (String s : statuses) {
            if (q.contains(s)) {
                conditions.put("status", s);
                break;
            }
        }

        int limit = 20;
        if (q.contains("前10") || q.contains("前十")) {
            limit = 10;
        } else if (q.contains("前20") || q.contains("前二十")) {
            limit = 20;
        } else if (q.contains("全部") || q.contains("所有")) {
            limit = 100;
        }
        conditions.put("limit", limit);

        if (!conditions.containsKey("region") && !conditions.containsKey("qualification")
                && !conditions.containsKey("status")) {
            conditions.put("keyword", q);
        }

        return conditions;
    }

    private List<Supplier> executeQuery(Map<String, Object> conditions) {
        List<Supplier> all = supplierService.getAllSuppliers();

        String qualification = (String) conditions.get("qualification");
        String region = (String) conditions.get("region");
        String city = (String) conditions.get("city");
        String status = (String) conditions.get("status");
        String keyword = (String) conditions.get("keyword");
        int limit = conditions.containsKey("limit") ? (int) conditions.get("limit") : 20;

        return all.stream()
                .filter(s -> qualification == null ||
                        (s.getQualification() != null && s.getQualification().equalsIgnoreCase(qualification)))
                .filter(s -> region == null ||
                        (s.getRegion() != null && s.getRegion().contains(region)) ||
                        (s.getCity() != null && (s.getCity().contains(region) || region.contains(s.getCity()))))
                .filter(s -> city == null ||
                        (s.getCity() != null && s.getCity().contains(city)))
                .filter(s -> status == null ||
                        (s.getStatus() != null && s.getStatus().equals(status)))
                .filter(s -> keyword == null ||
                        (s.getName() != null && s.getName().contains(keyword)) ||
                        (s.getBusinessScope() != null && s.getBusinessScope().contains(keyword)) ||
                        (s.getContactPerson() != null && s.getContactPerson().contains(keyword))))
                .limit(limit)
                .toList();
    }

    @Override
    public String generateSummary(String question, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("根据您的问题「").append(question).append("」，共找到 ").append(count).append(" 家供应商。");

        if (count == 0) {
            return sb.append("未找到符合条件的供应商，请尝试放宽搜索条件。").toString();
        }

        if (count > 0) {
            sb.append("结果已展示在上方列表中，您可以查看详细信息或进行抽取操作。");
        }

        if (aiEnabled && apiKey != null && !apiKey.isEmpty()) {
            sb.append("（此结果由 AI 智能解析生成）");
        } else {
            sb.append("（当前为智能模式解析，如需更精准的结果，请配置通义千问 API Key）");
        }

        return sb.toString();
    }
}
