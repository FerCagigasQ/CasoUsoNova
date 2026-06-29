# Demo 5: Async Notification System

**Duration**: 35 minutes | **Complexity**: Intermediate  
**Target Audience**: Backend engineers, DevOps, event-driven architects  
**Agents**: nova-async-comm, nova-service-gen, nova-frontend-gen

---

## Overview

This demo introduces **event-driven architecture**: guarantees emit events (created, issued, amended, claimed, expired) that are published to a message broker (RabbitMQ/Kafka), consumed by notification workers, and delivered via email, Slack, webhooks, or real-time dashboard updates.

### What You'll Show
- Event publishing on guarantee state changes
- Message broker queues (RabbitMQ)
- Async notification workers
- Dead-letter queue (DLQ) for failed deliveries
- Real-time dashboard updates via WebSocket/SSE
- Email notifications to stakeholders

### Business Value
- **Real-time notifications**: Applicants/beneficiaries notified instantly on status changes
- **Scalable architecture**: Notifications processed asynchronously (no blocking)
- **Reliability**: Failed notifications retry automatically via DLQ
- **Multi-channel delivery**: Email, SMS, Slack, webhooks all supported
- **Audit trail**: Every notification attempt is logged

---

## Agent Responsibilities

### nova-async-comm (Messaging & Events)
**Must implement for this demo:**
- [ ] Event publisher in GuaranteeService (GUARANTEE_CREATED, ISSUED, AMENDED, CLAIMED, EXPIRED)
- [ ] RabbitMQ/Kafka broker setup with `guarantees.events` exchange/topic
- [ ] Message format: JSON with guaranteeId, eventType, timestamp, parties
- [ ] Dead-letter queue for failed deliveries
- [ ] Event retry logic (exponential backoff, max 3 retries)

### nova-service-gen (Backend)
**Must implement for this demo:**
- [ ] `notifyGuaranteeCreated(guarantee)` method in NotificationService
- [ ] `notifyGuaranteeIssued(guarantee)` method
- [ ] `notifyClaimSubmitted(claim)` method
- [ ] Integration with GuaranteeService (call notify* after state change)
- [ ] RabbitTemplate configuration (Spring AMQP)

### nova-frontend-gen (UI)
**Must implement for this demo:**
- [ ] WebSocket/SSE client setup for real-time updates
- [ ] Subscribe to `/topic/guarantees` for status changes
- [ ] Toast notifications on guarantee state change
- [ ] Notification center component (history of all notifications)
- [ ] Auto-refresh guarantee list on notification received

### nova-ops-monitor (Infrastructure)
**Must implement for this demo:**
- [ ] RabbitMQ management UI monitoring
- [ ] Prometheus metrics for event publish/consume rates
- [ ] Grafana dashboard: Messages in queue, DLQ size, consumer lag
- [ ] Alert on DLQ growth (indicates systemic issues)
- [ ] Message throughput monitoring (events/sec)

---

## Prerequisites (10 minutes)

### Add RabbitMQ to Docker Compose

**Add to docker-compose.yml**:
```yaml
rabbitmq:
  image: rabbitmq:3-management-alpine
  ports:
    - "5672:5672"      # AMQP
    - "15672:15672"    # Management UI
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
  healthcheck:
    test: rabbitmq-diagnostics -q ping
    interval: 30s
    timeout: 10s
    retries: 5
```

**Start RabbitMQ**:
```bash
docker-compose up rabbitmq -d
```

**Verify**:
- Access Management UI: http://localhost:15672 (guest/guest)
- Should show 0 queues (initially)

### Add Notification Service (Backend)

