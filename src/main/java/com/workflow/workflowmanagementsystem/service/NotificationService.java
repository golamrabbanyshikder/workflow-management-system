package com.workflow.workflowmanagementsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService implements MqttCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;
    
    @Value("${mqtt.client.id:workflow-system}")
    private String clientId;
    
    @Value("${mqtt.username:}")
    private String username;
    
    @Value("${mqtt.password:}")
    private String password;
    
    @Value("${mqtt.topic.prefix:workflow}")
    private String topicPrefix;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MqttClient mqttClient;
    
    @PostConstruct
    public void init() {
        try {
            connectToMqttBroker();
        } catch (Exception e) {
            logger.error("Failed to initialize MQTT client: {}", e.getMessage());
            // Continue without MQTT - system will still work but notifications won't be sent
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (MqttException e) {
            logger.error("Error closing MQTT connection: {}", e.getMessage());
        }
    }
    
    private void connectToMqttBroker() throws MqttException {
        try {
            // Create MQTT client
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            
            // Set callback
            mqttClient.setCallback(this);
            
            // Configure connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setConnectionTimeout(30);
            connectOptions.setKeepAliveInterval(60);
            
            if (!username.isEmpty()) {
                connectOptions.setUserName(username);
                connectOptions.setPassword(password.toCharArray());
            }
            
            // Connect to broker
            mqttClient.connect(connectOptions);
            
            // Subscribe to notification topics
            String[] topics = {
                topicPrefix + "/tasks/assigned",
                topicPrefix + "/tasks/updated",
                topicPrefix + "/tasks/completed",
                topicPrefix + "/workflows/created",
                topicPrefix + "/workflows/updated"
            };
            
            for (String topic : topics) {
                mqttClient.subscribe(topic, 1);
                logger.info("Subscribed to MQTT topic: {}", topic);
            }
            
            logger.info("Successfully connected to MQTT broker: {}", brokerUrl);
            
        } catch (MqttException e) {
            logger.error("Failed to connect to MQTT broker: {}", e.getMessage());
            throw e;
        }
    }
    
    // Send task assignment notification
    public void notifyTaskAssigned(Task task, User assignedTo, User assignedBy) {
        try {
            Map<String, Object> notification = createNotification(
                "Task Assigned",
                String.format("Task '%s' has been assigned to you by %s", 
                    task.getTitle(), assignedBy.getUsername()),
                "TASK_ASSIGNED",
                task.getId(),
                assignedTo.getId()
            );
            
            sendNotification(topicPrefix + "/tasks/assigned", notification);
            logger.info("Sent task assignment notification for task {} to user {}", 
                task.getId(), assignedTo.getUsername());
            
        } catch (Exception e) {
            logger.error("Failed to send task assignment notification: {}", e.getMessage());
        }
    }
    
    // Send task update notification
    public void notifyTaskUpdated(Task task, User updatedBy) {
        try {
            Map<String, Object> notification = createNotification(
                "Task Updated",
                String.format("Task '%s' has been updated by %s", 
                    task.getTitle(), updatedBy.getUsername()),
                "TASK_UPDATED",
                task.getId(),
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null
            );
            
            sendNotification(topicPrefix + "/tasks/updated", notification);
            logger.info("Sent task update notification for task {}", task.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send task update notification: {}", e.getMessage());
        }
    }
    
    // Send task completion notification
    public void notifyTaskCompleted(Task task, User completedBy) {
        try {
            Map<String, Object> notification = createNotification(
                "Task Completed",
                String.format("Task '%s' has been completed by %s", 
                    task.getTitle(), completedBy.getUsername()),
                "TASK_COMPLETED",
                task.getId(),
                task.getWorkflow().getCreatedBy().getId()
            );
            
            sendNotification(topicPrefix + "/tasks/completed", notification);
            logger.info("Sent task completion notification for task {}", task.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send task completion notification: {}", e.getMessage());
        }
    }
    
    // Send workflow creation notification
    public void notifyWorkflowCreated(Workflow workflow, User createdBy) {
        try {
            Map<String, Object> notification = createNotification(
                "Workflow Created",
                String.format("New workflow '%s' has been created by %s", 
                    workflow.getName(), createdBy.getUsername()),
                "WORKFLOW_CREATED",
                workflow.getId(),
                null // Broadcast to all
            );
            
            sendNotification(topicPrefix + "/workflows/created", notification);
            logger.info("Sent workflow creation notification for workflow {}", workflow.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send workflow creation notification: {}", e.getMessage());
        }
    }
    
    // Send workflow update notification
    public void notifyWorkflowUpdated(Workflow workflow, User updatedBy) {
        try {
            Map<String, Object> notification = createNotification(
                "Workflow Updated",
                String.format("Workflow '%s' has been updated by %s", 
                    workflow.getName(), updatedBy.getUsername()),
                "WORKFLOW_UPDATED",
                workflow.getId(),
                null // Broadcast to all
            );
            
            sendNotification(topicPrefix + "/workflows/updated", notification);
            logger.info("Sent workflow update notification for workflow {}", workflow.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send workflow update notification: {}", e.getMessage());
        }
    }
    
    // Send custom notification
    public void sendCustomNotification(String title, String message, String type, Long entityId, Long userId) {
        try {
            Map<String, Object> notification = createNotification(title, message, type, entityId, userId);
            sendNotification(topicPrefix + "/custom", notification);
            logger.info("Sent custom notification: {}", title);
            
        } catch (Exception e) {
            logger.error("Failed to send custom notification: {}", e.getMessage());
        }
    }
    
    private Map<String, Object> createNotification(String title, String message, String type, Long entityId, Long userId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", System.currentTimeMillis()); // Unique ID
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("entityId", entityId);
        notification.put("userId", userId);
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.put("isRead", false);
        return notification;
    }
    
    private void sendNotification(String topic, Map<String, Object> notification) throws Exception {
        if (mqttClient != null && mqttClient.isConnected()) {
            String payload = objectMapper.writeValueAsString(notification);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // At least once delivery
            message.setRetained(false);
            
            mqttClient.publish(topic, message);
            logger.debug("Published notification to topic {}: {}", topic, payload);
        } else {
            logger.warn("MQTT client not connected. Notification not sent: {}", notification.get("title"));
        }
    }
    
    // MQTT Callback implementations
    
    @Override
    public void connectionLost(Throwable cause) {
        logger.error("MQTT connection lost: {}", cause.getMessage());
        // Attempt to reconnect
        try {
            Thread.sleep(5000); // Wait 5 seconds before reconnecting
            connectToMqttBroker();
        } catch (Exception e) {
            logger.error("Failed to reconnect to MQTT broker: {}", e.getMessage());
        }
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        logger.debug("Received message on topic {}: {}", topic, payload);
        
        // Process incoming notifications if needed
        // This could be used for inter-service communication or real-time updates
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.debug("Message delivery complete");
    }
    
    // Check if MQTT is connected
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
    
    // Get connection status
    public String getConnectionStatus() {
        if (mqttClient == null) {
            return "NOT_INITIALIZED";
        } else if (mqttClient.isConnected()) {
            return "CONNECTED";
        } else {
            return "DISCONNECTED";
        }
    }
}