**Create NotificationService** (Spring component):
```java
@Service
@Slf4j
public class NotificationService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void notifyGuaranteeCreated(Guarantee guarantee) {
        GuaranteeEvent event = new GuaranteeEvent(
            "GUARANTEE_CREATED",
            guarantee.getId(),
            guarantee.getApplicant().getEmail(),
            guarantee.getBeneficiary().getEmail()
        );
        rabbitTemplate.convertAndSend("guarantees.events", event);
        log.info("Event published: GUARANTEE_CREATED for {}", guarantee.getReference());
    }
    
    public void notifyClaimSubmitted(Claim claim) {
        ClaimEvent event = new ClaimEvent(
            "CLAIM_SUBMITTED",
            claim.getId(),
            claim.getGuarantee().getId()
        );
        rabbitTemplate.convertAndSend("guarantees.events", event);
        log.info("Event published: CLAIM_SUBMITTED for claim {}", claim.getId());
    }
}
```

**Add to GuaranteeService** (trigger events on state changes):
```java
@Transactional
public GuaranteeDTO issue(Long id) {
    Guarantee g = repository.findById(id).orElseThrow();
    g.setStatus(GuaranteeStatus.ISSUED);
    repository.save(g);
    notificationService.notifyGuaranteeIssued(g);  // ← Event published
    return mapper.toDTO(g);
}
```

---

## Step-by-Step Guide (25 minutes)

### Step 1: Understand Event-Driven Architecture (3 min)

**Diagram**:
```
┌─────────────────────────────────────────────────┐
│       Guarantee CRUD Operations                 │
│  (Create, Issue, Amend, Claim, Expire)          │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
         ┌──────────────────┐
         │ GuaranteeService │
         │  + Events        │
         └────────┬─────────┘
                  │ publishes
                  ▼
       ┌────────────────────────┐
       │  RabbitMQ Broker       │
       │  Exchange: g.events    │
       │  Queue: notifications  │
       └────────────┬───────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
        ▼           ▼           ▼
    ┌────────┐ ┌────────┐ ┌──────────┐
    │ Email  │ │ Slack  │ │ Webhook  │
    │ Worker │ │ Worker │ │ Worker   │
    └────────┘ └────────┘ └──────────┘
        │           │           │
        └───────────┴───────────┘
                    │
                    ▼
         ┌────────────────────┐
         │  Notifications     │
         │  Sent to Users     │
         └────────────────────┘
```

**Explain**:
- **Publisher**: GuaranteeService emits events
- **Broker**: RabbitMQ queues events reliably
- **Consumers**: Separate workers handle each delivery channel
- **Async**: Main API call returns immediately (non-blocking)

### Step 2: Create a Guarantee and Monitor Events (5 min)

**In Frontend**:
1. Create a new guarantee (same as Demo 1, Steps 1-2)
2. Upon successful creation, check RabbitMQ

**In RabbitMQ Management UI** (http://localhost:15672):
1. Login: guest/guest
2. Click **Queues** tab
3. Should see a new queue: `guarantees.notifications`
4. Shows 1 message pending (the GUARANTEE_CREATED event)

**Explain**:
- Backend published the event immediately after guarantee creation
- Message is in queue waiting for a consumer worker

### Step 3: View Event Details (3 min)

**In RabbitMQ Management UI**:
1. Click on `guarantees.notifications` queue
2. Click **Get Messages**
3. Show the message payload:
   ```json
   {
     "eventType": "GUARANTEE_CREATED",
     "guaranteeId": 7,
     "applicantEmail": "acme@example.com",
     "beneficiaryEmail": "bbva-projects@example.com",
     "timestamp": "2026-06-29T14:30:00Z"
   }
   ```

**In Backend Logs**:
```bash
docker logs guarantees-service | grep "Event published"
# Output: Event published: GUARANTEE_CREATED for BG-2026-DEMO-001
```

### Step 4: Start Notification Workers (5 min)

**Email Worker** (Spring component):
```java
@Component
@Slf4j
public class EmailNotificationWorker {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @RabbitListener(queues = "guarantees.notifications")
    public void handleEvent(GuaranteeEvent event) {
        if ("GUARANTEE_CREATED".equals(event.getEventType())) {
            sendEmail(event.getApplicantEmail(), 
                "New Guarantee Created", 
                "Your guarantee has been created successfully.");
            log.info("Email sent to {}", event.getApplicantEmail());
        }
    }
    
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
```

**Start Email Worker**:
```bash
# Spring Boot detects @RabbitListener and auto-starts listening
docker-compose restart guarantees-service
```

**Monitor**:
- RabbitMQ queue should now show **0 pending messages** (consumed)
- Email sent to applicant and beneficiary (check logs or mock SMTP)

### Step 5: Simulate Event Flow: Issue Guarantee (5 min)

**In Frontend**:
1. Go to guarantee detail (BG-2026-DEMO-001)
2. Click **Issue Guarantee** button

**Backend Flow**:
1. `GuaranteeService.issue()` validates and updates status to ISSUED
2. Publishes event: `GUARANTEE_ISSUED`
3. Returns to frontend (fast, async)

**In RabbitMQ**:
1. New event appears in queue: `GUARANTEE_ISSUED`
2. Email worker immediately consumes it
3. Sends notification email to both parties

**In Logs**:
```
Event published: GUARANTEE_ISSUED for BG-2026-DEMO-001
Email sent to acme@example.com: "Guarantee Issued"
```

### Step 6: Simulate Failure & Retry (Dead-Letter Queue) (4 min)

**Scenario**: Email worker crashes mid-processing.

**Mock a failure**:
```java
@RabbitListener(queues = "guarantees.notifications")
public void handleEvent(GuaranteeEvent event) {
    if ("GUARANTEE_ISSUED".equals(event.getEventType())) {
        throw new RuntimeException("Simulated email service down");  // ← Force failure
    }
}
```

**RabbitMQ Behavior**:
1. Worker throws exception (email fails)
2. RabbitMQ retries 3 times
3. After 3 failures, message moves to **Dead-Letter Queue** (DLQ)

**In RabbitMQ Management**:
1. New queue appears: `guarantees.notifications.dlq`
2. Shows 1 message (the failed GUARANTEE_ISSUED event)
3. Operator can manually inspect and retry or discard

**Explain**:
- **Reliability**: Failed notifications don't block main workflow
- **Observability**: DLQ shows what went wrong
- **Recovery**: Manual retry or fix root cause and reprocess

### Step 7: Real-time Dashboard Updates (3 min)

**Future enhancement**: WebSocket/SSE connection from frontend to backend.

**Concept**:
```java
@Component
public class NotificationBroadcaster {
    
    private SimpMessagingTemplate template;
    
    public void broadcastEvent(GuaranteeEvent event) {
        // Send to WebSocket subscribers
        template.convertAndSend("/topic/guarantees", event);
    }
}
```

**In Frontend**:
```typescript
// Subscribe to real-time updates
this.stompClient.subscribe('/topic/guarantees', (event) => {
  // Update dashboard in real-time
  this.guarantees = this.guarantees.map(g => 
    g.id === event.guaranteeId ? { ...g, status: event.newStatus } : g
  );
});
```

**User Experience**:
- No need to refresh page
- Guarantee status updates automatically when issued
- Shows "Live" badge on dashboard

---

## Discussion Points

### For Backend Engineers
1. **Event Design**: "What events should we publish?"
   - Answer: GUARANTEE_CREATED, ISSUED, AMENDED, CLAIMED, EXPIRED, CANCELLED
2. **Message Format**: "Should we send full guarantee data or just ID?"
   - Answer: ID only (consumer fetches from DB if needed); minimizes message size
3. **Ordering**: "What if events arrive out of order?"
   - Answer: Use event versioning + timestamp; consumer deduplicates

### For Operations
1. **Message Durability**: "What if RabbitMQ goes down?"
   - Answer: Messages persist to disk; recovered on restart
2. **Scaling**: "Can we handle 100K events/sec?"
   - Answer: Scale workers horizontally; RabbitMQ handles millions/sec
3. **Monitoring**: "How do we track notification delivery?"
   - Answer: Log every email sent; metrics: published, delivered, failed

---

## Technical Details

### RabbitMQ Configuration

```yaml
# application.yml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: guest
    password: guest
    
# Queue setup
@Configuration
public class RabbitConfig {
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable("guarantees.notifications")
            .deadLetterExchange("guarantees.dlx")
            .ttl(86400000)  // 24 hour TTL
            .build();
    }
    
    @Bean
    public Queue dlq() {
        return QueueBuilder.durable("guarantees.notifications.dlq").build();
    }
    
    @Bean
    public Exchange dlxExchange() {
        return new DirectExchange("guarantees.dlx");
    }
    
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlq())
            .to(dlxExchange())
            .with("guarantees.#");
    }
}
```

### Event Publishing Pattern

```java
@Service
public class GuaranteeService {
    
    @Autowired
    private GuaranteeRepository repository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Transactional
    public GuaranteeDTO issue(Long id) {
        Guarantee g = repository.findById(id).orElseThrow();
        
        // 1. Update state
        g.setStatus(GuaranteeStatus.ISSUED);
        g.setIssuedDate(LocalDate.now());
        repository.save(g);
        
        // 2. Publish event (async, non-blocking)
        GuaranteeEvent event = new GuaranteeEvent(
            "GUARANTEE_ISSUED",
            g.getId(),
            g.getReference(),
            g.getApplicant().getEmail(),
            g.getBeneficiary().getEmail()
        );
        rabbitTemplate.convertAndSend("guarantees.events", event);
        
        // 3. Return immediately (before notifications sent)
        return mapper.toDTO(g);
    }
}
```

### Async Listener Pattern

```java
@Component
@Slf4j
public class EmailNotificationListener {
    
    @Autowired
    private NotificationService notificationService;
    
    @RabbitListener(queues = "guarantees.notifications")
    public void onGuaranteeEvent(GuaranteeEvent event) {
        try {
            if ("GUARANTEE_ISSUED".equals(event.getEventType())) {
                notificationService.sendIssuanceEmail(event);
            } else if ("GUARANTEE_AMENDED".equals(event.getEventType())) {
                notificationService.sendAmendmentEmail(event);
            } else if ("CLAIM_SUBMITTED".equals(event.getEventType())) {
                notificationService.sendClaimEmail(event);
            }
        } catch (Exception e) {
            log.error("Failed to send notification for event: {}", event, e);
            throw e;  // RabbitMQ will retry, then move to DLQ
        }
    }
}
```

---

## Proposed Improvements (for Delegation)

### nova-async-comm (Messaging & Events)
- [ ] **Kafka integration** as RabbitMQ alternative (higher throughput)
- [ ] **Event versioning** (schema evolution for breaking changes)
- [ ] **Slack notifications** (create Slack worker)
- [ ] **SMS notifications** (Twilio integration)
- [ ] **Webhook delivery** (retry with exponential backoff)

### nova-service-gen (Backend)
- [ ] **Event sourcing** (store all events as immutable log)
- [ ] **CQRS pattern** (separate read/write models)
- [ ] **Saga pattern** (distributed transactions across services)

### nova-frontend-gen (UI)
- [ ] **WebSocket/SSE integration** (real-time updates)
- [ ] **Notification toast** (top-right corner alerts)
- [ ] **Notification center** (history of all notifications)

### nova-ops-monitor (Infrastructure)
- [ ] **RabbitMQ metrics** in Prometheus/Grafana
- [ ] **Alert on DLQ growth** (indicates systemic issues)
- [ ] **Message throughput monitoring** (events/sec)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| RabbitMQ not starting | Check port 5672 not in use; verify image pulled |
| Events not consumed | Verify queue name matches listener; check logs for errors |
| DLQ growing | Review error logs; fix root cause (e.g., email service down) |
| High latency | Scale workers: run 3+ instances of notification service |

---

**Previous Demo**: [Demo 4: Multi-tenancy & Scaling](./demo-04-multitenancy-scaling.md)  
**Repository**: [Return to Prompts README](./README.md)
